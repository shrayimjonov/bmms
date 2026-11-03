package uz.paykap.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.dto.GatewayQueryResult;
import uz.devops.bmcore.dto.request.*;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.devops.bmcore.gateway.GatewayService;
import uz.devops.bmcore.projection.GatewayResultParamProjection;
import uz.javlon.commons.base.Result;
import uz.javlon.commons.base.ResultData;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.agent.tran.AdminTranServiceV2;
import uz.paykap.agent.tran.AdminTranServiceV3;
import uz.paykap.agent.tran.CommonTranServiceV3;
import uz.paykap.agent.tran.dto.CalculationService;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.domain.Bill;
import uz.paykap.domain.BookkeepingTemplate;
import uz.paykap.domain.Transaction;
import uz.paykap.event.dto.SendToMailEvent;
import uz.paykap.gateway.GatewayServiceResolver;
import uz.paykap.repository.BillRepository;
import uz.paykap.repository.BookkeepingTemplateRepository;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.security.SecurityUtils;
import uz.paykap.sender.SenderResolverV3;
import uz.paykap.service.CalculateUtils;
import uz.paykap.service.OverpayService;
import uz.paykap.service.TransactionService;
import uz.paykap.service.cached.GatewaySelectorService;
import uz.paykap.service.dto.OverpayResult;
import uz.paykap.service.dto.PaymentEditDTO;
import uz.paykap.service.dto.TransactionDTO;
import uz.paykap.service.mapper.TransactionMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Service
@Slf4j
@AllArgsConstructor
public class OverpayServiceImpl implements OverpayService {

    public static final String TRANS_ID = "transId";

    @Override
    public ResultData<List<TransactionDTO>> payGateway(List<Long> tranIds, String login) {
//        log.debug("");
        List<Transaction> transactions = new ArrayList<>();

        for (Long tranId : tranIds) {
            try {
                Transaction transaction = transactionRepository.findTop1ByIdAndRecipientSuccess(tranId, false).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));
                transactions.add(transaction);
            } catch (Exception e) {
                log.warn("Transaction with id " + tranId + " is not found! ");
            }
        }

        List<TransactionDTO> simpleBillDTOS = new ArrayList<>();

        for (Transaction transaction : transactions) {
            try {
                simpleBillDTOS.add(toPayGateway(transaction, login));
            } catch (Exception e) {
                log.warn("Transaction :" + transaction + "finished with error :" + e.getLocalizedMessage());
            }
        }
        return new ResultData<>(simpleBillDTOS);
    }

    @Override
    public ResultData<OverpayResult> cancelGateway(Long tranId) {

        Result resultCancel = adminTranServiceV3.cancel(tranId);

        OverpayResult overpayResult = new OverpayResult()
            .success(resultCancel.isSuccess())
            .requestType(RequestType.CANCEL);

        log.debug("Cancel completed with result : {}", overpayResult.isSuccess());

        return new ResultData<>(overpayResult);
    }

    @Override
    public ResultData<OverpayResult> payUzcard(Long tranId) {

        Transaction transaction = transactionService.getOne(tranId).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));

        Bill bill = billRepository.findTop1ByIdAndType(transaction.getRecipientBillId(), RequestType.PAY).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));

        CommonPayRequest payDTO = new PayRequest()
            .amount(transaction.getSenderAmount())
            .billId(transaction.getRecipientBillId())
            .type(PayType.UZCARD)
            .params(bill.getRequestJson());

        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

        log.info("Username {} wants to pay", login);

        SimpleBillResponse simpleBillDTO = commonTranService.pay(payDTO);

        OverpayResult overpayResult = new OverpayResult()
            .success(simpleBillDTO.getSuccess())
            .resultText(simpleBillDTO.getResponseText())
            .requestType(RequestType.PAY);

        return new ResultData<>(overpayResult);
    }

    @Override
    public ResultData<OverpayResult> cancelSender(Long tranId) {

        if (tranId == null) {
            return new ResultData<>(new CodedException(ProblemEnum.INVALID_PARAMS));
        }

        Transaction transaction = transactionService.getOne(tranId).orElseThrow(() -> new CodedException(ProblemEnum.NOT_FOUND, "Транзакция не найдена с ид. = " + tranId));

        log.debug("Transaction to reverse from uzcard : {}", transaction);

        ResultData<Transaction> data = senderResolver.reverse(transaction.getPayType(), transaction, transaction.getRequestLogin());

        OverpayResult overpayResult = new OverpayResult()
            .resultText(data.getData().getRecipientStatusText())
            .success(data.getData().isSenderSuccess())
            .requestType(RequestType.CANCEL);

        return new ResultData<>(overpayResult);
    }


    @Override
    public TransactionDTO toPayGateway(Transaction transaction, String agentName) {

        Bill bill = billRepository.findTop1ByIdAndType(transaction.getRecipientBillId(), RequestType.PAY).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));

        //todo check status on gateway billing
        Map<String, Object> agentParams = new HashMap<>();

        if (bill.getRequestJson() != null && bill.getRequestJson().containsKey("MFO")) {
            agentParams.put("MFO", bill.getRequestJson().get("MFO"));
        }

        CommonCheckRequest simpleCheckDTO = new CheckRequest()
            .recipientId(transaction.getRecipientServiceId())
            .requestGroup(transaction.getRequestGroup())
            .account(transaction.getRecipientAccount())
            .requestSubGroup(transaction.getRequestSubAccount())
            .agentTranId(transaction.getRequestTrId())
            .agentParams(agentParams)
