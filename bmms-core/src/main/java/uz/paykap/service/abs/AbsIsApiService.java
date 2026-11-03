package uz.paykap.service.abs;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.devops.nci.isApi.config.NciIsApiProperties;
import uz.devops.nci.isApi.dto.enums.NciIsApiAct;
import uz.devops.nci.isApi.dto.request.*;
import uz.devops.nci.isApi.dto.response.GetAccountWithBalanceResponseDTO;
import uz.devops.nci.isApi.dto.response.TransactionResponseDTO;
import uz.devops.nci.isApi.service.NciIsApiService;
import uz.javlon.commons.base.BasicResultData;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.config.Constants;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.service.abs.request.*;
import uz.paykap.service.abs.response.*;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.p2p.dto.response.ConfirmResult;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Slf4j
@AllArgsConstructor
@Service(AbsIsApiService.BEAN_NAME)
@ConditionalOnProperty(prefix = "application.abs", name = "serviceBeanName", havingValue = AbsIsApiService.BEAN_NAME)
public class AbsIsApiService extends AbstractAbsService {

    public static final String BEAN_NAME = "absIsApiService";
    public static final String STATIC_TYPE_DOCUMENT = "01";
    public static final String DEFAULT_CCY = "USD";
    public static final String DEFAULT_FX_DEAL = "20";
    public static final String DEFAULT_ACTION_ID = "19";
    public static final String FOREX_TRANSACTION_METHOD_NAME = "forexTransaction";
    public static final String DO_TRANSACTION_METHOD_NAME = "doTransaction";
    public static final String ANOR_PAY_IN_PROGRESS_STATUS_CODE = "5";

    private final NciIsApiService nciIsApiService;
    private final NciIsApiProperties nciIsApiProperties;
    private final ApplicationProperties applicationProperties;

    @Override
    public BmResultData<AbsTransactionResult> doTransaction(AbsTransactionRequest transactionDTO) {

        log.debug("doTransaction requested params : {} ", transactionDTO);

        BasicResultData<TransactionResponseDTO> nciIsApiResponse;

        if (transactionDTO.getBankCo() == null) {
            return new BmResultData<>(BmResultStatus.INVALID_PARAMS, "bankCo is null");
        }

        log.info("Bank mfo {} matches with ours {}, do normal tran", transactionDTO.getBankCo(), properties.getOurMfoPattern());
        CreateTransactionRequestDTO payment = getCreateTransactionRequestDTO(transactionDTO);

        BmResultData<CreateTransactionRequestDTO> validateResult = validateDto(payment);

        if (!validateResult.isSuccess()) {
            return new BmResultData<>(validateResult);
        }

        nciIsApiResponse = nciIsApiService.createTransaction(payment);

        return validateAndReturnResponse(nciIsApiResponse);
    }

    @Override
    public BmResultData<AnorPayTransactionResult> doAnorPayTransaction(AnorPayTransactionRequest requisitesRequest) {

        log.debug("Requested doTransactionByRequisites with params : {} ", requisitesRequest);

        BasicResultData<TransactionResponseDTO> nciIsApiResponse;

        log.info("Bank mfo {} NOT matches with ours {}, do FOREIGN payment by requisites tran", requisitesRequest.getBankCo(), properties.getOurMfoPattern());

        PaymentByRequisitesRequestDTO payment = getPaymentByRequisitesRequestDTO(requisitesRequest);

        BmResultData<PaymentByRequisitesRequestDTO> validateResult = validateDto(payment);

        if (!validateResult.isSuccess()) {
            return new BmResultData<>(validateResult);
        }

        nciIsApiResponse = nciIsApiService.paymentByRequisites(payment);

        return validateAndReturnAnorPayResponse(nciIsApiResponse);
    }

