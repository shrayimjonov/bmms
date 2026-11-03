package uz.paykap.agent.tran;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import uz.devops.bmcore.dto.GatewayQueryResult;
import uz.devops.bmcore.dto.request.*;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.devops.ofd.service.dto.OfdRequestWrapper;
import uz.devops.ofd.service.dto.OfdUtil;
import uz.devops.ofd.service.dto.request.OfdRequest;
import uz.devops.pusher.dto.PusherPaymentData;
import uz.devops.pusher.event.PusherPaymentEvent;
import uz.javlon.commons.base.Result;
import uz.javlon.commons.base.ResultData;
import uz.javlon.commons.base.SimpleResult;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.javlon.commons.result.HasData;
import uz.javlon.commons.result.HasResult;
import uz.javlon.commons.util.CardUtil;
import uz.paykap.agent.tran.check.GatewayCheckServiceResolver;
import uz.paykap.aop.fraud.EnableFraud;
import uz.paykap.aop.globalSettings.EnableCheck;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.config.Constants;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.gateway.GatewayServiceV3;
import uz.paykap.gateway.dto.ReconcileDTO;
import uz.paykap.gateway.dto.ReconcileResultDTO;
import uz.paykap.gateway.dto.SummaryReconcileResultDTO;
import uz.paykap.repository.BillRepository;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.security.AuthoritiesConstants;
import uz.paykap.security.SecurityUtils;
import uz.paykap.sender.SenderResolverV3;
import uz.paykap.sender.dto.SenderOtpParams;
import uz.paykap.service.ForeignPayService;
import uz.paykap.service.PcHelper;
import uz.paykap.service.TransactionService;
import uz.paykap.service.cached.GatewaySelectorService;
import uz.paykap.utils.NumberUtil;

import java.time.Instant;
import java.util.*;

@Slf4j
@AllArgsConstructor
public abstract class AbstractCommonTranService {

    protected final GatewaySelectorService gatewaySelectorService;
    private final GatewayCheckServiceResolver gatewayCheckServiceResolver;
    private final RecipientRepository recipientRepository;
    private final TransactionRepository transactionRepository;
    protected final SenderResolverV3 senderResolver;
    private final ForeignPayService foreignPayService;
    private final ApplicationProperties properties;
    private final PcHelper pcHelper;
    private final GatewayGetInfoService getInfoHelper;
    private final GatewayServiceV3 gatewayServiceV3;
    private final TransactionService transactionService;
    private final ApplicationEventPublisher eventPublisher;
    protected final BillRepository billRepository;
    protected final GatewayBillingServiceResolver gatewayBillingServiceResolver;

    public SimpleBillResponse getInfo(CommonCheckRequest infoRequest) {
        return getInfoHelper.getInfo(infoRequest);
    }

    public SimpleBillResponse status(CommonStatusRequest statusDTO) {
        return transactionService.getStatusSimpleDto(statusDTO);
    }

    protected SimpleResult verifyCheckRequestJson(Map<String, Object> requestJson) {
        if (requestJson == null || requestJson.get(Constants.PAN_PARAM) == null || requestJson.get(Constants.EXPIRY_PARAM) == null) {
            return new SimpleResult(false, new RuntimeException("card pan or expiry not found"));
        }
        return new SimpleResult(true, null);
    }

    protected void fillSenderParams(PayRequest payRequest, Map<String, Object> requestJson) {
        Map<String, Object> senderParams = new HashMap<>();
        if (payRequest.getParams() == null) {
            payRequest.params(senderParams);
        }

        String pan = (String) requestJson.get(Constants.PAN_PARAM);

        if (pcHelper.isValidToken(pan, P2pProcessorType.UZCARD) || pcHelper.isValidToken(pan, P2pProcessorType.HUMO)) {
            senderParams.put(Constants.TOKEN_PARAM, pan);
        } else {
            senderParams.put(Constants.PAN_PARAM, pan);
        }

        if (requestJson.get(Constants.EXPIRY_PARAM) != null) {
            senderParams.put(Constants.EXPIRY_PARAM, requestJson.get(Constants.EXPIRY_PARAM));
        }

        payRequest.getParams().putAll(senderParams);
    }

