package uz.paykap.gateway.humo.helperService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.dto.GatewayResult;
import uz.devops.bmcore.dto.request.SharedPayDTO;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.devops.bmcore.gateway.PrepayHandler;
import uz.devops.humo.service.HumoGatewayService;
import uz.devops.humo.service.dto.DebitConfirmRequestDTO;
import uz.devops.humo.service.dto.DebitCreateRequestDTO;
import uz.devops.humo.service.dto.DebitResultDTO;
import uz.devops.humo.service.result.HumoResult;
import uz.javlon.commons.result.HasData;
import uz.paykap.adapter.humo.HumoDebitDataAdapterV3;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.gateway.humo.HumoDebitGatewayServiceV3;
import uz.paykap.gateway.humo.dto.HumoDebitErrorCode;
import uz.paykap.service.GenerateNumberService;
import uz.paykap.service.MapService;
import uz.paykap.utils.TransactionPrefixGenerator;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;


@Slf4j
@Service
@AllArgsConstructor
public class HumoDebitPayService {

    public static final String SUCCESS = "000";
    public static final String EN = "en";
    private static final int DEBIT_SUCCESS = 4;
    private static final int CONFIRM_SUCCESS = 10;
    private final HumoGatewayService humoGatewayService;
    private final ApplicationProperties applicationProperties;
    private final GenerateNumberService generateNumberService;
    private final TransactionPrefixGenerator transactionPrefixUtil;
    private final MapService mapService;

    public GatewayResult pay(SharedPayDTO sharedPayDTO, PrepayHandler prepayHandler, String realPan) {
        log.debug("begin pay with params, sharedPayDTO: {}  and prepayHandler: {}", sharedPayDTO, prepayHandler);

        prepayHandler.saveRequestParams(prepayHandler.getCheckRequestJson(), Instant.now());

        HasData<DebitResultDTO> debitResult = debit(String.valueOf(sharedPayDTO.getPayDTO().getBillId()), sharedPayDTO.getTranId(), prepayHandler.getCheckRequestJson(), prepayHandler.getCurrency(), sharedPayDTO.getPayDTO().getGatewayAmount().longValue(), realPan);

        if (!debitResult.isSuccess()) {
            String responseText = debitResult.getMessage();
            log.warn(responseText);

            if (debitResult.isTimeout()) {
                return errorResult(HumoDebitErrorCode.CREATE_DEBIT_ERROR.getCode(), responseText).withStatus(TransactionStatus.WAITING);
            }

            return errorResult(HumoDebitErrorCode.CREATE_DEBIT_ERROR.getCode(), responseText);
        }

        DebitResultDTO debitCreateResult = debitResult.getData();
        // если ответ неуспешен
        if (debitCreateResult.getAction() != DEBIT_SUCCESS || debitCreateResult.getPaymentId() == null) {
            log.warn("#### Humo error when creating debit, response : {}", debitCreateResult);
            String message = debitCreateResult.getAction() != null ? "actionCode:" + debitCreateResult.getAction() : HumoDebitErrorCode.CREATE_DEBIT_NOT_SUCCESS.getDesc();
            return errorResult(HumoDebitErrorCode.CREATE_DEBIT_NOT_SUCCESS.getCode(), message);
        }
        log.info("humo debit success!!!");

        HasData<DebitResultDTO> confirmResult = confirm(debitCreateResult, generateNumberService.humoSessionId("humoDebitPayService", "Create" + sharedPayDTO.getPayDTO().getBillId()));

        if (!debitResult.isSuccess()) {
            String responseText = confirmResult.getMessage();
            log.warn(responseText);

            if (confirmResult.isTimeout()) {
                return errorResult(HumoDebitErrorCode.CONFIRM_DEBIT_ERROR.getCode(), responseText).withStatus(TransactionStatus.WAITING);
            }

            return errorResult(HumoDebitErrorCode.CONFIRM_DEBIT_ERROR.getCode(), responseText);
        }

        DebitResultDTO debitConfirmResult = confirmResult.getData();
        // если ответ неуспешен
        if (!Objects.equals(debitConfirmResult.getAuthActionCode(), SUCCESS) || debitConfirmResult.getAction() != CONFIRM_SUCCESS) {
            log.warn("#### Humo error when confirming debit, response : {}", debitConfirmResult);
            String message = debitConfirmResult.getAuthActionCode() != null ? "AuthActionCode:" + debitConfirmResult.getAuthActionCode() : HumoDebitErrorCode.CONFIRM_DEBIT_NOT_SUCCESS.getDesc();
            return errorResult(HumoDebitErrorCode.CONFIRM_DEBIT_NOT_SUCCESS.getCode(), message);
        }

        GatewayResult result = new GatewayResult();
        result.setSuccess(true);
        result.setExtId(confirmResult.getData().getPaymentId());
        result.setResponseText("SUCCESS");
        result.setResponseCode(debitConfirmResult.getAuthActionCode());
        result.setResponseTime(Instant.now());
        result.setResponseJson(mapService.convert(debitConfirmResult));

        log.info("return pay result: {}", result);
        return result;
    }

