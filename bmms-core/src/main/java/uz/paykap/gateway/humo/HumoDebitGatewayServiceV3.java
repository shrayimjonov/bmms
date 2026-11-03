package uz.paykap.gateway.humo;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.CommonBillService;
import uz.devops.bmcore.dto.GatewayResult;
import uz.devops.bmcore.dto.request.CommonStatusRequest;
import uz.devops.bmcore.dto.request.SharedCancelDTO;
import uz.devops.bmcore.dto.request.SharedCheckDTO;
import uz.devops.bmcore.dto.request.SharedPayDTO;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.gateway.CancelHandler;
import uz.devops.bmcore.gateway.GatewayBillingService;
import uz.devops.bmcore.gateway.GatewayResultHandler;
import uz.devops.bmcore.gateway.PrepayHandler;
import uz.devops.humo.service.dto.ReverseResultDTO;
import uz.devops.humo.service.dto.pojo.debit.NameValueItem;
import uz.devops.humo.service.dto.v3.getpayment.GetPaymentResult;
import uz.devops.humo.service.dto.v3.getpayment.GetPaymentStatusCodes;
import uz.javlon.commons.base.Result;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.javlon.commons.result.CommonResult;
import uz.javlon.commons.result.HasData;
import uz.paykap.adapter.humo.HumoDebitDataAdapterV3;
import uz.paykap.domain.AgentRecipient;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Recipient;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.gateway.BaseGatewayBillingService;
import uz.paykap.gateway.humo.dto.HumoDebitErrorCode;
import uz.paykap.gateway.humo.helperService.*;
import uz.paykap.repository.AgentRecipientRepository;
import uz.paykap.repository.BillRepository;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.security.SecurityUtils;
import uz.paykap.service.PcHelper;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service(HumoDebitGatewayServiceV3.NAME)
@AllArgsConstructor
public class HumoDebitGatewayServiceV3 extends BaseGatewayBillingService implements GatewayBillingService {

    public static final String NAME = "humoDebitGatewayService";
    private static final String ACTION_CODE = "auth_action_code";
    public static final String TERMINAL_ID = "terminalId";
    public static final String MERCHANT_ID = "merchantId";

    private final HumoDebitCheckService humoDebitCheckService;
    private final HumoDebitPayService humoDebitPayService;
    private final HumoDebitStatusService humoDebitStatusService;
    private final HumoReversService humoReversService;
    private final HumoDebitReconcileServiceV2 humoDebitReconcileService;
    private final BillRepository billRepository;
    private final CommonBillService commonBillService;
    private final RecipientRepository recipientRepository;
    private final AgentRecipientRepository agentRecipientRepository;
    private final PcHelper pcHelper;

    @Override
    public GatewayResult check(@NotNull @NotNull SharedCheckDTO sharedCheckDTO, @NotNull @NotNull GatewayResultHandler gatewayResultHandler) {

        log.info("Uzcard debit check : {}", sharedCheckDTO);
        if (sharedCheckDTO.getCheckDTO() == null || StringUtils.isEmpty(sharedCheckDTO.getCheckDTO().getAccount()) || sharedCheckDTO.getCheckDTO().getAmount() == null || sharedCheckDTO.getCheckDTO().getAmount() < 0) {
            return errorResult(HumoDebitErrorCode.INVALID_PARAMS.getCode(), HumoDebitErrorCode.INVALID_PARAMS.getDesc() + " account or amount not found");
        }

        /*if (MapUtil.keyNotFoundOrEmpty(sharedCheckDTO.getCheckDTO().getParams(), HumoDebitDataAdapterV3.EXPIRY_FIELD_NAME)) {
            return errorResult(HumoDebitErrorCode.INVALID_PARAMS.getCode(), HumoDebitErrorCode.INVALID_PARAMS.getDesc() + " expiry not found in params");
        }*/

        if (!pcHelper.isValidToken(sharedCheckDTO.getCheckDTO().getAccount(), P2pProcessorType.HUMO) &&
                !pcHelper.isValidPan(sharedCheckDTO.getCheckDTO().getAccount(), P2pProcessorType.HUMO)) {
            return errorResult(HumoDebitErrorCode.INVALID_PARAMS.getCode(), HumoDebitErrorCode.INVALID_PARAMS.getDesc() + " pan is not matches with pattern");
        }

        Map<String, Object> requestJson = new HashMap<>();
//        requestJson.put(HumoDebitDataAdapterV3.EXPIRY_FIELD_NAME, sharedCheckDTO.getCheckDTO().getParams().get(HumoDebitDataAdapterV3.EXPIRY_FIELD_NAME));
        requestJson.put(MERCHANT_ID, String.valueOf(sharedCheckDTO.getRecipientParams().getEposMerchant()));
        requestJson.put(TERMINAL_ID, String.valueOf(sharedCheckDTO.getRecipientParams().getEposTerminal()));

        return humoDebitCheckService.check(sharedCheckDTO.getCheckDTO(), gatewayResultHandler.getBillId(), gatewayResultHandler, requestJson);
    }