//            .amount(transaction.getSenderAmount())
            .params(bill.getRequestJson());

        SimpleBillResponse simpleBillDTO = commonTranService.check(simpleCheckDTO, agentName, transaction.getDate());

        if (!simpleBillDTO.getSuccess()) {
            log.warn("Check request in overpay is not success");
            throw new CodedException(ProblemEnum.BILLING_ERROR, "Check request in overpay is not success");
        } else {

            CommonPayRequest payDTO = new PayRequest()
                .billId(simpleBillDTO.getId())
                .amount(transaction.getRequestAmount())
                .type(transaction.getPayType())
                .params(bill.getRequestJson());

            simpleBillDTO = commonTranService.pay(payDTO, agentName, false);
            transaction.setMain(false);

            transactionRepository.save(transaction);

            if (simpleBillDTO.getSuccess()) {
                log.debug("Transaction :" + payDTO + "finished success");
                transaction.setStatus(TransactionStatus.FINISHED);
                transactionRepository.save(transaction);
            }
        }


        Transaction transactionResponse = transactionRepository.findTop1ByIdAndStatus(simpleBillDTO.getId(), simpleBillDTO.getStatus()).orElseThrow(() -> new CodedException(ProblemEnum.OBJECT_NOT_FOUND));

        return transactionMapper.toDto(transactionResponse);

//        return new OverpayResult()
//            .resultText(simpleBillDTO.getResponseText())
//            .success(simpleBillDTO.isSuccess())
//            .requestType(RequestType.PAY);
    }

    @Override
    public ResultData<OverpayResult> cancelAbs(Long tranId) {
        throw new CodedException(ProblemEnum.NOT_IMPLEMENTED);
  }

