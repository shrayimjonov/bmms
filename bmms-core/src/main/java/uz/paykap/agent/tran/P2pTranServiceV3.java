package uz.paykap.agent.tran;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import uz.devops.bmcore.dto.GatewayQueryResult;
import uz.devops.bmcore.dto.request.CommonCancelRequest;
import uz.devops.bmcore.dto.request.CommonStatusRequest;
import uz.devops.bmcore.dto.request.SharedCancelDTO;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.devops.ofd.service.dto.OfdRequestWrapper;
import uz.devops.ofd.service.dto.OfdUtil;
import uz.devops.ofd.service.dto.request.OfdRequest;
import uz.devops.pusher.config.PusherConstants;
import uz.devops.pusher.event.PusherEvent;
import uz.javlon.commons.base.ResultData;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.javlon.commons.util.CardUtil;
import uz.paykap.agent.tran.dto.P2pCalculatorService;
import uz.paykap.agent.tran.helper.P2pParamResolver;
import uz.paykap.agent.tran.p2p.P2pPaymentResolver;
import uz.paykap.aop.globalSettings.EnablePay;
import uz.paykap.aop.globalSettings.PreHandleRequest;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.config.Constants;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.gateway.GatewayServiceV3;
import uz.paykap.gateway.dto.ReconcileDTO;
import uz.paykap.gateway.dto.ReconcileResultDTO;
import uz.paykap.gateway.dto.SummaryReconcileResultDTO;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.security.AuthoritiesConstants;
import uz.paykap.security.SecurityUtils;
import uz.paykap.sender.SenderResolverV3;
import uz.paykap.service.TransactionService;
import uz.paykap.service.cached.GatewaySelectorService;
import uz.paykap.service.dto.p2p.*;
import uz.paykap.service.dto.p2p.calc.P2pCalcRequest;
import uz.paykap.service.dto.p2p.calc.P2pCalcResponse;
import uz.paykap.service.dto.result.BmResult;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.p2p.dto.P2pDTO;
import uz.paykap.service.p2p.dto.P2pParamResult;
import uz.paykap.service.p2p.dto.SenderAdditionalParam;
import uz.paykap.service.p2p.dto.response.P2pParticipantDto;
import uz.paykap.service.p2p.dto.response.P2pResponse;
import uz.paykap.service.p2p.register.P2pRegistry;
import uz.paykap.service.p2p.support.AbstractP2pRecipient;
import uz.paykap.service.p2p.support.AbstractP2pSender;
import uz.paykap.utils.NumberUtil;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
@Validated
@PreHandleRequest
public class P2pTranServiceV3 {

    public P2pCalcResponse calcCommission(P2pCalcRequest request) {
        if (request == null) {
            log.debug("Calc request is null");
            return new P2pCalcResponse().withSuccess(false);
        }

        Optional<P2pParamResult> p2pParam = p2pParamResolver.getP2pParamById(
                request.getFrom().getId(),
                request.getTo().getId(),
                request.getPromo() != null ? request.getPromo().getCode() : null
        );

        if (p2pParam.isEmpty()) {
            log.debug("P2pParam not found by : {}, {}", request.getFrom().getId(), request.getTo().getId());
            return new P2pCalcResponse().withSuccess(false).withResult(BmResultStatus.OPERATION_NOT_SUPPORTED);
        }

        P2pCalcResponse p2pCalcResponse = new P2pCalcResponse();

        p2pCalculatorService.calc(request, p2pCalcResponse, p2pParam.get());

        return p2pCalcResponse;
    }

