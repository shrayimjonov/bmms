package uz.paykap.agent.tran.hold;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import uz.devops.bmcore.dto.GatewayQueryResult;
import uz.devops.bmcore.dto.request.CommonPayRequest;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.devops.bmcore.gateway.GatewayBillingService;
import uz.devops.bmcore.gateway.SenderSkippable;
import uz.devops.bmcore.sender.dto.SenderResult;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.javlon.commons.result.HasData;
import uz.javlon.commons.result.HasResult;
import uz.paykap.agent.tran.confirm.DebitConfirmService;
import uz.paykap.agent.tran.dto.CalculationService;
import uz.paykap.agent.tran.helper.AgentHelper;
import uz.paykap.config.Constants;
import uz.paykap.domain.Transaction;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.security.SecurityUtils;
import uz.paykap.sender.SenderResolverV3;
import uz.paykap.service.cached.GatewaySelectorService;
import uz.paykap.web.rest.errors.BmTransactionException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

@Slf4j
@Component
@AllArgsConstructor
public class CommonHoldServiceHoldHelper {

    public static final String OK = "OK";
    public static final String INTERNAL = "internal";
    public static final String REF_NUM = "REF_NUM";
    public static final String DATE = "DATE";
    public static final String TOKEN_AND_PAN_NOT_FOUND = "token and pan not found";

    private final TransactionRepository transactionRepository;
    private final CalculationService calculationService;
    private final GatewaySelectorService gatewaySelectorService;
    private final SenderResolverV3 senderResolver;
    private final ApplicationContext applicationContext;
    private final AgentHelper agentHelper;

    public SimpleBillResponse reHold(CommonPayRequest payDTO) {
        String login = getCurrentUserName();

        Transaction transaction = findTransaction(payDTO, login);

        log.info("\n\n###### Hold Request from {} ######: \n{}\n", login, payDTO);

//        SimpleBillResponse invalidParamResponse = verifyHoldRequest(payDTO);
//        if (invalidParamResponse != null) return invalidParamResponse;

        if (transaction.getStatus() == TransactionStatus.SUCCESS) {
            return new SimpleBillResponse()
                    .success(false)
                    .status(TransactionStatus.HOLD_ERROR)
                    .responseText("Transaction already finished");
        }

        if (transaction.getStatus() != TransactionStatus.HOLD_SUCCESS) {
            return new SimpleBillResponse()
                    .success(false)
                    .status(TransactionStatus.HOLD_ERROR)
                    .responseText(ProblemEnum.INVALID_PARAMS.getDetail());
        }

//        SimpleBillResponse transactionErrorResponse = verifyHoldTransaction(transaction, payDTO);
//        if (transactionErrorResponse != null) return transactionErrorResponse;

        GatewayQueryResult gatewayQueryResult = gatewaySelectorService.getValidGatewayQueryResultV5(transaction.getRecipientServiceId(), login, transaction.getRequestGroup());

        SimpleBillResponse amountErrorResponse = verifyAmountAndUpdateTran(transaction, payDTO, gatewayQueryResult);
        if (amountErrorResponse != null) return amountErrorResponse;

        //Do only if checked ok, mark as payment in progress (status=PAYING) and continue payment
        transaction.status(TransactionStatus.PAYING);
        transaction.payType(payDTO.getType());
        transaction = transactionRepository.save(transaction);
        // payment will go on
        log.debug("OK - Transaction found : {}", transaction);

        whenAgentCanNotProceedThrowException(payDTO, login, transaction);
        setPayTypeWithBin(payDTO, transaction);

        GatewayBillingService gatewayService = getGatewayService(transaction, gatewayQueryResult);
        payDTO.setSettingId(gatewayQueryResult.getSettingId());

        if (gatewayIsSkippable(payDTO, transaction, gatewayService)) return fromTransaction(transaction);

        HasData<Transaction> hold = senderResolver.reHold(payDTO, transaction, login);
        return verifyHoldTranAndReturn(payDTO, transaction, hold);
    }