//    @Override
//    public ResultData<OverpayResult> pay(PaymentEditDTO paymentEditDTO) {
//
//        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));
//
//        Transaction transaction = transactionRepository.findTop1ByIdAndStatus(paymentEditDTO.getTransactionId(), paymentEditDTO.getStatus()).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));
//
//        log.debug("Transaction : {}", transaction.toString());
//        boolean isCheckSuccess;
//        Long billId;
//
//        SimpleBillResponse simpleBillDTO = new SimpleBillDTO();
//
//        Bill bill = billRepository.findById(transaction.getRecipientBillId()).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));
//
//        String mfo = "";
//        if (bill.getRequestJson() != null) {
//            mfo = bill.getRequestJson().get("mfo");
//        }
//
//        if (paymentEditDTO.getStatus() != TransactionStatus.CHECK_OK) {
//            simpleBillDTO = check(paymentEditDTO, mfo, transaction);
//
//            isCheckSuccess = simpleBillDTO.isSuccess();
//            billId = simpleBillDTO.getId();
//
//        } else {
//            isCheckSuccess = true;
//            billId = transaction.getId();
//        }
//
//
//        if (!isCheckSuccess) {
//            log.warn("Check request in overpay is not success");
//
//        } else {
//
//            CommonPayRequest payDTO = new PayRequest()
//                .billId(billId)
//                .amount(paymentEditDTO.getAmount())
//                .params(paymentEditDTO.getParams())
//                .type(paymentEditDTO.getPayType());
//
//            simpleBillDTO = adminTranServiceV2.pay(payDTO);
//
//            if (simpleBillDTO.isSuccess()) {
//                log.debug("Transaction :" + payDTO + "finished success");
//                transaction.setStatus(TransactionStatus.SUCCESS);
//                transactionRepository.save(transaction);
//            }
//        }
//
//        return new ResultData<>(new OverpayResult()
//            .resultText(simpleBillDTO.getResponseText())
//            .success(simpleBillDTO.isSuccess())
//            .requestType(RequestType.PAY));
//    }

    protected void swapAndSave(Transaction transaction1, Transaction transaction2) {
        Transaction clone1 = transactionMapper.clone(transaction1);
        Long id1 = clone1.getId();

        Transaction clone2 = transactionMapper.clone(transaction2);
        Long id2 = clone1.getId();

        clone1.setId(id2);
        clone2.setId(id1);

        transactionRepository.save(clone1);
        transactionRepository.save(clone2);
    }

    @Override
    public TransactionDTO edit(PaymentEditDTO paymentEditDTO) {

        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

        log.info("###### Edit Request from {} ######: \n{}\n", login, paymentEditDTO);

        Transaction mainTransaction = transactionRepository.findById(paymentEditDTO.getTransactionId()).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));

        Bill mainBill = billRepository.findById(mainTransaction.getRecipientBillId()).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));

//        String account = paymentEditDTO.getParams().get("account");


//        if (account == null) {
//            throw new CodedException(ProblemEnum.INVALID_PARAMS);
//        }

        String mfo = "";
        if (mainBill.getRequestJson() != null) {
            mfo = String.valueOf(mainBill.getRequestJson().get("mfo"));
        }

        CommonCheckRequest simpleCheckDTO = new CheckRequest()
            .recipientId(paymentEditDTO.getRecipientId())
            .params(paymentEditDTO.getParams())
            .agentParams(getAgentParams(mainTransaction, mfo))
            .account(paymentEditDTO.getAccount())
            .agentTranId(mainTransaction.getRequestTrId())
            .requestGroup(mainTransaction.getRequestGroup())
            .requestSubGroup(mainTransaction.getRequestSubAccount());

        log.debug("checkDto :{}", simpleCheckDTO);

        SimpleBillResponse simpleBillDTO = commonTranService.check(simpleCheckDTO, mainTransaction.getRequestLogin(), mainTransaction.getDate());

        log.debug("Check response :{}", simpleBillDTO);

        Transaction checkTransaction = transactionRepository.findTop1ByIdAndStatus(simpleBillDTO.getId(), TransactionStatus.CHECK_OK).orElseThrow(() -> new CodedException(ProblemEnum.BILLING_ERROR));

        log.debug("Check transaction :{}", checkTransaction);

        GatewayQueryResult gatewayQueryResult = gatewaySelectorService.getValidGatewayQueryResultV5(checkTransaction.getRecipientServiceId(), checkTransaction.getRequestLogin(), checkTransaction.getRequestGroup());

        log.debug("Gateway query result :{}", gatewayQueryResult);

        checkTransaction.requestAmount(paymentEditDTO.getAmount());

        calculationService.calc(checkTransaction, gatewayQueryResult);

        checkTransaction.setPayType(mainTransaction.getPayType());

        String content = fillContentEdit(mainTransaction);

//        mailService.sendEmail("user@example.com", "Данные по редактированию платежа", content);
        for (String mail : applicationProperties.getDefaultToMails()) {
            applicationEventPublisher.publishEvent(new SendToMailEvent(this, mail, "Данные по редактированию платежа", content, false, false));
        }
//        applicationEventPublisher.publishEvent(new SendToMailEvent(this, applicationProperties.getDefaultEmailToKapital(), "Данные по редактированию платежа", content, false, false));

        log.debug("After calculation :{}", checkTransaction.toString());

//        swapAndSave(mainTransaction, checkTransaction);

        return transactionMapper.toDto(transactionRepository.save(mainTransaction));
    }