    protected SimpleResult verifyOtp(Long billId, Map<String, Object> params, String login) {
        if (params == null || params.get(Constants.OTP_PARAM) == null) {
            return new SimpleResult(false, new RuntimeException("otp not found"));
        }
        final Result verified = foreignPayService.verify(login, billId, String.valueOf(params.get(Constants.OTP_PARAM)));
        if (!verified.isSuccess()) {
            return new SimpleResult(false, new RuntimeException("sms not verified"));
        }
        return new SimpleResult(true, null);
    }

    public SummaryReconcileResultDTO reconcile(ReconcileDTO reconcileDTO) {

        String username = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

        List<ReconcileResultDTO> reconcileResultDTOS = transactionRepository.getReconcileData(reconcileDTO.getFromTime(), reconcileDTO.getToTime(), username);

        return new SummaryReconcileResultDTO(transactionRepository.getTotalRecipientAmount(reconcileDTO.getFromTime(), reconcileDTO.getToTime(), username), transactionRepository.getTotalCountReconcile(reconcileDTO.getFromTime(), reconcileDTO.getToTime(), username), reconcileResultDTOS);
    }

    public List<SimpleBillResponse> getStatusList(List<StatusRequest> statusDTOS) {

        if (statusDTOS == null || statusDTOS.size() == 0) {
            throw new CodedException(ProblemEnum.INVALID_PARAMS);
        }

        String username = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

        List<Long> id = new ArrayList<>();

        for (CommonStatusRequest statusDTO : statusDTOS) {
            if (statusDTO.getBillId() != null) {
                id.add(statusDTO.getBillId());
            }
        }

        List<Transaction> transactions = transactionRepository.findByRequestLoginAndIdIn(username, id);

        List<SimpleBillResponse> simpleBillDTOS = new ArrayList<>();
        for (Transaction transaction : transactions) {
            simpleBillDTOS.add(transactionService.toSimpleBillDto(transaction));
        }

        return simpleBillDTOS;
    }

    protected void pushOfd(String gatewayClassName, Long recipientAmount, Instant time, String requestTrId) {
        if (properties.getOfd().getSupportedGatewayNames().contains(gatewayClassName)) {
            final OfdRequest request = OfdUtil.buildMinimum(time, recipientAmount, requestTrId);
            log.info("Push supported gatewayService : {} : {}", gatewayClassName, request);
            eventPublisher.publishEvent(new OfdRequestWrapper(request, properties.getOfd().getPaymentTemplateName()));
        }
    }

    protected void push(Transaction transaction, GatewayQueryResult gatewayInfo) {

        if (transaction.getStatus() != TransactionStatus.SUCCESS) {
            log.debug("Push rejected, bcz transaction status : {}", transaction.getStatus());
            return;
        }

        List<String> tags = new ArrayList<>(20);

        tags.add(Constants.TAG_BM_PAYMENT);
        tags.add(gatewayInfo.getRecipientShortName());
        tags.add(gatewayInfo.getGatewayName());
        tags.add(gatewayInfo.getGatewayBean());
        tags.add(transaction.getEposTerminal());
        tags.add(transaction.getEposMerchant());
        tags.add("SUCCESS=" + (transaction.getStatus() == TransactionStatus.SUCCESS));

        log.debug("Tags resolved for pusher : {}", tags);

        PusherPaymentData paymentData = new PusherPaymentData(
                String.valueOf(transaction.getId()),
                transaction.getRecipientName(),
                transaction.getEposMerchant(),
                transaction.getEposTerminal(),
                CardUtil.maskIfBankCard(transaction.getSenderAccount()),
                transaction.getRecipientAccount(),
                transaction.getGatewayAmount() != null ? NumberUtil.format(transaction.getGatewayAmount() / transaction.getAmountRatio()) : "---",
                transaction.getSystemFeeAmount() != null ? NumberUtil.format(transaction.getSystemFeeAmount().doubleValue()) : "---",
                transaction.getDate()
        );

        eventPublisher.publishEvent(new PusherPaymentEvent(this, paymentData, tags));
    }