    public P2pResponse getInfo() {
        P2pPayRequest request = p2pRequestScopedContext.getRequest();

        if (request == null) {
            // INVALID_REQUEST
            throw new CodedException(ProblemEnum.INVALID_PARAMS, "request is null");
        }

        P2pAccount senderAccount = request.getFrom();
        P2pAccount recipientAccount = request.getTo();

        // P2P_NOT_PERMITTED
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

        log.info(
                ">>>>>>>>>> Get Info Request >>>>>>>>>>\nLogin : {}\nRequest:\n{}\nFrom : {}\nTo : {}\n",
                login,
                request,
                senderAccount,
                recipientAccount
        );

        // Check Sender

        if (senderAccount != null) {
            BmResult senderServiceResult = p2PRegistry.getSenderService(senderAccount);

            if (!senderServiceResult.isSuccess()) {
                // UNSUPPORTED_RECIPIENT
                return fromRequest(request, senderServiceResult);
            }

            P2pDTO senderDto = p2pRequestScopedContext.getSenderDto();
            AbstractP2pSender<P2pDTO> senderService = p2pRequestScopedContext.getSenderService();

            if (senderDto == null || senderService == null) {
                // UNSUPPORTED_SENDER
                return fromRequest(request, BmResultStatus.UNSUPPORTED_SENDER);
            }

            Map<String, Object> senderInfoData = new HashMap<>(0);

            // If sender is authorized, do not fetch one's phone
            if (!request.getAuthorized() && properties.getP2p().isGetSenderInfoEnabled()) {
                BmResultData<Map<String, Object>> senderInfoResult = senderService.getP2pInfo(senderDto);

                if (!senderInfoResult.isSuccess() || senderInfoResult.getData() == null) {
                    return fromRequest(request, senderInfoResult);
                }

                log.info("<<<<<<<<<< P2p sender info result <<<<<<<<<<:\n{}\n", senderInfoResult);

                senderInfoData = senderInfoResult.getData();
            }

            p2pRequestScopedContext.setSenderParticipantDto(
                    new P2pParticipantDto(
                            String.valueOf(senderService.getProcessorType()),
                            request.getAmount(),
                            0L,
                            request.getFrom().getCurrency(),
                            senderInfoData
                    )
            );
        }

        // Check Recipient
        if (recipientAccount != null) {
            BmResult recipientServiceResult = p2PRegistry.getRecipientService(recipientAccount);

            if (!recipientServiceResult.isSuccess()) {
                return fromRequest(request, recipientServiceResult);
            }

            P2pDTO recipientDto = p2pRequestScopedContext.getRecipientDto();
            AbstractP2pRecipient<P2pDTO> recipientService = p2pRequestScopedContext.getRecipientService();

            if (recipientDto == null || recipientService == null) {
                return fromRequest(request, "invalid scoped context");
            }

            BmResultData<Map<String, Object>> recipientInfoResult = p2pRequestScopedContext.getRecipientService().getP2pInfo(recipientDto);

            if (!recipientInfoResult.isSuccess() || recipientInfoResult.getData() == null) {
                return fromRequest(request, recipientInfoResult);
            }

            log.info("<<<<<<<<<< P2p recipient info result <<<<<<<<<<:\n{}\n", recipientInfoResult);

            p2pRequestScopedContext.setRecipientParticipantDto(
                    new P2pParticipantDto(
                            String.valueOf(recipientService.getProcessorType()),
                            request.getAmount(),
                            0L,
                            request.getTo().getCurrency(),
                            recipientInfoResult.getData()
                    )
            );
        }

        P2pResponse p2pResponse = new P2pResponse()
                .fromBase(request)
                .withSuccess(true)
                .withStatusCode(new BmResult(BmResultStatus.SUCCESS));

        if (senderAccount != null) {
            p2pResponse.withSender(getSenderParticipantDto(request));
        }

        if (recipientAccount != null) {
            p2pResponse.withRecipient(getRecipientParticipantDto(request));
        }

        if (p2pRequestScopedContext.getSenderDto() != null && p2pRequestScopedContext.getRecipientDto() != null) {
            Optional<P2pParamResult> p2pParam = p2pParamResolver.getP2pParamById(
                    p2pRequestScopedContext.getSenderDto().getCondition(),
                    p2pRequestScopedContext.getRecipientDto().getCondition(),
                    p2pRequestScopedContext.getRequest().getPromo() != null ? p2pRequestScopedContext.getRequest().getPromo().getCode() : null
            );

            if (p2pParam.isEmpty()) {
                log.debug(
                        "P2pParam not found by : {}, {}",
                        p2pRequestScopedContext.getSenderDto().getCondition(),
                        p2pRequestScopedContext.getRecipientDto().getCondition()
                );
                return new P2pResponse()
                        .fromBase(request)
                        .withSuccess(false)
                        .withStatusCode(new BmResult(BmResultStatus.OPERATION_NOT_SUPPORTED));
            }

            p2pCalculatorService.calcInfo(
                    request,
                    p2pResponse,
                    p2pParam.get(),
                    p2pRequestScopedContext.getSenderService(),
                    p2pRequestScopedContext.getRecipientService()
            );
        }

        try {
            log.info("\n\nP2p Info response : \n{}\n", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(p2pResponse));
        } catch (JsonProcessingException e) {
            log.info("\n\nP2p Info response : {}\n", p2pResponse);
        }

        return p2pResponse;
    }