//    public ResultData<OverpayResult> createPay2(CommonPayRequest payDTO) {
//        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));
//
//        log.info("###### Pay Request from {} ######: \n{}\n", login, payDTO);
//
//        Transaction transaction = transactionRepository.findTop1ByIdAndStatus(payDTO.getBillId(), TransactionStatus.CHECK_OK).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));
//
//        transaction.payType(payDTO.getType());
//
//        if (payDTO.getAmount() != null) {
//            transaction.setRequestAmount(payDTO.getAmount());
//        }
//
//        transaction = transactionRepository.save(transaction);
//
//        //TODO
//        if (!senderResolver.canAgentProceed(payDTO.getType(), login, payDTO, transaction.getSenderAmount()).getData()) {
//            throw new CodedException(ProblemEnum.INSUFFICIENT_FUNDS_ON_DEPOSIT);
//        }
//
//        var debit = senderResolver.debit(payDTO, transaction, login);
//
//        if (!debit.isSuccess()) {
//            throw debit.getException();
//        }
//
//        payDTO.setBillId(transaction.getRecipientBillId());
//
//        SimpleBillResponse result = gatewayServiceResolver.pay(payDTO);
//
//        transaction
//            .recipientDate(result.getResponseTime())
//            .recipientStatus(String.valueOf(result.getStatus()))
//            .recipientName(result.getRecipientShortName())
//            .recipientStatusText(result.getResponseText())
//            .recipientSuccess(result.isSuccess())
//            .status(result.isSuccess() ? TransactionStatus.SUCCESS : TransactionStatus.FAILED)
//            .recipientBillId(result.getId())
//            .recipientTrId(result.getExtId())
//            .date(Instant.now())
//        ;
//
//        transactionRepository.save(transaction);
//
//        if (!result.isSuccess()) {
//            senderResolver.reverse(payDTO.getType(), transaction, login);
//        }
//
//        // Tran.id is main id
//        result.setId(transaction.getId());
//        result.setStatus(transaction.getStatus());
//
//        log.info("###### Pay Response ######: \n{}\n", result);
//
//        return new ResultData<>(new OverpayResult()
//            .resultText(result.getResponseText())
//            .success(result.isSuccess())
//            .requestType(RequestType.PAY));
//    }


    @Override
    public TransactionDTO createPay(CommonPayRequest payDTO) {

        Transaction checkTransaction = transactionRepository.findById(payDTO.getBillId()).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND, "Transaction with id=" + payDTO.getBillId() + " not found"));

        switch (checkTransaction.getStatus()) {
            case CHECK_OK: {
                log.info("check_ok  - - - -  >  pay ");
                SimpleBillResponse result = commonTranService.pay(payDTO, checkTransaction.getRequestLogin(), false);

                log.info("###### Pay Response ######: \n{}\n", result);

                Transaction transaction = transactionRepository.findTop1ByIdAndStatus(result.getId(), result.getStatus()).orElseThrow(() -> new CodedException(ProblemEnum.INVALID_REQUEST, "Transaction not found"));

                return transactionMapper.toDto(transaction);

            }
            case FAILED: {

                log.info("failed  - - - -  >  pay ");

                return toPayGateway(checkTransaction, checkTransaction.getRequestLogin());
            }
            default: {
                log.info("Transaction status invalid");

                throw new CodedException(ProblemEnum.INVALID_REQUEST, "not check_ok, not failed");
            }
        }


    }

    @Override
    public List<Transaction> getTransactionListByDate(Instant fromDate, Instant toDate) {
        log.info("get transactionList by Date fromDate {}, toDate {}", fromDate, toDate);
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));
        log.info("by user {}", login);
        List<Transaction> transactions = transactionRepository.findByStatusAndDateBetween(TransactionStatus.SUCCESS, fromDate, toDate);
        if (transactions != null && transactions.size() > 0) {
            Iterator<Transaction> iterator = transactions.iterator();
            while (iterator.hasNext()) {
                Transaction transaction = iterator.next();
                if (transaction.getRecipientBillId() != null) {
                    Optional<Bill> bill = billRepository.findTop1ByIdAndSuccess(transaction.getRecipientBillId(), false);
                    if (!bill.isPresent()) {
                        iterator.remove();
                    }
                }
            }
        }