    public SimpleBillResponse unregisteredCheck(CheckRequest checkRequest) {
        if (checkRequest == null ||
                checkRequest.getAmount() == null ||
                checkRequest.getParams() == null ||
                checkRequest.getParams().get(Constants.PAN_PARAM) == null ||
                checkRequest.getParams().get(Constants.EXPIRY_PARAM) == null) {
            log.warn("invalid params");
            throw new CodedException(ProblemEnum.INVALID_PARAMS, "Не найден сумма или номер карты или срок карты");
        }

        validateRequestParams(checkRequest.getParams());
        checkRequest = reFormatUzCardExpiryParam(checkRequest);

        SimpleBillResponse checkResponse = check(checkRequest);
        log.debug("checkResponse: {}", checkResponse);
        if (!checkResponse.isSuccess() || !Objects.equals(checkResponse.getStatus(), TransactionStatus.CHECK_OK)) {
            log.error("return error checkResponse...");
            return checkResponse;
        }

        HasData<SenderOtpParams> otpParams = senderResolver.getOtpParams(String.valueOf(checkRequest.getParams().get(Constants.PAN_PARAM)), String.valueOf(checkRequest.getParams().get(Constants.EXPIRY_PARAM)));

        if (!otpParams.isSuccess()) {
            checkResponse
                    .success(false)
                    .status(TransactionStatus.CHECK_FAIL)
                    .responseText("OTP params error")
            ;
            return checkResponse;
        }

        log.debug("begin send otp...");
        HasResult result = foreignPayService.sendVerification(String.valueOf(checkRequest.getParams().get(Constants.PAN_PARAM)), checkResponse.getId(), otpParams.getData().getPhone(), checkRequest.getAmount());
        log.info("send sms result: {}", result);
        if (!result.isSuccess()) {
            log.error("sms send error...");
            throw new CodedException(ProblemEnum.INVALID_CODE_SMS_SENT, "Ошибка при отправке OTP");
        }

//        checkRequest.getParams().put(Constants.PAN_PARAM, otpParams.getToken());

//        String username = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));
//        ForeignPay foreignPay = foreignPayService.getUserPayment(username, otpParams.getMaskedCardInfo(), checkResponse.getId()).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND, "foreign pay not found, billId: " + tempId));
//        foreignPay.setBillId(checkResponse.getId());

//        log.debug("saved updated foreignPay: {}", foreignPay);
//        foreignPayService.save(foreignPayMapper.toDto(foreignPay));

        if (checkResponse.getData() == null) {
            checkResponse.setData(new HashMap<>());
        }

        String maskedPhone = otpParams.getData().getPhone().substring(0, 2) + "****" + otpParams.getData().getPhone().substring(otpParams.getData().getPhone().length() - 3);
        checkResponse.getData().put("otp_send_status", String.valueOf(result.isSuccess()));
        checkResponse.getData().put("phone", maskedPhone);
        checkResponse.getData().put("leftSeconds", String.valueOf(properties.getOtp().getTimeoutSeconds()));

        return checkResponse;
    }

    protected CheckRequest reFormatUzCardExpiryParam(CheckRequest request) {
        if (request == null) {
            return null;
        }
        return request.params(fixUzcardExpiryDateFormatAndSet(request.getParams()));
    }

    protected Map<String, Object> fixUzcardExpiryDateFormatAndSet(Map<String, Object> params) {
        if (params == null || !pcHelper.isValidPan(String.valueOf(params.get(Constants.PAN_PARAM)), P2pProcessorType.UZCARD)) {
            return params;
        }
        var expiry = String.valueOf(params.get(Constants.EXPIRY_PARAM));
        log.debug("process check and fix request expiry: {}", expiry);

        String formattedExpiryDate = pcHelper.validateAndFixUzcardExp(expiry);
        params.put(Constants.EXPIRY_PARAM, formattedExpiryDate);

        log.debug("process check and fix request expiry result: {}", params);
        return params;
    }

    private void validateRequestParams(Map<String, Object> requestParams) {
        if (requestParams == null) {

            log.warn("Params must be not null");
            throw new CodedException(ProblemEnum.INVALID_PARAMS);
        }
        if (requestParams.get(Constants.PAN_PARAM) == null) {

            log.warn("Pan parameter must be not null");
            throw new CodedException(ProblemEnum.INVALID_PARAMS);
        }
        if (requestParams.get(Constants.EXPIRY_PARAM) == null) {

            log.warn("Expiry parameter must be not null");
            throw new CodedException(ProblemEnum.INVALID_PARAMS);
        }
    }