    @EnablePay
    public P2pResponse p2pPay() {

        log.debug("Get request data from context");
        P2pPayRequest request = p2pRequestScopedContext.getRequest();
        P2pAccount senderAccount = p2pRequestScopedContext.getSenderAccount();
        P2pAccount recipientAccount = p2pRequestScopedContext.getRecipientAccount();

        if (senderAccount == null || recipientAccount == null) {
            throw new CodedException(ProblemEnum.INVALID_PARAMS, "sender or recipient account is null");
        }

        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

        p2pRequestScopedContext.setUsername(login);

        log.debug("Initializing new transaction");
        Transaction transaction = initP2pTransaction(login, request, senderAccount, recipientAccount);
        p2pRequestScopedContext.setTransaction(transaction);

        log.info("Resolving Sender : {}", senderAccount);
        BmResult senderResult = p2PRegistry.getSenderService(senderAccount);
        if (!senderResult.isSuccess()) {
            return handleP2pError(senderResult);
        }

        log.info(
                "Resolved senderService : {}, dto : {}",
                p2pRequestScopedContext.getSenderService().getClass().getSimpleName(),
                p2pRequestScopedContext.getSenderDto()
        );
        // Set sender data into transaction
        transaction
                .senderName(p2pRequestScopedContext.getSenderService().getClass().getSimpleName())
                .senderAccount(p2pRequestScopedContext.getSenderDto().getAccount())
                .senderGateway(String.valueOf(p2pRequestScopedContext.getSenderService().getProcessorType()))
                .senderBean(p2pRequestScopedContext.getSenderService().getClass().getSimpleName());
        transactionRepository.save(transaction);

        saveSenderMfo(transaction);

        log.info("Resolving Recipient : {}", recipientAccount);
        BmResult recipientResult = p2PRegistry.getRecipientService(recipientAccount);
        if (!recipientResult.isSuccess()) {
            return handleP2pError(recipientResult);
        }

        log.info(
                "Resolved recipientService : {}, dto : {}",
                p2pRequestScopedContext.getRecipientService().getClass().getSimpleName(),
                p2pRequestScopedContext.getRecipientDto()
        );
        // Set recipient data into transaction
        transaction
                .recipientName(p2pRequestScopedContext.getRecipientService().getClass().getSimpleName())
                .recipientAccount(p2pRequestScopedContext.getRecipientDto().getAccount())
                .recipientGateway(String.valueOf(p2pRequestScopedContext.getRecipientService().getProcessorType()))
                .recipientAmount(request.getAmount())
                .recipientCcy(recipientAccount.getCurrency())
                .recipientStatusText(p2pRequestScopedContext.getRecipientDto().getCondition());
        transactionRepository.save(transaction.status(TransactionStatus.CHECKING));

        saveRecipientMfo(transaction);

        log.info("Checking recipient info : {}", p2pRequestScopedContext.getRecipientDto().getAccount());
        BmResultData<Map<String, Object>> recipientInfoResult = p2pRequestScopedContext
                .getRecipientService()
                .getP2pInfo(p2pRequestScopedContext.getRecipientDto());
        if (!recipientInfoResult.isSuccess() || recipientInfoResult.getData() == null) {
            return handleP2pError(recipientInfoResult);
        }

        p2pRequestScopedContext.setRecipientInfo(recipientInfoResult.getData());

        // Mark CHECKED_OK
        transactionRepository.save(transaction.status(TransactionStatus.CHECK_OK));

        log.info("Recipient available {}", recipientInfoResult.getData());

        log.debug("Fetching p2pParams from db");
        Optional<P2pParamResult> p2pParam = p2pParamResolver.getP2pParam(
                p2pRequestScopedContext.getSenderService().getProcessorType(),
                p2pRequestScopedContext.getRecipientService().getProcessorType(),
                p2pRequestScopedContext.getSenderDto().getCondition(),
                p2pRequestScopedContext.getRecipientDto().getCondition(),
                p2pRequestScopedContext.getRequest().getPromo() != null ? p2pRequestScopedContext.getRequest().getPromo().getCode() : null
        );

        if (p2pParam.isEmpty()) {
            log.warn(
                    "P2pParam not found : {}, {}, {}, {}",
                    p2pRequestScopedContext.getSenderService().getProcessorType(),
                    p2pRequestScopedContext.getRecipientService().getProcessorType(),
                    p2pRequestScopedContext.getSenderDto().getCondition(),
                    p2pRequestScopedContext.getRecipientDto().getCondition()
            );
            return handleP2pError(new CodedException(ProblemEnum.OBJECT_NOT_FOUND, "p2p params"));
        }

        P2pResponse p2pResponse = p2pCalculatorService.calcPay(
                request,
                p2pParam.get(),
                (ccyRate, senderAmount, recipientAmount, systemCommissionUpAmount, systemCommissionDownAmount, systemFee, systemFeeAmount, roundAmount, gatewayAmount) ->
                        transaction
                                .senderAmount(senderAmount)
                                .ccyRate(ccyRate)
                                .recipientAmount(recipientAmount)
                                .gatewayAmount(gatewayAmount)
                                .roundAmount(roundAmount)
                                .systemCommissionUpAmount(systemCommissionUpAmount)
                                .systemCommissionDownAmount(systemCommissionDownAmount)
                                .systemCurrency(Constants.DEFAULT_CURRENCY_CODE)
                                .systemFeeAmount(systemFeeAmount)
                                .systemFee(systemFee)
                                .agentFeeAmount(0L),
                p2pRequestScopedContext.getSenderService(),
                p2pRequestScopedContext.getRecipientService()
        );

        transactionRepository.save(transaction.category(p2pParam.get().getName()).status(TransactionStatus.PAYING));

        BmResult bmResult = p2pPaymentResolver.resolve(p2pParam.get().getSenderPc(), p2pParam.get().getRecipientPc()).transactP2p(transaction, p2pParam.get());

        if (!bmResult.isSuccess() && transaction.getStatus() == TransactionStatus.PAYING) {
            transaction.status(TransactionStatus.FAILED).nextSenderStatusText(bmResult.getDetails());
            transactionRepository.save(transaction);
        }

        push(transaction);
        pushOfd(transaction);

        fillExtIdAndAccount(transaction, p2pResponse);

        log.info("OK, P2p processed successfully");
        return p2pResponse
                .withStatusCode(bmResult)
                .withSuccess(SUCCESS_STATUSES.contains(transaction.getStatus()))
                .withBmId(transaction.getId());
    }