    private HasData<DebitResultDTO> confirm(DebitResultDTO debitResultDTO, String sessionId) {

        DebitConfirmRequestDTO confirmRequestDTO = new DebitConfirmRequestDTO();
        confirmRequestDTO.setPaymentId(debitResultDTO.getPaymentId());
        confirmRequestDTO.setPaymentRef(debitResultDTO.getPaymentRef());
        confirmRequestDTO.setSessionId(sessionId);

        log.info("send request to humoGatewayService.confirmDebitV2: {}", confirmRequestDTO);
        HasData<DebitResultDTO> confirmResultDto = humoGatewayService.confirmDualMessageDebit(confirmRequestDTO);
        log.info("confirm return result: {}", confirmResultDto);

        return confirmResultDto;
    }

    private HasData<DebitResultDTO> debit(String billId, Long transactionId, Map<String, Object> checkRequestJson, Integer ccyCode, Long amount, String realPan) {

        DebitCreateRequestDTO requestDTO = new DebitCreateRequestDTO();
        requestDTO.setAmount(amount);
        requestDTO.setCcyCode(ccyCode);
        requestDTO.setPan(realPan);
        requestDTO.setExpDate(String.valueOf(checkRequestJson.get(HumoDebitDataAdapterV3.EXPIRY_FIELD_NAME)));
        requestDTO.setCentreID(String.valueOf(checkRequestJson.get("centreId")));
        requestDTO.setMerchantId(String.valueOf(checkRequestJson.get(HumoDebitGatewayServiceV3.MERCHANT_ID)));
        requestDTO.setTerminalId(String.valueOf(checkRequestJson.get(HumoDebitGatewayServiceV3.TERMINAL_ID)));
        requestDTO.setSessionId(generateNumberService.humoSessionId("humoDebitPayService", "Create" + billId));
        requestDTO.setPaymentRef(transactionPrefixUtil.getGatewayDebitPrefix(transactionId));
        requestDTO.setPointCode(applicationProperties.getHumoDebitGateway().getVirtualPointCode());
        requestDTO.setLanguage(EN);

//        callHumo(requestDTO, humoGatewayService::createDualMessageDebit, "humoGatewayService.createDebitV2");

        log.info("send request humoGatewayService.createDebitV2: {}", requestDTO);
        //todo HUMO-DEBIT-OPTIMIZE: HumoSenderService.innerDebit(), HumoSenderService.hold(), HumoDebitPayService.debit(), HumoP2pHelperService.hold() methodlari bir xil ish qilyapdi
        HasData<DebitResultDTO> debitResultDto = humoGatewayService.createDualMessageDebit(requestDTO);
        log.info("return result: {}", debitResultDto);

        return debitResultDto;
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