    private BmResultData<AbsTransactionResult> validateAndReturnResponse(BasicResultData<TransactionResponseDTO> nciIsApiResponse) {

        if (nciIsApiResponse == null) {
            log.warn("nciIsApiResponse.createTransaction returned null, returning ERROR");
            return new BmResultData<>(BmResultStatus.PC_ERROR, new AbsTransactionResult(null, "-999999", "Null response returned", "0"), "Null response returned");
        }

        log.debug("nciIsApiService.createTransaction result : {} ", nciIsApiResponse);

        if (!nciIsApiResponse.isSuccess()) {
            if (nciIsApiResponse.getData() == null) {
                log.warn("nciIsApiService.createTransaction failed and result is null, returning ERROR");

                String errorNote = nciIsApiResponse.getResponseText() != null ? nciIsApiResponse.getResponseText() : "Has response but data is null";

                return new BmResultData<>(BmResultStatus.PC_ERROR, new AbsTransactionResult(null, "-500", errorNote, "0"), errorNote);
            } else {
                AbsTransactionResult transactionResult = new AbsTransactionResult();
                transactionResult.setErrorCode(String.valueOf(nciIsApiResponse.getData().getResult()));
                transactionResult.setErrorNote(nciIsApiResponse.getData().getResultNote());
                transactionResult.setTransId(nciIsApiResponse.getData().getDocId());
                transactionResult.setCheckRequestId(nciIsApiResponse.getData().getId());
                return new BmResultData<>(BmResultStatus.PC_ERROR, transactionResult, transactionResult.getErrorCode() + " : " + transactionResult.getErrorNote());
            }
        }

        if (nciIsApiResponse.getData() == null) {
            log.warn("nciIsApiService.createTransaction success, but result is null, returning ERROR");
            return new BmResultData<>(BmResultStatus.PC_ERROR, new AbsTransactionResult(null, "-500", "Has response but data is null", "0"), "Has response but data is null");
        }

        /* ALL OK */

        log.info("nciIsApiService.createTransaction SUCCESS, returning : {}", nciIsApiResponse.getData());

        AbsTransactionResult transactionResult = new AbsTransactionResult();
        transactionResult.setErrorCode(String.valueOf(nciIsApiResponse.getData().getResult()));
        transactionResult.setErrorNote(nciIsApiResponse.getData().getResultNote());
        transactionResult.setTransId(nciIsApiResponse.getData().getDocId());
        transactionResult.setCheckRequestId(nciIsApiResponse.getData().getId());
        if (nciIsApiResponse.getData() != null && Objects.equals(nciIsApiResponse.getData().getResult(), ANOR_PAY_IN_PROGRESS_STATUS_CODE)) { // 5 - means in progress for payment by requisites confirm operation
            transactionResult.setWaiting(true);
        }
        return new BmResultData<>(BmResultStatus.SUCCESS, transactionResult, transactionResult.getErrorNote());
    }

    private BmResultData<AnorPayTransactionResult> validateAndReturnAnorPayResponse(BasicResultData<TransactionResponseDTO> anorPayResult) {

        if (anorPayResult == null) {
            log.warn("nciIsApiResponse.requisitesTransaction returned null, returning ERROR");
            return new BmResultData<>(BmResultStatus.PC_ERROR, new AnorPayTransactionResult("-1", "-999999", "Null response returned"), "Null response returned");
        }

        log.debug("nciIsApiService.requisitesTransaction result : {} ", anorPayResult);

        if (!anorPayResult.isSuccess()) {
            if (anorPayResult.getData() == null) {
                log.warn("nciIsApiService.requisitesTransaction failed and result is null, returning ERROR");

                String errorNote = anorPayResult.getResponseText() != null ? anorPayResult.getResponseText() : "Has response but data is null";

                return new BmResultData<>(BmResultStatus.PC_ERROR, new AnorPayTransactionResult("-1", "-500", errorNote), errorNote);
            } else {
                AnorPayTransactionResult transactionResult = new AnorPayTransactionResult();
                transactionResult.setResultCode(String.valueOf(anorPayResult.getData().getResult()));
                transactionResult.setResultNote(anorPayResult.getData().getResultNote());
                transactionResult.setId(anorPayResult.getData().getId());
                return new BmResultData<>(BmResultStatus.PC_ERROR, transactionResult, transactionResult.getResultNote() + " : " + transactionResult.getResultNote());
            }
        }

        if (anorPayResult.getData() == null) {
            log.warn("nciIsApiService.createTransaction success, but result is null, returning ERROR");
            return new BmResultData<>(BmResultStatus.PC_ERROR, new AnorPayTransactionResult("-1", "-500", "Has response but data is null"), "Has response but data is null");
        }

        /* ALL OK */

        log.info("nciIsApiService.requisitesTransaction SUCCESS, result : {}", anorPayResult.getData());

        AnorPayTransactionResult transactionResult = new AnorPayTransactionResult();
        transactionResult.setResultCode(String.valueOf(anorPayResult.getData().getResult()));
        transactionResult.setResultNote(anorPayResult.getData().getResultNote());
        transactionResult.setId(anorPayResult.getData().getId());
        return new BmResultData<>(BmResultStatus.SUCCESS, transactionResult, transactionResult.getResultNote());
    }