    @EnableFraud
    @EnableCheck
    public SimpleBillResponse checkV4(CommonCheckRequest checkDTO) {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

        Optional<Transaction> optionalTransaction = transactionRepository.findTop1ByRequestLoginAndRequestTrIdOrderByIdDesc(login, checkDTO.getAgentTranId());

        if (optionalTransaction.isPresent()) {
            SimpleBillResponse errorResponse = new SimpleBillResponse();
            errorResponse.setSuccess(false);
            errorResponse.setStatus(TransactionStatus.CHECK_FAIL);
            errorResponse.setResponseText("Транзакция с этим agentTranId уже существует");
            errorResponse.setRequestTime(Instant.now());
            errorResponse.setResponseTime(Instant.now());
            errorResponse.setAccount(checkDTO.getAccount());
            errorResponse.setAmount(checkDTO.getAmount());
            errorResponse.setRecipientShortName(String.valueOf(checkDTO.getRecipientId()));
            return errorResponse;
        }

        return check(checkDTO, login);
    }

    @EnableFraud
    @EnableCheck
    public SimpleBillResponse check(CommonCheckRequest checkDTO) {

        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

        log.info("\n\n>>>>>>>>>> Check Request >>>>>>>>>>\nLogin : {}\nRequest:\n{}\n", login, checkDTO);

        return check(checkDTO, login);
    }

    public SimpleBillResponse check(CommonCheckRequest checkDTO, String login) {
        return check(checkDTO, login, Instant.now());
    }

    public SimpleBillResponse check(CommonCheckRequest checkDTO, String login, Instant tranDate) {

        log.debug("Check({}, {}, {})", checkDTO, login, tranDate);

        validateCheckDTO(checkDTO);

        GatewayQueryResult validGatewayQueryResultV2 = gatewaySelectorService.getValidGatewayQueryResultV5(checkDTO.getRecipientId(), login, checkDTO.getRequestGroup());

        if (!Objects.equals(validGatewayQueryResultV2.getGatewayActive(), Boolean.TRUE) || !Objects.equals(validGatewayQueryResultV2.getRecipientActive(), Boolean.TRUE) || !Objects.equals(validGatewayQueryResultV2.getAgentRecipientActive(), Boolean.TRUE)) {
            log.warn("Agent {} requested not allowed recipient {} with id {}", login, validGatewayQueryResultV2.getRecipientShortName(), validGatewayQueryResultV2.getRecipientId());
            return getServiceNotActiveResponse();
        }

        return gatewayCheckServiceResolver.check(checkDTO, login, validGatewayQueryResultV2, Instant.now());
    }

//    public SimpleBillResponse check(CommonCheckRequest checkDTO, String login, GatewayQueryResult gatewayQueryResult, Instant tranDate) {
//        return gatewayCheckService.check(checkDTO, login, gatewayQueryResult, tranDate);
//    }

    private void validateCheckDTO(CommonCheckRequest checkDTO) {
        if (checkDTO.getRecipientId() == null) {
            log.warn("BAD - recipient Id not given (null) : {}", checkDTO);
            throw new CodedException(ProblemEnum.INVALID_PARAMS);
        }

        log.debug("OK - RecipientId is not null : {}", checkDTO.getRecipientId());
    }

    private SimpleBillResponse getServiceNotActiveResponse() {

        SimpleBillResponse simpleBillDTO = new SimpleBillResponse();

        return simpleBillDTO
                .success(false)
                .status(TransactionStatus.CHECK_FAIL)
                .responseText("Service not found")
                .responseTime(Instant.now())
                //
                ;
    }