//        return new ArrayList<Transaction>(Objects.requireNonNull(transactions));
        return transactions;
    }

    @Override
    public SimpleBillResponse doNewPayBill(Long tranId) {
        Transaction transaction = transactionRepository.findById(tranId).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));
        Bill bill = billRepository.findById(transaction.getRecipientBillId()).orElseThrow(() -> new CodedException(ProblemEnum.INVALID_PARAMS));
        if (transaction.getStatus().equals(TransactionStatus.SUCCESS) && !bill.isSuccess() && bill.getType().equals(RequestType.PAY)) {
            try {
                //TODO - fix for hg
//                if (transaction.getRecipientGateway().equals(HgGatewayService.HG_GATEWAY_NAME) && bill.getResponseCode() != null && org.apache.commons.lang.math.NumberUtils.isNumber(bill.getResponseCode()) && !HgResponseCode.getByCode(Integer.parseInt(bill.getResponseCode())).isFatal()) {
//                    return checkStatus(bill);
//                } else {
                return doNewBill(transaction);
//                }
            } catch (NumberFormatException e) {
                log.error("Error on parse HG responseCode to integer");
                throw new CodedException(ProblemEnum.PARSE_ERROR);
            }
        } else {
            log.error("Bill for rePay with HG does not meet the requirement");
            throw new CodedException(ProblemEnum.BILL_NOT_VALID);
        }
    }

    private SimpleBillResponse doNewBill(Transaction transaction) {
        CommonCheckRequest simpleCheckDTO = getCheckDto(transaction);
        SimpleBillResponse checkDTO = gatewayServiceResolver.check(simpleCheckDTO);
        if (checkDTO.getSuccess()) {
            CommonPayRequest payDTO = new PayRequest();
            payDTO.setBillId(checkDTO.getId());
            SimpleBillResponse payResult = gatewayServiceResolver.pay(payDTO);
            if (payResult.getSuccess()) {
                transaction.setRecipientBillId(payResult.getId());
                transaction.setRecipientStatusText(payResult.getResponseText());
                transaction = transactionRepository.save(transaction);
            } /*else {
            transaction.setRecipientBillId(payResult.getId());
            transaction.setRecipientStatusText(payResult.getResponseText());
            transaction = transactionRepository.save(transaction);
        }*/
            return payResult;
        } else {
            log.error("check from billing is error: {}", checkDTO);
            throw new CodedException(ProblemEnum.BILLING_ERROR);
        }
    }