    private BmResultData<AnorPayTransactionResult> validateAndReturnAnorPayConfirmResponse(BasicResultData<TransactionResponseDTO> anorPayResult) {

        if (anorPayResult == null || anorPayResult.getData() == null || anorPayResult.getData().getResult() == null) {
            log.warn("anorPay.requisitesTransaction returned result or data null, returning ERROR");
            return new BmResultData<>(BmResultStatus.PC_ERROR, new AnorPayTransactionResult("-1", "-999999", "Null response returned", ConfirmResult.FAILED), "Null response returned");
        }

        log.debug("anorPay.requisitesTransaction result : {} ", anorPayResult);

        AnorPayTransactionResult transactionResult = new AnorPayTransactionResult();
        transactionResult.setResultCode(String.valueOf(anorPayResult.getData().getResult()));
        transactionResult.setResultNote(anorPayResult.getData().getResultNote());
        transactionResult.setId(anorPayResult.getData().getId());

        switch (anorPayResult.getData().getResult()) {
            case 50: {
                log.info("anorPay.requisitesTransaction SUCCESS, result : {}", anorPayResult.getData());
                transactionResult.setResult(ConfirmResult.ACCEPTED);
                return new BmResultData<>(BmResultStatus.SUCCESS, transactionResult, transactionResult.getResultNote());
            }
            case 52: {
                transactionResult.setResult(ConfirmResult.REJECTED);
                return new BmResultData<>(BmResultStatus.R_REJECTED, transactionResult, transactionResult.getResultNote() + " : " + transactionResult.getResultNote());
            }
            case 1: //TODO
            case 5: {
                transactionResult.setResult(ConfirmResult.IN_PROGRESS);
                return new BmResultData<>(BmResultStatus.SUCCESS, transactionResult, transactionResult.getResultNote() + " : " + transactionResult.getResultNote());
            }
            default: {
                transactionResult.setResult(ConfirmResult.UNKNOWN);
                return new BmResultData<>(BmResultStatus.UNKNOWN_ERROR, transactionResult, transactionResult.getResultNote() + " : " + transactionResult.getResultNote());
            }
        }
    }

