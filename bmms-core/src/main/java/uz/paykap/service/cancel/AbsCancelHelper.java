package uz.paykap.service.cancel;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.javlon.commons.result.CommonResultData;
import uz.javlon.commons.result.HasData;
import uz.javlon.commons.result.HasResult;
import uz.javlon.svgate.dto.enumeration.TranType;
import uz.paykap.domain.Bill;
import uz.paykap.service.abs.AbsStatusService;
import uz.paykap.service.abs.AbstractAbsService;
import uz.paykap.service.abs.request.AbsTransactionInfoRequest;
import uz.paykap.service.abs.request.AbsTransactionRequest;
import uz.paykap.service.abs.request.AbsTransactionStatusRequest;
import uz.paykap.service.abs.response.AbsTransactionInfo;
import uz.paykap.service.abs.response.NciUpdateTranGroupType;
import uz.paykap.service.cancel.resolver.CancelTransactionService;
import uz.paykap.service.dto.cancel.CancelPaymentInfoDTO;
import uz.paykap.service.dto.cancel.CancelRequestDTO;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.result.nci.NciResultStatus;
import uz.paykap.utils.TransactionPrefixGenerator;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static uz.paykap.service.cancel.resolver.CancelTransactionService.*;

@Data
@Slf4j
@Service
@AllArgsConstructor
public class AbsCancelHelper {
    public static final String ABS_TRANSACTION = "ABS_TRANSACTION";
    public static final String CANCEL_TRANSACTION_PREFIX = "Отмена транзакции: ";
    public static final String CANCEL_PURPOSE_ID = "00668";
    private final AbsStatusService absStatusService;
    private final AbstractAbsService abstractAbsService;
    private final TransactionPrefixGenerator transactionPrefixGenerator;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    public HasData<Map<String, String>> cancel(CancelRequestDTO request) {
        log.info("check bank is open...");
        var bankIsOpen = absStatusService.checkBankOperDay();

        Map<String, String> response = new HashMap<>();

        if (bankIsOpen == null || !bankIsOpen.isSuccess() || !bankIsOpen.getData()) {
            response.put(CancelTransactionService.REQUEST_KEY_NAME, objectMapper.writeValueAsString(request.getPaymentId()));
            response.put(CancelTransactionService.RESPONSE_KEY_NANE, objectMapper.writeValueAsString("bank day is closing..."));
            return new CommonResultData<>(NciResultStatus.BANK_DAY_IS_CLOSING, response);
        }

        log.info("bank is open result data : {}", bankIsOpen);

        long diffDays = request.getTransactionDate().until(Instant.now(), ChronoUnit.DAYS);

        if (Math.abs(diffDays) < 1) {
            var bmResultData = abstractAbsService.sendCancelRequest(request.getPaymentId());
            response.put(CancelTransactionService.REQUEST_KEY_NAME, objectMapper.writeValueAsString(request.getPaymentId()));
            response.put(CancelTransactionService.RESPONSE_KEY_NANE, objectMapper.writeValueAsString(bmResultData != null ? bmResultData.getData() : null));
            return new CommonResultData<>(bmResultData != null && bmResultData.isSuccess() ? HasResult.SUCCESS : NciResultStatus.PC_ERROR, response, null);
        }
        if (!request.getParams().containsKey(ABS_TRANSACTION)) {
            response.put(CancelTransactionService.REQUEST_KEY_NAME, objectMapper.writeValueAsString(request.getPaymentId()));
            response.put(CancelTransactionService.RESPONSE_KEY_NANE, "abs transaction info not found");
            return new CommonResultData<>(NciResultStatus.PC_ERROR, response, "For create nci transaction billing transaction or abs transaction not found");
        }

        AbsTransactionInfo absTransactionInfo = objectMapper.readValue(request.getParams().get(ABS_TRANSACTION), AbsTransactionInfo.class);
        AbsTransactionRequest transactionRequest = new AbsTransactionRequest();
        transactionRequest.setAccCl(absTransactionInfo.getAccCo());
        transactionRequest.setNameCl(absTransactionInfo.getNameCo());
        transactionRequest.setBankCl(absTransactionInfo.getBankCo());
        transactionRequest.setAccCo(absTransactionInfo.getAccCl());
        transactionRequest.setNameCo(absTransactionInfo.getNameCl());
        transactionRequest.setBankCo(absTransactionInfo.getBankCl());
        transactionRequest.setSumma(Long.valueOf(absTransactionInfo.getSumma()));
        transactionRequest.setDocNum(request.getParams().containsKey(BILL_ID_KEY_NAME) ? request.getParams().get(BILL_ID_KEY_NAME) : String.valueOf(System.currentTimeMillis()));// TODO: 22.01.2022 aniqlashtirish kere
        transactionRequest.setDate(Timestamp.from(Instant.now()));
        transactionRequest.setPurposeId(CANCEL_PURPOSE_ID);
        transactionRequest.setPurpose(CANCEL_TRANSACTION_PREFIX + absTransactionInfo.getPurpose() + " " + absTransactionInfo.getTransId());
        transactionRequest.setRequestId(transactionPrefixGenerator.getCancelTranSessionId());
        transactionRequest.setRequestType(RequestType.PAY);

        log.info("Abs transaction request : {}", transactionRequest);
        var resultData = abstractAbsService.doTransaction(transactionRequest);
        log.info("Abs transaction result data : {}", resultData);

        response.put(CancelTransactionService.REQUEST_KEY_NAME, objectMapper.writeValueAsString(transactionRequest));
        response.put(CancelTransactionService.RESPONSE_KEY_NANE, objectMapper.writeValueAsString(resultData != null ? resultData.getData() : null));

        return new CommonResultData<>(resultData != null && resultData.isSuccess() ? HasResult.SUCCESS : NciResultStatus.PC_ERROR, response);
    }

