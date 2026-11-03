package uz.paykap.sender.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import uz.devops.bm.dummy.adapter.DummyDataAdapterV3;
import uz.devops.bmcore.dto.request.CommonPayRequest;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.javlon.commons.base.ResultData;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.javlon.commons.result.CommonResult;
import uz.javlon.commons.result.CommonResultData;
import uz.javlon.commons.result.HasData;
import uz.javlon.commons.result.HasResult;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.config.Constants;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Recipient;
import uz.paykap.domain.Transaction;
import uz.paykap.event.dto.PartnerBillingErrorEvent;
import uz.paykap.repository.BillRepository;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.sender.SenderService;
import uz.paykap.service.abs.AbstractAbsService;
import uz.paykap.service.abs.request.AbsTransactionRequest;
import uz.paykap.service.abs.response.AbsAccountResult;
import uz.paykap.service.abs.response.AbsTransactionResult;
import uz.paykap.service.abs.response.NciUpdateTranGroupType;
import uz.paykap.service.dto.AbsParams;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.result.BmResultData;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class NciSenderService extends SenderService {

    private static final String PURPOSE = " Оплата со счета за товар/услугу";
    private static final String ERROR_NOTE = "ERROR_NOTE";
    private static final String ERROR_CODE = "ERROR_CODE";
    private static final String EXT_ID = "EXT_ID";
    public static final String PURPOSE_ID = "00668";
    public static final String CHECK_REQUEST_ID = "checkRequestId";
    public static final String ABS = "abs";
    public static final String ACCOUNT = "account";
    public static final String SHORTNAME = "shortname";

    private final AbstractAbsService nciHelperService;
    private final TransactionRepository transactionRepository;
    private final RecipientRepository recipientRepository;
    private final BillRepository billRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ApplicationProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public HasResult canAgentProceed(PayType payType, String agentLogin, CommonPayRequest payDTO, Long amount) {
        log.info(">>>> begin canAgentProceed >>>>");
        if (payType != PayType.NCI) {
            log.debug("Valid type {}, but given {}", PayType.NCI, payType);
            return new CommonResultData<>(BmResultStatus.INVALID_PARAMS, null, "Тип должен быть NCI");
        }
        return canAgentProceed(payDTO, amount);
    }

    public HasResult canAgentProceed(CommonPayRequest payDTO, Long amount) {

        var validationResult = validateRequest(payDTO);
        log.info("Validation result : {}", validationResult);
        if (!validationResult.isSuccess()) {
            return validationResult;
        }
//        if (nciHelperService instanceof AbsFidoService) {
//            log.info("this abs service skipped check account and account balance");
//            return new BmResultData<>(BmResultStatus.SUCCESS);
//        }
        String account = String.valueOf(payDTO.getParams().get(Constants.ACCOUNT_PARAM));
        String mfo = String.valueOf(payDTO.getParams().getOrDefault(Constants.MFO_PARAM, properties.getAbs().getMfo()));

        BmResultData<AbsAccountResult> resultData = nciHelperService.sendGetAccountRequest(payDTO.getBillId(), account, mfo);
        if (!resultData.isSuccess() || resultData.getData() == null) {
            log.warn("nci getAccount({}) result is not enoughBalance or null", account);
//            setError(transaction, resultData.getMessage());
            return new CommonResult(BmResultStatus.PC_ERROR, resultData.getMessage());
        }

        AbsAccountResult absAccountResult = resultData.getData();

        if (StringUtils.isEmpty(absAccountResult.getSOutTmp())) {
            log.warn("nci account {} balance empty", account);
//            setError(transaction, "Баланс вернулся пустым");
            return new CommonResult(BmResultStatus.INSUFFICIENT_FUNDS_ON_SENDER, "Баланс вернулся пустым");
        }

        Long balanceAmount;
        try {
            balanceAmount = Long.valueOf(absAccountResult.getSOutTmp());
        } catch (NumberFormatException e) {
            log.warn("nci account balance not parsed to long");
            String responseText = "Баланс не верный '" + absAccountResult.getSOutTmp() + "'";
//            setError(transaction, responseText);
            return new CommonResult(BmResultStatus.PARSE_ERROR, responseText);
        }

        log.debug("account : {}, account balance : {}, sender amount : {}", account, balanceAmount, payDTO.getAmount());

        boolean enoughBalance = amount <= balanceAmount;

        log.debug("canAgentProceed : {}", enoughBalance);

        return new CommonResult(enoughBalance ? HasResult.SUCCESS : HasResult.UNKNOWN_ERROR);
    }

    @SneakyThrows
    @Override
    public HasData<Transaction> debit(CommonPayRequest payDTO, Transaction transaction, String agentLogin) {
        log.info(">>>> begin debit >>>>");

        var validationResult = validateRequest(payDTO, transaction);
        String responseText;
        if (!validationResult.isSuccess()) {
            return new CommonResultData<>(BmResultStatus.INVALID_PARAMS, transaction);
        }
        Optional<Recipient> optionalRecipient = recipientRepository.findTop1ByShortName(transaction.getRecipientName());
        log.info("Found recipient : {}", optionalRecipient);
        if (optionalRecipient.isEmpty()) {
            responseText = "Получателя не найден '" + transaction.getRecipientName() + "'";
            setError(transaction, responseText);
            return new CommonResultData<>(BmResultStatus.NOT_FOUND, transaction, responseText);
        }
        Recipient recipient = optionalRecipient.get();

        if (StringUtils.isEmpty(recipient.getSettlementAccount())) {
            log.error("recipient settlementAccount not found");
            responseText = "Не найден счет получателя (recipient.settlementAccount)";
            setError(transaction, responseText);
            return new CommonResultData<>(BmResultStatus.NOT_FOUND, transaction, responseText);
        }
        String senderAccount = String.valueOf(payDTO.getParams().get(Constants.ACCOUNT_PARAM));
        String senderName = String.valueOf(payDTO.getParams().get(Constants.NAME_PARAM));
        String senderMfo = String.valueOf(payDTO.getParams().get(Constants.MFO_PARAM));

        String params = recipient.getJsonParams();
        if (params == null || params.isEmpty()) {
            responseText = "recipient params null or empty";
            setError(transaction, responseText);
            return new CommonResultData<>(BmResultStatus.INVALID_PARAMS, transaction, responseText);
        }
        var jsonParams = objectMapper.readTree(params);
        if (!jsonParams.has(ABS)) {
            responseText = "recipient params not found nci object";
            setError(transaction, responseText);
            return new CommonResultData<>(BmResultStatus.INVALID_PARAMS, transaction, responseText);
        }
        var recipientParams = objectMapper.convertValue(jsonParams.get(ABS), AbsParams.class);

        AbsTransactionRequest transactionDTO = getTransactionRequest(transaction, senderAccount, senderName, senderMfo, recipientParams, recipient);

        Bill bill = doTransaction(payDTO, transaction, agentLogin, recipient, transactionDTO);

        if (!bill.isSuccess()) {
            applicationEventPublisher.publishEvent(new PartnerBillingErrorEvent(this, "Nci", bill.getResponseText(), transaction.getId()));
            setError(transaction, bill.getResponseText());
            return new CommonResultData<>(BmResultStatus.SENDER_PC_ERROR, transaction, bill.getResponseText());
        }

        transaction
                .senderAccount(bill.getAccount())
                .senderAmount(bill.getAmount())
                .senderCcy(transaction.getRecipientCcy())
                .senderDate(Instant.now())
                .senderGateway(PayType.NCI.name())
                .senderStatus(bill.getResponseCode())
                .senderStatusText(bill.getResponseText())
                .senderSuccess(bill.isSuccess())
                .senderTrId(bill.getExtId())
                .senderBillId(bill.getId())
        ;

        log.debug("Saving success transaction : {}", transaction);
        transactionRepository.save(transaction);
        return new CommonResultData<>(transaction);
    }

    private HasData<String> validateRequest(CommonPayRequest payDTO, Transaction transaction) {
        log.info("Pay dto params : {}", payDTO.getParams());
        if (payDTO.getParams() == null || !payDTO.getParams().containsKey(Constants.ACCOUNT_PARAM) || payDTO.getParams().get(Constants.ACCOUNT_PARAM) == null) {
            String responseText = "счет для оплаты не найден";
            setError(transaction, responseText);
            return new CommonResultData<>(responseText);
        }
        if (!payDTO.getParams().containsKey(Constants.NAME_PARAM) || payDTO.getParams().get(Constants.NAME_PARAM) == null) {
            String responseText = "счет для оплаты не найден";
            setError(transaction, responseText);
            return new CommonResultData<>(responseText);
        }
        if (!payDTO.getParams().containsKey(Constants.MFO_PARAM) || payDTO.getParams().get(Constants.MFO_PARAM) == null) {
            String responseText = "счет для оплаты не найден";
            setError(transaction, responseText);
            return new CommonResultData<>(responseText);
        }

        if (StringUtils.isEmpty(transaction.getRecipientName())) {
            String responseText = "имя поставщика не найден";
            setError(transaction, responseText);
            return new CommonResultData<>(responseText);
        }
        return new CommonResultData<>(BmResultStatus.SUCCESS, "success");
    }

    private HasData<String> validateRequest(CommonPayRequest payDTO) {
        log.info("Pay dto params : {}", payDTO.getParams());
        if (payDTO.getParams() == null || !payDTO.getParams().containsKey(Constants.ACCOUNT_PARAM) || payDTO.getParams().get(Constants.ACCOUNT_PARAM) == null) {
            String responseText = "счет для оплаты не найден";
            return new CommonResultData<>(BmResultStatus.INVALID_PARAMS, responseText);
        }
        if (!payDTO.getParams().containsKey(Constants.NAME_PARAM) || payDTO.getParams().get(Constants.NAME_PARAM) == null) {
            String responseText = "счет для оплаты не найден";
            return new CommonResultData<>(BmResultStatus.INVALID_PARAMS, responseText);
        }
        if (!payDTO.getParams().containsKey(Constants.MFO_PARAM) || payDTO.getParams().get(Constants.MFO_PARAM) == null) {
            String responseText = "счет для оплаты не найден";
            return new CommonResultData<>(BmResultStatus.INVALID_PARAMS, responseText);
        }
        return new CommonResultData<>(BmResultStatus.SUCCESS, "success");
    }

    private AbsTransactionRequest getTransactionRequest(Transaction transaction, String senderAccount, String senderName, String senderMfo, AbsParams absParams, Recipient recipient) {
        AbsTransactionRequest transactionDTO = new AbsTransactionRequest();

        transactionDTO.setRequestId(UUID.randomUUID().toString());
        transactionDTO.setAccCl(senderAccount); // From account
        transactionDTO.setNameCl(senderName); // From name
        transactionDTO.setBankCl(senderMfo); // From mfo
        transactionDTO.setAccCo(absParams.getAccount()); // To account
        transactionDTO.setNameCo(absParams.getName());
        transactionDTO.setBankCo(absParams.getMfo());
        transactionDTO.setSumma(transaction.getSenderAmount());
        transactionDTO.setDocNum(String.valueOf(transaction.getId()));
        transactionDTO.setDate(Timestamp.from(Instant.now()));
        transactionDTO.setPurposeId(absParams.getPurposeId());
        transactionDTO.setTransactionId(String.valueOf(transaction.getId())); // TODO - replace with docNum, optional
        transactionDTO.setSenderName(senderName);
        transactionDTO.setRecipientName(absParams.getName());

        transactionDTO.setSenderAccount(senderAccount);
        transactionDTO.setRecipientAccount(absParams.getAccount());

        transactionDTO.setSenderAccount(senderAccount);
        transactionDTO.setRecipientAccount(absParams.getAccount());
        transactionDTO.setTranType(absParams.getTranType());
        transactionDTO.setTax(absParams.getTax());

        var purpose = absParams.getPurpose();
        if (purpose.contains("${")) {
            Map variables = objectMapper.convertValue(transactionDTO, Map.class);
            variables.put(ACCOUNT, transaction.getRecipientAccount());
            variables.put(SHORTNAME, recipient.getShortName());
            transactionDTO.setPurpose(new StringSubstitutor(variables).replace(purpose));
        } else {
            transactionDTO.setPurpose(purpose);
        }
        return transactionDTO;
    }

    @Override
    public ResultData<Transaction> reverse(PayType payType, Transaction transaction, String agentLogin) {
        log.info(">>>> begin reverse >>>>");
        Optional<Bill> payBill = billRepository.findTop1ByIdAndSuccessAndType(transaction.getSenderBillId(), true, RequestType.PAY);
        if (payBill.isEmpty()) {
            log.warn("transaction for cancel not found");
            setError(transaction, "транзакция для отмена не найден");

            log.error("<<<< end reverse with error <<<<");
            return new ResultData<>(false, transaction, new CodedException(ProblemEnum.BILL_NOT_FOUND, "транзакция для отмена не найден"));
        }
        log.debug("find pay Bill: {}", payBill.get());
        Bill cancelBill = doCancel(payBill.get(), transaction, agentLogin);

        if (!cancelBill.isSuccess()) {
            log.debug("published partnerBillingErrorEvent...");
            applicationEventPublisher.publishEvent(new PartnerBillingErrorEvent(this, "Nci_cancel", cancelBill.getResponseText()));
        }

        if (!cancelBill.isSuccess()) {
            log.error("<<<< end reverse with error <<<<");
            return new ResultData<>(false, null, new CodedException(ProblemEnum.BILLING_ERROR));
        }

        transaction
                .senderStatus(TransactionStatus.SENDER_CANCELLED.name())
                .senderStatusText(cancelBill.getResponseText())
                .senderSuccess(true)
                .senderTrId(cancelBill.getExtId())
                .senderBillId(cancelBill.getId())
        ;

        log.debug("update transaction to: {}", transaction);
        transactionRepository.save(transaction);

        log.info("<<<< end reverse-success <<<<");
        return new ResultData<>(true, transaction, null);
    }

    @Override
    public HasData<Transaction> debitConfirm(CommonPayRequest payDTO, Transaction transaction, String agentLogin) {
        throw new CodedException(ProblemEnum.NOT_IMPLEMENTED);
    }

    @Override
    public PayType getSupportedPayType() {
        return PayType.NCI;
    }

    private Bill doTransaction(CommonPayRequest payDTO, Transaction transaction, String agentLogin, Recipient recipient, AbsTransactionRequest nciTransactionDTO) {

        Bill bill = new Bill()
                .recipientShortName(recipient.getShortName())
                .account(nciTransactionDTO.getRecipientAccount())
                .amount(transaction.getSenderAmount())
                .amountRatio(100)
                .currency(transaction.getSenderCcy())
                .type(RequestType.PAY)
                .login(agentLogin)
                .requestJson(payDTO.getParams())
                .requestTime(Instant.now())
                .responseTime(Instant.now())
                .success(false)
                .transactionId(transaction.getId())
                //
                ;

        Map<String, Object> requestJson = new HashMap<>();
        requestJson.put(CHECK_REQUEST_ID, nciTransactionDTO.getRequestId());
        bill.setRequestJson(requestJson);
        bill.setOutExtId(nciTransactionDTO.getRequestId());

        log.debug("Nci sender service pay Bill: {}", bill);
        bill = billRepository.save(bill);

        if ((nciTransactionDTO.getPurpose() == null || nciTransactionDTO.getPurpose().isEmpty()) && Objects.equals(transaction.getRecipientGateway(), DummyDataAdapterV3.DUMMY_GATEWAY_NAME)) {
            String customPurpose = PURPOSE + " " + transaction.getRecipientAccount() + " " + recipient.getShortName();
            nciTransactionDTO.setPurpose(customPurpose);
        }

        log.info("create nci transaction request: {}", nciTransactionDTO);
        BmResultData<AbsTransactionResult> nciTranResult = nciHelperService.doTransaction(nciTransactionDTO);
        log.info("nci return result: {}", nciTranResult);

        String errorNote = (nciTranResult.getData() != null && !StringUtils.isEmpty(nciTranResult.getData().getErrorNote())) ? nciTranResult.getData().getErrorNote() : (!StringUtils.isEmpty(nciTranResult.getDetails()) ? nciTranResult.getDetails() : "nci error");
        String errorCode = (nciTranResult.getData() != null && !StringUtils.isEmpty(nciTranResult.getData().getErrorCode())) ? nciTranResult.getData().getErrorCode() : "-1";
        String extId = (nciTranResult.getData() != null && !StringUtils.isEmpty(nciTranResult.getData().getTransId())) ? nciTranResult.getData().getTransId() : "-1";

        Map<String, Object> tranData = new HashMap<>();
        tranData.put(Constants.ACCOUNT_PARAM, nciTransactionDTO.getRecipientAccount());
        tranData.put(Constants.AMOUNT_PARAM, String.valueOf(transaction.getSenderAmount()));
        tranData.put(ERROR_CODE, errorCode);
        tranData.put(ERROR_NOTE, errorNote);
        tranData.put(EXT_ID, extId);

        bill
                .responseJson(tranData)
                .responseTime(Instant.now())
                .responseCode(errorCode)
                .responseText(errorNote)
                .extId(extId)
                .success(nciTranResult.isSuccess());

        log.debug("update Bill to: {}", bill);
        return billRepository.save(bill);
    }

    private Bill doCancel(Bill payBill, Transaction transaction, String agentLogin) {
        String requestId = "BILL_MASTER_" + System.currentTimeMillis() + transaction.getId();
        Bill cancelBill = new Bill()
                .recipientShortName(payBill.getRecipientShortName())
                .account(payBill.getAccount())
                .amount(transaction.getSenderAmount())
                .amountRatio(100)
                .currency(transaction.getSenderCcy())
                .type(RequestType.CANCEL)
                .login(agentLogin)
                .requestJson(payBill.getRequestJson())
                .requestTime(Instant.now())
                .responseTime(Instant.now())
                .success(false)
                .transactionId(transaction.getId())
                .outExtId(requestId);
        //
        ;
        cancelBill = billRepository.save(cancelBill);
        log.debug("create cancel Bill: {}", cancelBill);

        Map<String, Object> tranData = new HashMap<>();
        tranData.put(Constants.ACCOUNT_PARAM, cancelBill.getAccount());
        tranData.put(Constants.AMOUNT_PARAM, String.valueOf(transaction.getSenderAmount()));

        Long cancelTranId;
        try {
            cancelTranId = Long.valueOf(payBill.getExtId());
        } catch (NumberFormatException e) {
            log.error("parse nci tranId to long throw numberFormatException");
            cancelBill.setSuccess(false);
            cancelBill.responseJson(tranData)
                    .responseTime(Instant.now())
                    .responseCode("-1")
                    .responseText("parse nci tranId to long error")
                    .extId(payBill.getExtId())
                    .success(false);

            return billRepository.save(cancelBill);
        }


        log.info("sendCancelRequest with tranId: {} and requestId: {}", cancelTranId, requestId);
        BmResultData<AbsTransactionResult> cancelResponse = nciHelperService.sendCancelRequest(cancelTranId, requestId, "", NciUpdateTranGroupType.GENERAL);
        log.info("nci return result: {}", cancelResponse);

        String errorNote = (cancelResponse.getData() != null && !StringUtils.isEmpty(cancelResponse.getData().getErrorNote())) ? cancelResponse.getData().getErrorNote() : (!StringUtils.isEmpty(cancelResponse.getDetails()) ? cancelResponse.getDetails() : "nci error");
        String errorCode = (cancelResponse.getData() != null && !StringUtils.isEmpty(cancelResponse.getData().getErrorCode())) ? cancelResponse.getData().getErrorCode() : "-1";
        String extId = (cancelResponse.getData() != null && !StringUtils.isEmpty(cancelResponse.getData().getTransId())) ? cancelResponse.getData().getTransId() : "-1";
        tranData.put(ERROR_CODE, errorCode);
        tranData.put(ERROR_NOTE, errorNote);
        tranData.put(EXT_ID, extId);

        cancelBill
                .responseJson(tranData)
                .responseTime(Instant.now())
                .responseCode(errorCode)
                .responseText(errorNote)
                .extId(extId)
                .success(cancelResponse.isSuccess());

        log.debug("update Bill to: {}", cancelBill);
        return billRepository.save(cancelBill);
    }

    private void setError(Transaction transaction, String responseText) {
        transaction.setSenderSuccess(false);
        transaction.setSenderStatusText(responseText);
    }

    @Override
    public String getDescription() {
        return "Снятие средств со счета в NCI";
    }
}