    @SneakyThrows
    public BmResultData<Bill> confirmAnorPayTransaction(Transaction transaction, AnorPayTransactionRequest mainRequest, AnorPayTransactionResult mainResult) {

        log.debug("Creating AnorPay confirm bill from : {}", mainRequest);

        PaymentByRequisitesConfirmDTO confirmDTO = getPaymentByRequisitesConfirmDTO(mainResult.getId());

        Bill confirmBill = p2pHelperService.createP2pBill(transaction, RequestType.CONFIRM, P2pProcessorType.ANOR, mainResult, mainResult.getId());
        confirmBill.setExtId(mainResult.getId());

        billRepository.save(confirmBill);

        log.debug("AnorPay p2p bill saved : {}", confirmBill);

        BasicResultData<TransactionResponseDTO> nciIsApiResponse = nciIsApiService.paymentByRequisitesConfirm(confirmDTO);

        log.debug("Response from confirm : {}", nciIsApiResponse);

        if (nciIsApiResponse == null || nciIsApiResponse.getData() == null) {
            String message = "Confirm response or data is null";
            log.warn(message);
            return new BmResultData<>(BmResultStatus.PC_ERROR, message);
        }

        BmResultData<AnorPayTransactionResult> payConfirmResponse = validateAndReturnAnorPayConfirmResponse(nciIsApiResponse);

        if (payConfirmResponse.getData() != null) {
            payConfirmResponse.getData().setResultCode(mainResult.getResultCode());
        }

        confirmBill = updateAnorPayResultIntoBill(confirmBill, payConfirmResponse);

        log.debug("Confirm AnorPay bill saved : {}", confirmBill);

        if (Objects.equals(payConfirmResponse.getData().getResult(), ConfirmResult.ACCEPTED)) {
            return new BmResultData<>(BmResultStatus.SUCCESS, confirmBill, "AnorPay success confirm");
        } else if (Objects.equals(payConfirmResponse.getData().getResult(), ConfirmResult.IN_PROGRESS)) {

//            Thread.sleep(5000);
//
//            BmResultData<ConfirmResult> confirm2 = confirm(transaction, confirmBill);
//
//            if (!confirm2.isSuccess()) {
//                transaction.setStatus(TransactionStatus.R_NEED_CONFIRM);
//                return new BmResultData<>(BmResultStatus.SUCCESS, confirmBill, "AnorPay need confirm after 5 seconds");
//            }
//
//            if (confirm2.isSuccess() && confirm2.getData() == ConfirmResult.ACCEPTED) {
//                return new BmResultData<>(BmResultStatus.SUCCESS, confirmBill, "AnorPay success confirm after 5 seconds");
//            }
//
//            if (confirm2.isSuccess() && confirm2.getData() == ConfirmResult.REJECTED) {
//                return new BmResultData<>(BmResultStatus.R_REJECTED, confirmBill, "AnorPay rejected after 5 seconds");
//            }
//
//            if (confirm2.isSuccess() && confirm2.getData() == ConfirmResult.IN_PROGRESS) {
//                transaction.setStatus(TransactionStatus.R_NEED_CONFIRM);
//                return new BmResultData<>(BmResultStatus.SUCCESS, confirmBill, "AnorPay need confirm after 5 seconds");
//            }

            transaction.setStatus(TransactionStatus.R_NEED_CONFIRM);
            return new BmResultData<>(BmResultStatus.SUCCESS, confirmBill, "AnorPay need confirm after 5 seconds");

        }

        return new BmResultData<>(BmResultStatus.R_REJECTED, confirmBill, "AnorPay rejected");
    }

    // todo - optimize, use retry
    @SneakyThrows
    @Override
    public BmResultData<ConfirmResult> confirm(Transaction transaction, Bill confirmBill) {

        log.debug("Requested AnorPay confirm tran : {}, bill : {}", transaction.getId(), confirmBill.getId());

        BasicResultData<TransactionResponseDTO> nciIsApiResponse = nciIsApiService.paymentByRequisitesConfirm(getPaymentByRequisitesConfirmDTO(confirmBill.getExtId()));

        log.debug("Response from AnorPay confirm : {}", nciIsApiResponse);

        if (nciIsApiResponse == null || nciIsApiResponse.getData() == null) {
            String message = "Confirm response or data is null";
            log.warn(message);
            return new BmResultData<>(BmResultStatus.PC_ERROR, message);
        }

        BmResultData<AnorPayTransactionResult> resultData = validateAndReturnAnorPayConfirmResponse(nciIsApiResponse);

        log.debug("Confirm result : {}", resultData);

        return new BmResultData<>(BmResultStatus.SUCCESS, resultData.getData().getResult());
    }

    @Override
    public BmResultData<AbsAccountResult> sendGetAccountRequest(Long requestId, String account, String mfo) {
        log.info(" begin AbsIsApiService on method sendGetAccountRequest with params {} ", account);
        GetAccountRequestDTO nciIsApiRequest = getAccountRequestDTO(account, mfo);
        BasicResultData<GetAccountWithBalanceResponseDTO> nciIsApiResponse = nciIsApiService.getAccountWithBalance(nciIsApiRequest);
        log.info("NciIsApiService on method nciIsApiService.getAccount return {} ", nciIsApiResponse);

        AbsAccountResult absAccountResult = new AbsAccountResult();

        if (nciIsApiResponse.isSuccess()) {
            absAccountResult.setErrorCode(String.valueOf(nciIsApiResponse.getData().getResult()));
            absAccountResult.setErrorNote(nciIsApiResponse.getData().getResultNote());
            absAccountResult.setMfo(nciIsApiResponse.getData().getMfo());
            absAccountResult.setTransId(nciIsApiResponse.getData().getAccount());
            absAccountResult.setId(nciIsApiResponse.getData().getAccount());
            absAccountResult.setInn(nciIsApiResponse.getData().getInn());
            absAccountResult.setName(nciIsApiResponse.getData().getName());
            absAccountResult.setSOutTmp(String.valueOf(nciIsApiResponse.getData().getBalance()));

            return new BmResultData<>(BmResultStatus.SUCCESS, absAccountResult);
        }
        if (nciIsApiResponse.getData() != null) {
            absAccountResult.setErrorCode(String.valueOf(nciIsApiResponse.getData().getResult()));
            absAccountResult.setErrorNote(nciIsApiResponse.getData().getResultNote());
        }
        return new BmResultData<>(BmResultStatus.PC_ERROR, absAccountResult);
    }