    public SimpleBillResponse cancel(CommonCancelRequest cancelDTO) {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));
        return cancel(cancelDTO, login);
    }

    public SimpleBillResponse cancel(CommonCancelRequest cancelDTO, String login) {

        log.info("\n\n###### Cancel Request from {} ######: \n{}\n", login, cancelDTO);

        Transaction transaction;

        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {

            log.debug("ADMIN wants to cancel");
            // ADMIN
            transaction = transactionRepository.findTop1ById(cancelDTO.getBillId()).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND, "Transaction=" + cancelDTO.getBillId()));
        } else {

            log.debug("AGENT wants to cancel");
            // AGENT
            transaction = transactionRepository.findTop1ByRequestLoginAndId(login, cancelDTO.getBillId()).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND, "Transaction=" + cancelDTO.getBillId()));
        }

        log.debug("OK - Transaction found : {}", transaction);

        GatewayQueryResult gatewayQueryResult = gatewaySelectorService.getValidGatewayQueryResultV5(transaction.getRecipientServiceId(), login, transaction.getRequestGroup());

        // Cancel Recipient
        SimpleBillResponse result = cancelRecipient(login, transaction, gatewayQueryResult);


        ResultData<Transaction> senderCancelResponse = cancelSender(login, transaction, gatewayQueryResult);

        if (!senderCancelResponse.isSuccess()) {
            log.warn("Sender cancel error: {}", senderCancelResponse.getException().getLocalizedMessage());

        }

        if (senderCancelResponse.isSuccess() && result.isSuccess()) {
            log.info("Recipient and Sender cancelled ! ! !");

            transaction
                    .status(TransactionStatus.CANCELLED);
        }
        prepareResult(transaction, result);

        log.info("###### Cancel Response ######: \n{}\n", result);

        return result;
    }

    private SimpleBillResponse cancelRecipient(String login, Transaction transaction, GatewayQueryResult gatewayQueryResult) {

        SharedCancelDTO sharedCancelDTO = new SharedCancelDTO()
                .billId(transaction.getRecipientBillId())
                .tranId(transaction.getId())
                .login(login)
                .recipientParams(gatewayQueryResult)
                .recipientTrId(transaction.getRecipientTrId())
                .gatewayAmount(transaction.getGatewayAmount());

        log.debug("Sending cancel request to recipient {} : {}", gatewayQueryResult.getRecipientShortName(), sharedCancelDTO);

        SimpleBillResponse result = gatewayServiceV3.cancel(sharedCancelDTO);

        log.debug("Got cancel response from recipient {} : {}", transaction.getRecipientName(), result);

        String status = Objects.equals(result.isSuccess(), Boolean.TRUE) ? "cancelled" : TransactionStatus.R_CANCEL_FAILED.name();

        TransactionStatus recipientStatus = result.getStatus() != TransactionStatus.NEW
                ? result.getStatus()
                : (Objects.equals(result.isSuccess(), Boolean.TRUE)
                ? TransactionStatus.RECIPIENT_CANCELLED
                : TransactionStatus.R_CANCEL_FAILED);

        transaction
                .recipientDate(result.getResponseTime())
                .recipientStatus(status)
                .recipientName(result.getRecipientShortName())
                .recipientStatusText(transaction.getRecipientStatusText() + "->" + status)
                .recipientSuccess(result.isSuccess())
                .status(recipientStatus)
                .recipientBillId(result.getId())
                .recipientTrId(result.getExtId())
                .date(Instant.now())
        ;

        transactionRepository.save(transaction);

        return result;
    }

    private ResultData<Transaction> cancelSender(String login, Transaction transaction, GatewayQueryResult gatewayQueryResult) {
        log.debug("Cancel sender:");
//        Bill cancelBill = BillUtil.getInitialBill(transaction.getRequestLogin(),transaction.getRequestCcy(),transaction.getSenderAccount(),transaction.getRecipientName(),transaction.getId(),transaction.getSenderAmount(), RequestType.CANCEL);
//
//        cancelBill =

        // Get sender status before cancel
        boolean alreadyCancelledInPc = false;

        if (Objects.equals(gatewayQueryResult.getHasStatus(), Boolean.TRUE)) {

            var resultData = senderResolver.status(transaction, login);

            if (resultData != null && resultData.isSuccess()) {

                // If paid in SV and not cancelled in SV but transaction.senderSuccess marked as not success, make it success
                if (!resultData.getData().isCancelled() && resultData.getData().isPaid() && !Objects.equals(transaction.isSenderSuccess(), true)) {
                    log.info("Payment marked as SUCCESS in {} billing and not yet cancelled BUT transaction.senderSuccess marked as FAILED, made it success", transaction.getPayType());
                    transaction.senderSuccess(true);
                    transactionRepository.save(transaction);
                }

                // If cancelled in SV but transaction.senderSuccess marked as success, make it failed
                if (resultData.getData().isCancelled() && Objects.equals(transaction.isSenderSuccess(), true)) {
                    alreadyCancelledInPc = true;
                    transaction.senderSuccess(false);
                    transaction.senderStatusText(transaction.getSenderStatusText() + "->" + "canceled from UI");
                    transaction.status(TransactionStatus.SENDER_CANCELLED);
                    log.info("Payment marked as CANCELLED in {} billing BUT transaction.senderSuccess marked as SUCCESS, made it CANCELLED", transaction.getPayType());
                    transactionRepository.save(transaction);
                }

            } else {
                log.warn("Status check failed or not implemented for sender : {}", transaction.getPayType());
            }

        }

        if (!alreadyCancelledInPc) {

            log.info("Reversing sender {}...", transaction.getPayType());

            ResultData<Transaction> reverse = senderResolver.reverse(transaction.getPayType(), transaction, login);

            if (reverse == null || !reverse.isSuccess()) {
                transaction.setStatus(TransactionStatus.CANCEL_FAILED);
                if (reverse != null && reverse.getException() != null) {
                    log.warn("BAD - reverse not success, {}", reverse.getException().getLocalizedMessage());
                    transaction.senderStatusText(transaction.getSenderStatusText() + "->" + reverse.getException().getLocalizedMessage());
                } else {
                    log.warn("BAD - reverse not success");
                    transaction.senderStatusText(transaction.getSenderStatusText() + "->" + "reverse failed");
                }
                transactionRepository.save(transaction);

                return new ResultData<>(false, transaction, new CodedException(ProblemEnum.BILLING_ERROR));
            }

            transaction.senderStatusText(transaction.getSenderStatusText() + "->" + "canceled in sender");
            transaction.setStatus(TransactionStatus.SENDER_CANCELLED);

            log.debug("OK - reversed, tran={}, extId={}", transaction.getId(), transaction.getSenderTrId());
        }

        return new ResultData<>(transaction);
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
        result.getData().put("senderCcy", String.valueOf(transaction.getSenderCcy()));
    }

    public Transaction cancel(Long tranId) {
        log.info("Cancel request: {}", tranId);

        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));
        log.debug("Login: {}", login);

        Transaction transaction;

        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {

            log.debug("ADMIN wants to cancel");
            // ADMIN
            transaction = transactionRepository.findTop1ById(tranId).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND, "Transaction=" + tranId));
        } else {

            log.debug("AGENT wants to cancel");
            // AGENT
            transaction = transactionRepository.findTop1ByRequestLoginAndId(login, tranId).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND, "Transaction=" + tranId));
        }

        log.debug("OK - Transaction found : {}", transaction);

        if (transaction.getStatus() == TransactionStatus.CANCELLED
                || transaction.getStatus() == TransactionStatus.REVERSED
                || transaction.getStatus() == TransactionStatus.FINISHED) {
            log.info("Transaction already cancelled");
            return transaction;
        }

        GatewayQueryResult gatewayQueryResult = gatewaySelectorService.getValidGatewayQueryResultV5(transaction.getRecipientServiceId(), transaction.getRequestLogin(), transaction.getRequestGroup());
        log.debug("GatewayQueryResult: {}", gatewayQueryResult);

        if (transaction.isRecipientSuccess() == Boolean.TRUE) {
            log.info("Cancelling in recipient");
            SimpleBillResponse result = cancelRecipient(login, transaction, gatewayQueryResult);
            log.info("Cancel recipient result: {}", result);
        }

        if (transaction.isRecipientSuccess() != Boolean.TRUE && transaction.isSenderSuccess() == Boolean.TRUE) {
            log.info("Cancelling in sender");
            ResultData<Transaction> resultCancelSender = cancelSender(login, transaction, gatewayQueryResult);
            log.info("Cancel sender result: {}", resultCancelSender);
        }

        if (transaction.isRecipientSuccess() != Boolean.TRUE && transaction.isSenderSuccess() != Boolean.TRUE) {
            transaction
                    .status(TransactionStatus.CANCELLED)
                    .nextRecipientStatusText(TransactionStatus.CANCELLED)
            ;
            transaction = transactionRepository.save(transaction);

            log.debug("Transaction saved with status cancelled: {}", transaction);

            return transaction;
        }

        return transaction;
    }
}