    @SneakyThrows
    public CommonResultData<CancelPaymentInfoDTO> getInfo(Bill bill, TranType tranType, NciUpdateTranGroupType tranGroupType) {
        AbsTransactionStatusRequest statusRequest = new AbsTransactionStatusRequest();
        statusRequest.setRequestId(UUID.randomUUID().toString());
        statusRequest.setCheckStatusId(bill.getOutExtId());

        log.info("Status request by out id : {}", statusRequest);
        var tranInfo = absStatusService.getRequestStatusAndBody(statusRequest);
        log.info("Transaction info result : {}", tranInfo);
        CancelPaymentInfoDTO paymentInfoDTO = new CancelPaymentInfoDTO();
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put(REQUEST_KEY_NAME, objectMapper.writeValueAsString(statusRequest));
        String responseMessage = objectMapper.writeValueAsString(tranInfo != null ? (tranInfo.isSuccess() ? tranInfo.getData() : tranInfo.getMessage()) : "response is null");
        requestParams.put(RESPONSE_KEY_NANE, responseMessage);
        paymentInfoDTO.setParams(requestParams);

        if (tranInfo == null || !tranInfo.isSuccess()) {
            return new CommonResultData<>(HasResult.UNKNOWN_ERROR, paymentInfoDTO, responseMessage);
        }

        AbsTransactionInfoRequest request = new AbsTransactionInfoRequest();
        request.setGroupId(tranGroupType.getCode());
        request.setTransId(tranInfo.getData().getOldTransId());

        log.info("absStatusService getTransactionStatus request :{}", request);
        var resultData = absStatusService.getTransactionStatus(request);
        log.info("getTransactionStatus return result : {}", resultData);

        requestParams.put(REQUEST_KEY_NAME, objectMapper.writeValueAsString(request));
        requestParams.put(RESPONSE_KEY_NANE, objectMapper.writeValueAsString(resultData != null ? (resultData.isSuccess() ? resultData.getData() : resultData.getMessage()) : "response is null"));

        paymentInfoDTO.setParams(requestParams);

        log.info("Finally CancelPaymentInfoDTO : {}", paymentInfoDTO);
        if (resultData == null) {
            paymentInfoDTO.setStatus("response is null");
            paymentInfoDTO.setPaymentRef(request.getTransId());
            paymentInfoDTO.setTransactionStatus(TransactionStatus.FAILED);
            paymentInfoDTO.setSuccess(false);
            paymentInfoDTO.setPaymentId(null);
            return new CommonResultData<>(BmResultStatus.FAILED, paymentInfoDTO);
        }
        AbsTransactionInfo result = resultData.getData();
        paymentInfoDTO.setStatus(result.getErrorNote());
        paymentInfoDTO.setPaymentRef(request.getTransId());
        if (result.getTransactionStatus() != null) {
            paymentInfoDTO.setTransactionStatus(result.getTransactionStatus());
        } else {
            paymentInfoDTO.setTransactionStatus(Objects.equals(result.getErrorCode(), "0") ? TransactionStatus.SUCCESS : TransactionStatus.FAILED);
        }
        paymentInfoDTO.setSuccess(Objects.equals(result.getErrorCode(), "0"));
        paymentInfoDTO.setPaymentId(Objects.equals(result.getErrorCode(), "0") ? result.getTransId() : null);

        requestParams.put(ABS_TRANSACTION, objectMapper.writeValueAsString(result));
        log.info("return result : {}", paymentInfoDTO);
        return new CommonResultData<>(Objects.equals(result.getErrorCode(), "0") ? HasResult.SUCCESS : BmResultStatus.FAILED, paymentInfoDTO);
    }
}