    public SimpleBillResponse holdForAgent(CommonPayRequest payDTO) {
        String login = getCurrentUserName();
        Transaction transaction = findTransaction(payDTO, login);
        log.info("\n\n###### Hold Request from {} ######: \n{}\n", login, payDTO);

        SimpleBillResponse invalidParamResponse = verifyHoldRequest(payDTO);
        if (invalidParamResponse != null) return invalidParamResponse;

        SimpleBillResponse transactionErrorResponse = verifyHoldTransaction(transaction, payDTO);
        if (transactionErrorResponse != null) return transactionErrorResponse;

        GatewayQueryResult gatewayQueryResult = gatewaySelectorService.getValidGatewayQueryResultV5(transaction.getRecipientServiceId(), login, transaction.getRequestGroup());

        SimpleBillResponse amountErrorResponse = verifyAmountAndUpdateTran(transaction, payDTO, gatewayQueryResult);
        if (amountErrorResponse != null) return amountErrorResponse;

        //Do only if checked ok, mark as payment in progress (status=PAYING) and continue payment
        transaction.status(TransactionStatus.PAYING);
        transaction.payType(payDTO.getType());
        transaction = transactionRepository.save(transaction);
        // payment will go on
        log.debug("OK - Transaction found : {}", transaction);

        whenAgentCanNotProceedThrowException(payDTO, login, transaction);
        setPayTypeWithBin(payDTO, transaction);

        GatewayBillingService gatewayService = getGatewayService(transaction, gatewayQueryResult);
        payDTO.setSettingId(gatewayQueryResult.getSettingId());

        if (gatewayIsSkippable(payDTO, transaction, gatewayService)) return fromTransaction(transaction);

        HasData<Transaction> hold = senderResolver.holdForAgent(payDTO, transaction, login);
        return verifyHoldTranAndReturn(payDTO, transaction, hold);
    }

    private SimpleBillResponse verifyHoldRequest(CommonPayRequest payDTO) {
        if (payDTO.getParams() == null || ((payDTO.getParams().get(Constants.PAN_PARAM) == null || payDTO.getParams().get(Constants.EXPIRY_PARAM) == null) &&
                payDTO.getParams().get(Constants.TOKEN_PARAM) == null)) {
            return new SimpleBillResponse()
                    .success(false)
                    .status(TransactionStatus.HOLD_ERROR)
                    .responseText(TOKEN_AND_PAN_NOT_FOUND);
        }
        return null;
    }

    private SimpleBillResponse verifyHoldTranAndReturn(CommonPayRequest payDTO, Transaction transaction, HasData<Transaction> hold) {
        String message = hold.getData() != null && !StringUtils.isEmpty(hold.getData().getSenderStatusText()) ? hold.getData().getSenderStatusText() : hold.getMessage();

        if (!hold.isSuccess()) {
            log.warn("BAD - Debit not success : {}", hold.getMessage());
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setSenderStatusText(message);
            transactionRepository.save(transaction);

            throw new BmTransactionException(transaction, message);
        }

        payDTO.getAgentParams().put(DATE, String.valueOf(transaction.getDate()));
        if (!StringUtils.isEmpty(transaction.getSenderTrId())) {
            payDTO.getAgentParams().put(REF_NUM, transaction.getSenderTrId());
            if (transaction.getPayType() != null && transaction.getPayType().equals(PayType.HUMO) && !StringUtils.isEmpty(transaction.getSenderStatusText())) {
                log.info("humo refNum from senderStatusText");
                payDTO.getAgentParams().put(REF_NUM, transaction.getSenderStatusText());
            }
        }

        transaction.setSenderStatus(DebitConfirmService.HOLD_SUCCESS);
        transaction.setSenderStatusText(DebitConfirmService.HOLD_SUCCESS);
        transaction.setStatus(TransactionStatus.HOLD_SUCCESS);
        transaction.setSenderSuccess(true);
        log.debug("OK - Held");
        transactionRepository.save(transaction);
        return fromTransaction(transaction);
    }

    private boolean gatewayIsSkippable(CommonPayRequest payDTO, Transaction transaction, GatewayBillingService gatewayService) {
        if (gatewayService instanceof SenderSkippable) {

            SenderResult senderResult = ((SenderSkippable) gatewayService).getSenderResult(payDTO);

            transaction
                    .senderAccount(senderResult.getAccount())
                    .senderAmount(transaction.getSenderAmount())
                    .senderCcy(transaction.getSenderCcy())
                    .senderDate(senderResult.getDate())
                    .senderGateway(senderResult.getGateway())
                    .senderStatus(senderResult.getStatus())
                    //TODO new field for centreId from humo
                    .senderStatusText(senderResult.getStatusText())
                    .senderSuccess(senderResult.isSuccess())
                    .senderTrId(senderResult.getExtId())
                    .senderBillId(senderResult.getBillId());

            transactionRepository.save(transaction);
            log.warn("Hold skipped");
            return true;
        }
        return false;
    }