    @Override
    public BmResultData<AbsAccountResult> sendGetAccountRequest(AbsAccountDTO accountRequestDto) {
        return new BmResultData<>(BmResultStatus.NOT_IMPLEMENTED);
    }

    @Override
    public BmResultData<AbsTransactionResult> sendCancelRequest(Long cancelTransactionId, String requestId, String eposMerchant,NciUpdateTranGroupType tranGroupType) {
        if (eposMerchant == null || eposMerchant.isEmpty()) {
            return new BmResultData<>(BmResultStatus.PC_ERROR, new AbsTransactionResult("-9999", "-9999", "Request mfo is null or empty", null), "Request mfo is null or empty");
        }
        CancelRequestDTO requestDTO = new CancelRequestDTO();
        requestDTO.setAct(NciIsApiAct.CANCEL_TRANSACTION_501.getAct());
        requestDTO.setMfo(StringUtils.substring(eposMerchant, 0, 5));
        requestDTO.setTransactionId(String.valueOf(cancelTransactionId));
        log.info("Cancel request dto {}", requestDTO);

        BasicResultData<TransactionResponseDTO> resultData = nciIsApiService.cancelTransaction(requestDTO);
        return validateAndReturnResponse(resultData);
    }

    @Override
    public BmResultData<AbsTransactionResult> sendCancelRequest(Long tranId, String requestId, NciUpdateTranGroupType groupType) {
        return null;
    }

    @Override
    public BmResultData<AbsTransactionResult> doForexTransaction(AbsForexTransactionRequest transactionDTO) {
        if (transactionDTO.getMfo() == null || transactionDTO.getMfo().isEmpty() || transactionDTO.getAccCl() == null || transactionDTO.getAccCl().isEmpty() || transactionDTO.getAccCo() == null || transactionDTO.getAccCo().isEmpty() || transactionDTO.getAmountCo() == null || transactionDTO.getDeal() == null) {
            return new BmResultData<>(BmResultStatus.INVALID_PARAMS);
        }
        ForexRequestDTO forexRequestDTO = getForexRequestDTO(transactionDTO);
        BasicResultData<TransactionResponseDTO> nciIsApiResponse = nciIsApiService.forexTransaction(forexRequestDTO);
        log.info("response nci is api forex {}", nciIsApiResponse);

        return validateNciIsApiResultData(nciIsApiResponse, FOREX_TRANSACTION_METHOD_NAME);
    }

    @Override
    public BmResultData<List<AbsClientResult>> getClient(AbsClientRequest clientDTO) {
        return new BmResultData<>(BmResultStatus.NOT_IMPLEMENTED);
    }

    @Override
    public BmResultData<List<AbsBlackListResult>> getBlackList(AbsBlackListRequest nciHelperBlackListRequest) {
        return new BmResultData<>(BmResultStatus.NOT_IMPLEMENTED);
    }

    @Override
    public BmResultData<List<AbsCoursesResult>> getCourses(AbsCoursesRequest request) {
        return new BmResultData<>(BmResultStatus.NOT_IMPLEMENTED);
    }

