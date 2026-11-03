package uz.paykap.agent.tran;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.dto.request.CheckRequest;
import uz.devops.bmcore.dto.request.CommonPayRequest;
import uz.devops.bmcore.dto.request.PayRequest;
import uz.devops.bmcore.dto.request.SharedCancelDTO;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.javlon.commons.base.SimpleResult;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.agent.tran.check.GatewayCheckServiceResolver;
import uz.paykap.agent.tran.hold.CommonHoldConfirmHelper;
import uz.paykap.agent.tran.hold.CommonHoldReverseHoldHelper;
import uz.paykap.agent.tran.hold.CommonHoldServiceHoldHelper;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.domain.Bill;
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
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.hold.PartialReverseRequest;
import uz.paykap.service.hold.PartialReverseResponse;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class CommonHoldTranService extends AbstractCommonTranService {

    public CommonHoldTranService(GatewaySelectorService gatewaySelectorService, GatewayCheckServiceResolver gatewayCheckService, RecipientRepository recipientRepository, TransactionRepository transactionRepository, SenderResolverV3 senderResolver, ForeignPayService foreignPayService, ApplicationProperties properties, PcHelper pcHelper, GatewayGetInfoService getInfoHelper, GatewayServiceV3 gatewayServiceV3, TransactionService transactionService, ApplicationEventPublisher eventPublisher, CommonHoldServiceHoldHelper holdHelper, CommonHoldConfirmHelper confirmHelper, CommonHoldReverseHoldHelper reverseHoldHelper, BillRepository billRepository, GatewayBillingServiceResolver gatewayBillingServiceResolver) {
        super(gatewaySelectorService, gatewayCheckService, recipientRepository, transactionRepository, senderResolver, foreignPayService, properties, pcHelper, getInfoHelper, gatewayServiceV3, transactionService, eventPublisher, billRepository, gatewayBillingServiceResolver);
        this.holdHelper = holdHelper;
        this.confirmHelper = confirmHelper;
        this.reverseHoldHelper = reverseHoldHelper;
    }

    private final CommonHoldServiceHoldHelper holdHelper;
    private final CommonHoldConfirmHelper confirmHelper;
    private final CommonHoldReverseHoldHelper reverseHoldHelper;

    public SimpleBillResponse doHold(CommonPayRequest request) {
        return holdHelper.holdForAgent(request);
    }

    public SimpleBillResponse reHold(CommonPayRequest request) {
        return holdHelper.reHold(request);
    }

    public SimpleBillResponse holdWithOtp(PayRequest request) {

        Optional<String> login = SecurityUtils.getCurrentUserLogin();
        if (login.isEmpty()) {
            return holdErrorResult(request.getBillId(), getErrorMessage(new SimpleResult(false, null), "username not found"));
        }

        SimpleResult simpleResult = verifyOtp(request.getBillId(), request.getParams(), login.get());
        if (!simpleResult.isSuccess()) {
            return holdErrorResult(request.getBillId(), getErrorMessage(simpleResult, "verify otp error"));
        }

        Optional<Bill> checkBill = billRepository.findTop1ByTransactionIdAndTypeAndLoginAndSuccessTrue(request.getBillId(), RequestType.CHECK, login.get());
        if (checkBill.isEmpty()) {
            return holdErrorResult(request.getBillId(), getErrorMessage(new SimpleResult(false, null), "check bill not found"));
        }

        SimpleResult verifyParamResult = verifyCheckRequestJson(checkBill.get().getRequestJson());
        if (!verifyParamResult.isSuccess()) {
            return holdErrorResult(request.getBillId(), getErrorMessage(verifyParamResult, "card pan or expiry not found"));
        }

        fillSenderParams(request, checkBill.get().getRequestJson());
        return doHold(request);
    }

    private String getErrorMessage(SimpleResult simpleResult, String defaultMessage) {
        return simpleResult.getException() != null && !StringUtils.isEmpty(simpleResult.getException().getMessage()) ? simpleResult.getException().getMessage() : defaultMessage;
    }

    public SimpleBillResponse confirmHold(CommonPayRequest request) {
        return confirmHelper.holdConfirm(request);
    }

    public SimpleResult reverseHold(SharedCancelDTO cancelDTO) {
        return reverseHoldHelper.reverseHold(cancelDTO);
    }

    public SimpleBillResponse partialReverse(PartialReverseRequest request) {
        log.info("do partialReverseHold()");
        PartialReverseResponse partialReverseResponse = reverseHoldHelper.partialReverseHold(request);
        if (!Objects.equals(partialReverseResponse.getSuccess(), Boolean.TRUE) && partialReverseResponse.getErrorResponse() != null) {
            log.error("old hold transaction cancel failed");
            return partialReverseResponse.getErrorResponse();
        }

        CheckRequest checkRequest = createCheckRequest(partialReverseResponse);
        log.info("create new check: {}", checkRequest);
        SimpleBillResponse checkResponse = check(checkRequest);
        log.info("check result: {}", checkResponse);
        if (!checkResponse.isSuccess() || !Objects.equals(checkResponse.getStatus(), TransactionStatus.CHECK_OK)) {
            log.error("check result is not success");
            return errorResult(BmResultStatus.PARTIAL_REVERSE_ERROR);
        }

        CommonPayRequest payRequest = getPayRequest(checkResponse.getId(), partialReverseResponse.getParams());
        log.info("create new hold: {}", payRequest);
        SimpleBillResponse holdResult = holdHelper.holdForAgent(payRequest);
        log.info("hold result: {}", holdResult);
        return holdResult;
    }

    private SimpleBillResponse errorResult(BmResultStatus partialReverseError) {
        return null;
    }
    private SimpleBillResponse holdErrorResult(Long id, String responseText) {
        SimpleBillResponse simpleBillResponse = new SimpleBillResponse()
                .id(id)
                .success(false)
                .status(TransactionStatus.HOLD_ERROR)
                .responseTime(Instant.now())
                .responseText(responseText);

        log.warn("return hold error result: {}", simpleBillResponse);
        return simpleBillResponse;
    }

    private CheckRequest createCheckRequest(PartialReverseResponse reverseResponse) {
        CheckRequest checkDTO = new CheckRequest();
        checkDTO.setAmount(reverseResponse.getAmount());
        checkDTO.setAccount(reverseResponse.getRecipientAccount());
        checkDTO.setRecipientId(reverseResponse.getRecipientId());
        checkDTO.setAgentTranId(reverseResponse.getUsername() + "_" + System.currentTimeMillis());
        return checkDTO;
    }
    private CommonPayRequest getPayRequest(Long id, Map<String, Object> params) {
        PayRequest payRequest = new PayRequest();
        payRequest.setBillId(id);
        payRequest.setType(PayType.PC);
        payRequest.setParams(params);

        return payRequest;
    }
}