    private SimpleBillResponse verifyAmountAndUpdateTran(Transaction transaction, CommonPayRequest payDTO, GatewayQueryResult gatewayQueryResult) {
        if (transaction.getRequestAmount() == null) {

            log.debug("Amount NOT given on check, have to calculate financial information");
            if (payDTO.getAmount() == null) {
                log.warn("Amount NOT given on check and NOT in pay, throwing exception");
                transaction.setStatus(TransactionStatus.HOLD_ERROR);
                transaction.setSenderStatusText(ProblemEnum.INVALID_PARAMS.getDetail() + " . Amount not given");
                transaction.setSenderSuccess(false);
                transactionRepository.save(transaction);
                return getSimpleErrorResponse(transaction);
            }

            log.debug("Amount GIVEN on pay, calculate");
            transaction.requestAmount(payDTO.getAmount());
            transactionRepository.save(transaction);
            log.debug("OK - Tran saved : {}", transaction.getId());

            SimpleBillResponse amountErrorResponse = verifyAmount(transaction, gatewayQueryResult);
            if (amountErrorResponse != null) return amountErrorResponse;

            calculationService.calc(transaction, gatewayQueryResult);
            transactionRepository.save(transaction);
            log.debug("OK - Tran calc finished");
        }
        return null;
    }

    private SimpleBillResponse verifyHoldTransaction(Transaction transaction, CommonPayRequest payDTO) {
        if (transaction == null) {
            return prepareTransactionNotFoundBill();
        }
        if (transaction.getStatus() != TransactionStatus.CHECK_OK) {
            // Return current state of tran, payment could be in progress or waiting for provider's response
            return prepareTranCurrentStateBill(transaction);
        }
        return null;
    }

//    private String findAndGetSenderAccount(Map<String, String> params) {
//        if (params.get(Constants.PAN_PARAM) == null) {
//            throw new CodedException(ProblemEnum.INVALID_PARAMS, "sender account not found");
//        }
//        return params.get(Constants.PAN_PARAM);
//    }

    private GatewayBillingService getGatewayService(Transaction transaction, GatewayQueryResult gatewayQueryResult) {
        GatewayBillingService gatewayService = getServiceBean(gatewayQueryResult.getGatewayBean());
        if (!gatewayService.isPayTypeSupported(transaction.getPayType())) {
            throw new BmTransactionException(transaction, "payType " + transaction.getPayType() + " not supported by recipient gateway service");
        }
        return gatewayService;
    }