    /**
     * VALIDATOR
     *
     * @param nciIsApiResponse
     * @param methodName
     * @return
     */
    private BmResultData<AbsTransactionResult> validateNciIsApiResultData(BasicResultData<TransactionResponseDTO> nciIsApiResponse, String methodName) {
        if (nciIsApiResponse == null) {
            log.warn("nciIsApiResponse.{} returned null, returning ERROR", methodName);
            return new BmResultData<>(BmResultStatus.PC_ERROR, new AbsTransactionResult(null, "-999999", "Null response returned", "0"), "Null response returned");
        }

        log.debug("nciIsApiService.createTransaction result : {} ", nciIsApiResponse);

        if (!nciIsApiResponse.isSuccess()) {
            if (nciIsApiResponse.getData() == null) {
                log.warn("nciIsApiService.{} failed and result is null, returning ERROR", methodName);

                String errorNote = nciIsApiResponse.getResponseText() != null ? nciIsApiResponse.getResponseText() : "Has response but data is null";

                return new BmResultData<>(BmResultStatus.PC_ERROR, new AbsTransactionResult(null, "-500", errorNote, "0"), errorNote);
            } else {
                AbsTransactionResult transactionResult = new AbsTransactionResult();
                transactionResult.setErrorCode(String.valueOf(nciIsApiResponse.getData().getResult()));
                transactionResult.setErrorNote(nciIsApiResponse.getData().getResultNote());
                transactionResult.setTransId(nciIsApiResponse.getData().getDocId());
                transactionResult.setCheckRequestId(nciIsApiResponse.getData().getId());
                return new BmResultData<>(BmResultStatus.PC_ERROR, transactionResult, transactionResult.getErrorCode() + " : " + transactionResult.getErrorNote());
            }
        }

        if (nciIsApiResponse.getData() == null) {
            log.warn("nciIsApiService.{} success, but result is null, returning ERROR", methodName);
            return new BmResultData<>(BmResultStatus.PC_ERROR, new AbsTransactionResult(null, "-500", "Has response but data is null", "0"), "Has response but data is null");
        }

        /* ALL OK */

        log.info("nciIsApiService.{} SUCCESS, returning : {}", methodName, nciIsApiResponse.getData());

        AbsTransactionResult transactionResult = new AbsTransactionResult();
        transactionResult.setErrorCode(String.valueOf(nciIsApiResponse.getData().getResult()));
        transactionResult.setErrorNote(nciIsApiResponse.getData().getResultNote());
        transactionResult.setTransId(nciIsApiResponse.getData().getDocId());
        transactionResult.setCheckRequestId(nciIsApiResponse.getData().getId());
        return new BmResultData<>(BmResultStatus.SUCCESS, transactionResult, transactionResult.getErrorNote());

    }

    private ForexRequestDTO getForexRequestDTO(AbsForexTransactionRequest transactionDTO) {
        ForexRequestDTO forexRequestDTO = new ForexRequestDTO();
        forexRequestDTO.setAct(NciIsApiAct.FOREX_WITH_PURPOSE.getAct());
        forexRequestDTO.setMfo(transactionDTO.getMfo());
        forexRequestDTO.setAccCl(transactionDTO.getAccCl());
        forexRequestDTO.setAccCo(transactionDTO.getAccCo());
        forexRequestDTO.setSumma(transactionDTO.getAmountCo());//TODO summani amountCo dan olish olmaslikni aniqlashtirish kerak anorda amountCo va amountCl so`ravotti, davrda faqat summa !!!
        forexRequestDTO.setCcy(Constants.USD_CURRENCY);//todo currencyni aniqlashtirish kerak Javlon aka !!!
        forexRequestDTO.setFxDeal(String.valueOf(transactionDTO.getFxDeal()));
        forexRequestDTO.setDeal(String.valueOf(transactionDTO.getDeal()));
        forexRequestDTO.setId(String.valueOf(System.currentTimeMillis() + Instant.now().getEpochSecond() + new Random().nextInt()));
        forexRequestDTO.setActionId(DEFAULT_ACTION_ID);
        forexRequestDTO.setPurpose(transactionDTO.getPurpose());

        log.info("send request to nci is api forex {}", forexRequestDTO);
        return forexRequestDTO;
    }

