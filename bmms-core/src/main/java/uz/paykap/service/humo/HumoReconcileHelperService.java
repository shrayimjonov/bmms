package uz.paykap.service.humo;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uz.devops.humo.config.HumoProperties;
import uz.devops.humo.service.HumoGatewayService;
import uz.devops.humo.service.dto.pojo.debit.Details;
import uz.devops.humo.service.dto.pojo.debit.NameValueItem;
import uz.devops.humo.service.dto.pojo.getPayment.GetPaymentResponse;
import uz.devops.humo.service.dto.v3.getpayment.GetPaymentsRequest;
import uz.devops.humo.service.dto.v3.getpayment.getPayments.GetPaymentListResponse;
import uz.devops.humo.service.dto.v3.getpayment.getPayments.GetPaymentStatus;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.javlon.commons.result.HasData;
import uz.paykap.adapter.verification.pc.PcTranType;
import uz.paykap.config.ApplicationProperties;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class HumoReconcileHelperService {

    private static final String PAN = "pan";
    private static final String PAN_2 = "pan2";
    public static final String SOAP_TO_CARD = "SOAP_TOCARD_2";
    public static final String SOAP_DMS = "SOAP_DMS";
    public static final long START_BOOK_MARK = 1L;
    public static final long END_BOOK_MARK = 500L;
    public static final String AMOUNT = "amount";
    public static final String AUTH_TIME = "auth_time";
    private final HumoGatewayService humoGatewayService;
    private final HumoProperties humoProperties;
    private final ApplicationProperties applicationProperties;

    /**
     * @param pcTranType
     * @param fromDate
     * @param toDate
     * @return
     */
    public P2pRecoSummaryResult reconcileSummary(PcTranType pcTranType, Instant fromDate, Instant toDate) {
        log.debug("begin humo reconcile summary");
        log.debug("pcTranType: {}", pcTranType);
        if (pcTranType == null) {
            log.warn("invalid params");
            throw new CodedException(ProblemEnum.INVALID_PARAMS);
        }

        if (pcTranType == PcTranType.CREDIT) {
            List<GetPaymentResponse> resultList = new ArrayList<>();
            recursiveSendRequestAndFetchCreditTrans(fromDate, toDate, GetPaymentStatus.ACTIVE, SOAP_TO_CARD, SOAP_TO_CARD, START_BOOK_MARK, END_BOOK_MARK, false, resultList);
            return calculateAndReturnSummaryResult(resultList, applicationProperties.getTransactionPrefix().getP2pCreditPrefix(), GetPaymentStatus.ACTIVE);
        }

        if (pcTranType == PcTranType.DEBIT) {
            List<GetPaymentResponse> resultList = new ArrayList<>();
            recursiveSendRequestAndFetchCreditTrans(fromDate, toDate, GetPaymentStatus.FINISHED, SOAP_DMS, SOAP_DMS, START_BOOK_MARK, END_BOOK_MARK, true, resultList);
            return calculateAndReturnSummaryResult(resultList, applicationProperties.getTransactionPrefix().getP2pDebitPrefix(), GetPaymentStatus.FINISHED);
        }

        if (pcTranType == PcTranType.DEBIT_HOLD) {
            List<GetPaymentResponse> resultList = new ArrayList<>();
            recursiveSendRequestAndFetchCreditTrans(fromDate, toDate, GetPaymentStatus.ACTIVE, SOAP_DMS, SOAP_DMS, START_BOOK_MARK, END_BOOK_MARK, true, resultList);
            return calculateAndReturnSummaryResult(resultList, applicationProperties.getTransactionPrefix().getP2pDebitPrefix(), GetPaymentStatus.ACTIVE);
        }

        log.debug("return empty p2pRecoSummaryResult!");
        return new P2pRecoSummaryResult(0L, 0L);
    }

    protected void recursiveSendRequestAndFetchCreditTrans(Instant fromDate, Instant toDate, GetPaymentStatus status, String billRef, String payInstrRef, long startBookMark, long endBookMark, Boolean isDebit, List<GetPaymentResponse> resultList) {
        HasData<GetPaymentListResponse> paymentResultData = createRequestAndSendHumo(fromDate, toDate, status, billRef, payInstrRef, startBookMark, endBookMark);
        List<GetPaymentResponse> paymentItems = fetchPaymentItemsFromResult(paymentResultData, isDebit);
        if (paymentItems == null || paymentItems.isEmpty()) {
            log.warn("paymentItems is empty");
            return;
        }

        resultList.addAll(paymentItems);
        if (bookMarksIsNotEmptyAndIsNumber(paymentResultData)) {
            log.debug("bookMarks is not empty, call recursive getPayments() method...");
            try {
                startBookMark = Long.parseLong(paymentResultData.getData().getStartBookmark());
                endBookMark = Long.parseLong(paymentResultData.getData().getEndBookmark());
            } catch (NumberFormatException e) {
                return;
            }
            recursiveSendRequestAndFetchCreditTrans(fromDate, toDate, status, billRef, payInstrRef, startBookMark, endBookMark, isDebit, resultList);
        }
        log.debug("bookMarks is empty, end recursive getPayments() method");
    }

    private HasData<GetPaymentListResponse> createRequestAndSendHumo(Instant fromDate, Instant toDate, GetPaymentStatus status, String billRef, String payInstrRef, Long startBookMark, Long endBookMark) {
        GetPaymentsRequest request = new GetPaymentsRequest();
        request.setPaymentOriginator(humoProperties.getUsername());
        request.setFromDate(fromDate);
        request.setToDate(toDate);
        request.setStatus(status);
        request.setBillRef(billRef);
        request.setPayInstrRef(payInstrRef);
        request.setStartBookmark(startBookMark);
        request.setEndBookmark(endBookMark);

        log.debug("send getPaymentsV3() request: {}", request);
        return humoGatewayService.getPaymentList(request);
    }

    private List<GetPaymentResponse> fetchPaymentItemsFromResult(HasData<GetPaymentListResponse> debitAndCreditTranData, Boolean isDebit) {
        List<GetPaymentResponse> items = verifyAndGetItems(debitAndCreditTranData);
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }

        return items.parallelStream().filter(item -> verifyToDebitElseCredit(item.getDetails(), isDebit)).collect(Collectors.toList());
    }

    public List<GetPaymentResponse> verifyAndGetItems(HasData<GetPaymentListResponse> paymentsData) {
        if (paymentsData == null) {
            log.warn("getPaymentsData is null");
            return Collections.emptyList();
        }
        if (!Objects.equals(paymentsData.isSuccess(), Boolean.TRUE) || paymentsData.getData() == null) {
//            if (paymentsData.getException() != null) {
//                log.warn("getPaymentsData is not success");
//                paymentsData.getException().printStackTrace();
//                throw paymentsData.getException();
//            }
            // todo
            return Collections.emptyList();
        }

        GetPaymentListResponse paymentResponse = paymentsData.getData();
        if (paymentResponse.getPaymentDataArray() == null ||
                paymentResponse.getPaymentDataArray().getItem() == null ||
                paymentResponse.getPaymentDataArray().getItem().isEmpty()) {
            log.warn("getPaymentsData is empty");
            return Collections.emptyList();
        }
        return paymentResponse.getPaymentDataArray().getItem();
    }

    private boolean verifyToDebitElseCredit(Details detail, Boolean isDebit) {
        if (detail.getItems() == null || detail.getItems().isEmpty()) {
            return false;
        }

        boolean panKeyFind = false;
        boolean pan2KeyFind = false;
        for (NameValueItem item : detail.getItems()) {
            if (Objects.equals(item.getName(), PAN)) {
                panKeyFind = true;
            }
            if (Objects.equals(item.getName(), PAN_2)) {
                pan2KeyFind = true;
            }
            if (pan2KeyFind) {
                return !isDebit;
            }
        }
        return panKeyFind && isDebit;
    }

    private boolean bookMarksIsNotEmptyAndIsNumber(HasData<GetPaymentListResponse> paymentsData) {
        return !StringUtils.isEmpty(paymentsData.getData().getStartBookmark()) &&
                NumberUtils.isNumber(paymentsData.getData().getStartBookmark()) &&
                !StringUtils.isEmpty(paymentsData.getData().getEndBookmark()) &&
                NumberUtils.isNumber(paymentsData.getData().getEndBookmark());
    }

    private P2pRecoSummaryResult calculateAndReturnSummaryResult(List<GetPaymentResponse> humoTranList, String paymentRefPrefix, GetPaymentStatus status) {
        P2pRecoSummaryResult summaryResult = new P2pRecoSummaryResult();
        summaryResult.setProcessingTranList(new ArrayList<>());
        summaryResult.setTotalCount((long) humoTranList.size());
        if (summaryResult.getTotalCount() == 0) {
            log.warn("summaryResult size is 0, return empty result");
            summaryResult.setTotalAmountTiyin(0L);
            return summaryResult;
        }
        AtomicReference<Long> sum = new AtomicReference<>(0L);
        humoTranList.stream().filter(tran -> humoProperties.getSimulate() || tran.getPaymentRef().startsWith(paymentRefPrefix)).forEach(tran -> {
            Long amount = fetchAmountValue(tran.getDetails());
            String date = fetchDateValue(tran.getDetails());
            sum.updateAndGet(v -> v + amount);
            summaryResult.getProcessingTranList().add(new ReconcileTranInfo(verifyAndParseBmId(tran.getPaymentRef()), tran.getPaymentID(), amount, Objects.equals(tran.getStatus(), status.getDesc()), parseToInstant(date), false));
        });
        summaryResult.setTotalAmountTiyin(sum.get());
        log.debug("return summary result: {}", summaryResult);
        return summaryResult;
    }

    private Long verifyAndParseBmId(String bmId) {
        if (StringUtils.isEmpty(bmId)) {
            return -1L;
        }

        String id = StringUtils.substringAfterLast(bmId, "_");
        if (StringUtils.isEmpty(id) || !NumberUtils.isNumber(id)) {
            return -1L;
        }

        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

    private Instant parseToInstant(String date) {
        try {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.US);
            LocalDateTime localDateTime = LocalDateTime.parse(date, dateTimeFormatter);
            return localDateTime.toInstant(ZoneOffset.ofHours(5));
        } catch (Exception e) {
            return null;
        }
    }

    private Long fetchAmountValue(Details detail) {
        return detail.getItems().parallelStream().filter(item -> Objects.equals(item.getName(), AMOUNT)).map(NameValueItem::getValue).mapToLong(Long::parseLong).sum();
    }

    private String fetchDateValue(Details detail) {
        return detail.getItems().parallelStream().filter(item -> Objects.equals(item.getName(), AUTH_TIME)).map(NameValueItem::getValue).findAny().orElse("");
    }
}