    private String getCurrentUserName() {
        return SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));
    }

    private Transaction setPayTypeWithBin(CommonPayRequest payDTO, Transaction transaction) {
        /*
         * In PC Sender we are changed PayType by BIN, if so then save new transaction.payType in db
         * */
        // TODO - find elegant way to resolve base payType
        if (payDTO.getType() != null && !Objects.equals(payDTO.getType(), transaction.getPayType())) {
            transaction.setPayType(payDTO.getType());
            transactionRepository.save(transaction);
        }
        return transaction;
    }

    private GatewayBillingService getServiceBean(String gatewayBean) {
        try {
            return applicationContext.getBean(gatewayBean, GatewayBillingService.class);
        } catch (Exception e) {
            log.warn(e.getLocalizedMessage());
            throw new CodedException(ProblemEnum.SERVICE_NOT_FOUND, "Bean not found");
        }
    }

    private void whenAgentCanNotProceedThrowException(CommonPayRequest payDTO, String login, Transaction transaction) {
        var canAgentProceed = senderResolver.canAgentProceed(payDTO.getType(), login, payDTO, transaction.getSenderAmount());

        log.debug("CanAgentProceed: {}", canAgentProceed);

        if (!canAgentProceed.isSuccess()) {
            transaction.status(TransactionStatus.FAILED);
            transactionRepository.save(transaction);

            log.warn("Agent can NOT proceed (maybe, insufficient funds on deposit or card) : {} : {}", login, payDTO.getType());

            throw new BmTransactionException(transaction, canAgentProceed.getMessage());
        }
    }

    private Transaction findTransaction(CommonPayRequest payDTO, String login) {
        Transaction transaction;

        if (!Objects.equals(login, Constants.ADMIN_USERNAME)) {

            log.debug("AGENT wants to pay");
            // AGENT
            transaction = transactionRepository.findTop1ByRequestLoginAndId(login, payDTO.getBillId()).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));
        } else {

            log.debug("ADMIN wants to pay");
            // ADMIN
            transaction = transactionRepository.findTop1ById(payDTO.getBillId()).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));
        }
        return transaction;
    }

    private SimpleBillResponse prepareTransactionNotFoundBill() {

        SimpleBillResponse simpleBillDTO = new SimpleBillResponse();

        return simpleBillDTO
                .success(false)
                .status(TransactionStatus.OTHER)
                .responseText("Transaction not found")
                //
                ;
    }

    private SimpleBillResponse prepareTranCurrentStateBill(Transaction transaction) {
        SimpleBillResponse simpleBillDTO = fromTransaction(transaction);

        prepareResult(transaction, simpleBillDTO);

        switch (transaction.getStatus()) {
            case PAYING:
                simpleBillDTO.setResponseText("Payment in progress, please use status method later");
                simpleBillDTO.setSuccess(false);
                break;
            case WAITING:
                simpleBillDTO.setResponseText("Waiting for response of provider, please use status method later");
                simpleBillDTO.setSuccess(false);
                break;
        }

        return simpleBillDTO;
    }

    private SimpleBillResponse fromTransaction(Transaction transaction) {

        return new SimpleBillResponse()
                .requestTime(transaction.getDate())
                .responseTime(transaction.getRecipientDate())
                .success(transaction.isRecipientSuccess())
                .recipientShortName(transaction.getRecipientName())
                .responseText(transaction.getRecipientStatusText())
                .status(transaction.getStatus())
                .id(transaction.getId())
                .extId(transaction.getRequestTrId())
                .amount(transaction.getRequestAmount())
                .account(transaction.getRecipientAccount())
                .currency(transaction.getRequestCcy())
                //
                ;
    }

    private SimpleBillResponse verifyAmount(Transaction transaction, GatewayQueryResult gatewayQueryResult) {

        HasData<Integer> currencyResult = agentHelper.getAllowedCurrency4Agent(gatewayQueryResult, transaction.getRequestCcy());
        if (!currencyResult.isSuccess()) {
            transaction = updateErrorTransaction(transaction, currencyResult.getMessage());
            return getSimpleErrorResponse(transaction);
        }

        HasResult validAmountData = calculationService.validateAmount(transaction.getRequestAmount(), currencyResult.getData(), gatewayQueryResult);
        if (!validAmountData.isSuccess()) {
            transaction = updateErrorTransaction(transaction, validAmountData.getMessage());
            return getSimpleErrorResponse(transaction);
        }
        return null;
    }

    private Transaction updateErrorTransaction(Transaction transaction, String comment) {
        transaction
                .status(TransactionStatus.FAILED)
                .comment(comment)
                .senderSuccess(false)
                .recipientSuccess(false)
                .recipientStatusText(comment)
                .date(Instant.now())
        ;

        return transactionRepository.save(transaction);
    }

    private SimpleBillResponse getSimpleErrorResponse(Transaction transaction) {
        SimpleBillResponse result = new SimpleBillResponse();
        result.id(transaction.getId());
        result.account(transaction.getRecipientAccount());
        result.amount(transaction.getRequestAmount());
        result.currency(transaction.getRequestCcy());
        result.status(transaction.getStatus());
        result.success(false);
        result.amountRatio(transaction.getAmountRatio());
        result.responseText(transaction.getSenderStatusText() + ", " + transaction.getRecipientStatusText());
        result.responseTime(transaction.getDate());
        result.requestTime(transaction.getDate());
//        result.supportsFinal(true); //TODO
        return result;
    }

    private void prepareResult(Transaction transaction, SimpleBillResponse result) {
        // Tran.id is main id
        result.setId(transaction.getId());
        result.setStatus(transaction.getStatus());
        result.setAmount(transaction.getRequestAmount());

        if (result.getData() == null) {
            result.setData(new HashMap<>(3));
        }

        result.getData().put("rate", String.valueOf(transaction.getCcyRate()));
        result.getData().put("recipientCcy", String.valueOf(transaction.getRecipientCcy()));
        result.getData().put("recipientAmount", String.format(Locale.US, "%.2f", transaction.getGatewayAmount()));
//        if (transaction.getSenderAmount() != null) {
//            result.getData().put("senderAmount", String.format(Locale.US, "%.2f", transaction.getSenderAmount() / 100d));
//        }
        result.getData().put("senderCcy", String.valueOf(transaction.getSenderCcy()));
    }
}