    private CreateTransactionRequestDTO getCreateTransactionRequestDTO(AbsTransactionRequest transactionDTO) {
        CreateTransactionRequestDTO requestDTO = new CreateTransactionRequestDTO();
        requestDTO.setAccCl(transactionDTO.getAccCl());
        requestDTO.setBankCl(transactionDTO.getBankCl());
        requestDTO.setAccCo(transactionDTO.getAccCo());
        requestDTO.setBankCo(transactionDTO.getBankCo());
//        requestDTO.setInnCo(transactionDTO.getInnCo());
        requestDTO.setNameCo(transactionDTO.getNameCo());
        requestDTO.setSumma(transactionDTO.getSumma());
        requestDTO.setPurpose(transactionDTO.getPurpose());
        requestDTO.setPurposeId(transactionDTO.getPurposeId());
        requestDTO.setDocNum(transactionDTO.getDocNum());

        requestDTO.setSign(nciIsApiProperties.getSign());
        requestDTO.setSessionId(nciIsApiProperties.getSessionId().getCreateTransaction()); // todo

        requestDTO.setState(transactionDTO.getState() != null ? transactionDTO.getState() : nciIsApiProperties.getDefaultState()); // todo
        requestDTO.setTypeDocument(STATIC_TYPE_DOCUMENT); // todo
        requestDTO.setTransactionId(transactionDTO.getTransactionId()); // todo
        requestDTO.setTransactionPurposeId(transactionDTO.getTransactionId()); // todo

//        requestDTO.setCashCode(transactionDTO.getCashCode());
        return requestDTO;
    }

    private PaymentByRequisitesRequestDTO getPaymentByRequisitesRequestDTO(AnorPayTransactionRequest transactionDTO) {
        PaymentByRequisitesRequestDTO requestDTO = new PaymentByRequisitesRequestDTO();

        requestDTO.setAccCl(transactionDTO.getAccCl());
        requestDTO.setBankCl(transactionDTO.getBankCl());
        requestDTO.setNameCl(transactionDTO.getNameCl());
        requestDTO.setInnCl(transactionDTO.getInnCl() != null ? transactionDTO.getInnCl() : DEFAULT_INN_CL);
        requestDTO.setAccCo(transactionDTO.getAccCo());
        requestDTO.setBankCo(transactionDTO.getBankCo());
        requestDTO.setInnCo(transactionDTO.getInnCo());
        requestDTO.setNameCo(transactionDTO.getNameCo());
        requestDTO.setSumma(transactionDTO.getSumma());
        requestDTO.setPurpose(transactionDTO.getPurpose());
        requestDTO.setPurposeId(transactionDTO.getPurposeId());
        requestDTO.setDocNum(transactionDTO.getDocNum());

        requestDTO.setSign(nciIsApiProperties.getSign());
        requestDTO.setSessionId(nciIsApiProperties.getSessionId().getCreateTransaction()); // todo

        return requestDTO;
    }

    private PaymentByRequisitesConfirmDTO getPaymentByRequisitesConfirmDTO(String confirmId) {
        PaymentByRequisitesConfirmDTO confirmDTO = new PaymentByRequisitesConfirmDTO(confirmId);
        confirmDTO.setAct(NciIsApiAct.PAYMENT_BY_REQUISITES_CONFIRM.getAct());
        return confirmDTO;
    }

    @Deprecated
    private GetAccountRequestDTO getAccountRequestDTO(String account) {
        return getAccountRequestDTO(account, applicationProperties.getAbs().getMfo());
    }

    private GetAccountRequestDTO getAccountRequestDTO(String account, String mfo) {
        GetAccountRequestDTO requestDTO = new GetAccountRequestDTO();
        requestDTO.setSessionId(applicationProperties.getAbs().getGetAccountSessionId());
        requestDTO.setSign(nciIsApiProperties.getSign());
        requestDTO.setAct(NciIsApiAct.GET_ACCOUNT.getAct());
        requestDTO.setAccount(account);
        requestDTO.setMfo(mfo);
        return requestDTO;
    }

    @Override
    protected boolean hasConfirm() {
        return true;
    }

    @Override
    public BmResultData<List<MultiTransactionResultDTO>> doMultiTransaction(AbsMultiTransactionDTO transactionDTO) {
        return new BmResultData<>(BmResultStatus.NOT_IMPLEMENTED);
    }
}