    @Override
    public GatewayResult pay(@NotNull @NotNull SharedPayDTO sharedPayDTO, @NotNull @NotNull PrepayHandler prepayHandler) {
        log.info("Humo debit pay : {}", sharedPayDTO);

        String realPan = humoDebitCheckService.getRealPan(prepayHandler.getAccount(), sharedPayDTO.getPayDTO().getBillId());

        return humoDebitPayService.pay(sharedPayDTO, prepayHandler, realPan);
    }

    @Override
    public GatewayResult cancel(@NotNull @NotNull SharedCancelDTO sharedCancelDTO, @NotNull @NotNull CancelHandler cancelHandler) {
        log.info("do cancel for sharedCancelDTO: {}", sharedCancelDTO);


        HasData<ReverseResultDTO> resultData;
        try {
            resultData = humoReversService.cancel(sharedCancelDTO, cancelHandler);
        } catch (Exception e) {
            e.printStackTrace();
            String message = !StringUtils.isEmpty(e.getLocalizedMessage()) ? e.getLocalizedMessage() : (!StringUtils.isEmpty(e.getMessage()) ? e.getMessage() : HumoDebitErrorCode.REVERSE_ERROR.getDesc());
            return errorResult(HumoDebitErrorCode.REVERSE_ERROR.getCode(), message);
        }

        if (resultData == null) {
            log.warn("Reverse request is unknown");
            return errorResult(HumoDebitErrorCode.REVERSE_NO_REPLY.getCode(), HumoDebitErrorCode.REVERSE_NO_REPLY.getDesc());
        }

        if (!resultData.isSuccess()) {
            String message = resultData.getMessage();
            log.warn("Reverse request is not success: {}", message);
            return errorResult(HumoDebitErrorCode.REVERSE_NOT_SUCCESS.getCode(), message);
        }

        ReverseResultDTO reverseResultDTO = resultData.getData();
        Map<String, Object> responseJson = new HashMap<>();
        responseJson.put("pan", cancelHandler.getAccount());
        responseJson.put("success", String.valueOf(reverseResultDTO.isSuccess()));

        GatewayResult result = new GatewayResult();
        result.setSuccess(true);
        result.setResponseText(reverseResultDTO.isSuccess() ? "SUCCESS" : HumoDebitErrorCode.REVERSE_NOT_SUCCESS.getDesc());
        result.setResponseCode(reverseResultDTO.isSuccess() ? "0" : HumoDebitErrorCode.REVERSE_NOT_SUCCESS.getCode());
        result.setResponseTime(Instant.now());
        result.setResponseJson(responseJson);

        log.info("return reverse result: {}", result);
        return result;
    }

    @Override
    public Long getDefaultCheckAmountIfNull() {
        return null;
    }