//    private SimpleBillResponse checkStatus(Bill bill) {
//        CommonStatusRequest statusDTO = new StatusRequest();
//        statusDTO.setBillId(bill.getId());
//        GatewayService gatewayService = applicationContext.getBean(HgGatewayService.BEAN_NAME, GatewayService.class);
//        return gatewayService.status(statusDTO);
//    }

    private CheckRequest getCheckDto(Transaction transaction) {
        CheckRequest simpleCheckDTO = new CheckRequest();
        simpleCheckDTO.setGatewayAmount(transaction.getGatewayAmount());
        simpleCheckDTO.setAgentTranId(transaction.getRequestTrId());
        simpleCheckDTO.setRecipientId(transaction.getRecipientServiceId());
        simpleCheckDTO.setAccount(transaction.getRecipientAccount());
        simpleCheckDTO.setCurrency(transaction.getRecipientCcy());
        simpleCheckDTO.setCurrency(transaction.getRecipientCcy());

        Map<String, Object> checkDtoMap = new HashMap<>();
        checkDtoMap.put(TRANS_ID, transaction.getId().toString());
        simpleCheckDTO.setParams(checkDtoMap);

        return simpleCheckDTO;
    }

    @Override
    public boolean sendCancelData(Long tranId) {
        log.info("Sending cancel data by tran ID :{}", tranId);
        Transaction transaction = transactionRepository.findTop1ByIdAndStatus(tranId, TransactionStatus.SUCCESS).orElseThrow(() -> new CodedException(ProblemEnum.OBJECT_NOT_FOUND));
        String content = fillContentCancel(transaction, transaction.getRecipientDate());
        log.debug("Send cancel data to email :{}", content);
        try {
            for (String mail : applicationProperties.getDefaultToMails()) {
                applicationEventPublisher.publishEvent(new SendToMailEvent(this, mail, "Данные по отмене платежа", content, false, false));
            }
        } catch (Exception e) {
            log.warn(e.getLocalizedMessage());
            return false;
        }
        return true;
    }

    @Override
    public TransactionDTO enterAmount(PaymentEditDTO paymentEditDTO) {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

        log.info("###### Enter amount Request from {} ######: \n{}\n", login, paymentEditDTO);

        Transaction transaction = transactionRepository.findTop1ByIdAndStatus(paymentEditDTO.getTransactionId(), TransactionStatus.CHECK_OK).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));

        GatewayQueryResult gatewayQueryResult = gatewaySelectorService.getValidGatewayQueryResultV5(transaction.getRecipientServiceId(), transaction.getRequestLogin(), transaction.getRequestGroup());

        log.debug("Gateway query result :{}", gatewayQueryResult);

        transaction.requestAmount(paymentEditDTO.getAmount());

        calculationService.calc(transaction, gatewayQueryResult);

        transaction.setPayType(paymentEditDTO.getPayType());

        log.debug("After calculation :{}", transaction.toString());

        return transactionMapper.toDto(transactionRepository.save(transaction));
    }

    @Override
    public TransactionDTO innerCancel(Long tranId) {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

        log.info("###### Inner cancel Request from {} ######: \n{}\n", login, tranId);

        CommonCancelRequest cancelDTO = new CancelRequest();
        cancelDTO.setBillId(tranId);

        Transaction transaction = transactionRepository.findById(tranId).orElseThrow(() -> new CodedException(ProblemEnum.BILLING_ERROR));

        SimpleBillResponse simpleBillDTO = adminTranServiceV2.innerCancel(cancelDTO, transaction.getRequestLogin());

        Transaction cancelTransaction = transactionRepository.findTop1ByIdAndStatus(simpleBillDTO.getId(), TransactionStatus.CANCELLED).orElseThrow(() -> new CodedException(ProblemEnum.BILLING_ERROR));

        return transactionMapper.toDto(cancelTransaction);
    }

    @Override
    public ResultData<OverpayResult> checkStatusFromGateway(Long transactionId) {
        return getStatusAndSync(transactionId, false);
    }

    private ResultData<OverpayResult> getStatusAndSync(Long transactionId, boolean sync) {
        Optional<Transaction> optional = transactionRepository.findTop1ById(transactionId);

        if (optional.isEmpty()) {
            return new ResultData<>(new OverpayResult().requestType(RequestType.STATUS).resultText("Транзакция не найдена : " + transactionId).success(false));
        }

        Transaction transaction = optional.get();

        Optional<GatewayResultParamProjection> recipientOptional = recipientRepository.getByRecipientShortName(transaction.getRecipientName());

        if (recipientOptional.isEmpty()) {
            return new ResultData<>(new OverpayResult().requestType(RequestType.STATUS).resultText("Получатель не найдена : " + transaction.getRecipientName()).success(false));
        }

        //TODO - fix to V3
        GatewayService gatewayService = applicationContext.getBean(recipientOptional.get().getGatewayBean(), GatewayService.class);

        CommonStatusRequest statusDTO = new StatusRequest();
        statusDTO.setBillId(transaction.getRecipientBillId());

        SimpleBillResponse status = gatewayService.status(statusDTO);

        if (sync) {
//            if (transaction.isRecipientSuccess() != status.isSuccess()) {
            transaction.setRecipientSuccess(status.isSuccess());
            transaction.setRecipientStatusText(status.getResponseText());
            transaction.setStatus(status.getStatus());
            transaction.setRecipientStatus(String.valueOf(status.isSuccess()));
//            }
            transactionRepository.save(transaction);
        }

        return new ResultData<>(new OverpayResult().success(status.isSuccess()).resultText("Статус у поставщика : " + status.getResponseText()).requestType(RequestType.STATUS));
    }

    @Override
    public ResultData<OverpayResult> syncStatusFromGateway(Long transactionId) {
        return getStatusAndSync(transactionId, true);
    }

    private SimpleBillResponse check(PaymentEditDTO paymentEditDTO, String mfo, Transaction transaction) {

        CommonCheckRequest simpleCheckDTO = new CheckRequest()
            .recipientId(paymentEditDTO.getRecipientId())
            .params(paymentEditDTO.getParams())
            .agentParams(getAgentParams(transaction, mfo))
            .account(transaction.getRecipientAccount())
            .agentTranId(transaction.getRequestTrId())
            .requestGroup(transaction.getRequestGroup())
            .requestSubGroup(transaction.getRequestSubAccount());

        return commonTranService.check(simpleCheckDTO, transaction.getRequestLogin(), transaction.getDate());
    }



    private Map<String, String> getMapFromString(String jsonObjectAsString) {
        if (jsonObjectAsString == null || jsonObjectAsString.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(jsonObjectAsString, new TypeReference<Map<String, String>>() {
            });
        } catch (IOException e) {
            return Collections.emptyMap();
        }

    }

    private String getJsonStringFromMap(Map<String, String> stringStringMap) {
        if (stringStringMap.isEmpty()) {
            return "";
        }
        try {
            return objectMapper.writeValueAsString(stringStringMap);
        } catch (IOException e) {
            log.warn("Error when converting map to json string !!!");
            return "";
        }
    }

    private Double getAmount(BookkeepingTemplate bookkeepingTemplate, Transaction transaction) {

        Long amount = 0L;
        Long fee = 0L;
        Long commission = 0L;


        amount += transaction.getRequestAmount();
        if (transaction.getSystemFeeAmount() != null) {
            fee += transaction.getSystemFeeAmount();
        }
        if (transaction.getSystemCommissionDownAmount() != null) {
            commission += transaction.getSystemCommissionDownAmount();
        }


        log.debug("replacing value ...");
        return calculateUtils.calculateAmountRule(bookkeepingTemplate.getCalculateAmountRule().replace("AMOUNT", String.valueOf(amount)).replace("FEE", String.valueOf(fee)).replace("COMMISSION", String.valueOf(commission)));

    }

    private Map<String, Object> getAgentParams(Transaction transaction, String mfo) {
        Map<String, Object> agentparams = new HashMap<>();

        agentparams.put("requestSubGroup", transaction.getRequestSubAccount());
        agentparams.put("sessionid", transaction.getRequestTrId());
        agentparams.put("systemuserid", transaction.getRequestLogin());
        agentparams.put("machinegroup", transaction.getRequestGroup());
        agentparams.put("mfo", mfo);
        return agentparams;

    }

    private String fillContentCancel(Transaction transaction, Instant date) {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

        return "Отмена платежа: " +
            " \n GatewayTranId: " + transaction.getRecipientTrId() +
            " \n BankTranId: " + transaction.getId() +
            " \n Account: " + transaction.getRecipientAccount() +
            " \n Amount: " + transaction.getRequestAmount() +
            " \n Recipient: " + transaction.getRecipientName() +
            " \n Gateway: " + transaction.getRecipientGateway() +
            " \n Date: " + date +
            " \n Cancelled by: " + login;
    }

    private String fillContentEdit(Transaction transaction) {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

        return "Редактирование платежа:" +
            " \n GatewayTranId: " + transaction.getRecipientTrId() +
            " \n BankTranId: " + transaction.getId() +
            " \n Account: " + transaction.getRecipientAccount() +
            " \n Amount: " + transaction.getRequestAmount() +
            " \n Recipient: " + transaction.getRecipientName() +
            " \n Gateway: " + transaction.getRecipientGateway() +
            " \n Edited by: " + login;
    }

    private final ObjectMapper objectMapper;

    private final CommonTranServiceV3 commonTranService;
    private final AdminTranServiceV2 adminTranServiceV2;
    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;
    private final BillRepository billRepository;
    private final SenderResolverV3 senderResolver;
    private final BookkeepingTemplateRepository bookkeepingTemplateRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final GatewayServiceResolver gatewayServiceResolver;
    private final CalculationService calculationService;
    private final GatewaySelectorService gatewaySelectorService;
    private final TransactionMapper transactionMapper;
    private final ApplicationContext applicationContext;
    private final ApplicationProperties applicationProperties;
    private final RecipientRepository recipientRepository;
    private final CalculateUtils calculateUtils;
    private final AdminTranServiceV3 adminTranServiceV3;

}