    private void pushOfd(Transaction transaction) {
        if (transaction.getSystemCommissionUpAmount() <= 0) {
            log.debug("Ignored ofd, bcz commission is null or 0");
            return;
        }

        final OfdRequest request = OfdUtil.buildMinimum(
                transaction.getDate(),
                transaction.getSystemCommissionUpAmount(),
                0,
                transaction.getRequestTrId()
        );

        if (
                p2pRequestScopedContext.getRequest().getFrom() != null &&
                        p2pRequestScopedContext.getRequest().getFrom().getParams() != null &&
                        p2pRequestScopedContext.getRequest().getFrom().getParams().has("phone")
        ) {
            request.withPhoneNumber(p2pRequestScopedContext.getRequest().getFrom().getParams().get("phone").asText());
        }
        log.debug("send OFD : {}", request);

        publisher.publishEvent(new OfdRequestWrapper(request, properties.getOfd().getP2pTemplateName()));
    }

    private void fillExtIdAndAccount(Transaction transaction, P2pResponse p2pResponse) {
        p2pResponse.getSender().addDataExtId(transaction.getSenderTrId());
        p2pResponse.getSender().addDataAccount(transaction.getSenderAccount());
        p2pResponse.getRecipient().addDataExtId(transaction.getRecipientTrId());
        p2pResponse.getRecipient().addDataAccount(transaction.getRecipientAccount());
    }

