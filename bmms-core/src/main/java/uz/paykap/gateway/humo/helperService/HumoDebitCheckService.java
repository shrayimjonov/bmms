package uz.paykap.gateway.humo.helperService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uz.devops.bmcore.dto.GatewayResult;
import uz.devops.bmcore.dto.request.CommonCheckRequest;
import uz.devops.bmcore.gateway.GatewayResultHandler;
import uz.devops.humoMiddleware.dto.basic.ItemInfo;
import uz.devops.issuing.dto.basicDTOs.OperationConnectionInfo;
import uz.devops.issuing.dto.request.getRealCard.GetRealCardRequest;
import uz.devops.issuing.dto.request.getRealCard.GetRealCardRequestBody;
import uz.devops.issuing.dto.request.getRealCard.GetRealCardRequestEnv;
import uz.devops.issuing.dto.request.getRealCard.GetRealCardRequestParams;
import uz.devops.issuing.dto.response.getRealCard.GetRealCardResponseEnv;
import uz.devops.issuing.service.IssuingService;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.javlon.commons.util.CardUtil;
import uz.paykap.adapter.humo.HumoDebitDataAdapterV3;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.config.Constants;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.gateway.humo.dto.HumoDebitErrorCode;
import uz.paykap.service.HumoMiddlewareHelperService;
import uz.paykap.service.MapService;
import uz.paykap.service.PcHelper;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class HumoDebitCheckService {

    private static final String BILL_MS_PREFIX = "BILL_MS_";

    private final HumoMiddlewareHelperService humoMiddlewareHelperService;
    private final ApplicationProperties applicationProperties;
    private final IssuingService humoIssuingPort;
    private final PcHelper pcHelper;
    private final MapService mapService;

    public GatewayResult check(
            CommonCheckRequest checkDTO,
            Long billId,
            GatewayResultHandler gatewayResultHandler,
            Map<String, Object> requestJson
    ) {
        String realPan = getRealPan(checkDTO.getAccount(), billId);

        var balanceResultData = humoMiddlewareHelperService.retrieveCardV3(realPan);

        if (!balanceResultData.isSuccess() || balanceResultData.getData() == null) {
            return errorResult(String.valueOf(balanceResultData.getCode()), balanceResultData.getMessage());
        }

        var balance = balanceResultData.getData();

        if (Objects.equals(applicationProperties.getHumo().getVerifyValidCard(), Boolean.TRUE)) {
            List<ItemInfo> itemInfoList = balance.getCard().getStatuses().getItem();
            Optional<ItemInfo> itemInfo = itemInfoList.stream().filter(itemV3 -> "CARD".equals(itemV3.getType())).findAny();
            if (itemInfo.isEmpty()) {
                log.warn("card status not found");
                return errorResult(HumoDebitErrorCode.CARD_STATUS_NOT_FOUND.getCode(), HumoDebitErrorCode.CARD_STATUS_NOT_FOUND.getDesc());
            }

            if (!Objects.equals(itemInfo.get().getActionCode(), "ACTIVE")) {
                String errorResponseText = "card not valid. Status code: " + itemInfo.get().getActionCode();

                log.warn(errorResponseText);
                return errorResult(HumoDebitErrorCode.CARD_NOT_ACTIVE.getCode(), HumoDebitErrorCode.CARD_NOT_ACTIVE.getDesc());
            }
        }

        if (Objects.equals(applicationProperties.getHumo().getVerifyEnoughBalance(), Boolean.TRUE)) {
            if (checkDTO.getGatewayAmount() != null) {
                Long availableAmount = balance.getBalance().getAvailableAmount();
                if (availableAmount < checkDTO.getGatewayAmount().longValue()) {
                    String message =
                            "Balance is less. Required: " + checkDTO.getGatewayAmount().longValue() + " , available: " + availableAmount;
                    log.info(message);
                    return errorResult(
                            HumoDebitErrorCode.INSUFFICIENT_FUNDS_ON_SENDER.getCode(),
                            HumoDebitErrorCode.INSUFFICIENT_FUNDS_ON_SENDER.getDesc()
                    );
                }
            }
        }

        if (balance.getCard() != null && StringUtils.hasText(balance.getCard().getExpiry())) {
            requestJson.put(HumoDebitDataAdapterV3.EXPIRY_FIELD_NAME, balance.getCard().getExpiry());
        }

        requestJson.put(Constants.CENTRE_ID_PARAM, balance.getCard().getInstitutionId());

        gatewayResultHandler.preSaveRequestParams(requestJson, Instant.now());

        log.debug("saved preSaveRequestParams: {}", requestJson);

        GatewayResult result = new GatewayResult();
        result.setSuccess(true);
        result.setResponseText("SUCCESS");
        result.setResponseCode("0");
        result.setResponseTime(Instant.now());
        result.setResponseJson(mapService.convert(balance));

        log.info("return success result: {}", result);

        return result;
    }

    public String getRealPan(String account, Long extId) {
        log.info("Get real pan from fake pan: {}", account);

        if (pcHelper.isValidPan(account, P2pProcessorType.HUMO)) {
            log.info("Account is realPan, return realPan...");
            return account;
        }

        OperationConnectionInfo connectionInfo = new OperationConnectionInfo();
        connectionInfo.setBankC(account.substring(4, 6));
        connectionInfo.setGroupc(applicationProperties.getHumoDebitGateway().getGroupC());
        connectionInfo.setExternalSessionId(BILL_MS_PREFIX + "_" + System.currentTimeMillis() + "_" + extId);

        log.info("OperationConnectionInfo: ");
        log.info("-- bankC: {}", connectionInfo.getBankC());
        log.info("-- groupC: {}", connectionInfo.getGroupc());
        log.info("-- extId: {}", connectionInfo.getExternalSessionId());

        GetRealCardRequestEnv cardRequest = new GetRealCardRequestEnv(
                new GetRealCardRequestBody(new GetRealCardRequest(connectionInfo, new GetRealCardRequestParams(account)))
        );

        log.info("RowType_GetRealCard_Request: ");
        log.info("-- pseudo pan: {}", cardRequest.getBody().getQueryCardRequest().getParameters().getCard());

        GetRealCardResponseEnv getRealCardResponseEnv = null;
        try {
            getRealCardResponseEnv = humoIssuingPort.getRealCard(cardRequest);

            if (
                    getRealCardResponseEnv != null &&
                            getRealCardResponseEnv.getBody() != null &&
                            getRealCardResponseEnv.getBody().getQueryCardResponse() != null &&
                            getRealCardResponseEnv.getBody().getQueryCardResponse().getResponseInfo() != null
            )
                log.info("response_code: {}", getRealCardResponseEnv.getBody().getQueryCardResponse().getResponseInfo().getResponseCode());
        } catch (Exception e) {
            log.warn("Exception when get real pan : {}", e.getMessage());
            errorResult(HumoDebitErrorCode.GET_REAL_PAM_EXCEPTION.getCode(), HumoDebitErrorCode.GET_REAL_PAM_EXCEPTION.getDesc());
            throw new CodedException(ProblemEnum.INTERNAL_ERROR, "error when get real pan");
        }

        if (
                getRealCardResponseEnv == null ||
                        getRealCardResponseEnv.getBody() == null ||
                        getRealCardResponseEnv.getBody().getQueryCardResponse() == null ||
                        getRealCardResponseEnv.getBody().getQueryCardResponse().getRealCardDetails() == null
        ) {
            log.warn("Error result");
            throw new CodedException(ProblemEnum.NOT_FOUND, "error when get real pan");
        }

        if (StringUtils.isEmpty(getRealCardResponseEnv.getBody().getQueryCardResponse().getRealCardDetails().getRealCardNumber())) {
            log.warn("Get real pan is null");
            throw new CodedException(ProblemEnum.NOT_FOUND, "real pan not found");
        }

        String realPan = getRealCardResponseEnv.getBody().getQueryCardResponse().getRealCardDetails().getRealCardNumber();
        log.info("real pan is: {}", CardUtil.maskIfBankCard(realPan));
        return realPan;
    }

    //    private Map<String, String> parseToMap(BalanceResultDTO resultDTO) {
    //        Map<String, String> resultMap = new HashMap<>();
    //
    //        AccountV3 account = resultDTO.getAccount();
    //        BalanceV3 balance = resultDTO.getBalance();
    //        CardV3 card = resultDTO.getCard();
    //
    //        resultMap.putAll(ClassToMapUtil.convertObjToMapString(account.getClass(), "Account", account));
    //        resultMap.putAll(ClassToMapUtil.convertObjToMapString(card.getClass(), "Card", card));
    //        resultMap.putAll(ClassToMapUtil.convertObjToMapString(balance.getClass(), "Balance", balance));
    //
    //        log.info("return responseMap: {}", resultMap);
    //        return resultMap;
    //    }

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