    /**
     * status is not implemented
     * if need, verify and do implement
     *
     * @param dto
     * @return
     */
    public SimpleBillResponse status(CommonStatusRequest dto) {

        log.info("Status humo : {}", dto);
        Bill bill = billRepository.findTop1ByIdAndLoginAndType(
                dto.getBillId(),
                SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND)),
                RequestType.PAY).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));

        HasData<GetPaymentResult> statusResult = humoDebitStatusService.checkStatus(bill.getExtId(), String.valueOf(bill.getId()));

        if (!statusResult.isSuccess()) {
            bill
                    .responseCode(String.valueOf(statusResult.getCode()))
                    .responseText(statusResult.getMessage())
                    .responseTime(Instant.now())
                    .success(false)
            ;
            return commonBillService.toSimple(bill);
        }

        List<NameValueItem> itemV3s = statusResult.getData().getDetail().getItems();
        final Optional<NameValueItem> optionalAuthActionCode = itemV3s.stream().filter(item -> item.getName().equals(ACTION_CODE)).findFirst();

        if (optionalAuthActionCode.isEmpty()) {
            return commonBillService.toSimple(bill);
        }

        if (GetPaymentStatusCodes.getByActionCode(optionalAuthActionCode.get().getValue()).equals(GetPaymentStatusCodes.UNKNOWN)) {
            return commonBillService.toSimple(bill);
        }

        if (!GetPaymentStatusCodes.getByActionCode(optionalAuthActionCode.get().getValue()).getSuccess()) {
            bill
                    .responseText(GetPaymentStatusCodes.getByActionCode(optionalAuthActionCode.get().getValue()).getMsgFull())
                    .responseCode(optionalAuthActionCode.get().getValue())
                    .success(false);

            return commonBillService.saveToSimple(bill);
        }

        if (!bill.isSuccess()) {
            bill
                    .responseText(GetPaymentStatusCodes.getByActionCode(optionalAuthActionCode.get().getValue()).getMsgFull())
                    .responseCode(optionalAuthActionCode.get().getValue())
                    .success(true);

            return commonBillService.saveToSimple(bill);
        }

        return commonBillService.toSimple(bill);
    }

    /**
     * reconcile is not implemented
     * if need, verify and do implement
     * !!! if no run reconcile, humo transactions
     * !!! automatic will be canceled in PC (processing center)
     *
     * @param from
     * @param to
     * @return
     */
    public Result reconcile(Instant from, Instant to) {
        log.debug("Reconcile implemented, do humoDebitReconcileService.reconcile()...");
        log.debug("find recipient by shortName: {}", HumoDebitDataAdapterV3.HUMO_DEBIT_RECIPIENT_SHORT_NAME);
        Recipient recipient = recipientRepository.findTop1ByShortName(HumoDebitDataAdapterV3.HUMO_DEBIT_RECIPIENT_SHORT_NAME).orElseThrow(() -> new CodedException(ProblemEnum.NOT_FOUND, "recipient not found"));
        List<AgentRecipient> agentRecipients = agentRecipientRepository.findAllByRecipient_IdAndActive(recipient.getId(), true);


        Set<String> uniqueTerminalList = new TreeSet<>();
        agentRecipients.forEach(agentRecipient -> {
            uniqueTerminalList.add(agentRecipient.getEposTerminal());
        });


        AtomicReference<Boolean> allHumoDebitReconcileSuccess = new AtomicReference<>(true);
        uniqueTerminalList.forEach(terminal -> {
            final CommonResult commonResult = humoDebitReconcileService.reconcile(terminal);
            if (commonResult == null || !commonResult.isSuccess()) {
                allHumoDebitReconcileSuccess.set(false);
            }
        });

        return allHumoDebitReconcileSuccess.get() ? Result.SUCCESS : Result.FAILED;
    }

    private GatewayResult errorResult(String responseCode, String responseText) {
        GatewayResult gatewayResult = new GatewayResult();

        gatewayResult.setSuccess(false);
        gatewayResult.setResponseCode(responseCode);
        gatewayResult.setResponseText(responseText);
        gatewayResult.setResponseTime(Instant.now());

        log.error("return errorResult: {}", gatewayResult);
        return gatewayResult;
    }
}