    private void saveSenderMfo(Transaction transaction) {
        if (
                p2pRequestScopedContext.getSenderService().getProcessorType() != P2pProcessorType.NCI
        ) {
            transaction.setSenderServiceId(0L);
            transactionRepository.save(transaction);
            return;
        }
        String senderCondition = p2pRequestScopedContext.getSenderDto().getCondition();
        if (
                StringUtils.isEmpty(senderCondition) ||
                        senderCondition.length() != (p2pRequestScopedContext.getSenderDto().getAccount().length() + 5)
        ) {
            return;
        }

        String mfo = senderCondition.substring(0, 5);
        long mfoServiceId = 0L;

        try {
            mfoServiceId = Long.parseLong(mfo);
            transaction.setSenderServiceId(mfoServiceId);
            transactionRepository.save(transaction);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void saveRecipientMfo(Transaction transaction) {
        if (
                p2pRequestScopedContext.getRecipientService().getProcessorType() != P2pProcessorType.NCI
        ) {
            transaction.setRecipientServiceId(0L);
            transactionRepository.save(transaction);
            return;
        }

        String recipientCondition = p2pRequestScopedContext.getRecipientDto().getCondition();
        if (
                StringUtils.isEmpty(recipientCondition) ||
                        recipientCondition.length() != (p2pRequestScopedContext.getRecipientDto().getAccount().length() + 5)
        ) {
            return;
        }

        String mfo = recipientCondition.substring(0, 5);
        long mfoServiceId = 0L;

        try {
            mfoServiceId = Long.parseLong(mfo);
            transaction.setRecipientServiceId(mfoServiceId);
            transactionRepository.save(transaction);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void push(Transaction transaction) {
        List<String> tags = new ArrayList<>(20);

        tags.add(Constants.TAG_BM_P2P);
        tags.add(transaction.getSenderGateway());
        tags.add(transaction.getRecipientGateway());
        tags.add(transaction.getEposTerminal());
        tags.add(transaction.getEposMerchant());
        tags.add("SUCCESS=" + (transaction.getStatus() == TransactionStatus.SUCCESS));

        log.debug("Tags resolved for p2p pusher : {}", tags);

        HashMap<String, String> p2pData = new HashMap<>(10);

        if (transaction.getStatus() == TransactionStatus.SUCCESS || transaction.getStatus() == TransactionStatus.WAITING) {
            p2pData.put("title", "☑️P2P успешно ☑️");
        } else {
            p2pData.put("title", "❌️P2P не прошла ❌");
        }

        p2pData.put("dataType", "P2P");
        p2pData.put("chequeNumber", String.valueOf(transaction.getId()));
        p2pData.put("senderGateway", transaction.getSenderGateway());
        p2pData.put("senderAccount", CardUtil.maskIfBankCard(transaction.getSenderAccount()));
        p2pData.put("senderAmount", NumberUtil.format(transaction.getSenderAmount() / 100d));
        p2pData.put("recipientGateway", transaction.getRecipientGateway());
        p2pData.put("recipientAccount", CardUtil.maskIfBankCard(transaction.getRecipientAccount()));
        p2pData.put("recipientAmount", NumberUtil.format(transaction.getRecipientAmount() / 100d));
        p2pData.put(
                "date",
                PusherConstants.DATE_TIME_FORMATTER.format(LocalDateTime.ofInstant(transaction.getDate(), ZoneId.systemDefault()))
        );
        p2pData.put("status", String.valueOf(transaction.getStatus()));

        publisher.publishEvent(new PusherEvent(this, p2pData, tags));
    }

    public P2pResponse p2pCancel() {

        log.info("Request to p2p cancel");

        var login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));
        log.info("Username, who cancel: {}", login);

        p2pRequestScopedContext.setUsername(login);

        var agentTranId = p2pRequestScopedContext.getAgentTranId();
        log.debug("Agent tran id: {}", agentTranId);

        Optional<Transaction> transactionOptional;
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            log.debug("ADMIN wants to cancel");
            transactionOptional = transactionRepository.findTop1ByRequestTrId(agentTranId);
        } else {
            transactionOptional = transactionRepository.findTop1ByRequestLoginAndRequestTrId(login, agentTranId);
        }

        if (transactionOptional.isEmpty()) {
            log.warn("Transaction with \nagentTranId : {}, \nlogin: {} not found", agentTranId, login);
            throw new CodedException(ProblemEnum.OBJECT_NOT_FOUND, "Transaction");
        }

        var transaction = transactionOptional.get();
        log.debug("Transaction to cancel : {}", transaction);

        if ((transaction.getSenderSuccess() == null || transaction.getRecipientSuccess() == null)) {
            if (transaction.getDate() != null && transaction.getDate().plusSeconds(600).isBefore(Instant.now())) {
                publishCancelFailedEvent(transaction);
            }
            log.info("Transaction in process");
            return new P2pResponse()
                    .withStatusCode(new BmResult(BmResultStatus.IN_PROCESS, "Transaction in process"))
                    .withSuccess(false)
                    .withAgentTranId(agentTranId);
        }

        if (transaction.getStatus() == TransactionStatus.CANCELLED || transaction.getStatus() == TransactionStatus.REVERSED) {
            log.info("Transaction already cancelled");
            return new P2pResponse()
                    .withStatusCode(new BmResult(BmResultStatus.SUCCESS, "Transaction already cancelled"))
                    .withSuccess(true)
                    .withAgentTranId(agentTranId);
        }

        p2pRequestScopedContext.setTransaction(transaction);

        P2pProcessorType senderPc = P2pProcessorType.valueOf(transaction.getSenderGateway());
        P2pProcessorType recipientPc = P2pProcessorType.valueOf(transaction.getSenderGateway());

        return p2pPaymentResolver.resolve(senderPc, recipientPc).cancelP2p(transaction);
    }

    private void publishCancelFailedEvent(Transaction transaction) {
        List<String> tags = new ArrayList<>();
        tags.add(Constants.TAG_BM_P2P);
        tags.add(transaction.getSenderGateway());
        tags.add(transaction.getRecipientGateway());
        tags.add(transaction.getEposTerminal());
        tags.add(transaction.getEposMerchant());
        tags.add("SUCCESS=" + (transaction.getStatus() == TransactionStatus.SUCCESS));

        log.debug("Tags resolved for p2p pusher : {}", tags);

        HashMap<String, String> p2pData = new HashMap<>(10);
        p2pData.put("P2P-Cancel", "ne otmenyatsya v techenii 10 minut, nado ruchnuyu vmeshatelstvo");
        p2pData.put("id", String.valueOf(transaction.getId()));
        p2pData.put("senderSuccess", String.valueOf(transaction.getSenderSuccess()));
        p2pData.put("recipientSuccess", String.valueOf(transaction.getRecipientSuccess()));
        p2pData.put("status", String.valueOf(transaction.getStatus()));
        p2pData.put("senderAccount", transaction.getSenderAccount());
        p2pData.put("recipientAccount", transaction.getRecipientAccount());
        p2pData.put("amount", String.valueOf(transaction.getRequestAmount()));
        p2pData.put("agent", transaction.getRequestLogin());
        p2pData.put("agentTranId", transaction.getRequestTrId());

        publisher.publishEvent(new PusherEvent(this, p2pData, tags));
    }

    public SimpleBillResponse status(CommonStatusRequest statusDTO) {
        return transactionService.getStatusSimpleDto(statusDTO);
    }

    public SimpleBillResponse cancel(CommonCancelRequest cancelDTO) {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));
        return cancel(cancelDTO, login);
    }

    public SummaryReconcileResultDTO reconcile(ReconcileDTO reconcileDTO) {
        String username = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

        List<ReconcileResultDTO> reconcileResultDTOS = transactionRepository.getReconcileData(
                reconcileDTO.getFromTime(),
                reconcileDTO.getToTime(),
                username
        );

        return new SummaryReconcileResultDTO(
                transactionRepository.getTotalRecipientAmount(reconcileDTO.getFromTime(), reconcileDTO.getToTime(), username),
                transactionRepository.getTotalCountReconcile(reconcileDTO.getFromTime(), reconcileDTO.getToTime(), username),
                reconcileResultDTOS
        );
    }

    public SimpleBillResponse cancel(CommonCancelRequest cancelDTO, String login) {
        log.info("\n\n###### Cancel Request from {} ######: \n{}\n", login, cancelDTO);

        Transaction transaction = transactionRepository
                .findTop1ByIdAndStatus(cancelDTO.getBillId(), TransactionStatus.SUCCESS)
                .orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));

        log.debug("OK - Transaction found : {}", transaction);

        GatewayQueryResult gatewayQueryResult = gatewaySelectorService.getValidGatewayQueryResultV5(
                transaction.getRecipientServiceId(),
                login,
                transaction.getRequestGroup()
        );

        //        ResultData<Transaction> reverse = senderResolver.reverse(transaction.getPayType(), transaction, login);
        //
        //        if (!reverse.isSuccess()) {
        //            log.warn("BAD - reverse not success");
        //            throw reverse.getException();
        //        }
        //
        //        log.debug("OK - reversed");

        SharedCancelDTO sharedCancelDTO = new SharedCancelDTO()
                .billId(transaction.getRecipientBillId())
                .tranId(transaction.getId())
                .login(login)
                .recipientParams(gatewayQueryResult)
                .recipientTrId(transaction.getRecipientTrId())
                .gatewayAmount(transaction.getGatewayAmount());

        log.debug("Sending request to recipient : {}", sharedCancelDTO);

        SimpleBillResponse result = gatewayServiceV3.cancel(sharedCancelDTO);

        log.debug("Received response from recipient : {}", result);

        TransactionStatus status = result.getStatus() != TransactionStatus.NEW
                ? result.getStatus()
                : (Objects.equals(result.isSuccess(), Boolean.TRUE) ? TransactionStatus.CANCELLED : TransactionStatus.FAILED);

        transaction
                .recipientDate(result.getResponseTime())
                .recipientStatus(String.valueOf(result.isSuccess()))
                .recipientName(result.getRecipientShortName())
                .recipientStatusText(result.getResponseText())
                .recipientSuccess(result.isSuccess())
                .status(status)
                .recipientBillId(result.getId())
                .recipientTrId(result.getExtId())
                .date(Instant.now());

        transactionRepository.save(transaction);

        if (status == TransactionStatus.CANCELLED) {
            log.warn("Transaction cancelled, reversing to agent...");
            ResultData<Transaction> reverse = senderResolver.reverse(transaction.getPayType(), transaction, login);

            if (!reverse.isSuccess()) {
                log.warn("BAD - reverse not success");
            }

            log.debug("OK - reversed");
        }

        prepareResult(transaction, result);

        log.info("###### Cancel Response ######: \n{}\n", result);

        return result;
    }

    protected Transaction initP2pTransaction(
            String login,
            BaseP2pRequest p2pCheckRequest,
            @NotNull P2pAccount senderAccount,
            @NotNull P2pAccount recipientAccount
    ) {
        String senderName = String.valueOf(senderAccount.getType());
        String recipientName = String.valueOf(recipientAccount.getType());

        JsonNode params = senderAccount.getParams();

        String sAccount = String.valueOf(params);
        if (sAccount.length() > 64) {
            sAccount = String.valueOf(params.get("account"));
        }

        Transaction transaction = new Transaction()
                .date(Instant.now())
                .requestAmount(p2pCheckRequest.getAmount() != null ? p2pCheckRequest.getAmount() : 0L)
                .requestCcy(p2pCheckRequest.getCurrency())
                .requestTrId(p2pCheckRequest.getAgentTranId())
                .requestLogin(login)
                .requestGroup(login)
                .senderName(senderName)
                .senderAccount(sAccount)
                .senderCcy(senderAccount.getCurrency())
                .amountRatio(Constants.DEFAULT_AMOUNT_RATIO)
                .requestSubAccount(login)
                .payType(PayType.P2P)
                .comment(senderName + " -> " + recipientName)
                .roundAmount(0d)
                .status(TransactionStatus.NEW)
                .promo(p2pCheckRequest.getPromo() != null ? p2pCheckRequest.getPromo().getCode() : null);

        return transactionRepository.save(transaction);
    }

    protected P2pResponse handleP2pError(BmResult status) {
        String localizedMessage;

        if (status != null) {
            localizedMessage = status.getMessage();
        } else {
            localizedMessage = "Unknown exception : " + p2pRequestScopedContext.getRequest().getAgentTranId();
        }

        log.error("Error occurred while doing p2p : {}", localizedMessage);

        Transaction transaction = p2pRequestScopedContext.getTransaction();

        prepareFailedTransaction(transaction, localizedMessage, TransactionStatus.FAILED);

        transactionRepository.save(transaction);

        return fromRequest(p2pRequestScopedContext.getRequest(), status);
    }

    protected P2pResponse handleP2pError(CodedException codedException) {
        String localizedMessage;

        if (codedException != null) {
            localizedMessage = codedException.getLocalizedMessage();
        } else {
            localizedMessage = "Unknown exception : " + p2pRequestScopedContext.getRequest().getAgentTranId();
        }

        log.error("Error occurred while doing p2p : {}", localizedMessage);

        Transaction transaction = p2pRequestScopedContext.getTransaction();

        prepareFailedTransaction(transaction, localizedMessage, TransactionStatus.FAILED);

        transactionRepository.save(transaction);

        return fromRequest(p2pRequestScopedContext.getRequest(), localizedMessage);
    }

    private P2pResponse p2pError(CodedException codedException) {
        String localizedMessage;

        if (codedException != null) {
            localizedMessage = codedException.getLocalizedMessage();
        } else {
            localizedMessage = "Unknown exception : " + p2pRequestScopedContext.getRequest().getAgentTranId();
        }

        log.error("Error occurred while doing p2p : {}", localizedMessage);

        Transaction transaction = p2pRequestScopedContext.getTransaction();

        prepareFailedTransaction(transaction, localizedMessage, TransactionStatus.CANCEL_FAILED);

        transactionRepository.save(transaction);

        return new P2pResponse().withStatusMessage(localizedMessage).withSuccess(false);
    }

    private void prepareFailedTransaction(Transaction transaction, String errorMessage, TransactionStatus status) {
        transaction
                .recipientDate(Instant.now())
                //            .recipientName(result.getRecipientShortName())
                //            .recipientStatus(String.valueOf(result.isSuccess()))
                .nextRecipientStatusText(errorMessage)
                //            .recipientSuccess(result.isSuccess())
                //            .recipientSuccess(result.isSuccess())
                .status(status)// //            .recipientTrId(result.getExtId()) //            .recipientBillId(result.getId())
        ;
    }

    private P2pResponse fromRequest(P2pPayRequest p2PPayRequest, String exceptionLocalizedMessage) {
        return fromRequest(p2PPayRequest, false, exceptionLocalizedMessage);
    }

    private P2pResponse fromRequest(P2pPayRequest p2PPayRequest, BmResultStatus status) {
        return fromRequest(p2PPayRequest, new BmResult(status));
    }

    private P2pResponse fromRequest(P2pPayRequest p2PPayRequest, BmResult result) {
        if (result == null) {
            result = new BmResult(BmResultStatus.UNKNOWN_ERROR);
        }

        return new P2pResponse()
                .fromBase(p2PPayRequest)
                .withStatusCode(result)
                //            .withSuccess(result.isSuccess())
                .withSender(getSenderParticipantDto(p2PPayRequest))
                .withRecipient(getRecipientParticipantDto(p2PPayRequest)); //
    }

    private P2pResponse fromRequest(P2pPayRequest p2PPayRequest, boolean success, String responseText) {
        return new P2pResponse()
                .fromBase(p2PPayRequest)
                .withSuccess(success)
                .withStatusMessage(responseText)
                .withSender(getSenderParticipantDto(p2PPayRequest))
                .withRecipient(getRecipientParticipantDto(p2PPayRequest)); //
    }

    protected P2pParticipantDto getSenderParticipantDto(P2pPayRequest p2PPayRequest) {
        P2pParticipantDto participantDto = p2pRequestScopedContext.getSenderParticipantDto();

        if (participantDto == null) {
            participantDto =
                    new P2pParticipantDto(
                            String.valueOf(p2PPayRequest.getFrom().getParams()),
                            p2PPayRequest.getAmount(),
                            0L,
                            p2PPayRequest.getFrom().getCurrency(),
                            new HashMap<>()
                    );
        }

        return participantDto;
    }

    protected P2pParticipantDto getRecipientParticipantDto(P2pPayRequest p2PPayRequest) {
        P2pParticipantDto participantDto = p2pRequestScopedContext.getRecipientParticipantDto();

        if (participantDto == null) {
            participantDto =
                    new P2pParticipantDto(
                            String.valueOf(p2PPayRequest.getTo().getParams()),
                            p2PPayRequest.getAmount(),
                            0L,
                            p2PPayRequest.getTo().getCurrency(),
                            new HashMap<>()
                    );
        }

        return participantDto;
    }

    private P2pResponse p2pResponseFromTransaction(Transaction transaction, BmResult statusResult) {
        P2pParticipantDto sender = new P2pParticipantDto()
                .withAmount(transaction.getSenderAmount())
                .withCommission(transaction.getSystemCommissionUpAmount() + transaction.getSystemCommissionDownAmount())
                .withCurrency(transaction.getSenderCcy())
                .withName(transaction.getSenderName());

        P2pParticipantDto recipient = new P2pParticipantDto()
                .withAmount(transaction.getRecipientAmount())
                .withCommission(0L) //TODO
                .withCurrency(transaction.getRecipientCcy())
                .withName(transaction.getRecipientName());

        P2pResponse p2pResponse = new P2pResponse()
                .withSuccess(transaction.getStatus() == TransactionStatus.SUCCESS)
                .withStatusCode(statusResult)
                //            .withStatusMessage(transaction.getSenderStatusText() + ", " + transaction.getRecipientStatus())
                .withSender(sender)
                .withRecipient(recipient);

        p2pResponse.withAmount(transaction.getRequestAmount()).withAgentTranId(transaction.getRequestTrId());

        return p2pResponse;
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
        result.getData().put("ccy", String.valueOf(transaction.getRecipientCcy()));
        result.getData().put("recipientAmount", String.valueOf(transaction.getGatewayAmount()));
    }

    public BmResultData<Bill> continueP2pTransaction(Transaction transaction, SenderAdditionalParam senderAdditionalParam) {
        if (
                (StringUtils.isEmpty(transaction.getSenderName()) || transaction.getSenderName().length() < 1) &&
                        (StringUtils.isEmpty(transaction.getRecipientName()) || transaction.getRecipientName().length() < 1)
        ) {
            return new BmResultData<>(BmResultStatus.INVALID_BEAN_NAME);
        }

        String recipientServiceBean =
                transaction.getRecipientName().substring(0, 2).toLowerCase() + transaction.getRecipientName().substring(2);
        String senderServiceBean = transaction.getSenderName().substring(0, 2).toLowerCase() + transaction.getSenderName().substring(2);

        log.info("senderServiceBeanName: {}", senderServiceBean);
        log.info("recipientServiceBeanName: {}", recipientServiceBean);

        var recipientBean = applicationContext.getBean(recipientServiceBean, AbstractP2pRecipient.class);

        return recipientBean.continueCredit(transaction, senderServiceBean, recipientServiceBean, senderAdditionalParam);
    }

    private static final List<TransactionStatus> SUCCESS_STATUSES = Arrays.asList(
            TransactionStatus.SUCCESS,
            TransactionStatus.S_NEED_CONFIRM,
            TransactionStatus.R_NEED_CONFIRM
    );

    private final GatewaySelectorService gatewaySelectorService;
    protected final TransactionRepository transactionRepository;
    private final TransactionService transactionService;
    private final GatewayServiceV3 gatewayServiceV3;
    private final SenderResolverV3 senderResolver;
    protected final P2pRequestScopedContext p2pRequestScopedContext;
    protected final P2pRegistry p2PRegistry;
    protected final P2pParamResolver p2pParamResolver;
    protected final P2pCalculatorService p2pCalculatorService;
    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper;
    protected final ApplicationEventPublisher publisher;
    private final P2pPaymentResolver p2pPaymentResolver;
    private final ApplicationProperties properties;
}
