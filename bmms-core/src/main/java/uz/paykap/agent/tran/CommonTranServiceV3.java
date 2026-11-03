package uz.paykap.agent.tran;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.dto.GatewayQueryResult;
import uz.devops.bmcore.dto.request.AgentPayRequest;
import uz.devops.bmcore.dto.request.CommonPayRequest;
import uz.devops.bmcore.dto.request.PayRequest;
import uz.devops.bmcore.dto.request.SharedPayDTO;
import uz.devops.bmcore.dto.response.P2pParticipantDto;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.devops.bmcore.gateway.GatewayBillingService;
import uz.devops.bmcore.gateway.SenderSkippable;
import uz.devops.bmcore.sender.dto.SenderResult;
import uz.javlon.commons.base.SimpleResult;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.javlon.commons.result.HasData;
import uz.javlon.commons.result.HasResult;
import uz.paykap.agent.tran.check.GatewayCheckServiceResolver;
import uz.paykap.agent.tran.confirm.DebitConfirmService;
import uz.paykap.agent.tran.dto.CalculationService;
import uz.paykap.agent.tran.helper.AgentHelper;
import uz.paykap.aop.globalSettings.EnablePay;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.config.Constants;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.gateway.GatewayServiceV3;
import uz.paykap.repository.BillRepository;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.security.SecurityUtils;
import uz.paykap.sender.SenderResolverV3;
import uz.paykap.service.ForeignPayService;
import uz.paykap.service.PcHelper;
import uz.paykap.service.TransactionService;
import uz.paykap.service.cached.GatewaySelectorService;
import uz.paykap.service.dto.HoldPayRequest;
import uz.paykap.utils.ExceptionUtil;
import uz.paykap.web.rest.errors.BmTransactionException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class CommonTranServiceV3 extends AbstractCommonTranService {

    public static final String OK = "OK";
    public static final String INTERNAL = "internal";
    public static final String DATE = "DATE";

    public CommonTranServiceV3(GatewaySelectorService gatewaySelectorService, GatewayCheckServiceResolver gatewayCheckService, RecipientRepository recipientRepository, TransactionRepository transactionRepository, SenderResolverV3 senderResolver, ForeignPayService foreignPayService, ApplicationProperties properties, PcHelper pcHelper, GatewayGetInfoService getInfoHelper, BillRepository billRepository, DebitConfirmService debitConfirmService, TransactionService transactionService, GatewayServiceV3 gatewayServiceV3, CalculationService calculationService, ApplicationEventPublisher eventPublisher, AgentHelper agentHelper, GatewayBillingServiceResolver gatewayBillingServiceResolver) {
        super(gatewaySelectorService, gatewayCheckService, recipientRepository, transactionRepository, senderResolver, foreignPayService, properties, pcHelper, getInfoHelper, gatewayServiceV3, transactionService, eventPublisher, billRepository, gatewayBillingServiceResolver);
        this.transactionRepository = transactionRepository;
        this.debitConfirmService = debitConfirmService;
        this.gatewayServiceV3 = gatewayServiceV3;
        this.calculationService = calculationService;
        this.agentHelper = agentHelper;
    }

    public SimpleBillResponse unregisteredPay(PayRequest simple) {

        Optional<String> login = SecurityUtils.getCurrentUserLogin();
        if (login.isEmpty()) {
            return getErrorResult(simple.getBillId(), TransactionStatus.FAILED, getErrorMessage(new SimpleResult(false, null), "username not found"));
        }

        SimpleResult otpVerifiedResult = verifyOtp(simple.getBillId(), simple.getParams(), login.get());
        if (!otpVerifiedResult.isSuccess()) {
            return getErrorResult(simple.getBillId(), TransactionStatus.CHECKING, getErrorMessage(otpVerifiedResult, "verify otp error"));
        }

        Optional<Bill> checkBill = billRepository.findTop1ByTransactionIdAndTypeAndLoginAndSuccessTrue(simple.getBillId(), RequestType.CHECK, login.get());
        if (checkBill.isEmpty()) {
            return getErrorResult(simple.getBillId(), TransactionStatus.FAILED, getErrorMessage(new SimpleResult(false, null), "check bill not found"));
        }

        SimpleResult verifyParamResult = verifyCheckRequestJson(checkBill.get().getRequestJson());
        if (!verifyParamResult.isSuccess()) {
            return getErrorResult(simple.getBillId(), TransactionStatus.FAILED, getErrorMessage(verifyParamResult, "card pan or expiry not found"));
        }

        fillSenderParams(simple, checkBill.get().getRequestJson());
        return pay(simple);
    }

    private String getErrorMessage(SimpleResult simpleResult, String otherMessage) {
        return simpleResult.getException() != null && !StringUtils.isEmpty(simpleResult.getException().getMessage()) ? simpleResult.getException().getMessage() : otherMessage;
    }

    private SimpleBillResponse getErrorResult(Long id, TransactionStatus transactionStatus, String responseText) {
        SimpleBillResponse simpleBillResponse = new SimpleBillResponse()
                .id(id)
                .success(false)
                .status(transactionStatus)
                .responseTime(Instant.now())
                .responseText(responseText);

        log.warn("return error result: {}", simpleBillResponse);
        return simpleBillResponse;
    }

    @EnablePay
    public SimpleBillResponse pay(CommonPayRequest payDTO) {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

        return pay(payDTO, login, false);
    }

    @EnablePay
    @Deprecated
    public SimpleBillResponse holdPaymentWithOtp(HoldPayRequest payDTO) {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));
        SimpleResult simpleResult = verifyOtp(payDTO.getBillId(), payDTO.getParams(), login);
        if (!simpleResult.isSuccess()) {
            if (simpleResult.getException() != null) {
                throw new CodedException(ProblemEnum.INVALID_PARAMS, simpleResult.getException().getMessage());
            }
            throw new CodedException(ProblemEnum.INVALID_PARAMS, "verify otp error");
        }
        return holdPayment(payDTO);
    }

    @EnablePay
    @Deprecated
    public SimpleBillResponse holdPayment(HoldPayRequest payDTO) {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));
        return pay(payDTO, login, payDTO.getIsOnlyHold());
    }

    @EnablePay
    public SimpleBillResponse pay(AgentPayRequest agentPayRequest) {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

        Optional<Transaction> optionalTransaction = transactionRepository.findFirstByRequestTrId(agentPayRequest.getAgentTranId());
        if (optionalTransaction.isEmpty()) {
            SimpleBillResponse errorResponse = new SimpleBillResponse();
            errorResponse.setSuccess(false);
            errorResponse.setStatus(TransactionStatus.FAILED);
            errorResponse.setResponseText("Транзакция с этим agentTranId не найден");
            errorResponse.setResponseTime(Instant.now());
            errorResponse.setRequestTime(Instant.now());
            return errorResponse;
        }

        Transaction transaction = optionalTransaction.get();
        PayRequest payRequest = new PayRequest();
        payRequest.setBillId(transaction.getId());
        payRequest.setType(agentPayRequest.getType());
        payRequest.setParams(agentPayRequest.getParams());
        payRequest.setAmount(agentPayRequest.getAmount());

        return pay(payRequest, login, false);

    }

    @Deprecated
    public SimpleBillResponse confirmPayment(PayRequest payDto) {
        return debitConfirmService.checkParamsAndConfirm(payDto);
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
            result.setData(new HashMap<>(7));
        }

        result.getData().put("rate", String.valueOf(transaction.getCcyRate()));
        result.getData().put("recipientCcy", String.valueOf(transaction.getRecipientCcy()));
        result.getData().put("recipientAmount", String.format(Locale.US, "%.2f", transaction.getGatewayAmount()));
        result.getData().put("senderCcy", String.valueOf(transaction.getSenderCcy()));
    }

    private void fillReceiver(P2pParticipantDto receiver, Transaction transaction) {
        receiver.setAmount(receiver.getAmount() != null ? receiver.getAmount() : transaction.getRecipientAmount());
        receiver.setCurrency(receiver.getCurrency() != null ? receiver.getCurrency() : transaction.getRecipientCcy());
        receiver.setCommissionAmount(receiver.getCommissionAmount() != null ? receiver.getCommissionAmount() : 0);
        receiver.setName(receiver.getName() != null ? receiver.getName() : transaction.getRecipientName());
        receiver.addDataAccount(!StringUtils.isEmpty(receiver.fetchDataAccount()) ? receiver.fetchDataAccount() : transaction.getRecipientAccount());
        receiver.addDataExtId(!StringUtils.isEmpty(receiver.fetchDataExtId()) ? receiver.fetchDataExtId() : transaction.getRecipientTrId());
    }

    private void fillSender(P2pParticipantDto sender, Transaction transaction) {
        sender.setAmount(sender.getAmount() != null ? sender.getAmount() : transaction.getSenderAmount());
        sender.setCurrency(sender.getCurrency() != null ? sender.getCurrency() : transaction.getSenderCcy());
        sender.setCommissionAmount(sender.getCommissionAmount() != null ? sender.getCommissionAmount() : calculateCommission(transaction));
        sender.setName(sender.getName() != null ? sender.getName() : transaction.getSenderName());
        sender.addDataAccount(!StringUtils.isEmpty(sender.fetchDataAccount()) ? sender.fetchDataAccount() : transaction.getSenderAccount());
        sender.addDataExtId(!StringUtils.isEmpty(sender.fetchDataExtId()) ? sender.fetchDataExtId() : transaction.getSenderTrId());
    }

    private Long calculateCommission(Transaction transaction) {
        if (transaction.getSystemCommissionDownAmount() == null && transaction.getSystemCommissionUpAmount() == null) {
            return 0L;
        }
        long amount = transaction.getSystemCommissionUpAmount() != null ? transaction.getSystemCommissionUpAmount() : 0L;
        amount += transaction.getSystemCommissionDownAmount() != null ? transaction.getSystemCommissionDownAmount() : 0L;

        return amount;
    }

    //TODO - separate into pay and adminPay
    @EnablePay
    public SimpleBillResponse pay(CommonPayRequest payDTO, String login, Boolean isHoldPay) {

        log.info("\n\n###### Pay Request from {} ######: \n{}\n", login, payDTO);
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

        if (transaction == null) {
            return prepareTransactionNotFoundBill();
        }

        if (transaction.getStatus() == TransactionStatus.CHECK_OK) {
            //Do only if checked ok, mark as payment in progress (status=PAYING) and continue payment
            transaction.status(TransactionStatus.PAYING);
            transaction = transactionRepository.save(transaction);

            // payment will go on
        } else {
            // Return current state of tran, payment could be in progress or waiting for provider's response
            return prepareTranCurrentStateBill(transaction);
        }

        log.debug("OK - Transaction found : {}", transaction);

        transaction.payType(payDTO.getType());

        GatewayQueryResult gatewayQueryResult = gatewaySelectorService.getValidGatewayQueryResultV5(transaction.getRecipientServiceId(), login, transaction.getRequestGroup());

        final Optional<GatewayBillingService> gatewayBillingService = gatewayBillingServiceResolver.get(gatewayQueryResult.getGatewayBean());

        if (gatewayBillingService.isEmpty()) {
            transaction.status(TransactionStatus.FAILED);
            transaction.senderStatusText("Service with bean name " + gatewayQueryResult.getGatewayBean() + " not found");
            transaction = transactionRepository.save(transaction);
            return prepareServiceNotFoundResult();
        }

        GatewayBillingService gatewayService = gatewayBillingService.get();

        if (transaction.getRequestAmount() == null) {

            log.debug("Amount NOT given on check, have to calculate financial information");

            if (payDTO.getAmount() == null) {

                log.warn("Amount NOT given in check and NOT in pay, throwing exception");

                throw new BmTransactionException(transaction, "Amount NOT given in check and NOT in pay");
            }

            log.debug("Amount {} GIVEN in pay, calculate...", payDTO.getAmount());

            transaction.requestAmount(payDTO.getAmount());

            transactionRepository.save(transaction);

            log.debug("OK - Tran saved : {}", transaction.getId());

            HasData<Integer> currencyResult = agentHelper.getAllowedCurrency4Agent(gatewayQueryResult, transaction.getRequestCcy());

            if (!currencyResult.isSuccess()) {
                transaction
                        .status(TransactionStatus.FAILED)
                        .comment(currencyResult.getMessage())
                        .senderSuccess(false)
                        .recipientSuccess(false)
                        .recipientStatusText(currencyResult.getMessage())
                        .date(Instant.now())
                ;

                transaction = transactionRepository.save(transaction);

                return getSimpleErrorResponse(transaction);
            }

            HasResult resultData = calculationService.validateAmount(payDTO.getAmount(), currencyResult.getData(), gatewayQueryResult);

            if (!resultData.isSuccess()) {
                transaction
                        .status(TransactionStatus.FAILED)
                        .comment(resultData.getMessage())
                        .senderSuccess(false)
                        .recipientSuccess(false)
                        .recipientStatusText(resultData.getMessage())
                        .date(Instant.now())
                ;

                transaction = transactionRepository.save(transaction);

                return getSimpleErrorResponse(transaction);
            }

            calculationService.calc(transaction, gatewayQueryResult);

            log.debug("OK - Tran calc finished");
        }

        transaction = transactionRepository.save(transaction);

        // TODO - replace with HasResult
        var canAgentProceed = senderResolver.canAgentProceed(payDTO.getType(), login, payDTO, transaction.getSenderAmount());

        log.debug("CanAgentProceed: {}", canAgentProceed);

        if (!canAgentProceed.isSuccess()) {
            transaction.status(TransactionStatus.FAILED);
            transactionRepository.save(transaction);

            log.warn("Agent can NOT proceed (maybe, insufficient funds on deposit or card) : {} : {}", login, payDTO.getType());

            throw new BmTransactionException(transaction, canAgentProceed.getMessage());
        }

        /*
         * In PC Sender we are changed PayType by BIN, if so then save new transaction.payType in db
         * */
        // TODO - find elegant way to resolve base payType
        if (payDTO.getType() != null && !Objects.equals(payDTO.getType(), transaction.getPayType())) {
            transaction.setPayType(payDTO.getType());
            transaction = transactionRepository.save(transaction);
        }

        if (!gatewayService.isPayTypeSupported(transaction.getPayType())) {
            throw new BmTransactionException(transaction, "payType " + transaction.getPayType() + " not supported by recipient gateway service");
        }

        payDTO.setSettingId(gatewayQueryResult.getSettingId());

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

            log.warn("Debit skipped");
        } else {

            HasData<Transaction> debit = senderResolver.debit(payDTO, transaction, login);

            if (!debit.isSuccess()) {
                log.warn("BAD - Debit not success : {}", debit.getMessage());

                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setSenderSuccess(false);
                transaction.setSenderStatusText(debit.getMessage());

                if (ExceptionUtil.isTimeoutException(debit, transaction.getPayType())) {
                    transaction.setStatus(TransactionStatus.S_NEED_CONFIRM);
                    transaction.setSenderStatus("GATEWAY_TIMEOUT");
                    transaction.setSenderSuccess(false);
                }

                transactionRepository.save(transaction);

                throw new BmTransactionException(transaction, debit.getMessage());
            }

            payDTO.getAgentParams().put(DATE, String.valueOf(transaction.getDate()));
            if (!StringUtils.isEmpty(transaction.getSenderTrId())) {
                payDTO.getAgentParams().put(Constants.REF_NUM_PARAM, transaction.getSenderTrId());
                if (transaction.getPayType() != null && transaction.getPayType().equals(PayType.HUMO) && !StringUtils.isEmpty(transaction.getSenderStatusText()) && transaction.getSenderStatusText() != null) {
                    log.info("humo refNum from senderStatusText");
                    payDTO.getAgentParams().put(Constants.REF_NUM_PARAM, transaction.getSenderStatusText());
                }
            }

            if (isHoldPay) {
                transaction.setSenderStatus(DebitConfirmService.HOLD_SUCCESS);
                transaction.setSenderStatusText(DebitConfirmService.HOLD_SUCCESS);
                transaction.setSenderSuccess(true);
            }
            log.debug("OK - Debited");
        }

        // Save transaction after sender debit ! ! !
        transaction = transactionRepository.save(transaction);

        if (!Objects.equals(transaction.getEposMerchant(), gatewayQueryResult.getEposMerchant())) {
            log.debug("Update epos in gatewayQueryResult : ");
            gatewayQueryResult.setEposMerchant(transaction.getEposMerchant());
        }

        if (!Objects.equals(transaction.getEposTerminal(), gatewayQueryResult.getEposTerminal())) {
            gatewayQueryResult.setEposTerminal(transaction.getEposTerminal());
        }

        payDTO.setBillId(transaction.getRecipientBillId());
        payDTO.setTranId(transaction.getId());

        payDTO.setGatewayAmount(transaction.getGatewayAmount());

        SharedPayDTO sharedPayDTO = new SharedPayDTO()
                .login(login)
                .payDTO(payDTO)
                .recipientParams(gatewayQueryResult)
                .tranId(transaction.getId())
                .gatewayAmount(transaction.getGatewayAmount());

        if (payDTO.getAgentParams() == null) {
            payDTO.setAgentParams(new HashMap<>());
        }

        log.debug("Sending PAY request to {} ({}) : {}", gatewayQueryResult.getRecipientShortName(), gatewayService.getClass().getSimpleName(), sharedPayDTO);

        SimpleBillResponse result = gatewayServiceV3.pay(sharedPayDTO, gatewayService);

        log.debug("Received response from {} ({}) : {}", gatewayQueryResult.getRecipientShortName(), gatewayService.getClass().getSimpleName(), result);

        TransactionStatus status = result.getStatus() != TransactionStatus.NEW ? result.getStatus() : (Objects.equals(result.isSuccess(), Boolean.TRUE) ? TransactionStatus.SUCCESS : TransactionStatus.FAILED);

        transaction
                .recipientDate(result.getResponseTime())
                .recipientStatus(String.valueOf(result.isSuccess()))
                .recipientName(result.getRecipientShortName())
                .recipientStatusText(result.getResponseText())
                .recipientSuccess(result.isSuccess())
                .status(status)
                .recipientBillId(result.getId())
                .recipientTrId(result.getExtId())
                .date(Instant.now())
        ;

        if (Objects.equals(transaction.getStatus(), TransactionStatus.WAITING)) {
            transaction.setStatus(TransactionStatus.R_NEED_CONFIRM);
        }

        if (gatewayService instanceof SenderSkippable) {
            transaction
                    .senderDate(result.getResponseTime())
                    .senderStatus(String.valueOf(result.isSuccess()))
                    //TODO new field for centreId from humo
                    .senderStatusText(result.getResponseText())
                    .senderSuccess(result.isSuccess())
                    .senderTrId(result.getExtId())
                    .senderBillId(result.getId());
        }

        transactionRepository.save(transaction);

        if (status == TransactionStatus.FAILED) {
            log.warn("Payment failed, reversing...");
            senderResolver.reverse(payDTO.getType(), transaction, login);
            transactionRepository.save(transaction);
        }

        if (status == TransactionStatus.SUCCESS && payDTO.getType() == PayType.HUMO && !isHoldPay) {
            log.info("Humo confirm debit");
            senderResolver.debitConfirm(payDTO, transaction, login);
            transactionRepository.save(transaction);
        }

        prepareWithAdditionalResult(transaction, result);

        log.info("###### Pay Response ######: \n{}\n", result);

        push(transaction, gatewayQueryResult);
        pushOfd(gatewayQueryResult.getGatewayName(), transaction.getRecipientAmount(), transaction.getRecipientDate(), transaction.getRequestTrId());
        return result;
    }

    private void prepareWithAdditionalResult(Transaction transaction, SimpleBillResponse result) {
        prepareResult(transaction, result);

        if (result.getSender() == null) {
            result.setSender(new P2pParticipantDto());
        }
        fillSender(result.getSender(), transaction);

        if (result.getReceiver() == null) {
            result.setReceiver(new P2pParticipantDto());
        }
        fillReceiver(result.getReceiver(), transaction);
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

    private SimpleBillResponse prepareTransactionNotFoundBill() {

        SimpleBillResponse simpleBillDTO = new SimpleBillResponse();

        return simpleBillDTO
                .success(false)
                .status(TransactionStatus.OTHER)
                .responseText("Transaction not found")
                //
                ;
    }

    private SimpleBillResponse prepareServiceNotFoundResult() {

        SimpleBillResponse simpleBillDTO = new SimpleBillResponse();

        return simpleBillDTO
                .success(false)
                .status(TransactionStatus.FAILED)
                .responseText("Service not found")
                //
                ;
    }

    //    private final GatewaySelectorService gatewaySelectorService;
    private final TransactionRepository transactionRepository;
    private final DebitConfirmService debitConfirmService;
    private final GatewayServiceV3 gatewayServiceV3;
//    private final SenderResolverV3 senderResolver;
    private final CalculationService calculationService;
//    private final ApplicationContext applicationContext;
//    private final PcHelper pcHelper;
    private final AgentHelper agentHelper;
}
