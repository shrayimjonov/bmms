package uz.paykap.gateway.humo;

import java.text.DecimalFormat;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.dto.GatewayResult;
import uz.devops.bmcore.dto.request.SharedCancelDTO;
import uz.devops.bmcore.dto.request.SharedCheckDTO;
import uz.devops.bmcore.dto.request.SharedPayDTO;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.devops.bmcore.gateway.CancelHandler;
import uz.devops.bmcore.gateway.GatewayBillingService;
import uz.devops.bmcore.gateway.GatewayResultHandler;
import uz.devops.bmcore.gateway.PrepayHandler;
import uz.devops.humo.service.HumoGatewayService;
import uz.devops.humo.service.dto.CreditRequestDTO;
import uz.devops.humo.service.dto.CreditResultDTO;
import uz.devops.humo.service.dto.ReverseResultDTO;
import uz.devops.humo.service.result.HumoResult;
import uz.javlon.commons.result.HasData;
import uz.javlon.commons.util.CardUtil;
import uz.javlon.svgate.domain.Bank;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.config.Constants;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.gateway.BankUtilService;
import uz.paykap.gateway.BaseGatewayBillingService;
import uz.paykap.gateway.humo.dto.HumoDebitErrorCode;
import uz.paykap.gateway.humo.helperService.HumoReversService;
import uz.paykap.sender.impl.HumoSenderService;
import uz.paykap.service.GenerateNumberService;
import uz.paykap.service.HumoMiddlewareHelperService;
import uz.paykap.service.PcHelper;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.humo.HumoP2pHelperService;
import uz.paykap.utils.TransactionPrefixGenerator;

@Slf4j
@Service(HumoCreditGatewayServiceV3.NAME)
public class HumoCreditGatewayServiceV3 extends BaseGatewayBillingService implements GatewayBillingService {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

    public static final String NAME = "humoCreditV3";
    public static final String FIO = "fio";
    public static final String COMMISSION = "commission";
    public static final String EXISTS = "exists";
    public static final String MERCHANT_NAME = "merchant_name";
    public static final String REFNUM = "refnum";
    public static final String DATE = "date";
    public static final String STATUS = "status";

    private final HumoReversService humoReversService;
    private final HumoMiddlewareHelperService humoMiddlewareHelperService;
    private final Pattern realPanPattern;
    private final Pattern pseudoPanPattern;
    private final PcHelper pcHelper;

    @Override
    public GatewayResult check(@NotNull SharedCheckDTO sharedCheckDTO, @NotNull GatewayResultHandler gatewayResultHandler) {
        log.info("Humo check : {}", sharedCheckDTO);

        String account = sharedCheckDTO.getCheckDTO().getAccount();
        Long amount = sharedCheckDTO.getCheckDTO().getAmount();

        if (isNullOrEmpty(account) || amount == null || amount < 1) {
            return getErrorGatewayResult("Invalid account or amount");
        }

        //        boolean isValidRealPan = isRealPan(account);
        //        boolean isValidPseudoPan = isPseudoPan(account);

        String realPan;

        if (isRealPan(account)) {
            realPan = account;

            // Check if bank supported
            bankUtilService.validateAccountAndGetBank(realPan);
        } else if (isPseudoPan(account)) {
            // Check if bank supported
            bankUtilService.validateAccountAndGetBank(account);

            try {
                //                realPan = humoHelperService.getRealPan(account).getData();

                final BmResultData<String> realPanResult = humoHelperService.getRealPan(account);

                if (realPanResult == null) {
                    return getErrorGatewayResult("Humo getRealPan failed, bmResult is null");
                }

                if (
                    !realPanResult.isSuccess() ||
                    Objects.isNull(realPanResult.getData()) ||
                    !pcHelper.isValidPan(realPanResult.getData(), P2pProcessorType.HUMO)
                ) {
                    log.warn("Error when getting realPan from Humo : " + realPanResult.getMessage());
                    return getErrorGatewayResult("Error when getting realPan from Humo : " + realPanResult.getMessage());
                }

                realPan = realPanResult.getData();
                log.info("Real pan : {}", CardUtil.mask(realPan));
            } catch (Exception e) {
                return getErrorGatewayResult("Exception when getting real pan : " + e.getLocalizedMessage());
            }
        } else {
            return getErrorGatewayResult("not valid Humo pan");
        }

        log.debug("Bank is supported for : {}", CardUtil.mask(realPan));

        try {
            var simpleResultData = humoMiddlewareHelperService.retrieveCardV3(realPan).getData();

            Map<String, Object> params = sharedCheckDTO.getCheckDTO().getParams();
            params.put(Constants.PAN_PARAM, realPan);
            params.put(Constants.CENTRE_ID_PARAM, simpleResultData.getCard().getInstitutionId());
            params.put(Constants.EXPIRY_PARAM, simpleResultData.getCard().getExpiry());

            gatewayResultHandler.preSaveRequestParams(params, Instant.now());

            String maskedPan = CardUtil.mask(realPan);

            HashMap<String, Object> data = new HashMap<>();
            data.put(Constants.PAN_PARAM, maskedPan);
            data.put(FIO, simpleResultData.getCard().getNameOnCard());
            data.put(Constants.MASKED_PAN_PARAM, maskedPan);
            // TODO - fix in production
            data.put(
                COMMISSION,
                DECIMAL_FORMAT.format(
                    sharedCheckDTO.getGatewayAmount() * sharedCheckDTO.getRecipientParams().getSystemCommissionUp() / 100d
                )
            );
            data.put(EXISTS, String.valueOf(simpleResultData.getCard().getNameOnCard() != null));

            return new GatewayResult()
                .responseJson(data)
                .responseTime(Instant.now())
                .responseText(Constants.OK)
                .responseCode(Constants.OK)
                .extId(simpleResultData.getCard().getInstitutionId())
                .success(true);
        } catch (Exception e) {
            return getErrorGatewayResult(e.getLocalizedMessage());
        }
    }

    @Override
    public GatewayResult pay(@NotNull SharedPayDTO sharedPayDTO, @NotNull PrepayHandler prepayHandler) {
        //TODO - Javlon, fix gateway amount

        log.info("Pay humo : {}", sharedPayDTO);

        GatewayResult gatewayResult = new GatewayResult();

        //        String pan;
        //        try {
        //            pan = humoSenderService.getRealPan(prepayHandler.getAccount(), METHOD_PREFIX_PAY + String.valueOf(sharedPayDTO.getTranId()));
        //            log.info("Real pan: {}", pan);
        //        } catch (Exception e) {
        //            log.warn("Exception when getting real pan: {}", e.getLocalizedMessage());
        //
        //            gatewayResult
        //                .responseJson(MapUtil.errorMap(e))
        //                .responseTime(Instant.now())
        //                .responseText(StringUtils.substring(e.getLocalizedMessage(), 0, 126))
        //                .responseCode("FAILED")
        //                .success(false)
        //            ;
        //
        //            return gatewayResult;
        //        }

        try {
            Bank bank = bankUtilService.validateAccountAndGetBank(prepayHandler.getAccount());

            log.debug("Bank found : {}", bank);

            // Humo-credit recipient tiyinda bo'lgani uchun va
            // PayDto.gatewayAmount tiyinda double bo'lib kelgani uchun
            // gatewayAmount.longValue() olinmoqda
            if (sharedPayDTO.getGatewayAmount() == null) {
                String errorText = "Humo credit gateway amount is null";

                return getErrorGatewayResult(errorText);
            }

            Long amount = sharedPayDTO.getGatewayAmount().longValue();
            log.debug("Amount : {}", amount);

            CreditRequestDTO creditRequestDTO = new CreditRequestDTO();
            creditRequestDTO.setPaymentRef(transactionPrefixUtil.getGatewayCreditPrefix(sharedPayDTO.getTranId()));
            creditRequestDTO.setSessionId(generateNumberService.humoSessionId(NAME, String.valueOf(sharedPayDTO.getTranId())));
            creditRequestDTO.setCentreId(String.valueOf(prepayHandler.getCheckRequestJson().get(Constants.CENTRE_ID_PARAM)));
            creditRequestDTO.setPointCode(applicationProperties.getHumo().getVirtualPointCode());
            creditRequestDTO.setTerminalId(bank.getTerminalId());
            creditRequestDTO.setMerchantId(bank.getMerchantId());
            creditRequestDTO.setAmount(amount);
            creditRequestDTO.setExpiry(String.valueOf(prepayHandler.getCheckRequestJson().get(Constants.EXPIRY_PARAM)));
            creditRequestDTO.setPan(String.valueOf(prepayHandler.getCheckRequestJson().get(Constants.PAN_PARAM)));

            Map<String, Object> billRequestJson = prepayHandler.getCheckRequestJson();
            if (billRequestJson == null) {
                billRequestJson = new HashMap<>();
            }
            billRequestJson.put(Constants.CENTRE_ID_PARAM, creditRequestDTO.getCentreId());
            prepayHandler.saveRequestParams(billRequestJson, Instant.now());

            log.debug("Bank cardBin: {}", StringUtils.substring(prepayHandler.getAccount(), 0, 6));

            HasData<CreditResultDTO> creditSimpleResult = humoGatewayService.credit(creditRequestDTO);

            log.info("Credit simple result: {}", creditSimpleResult);

            if (!creditSimpleResult.isSuccess()) {
                log.warn("Error when humo credit card: {}", creditSimpleResult.getMessage());
                if (creditSimpleResult.isTimeout()) {
                    return getErrorGatewayResult(gatewayResult, creditSimpleResult.getMessage()).withStatus(TransactionStatus.WAITING);
                }
                return getErrorGatewayResult(gatewayResult, creditSimpleResult.getMessage());
            }

            if (creditSimpleResult.getData() == null) {
                return getErrorGatewayResult(gatewayResult, "Credit result invalid");
            }

            String masked = CardUtil.mask(prepayHandler.getAccount());

            HashMap<String, Object> data = new HashMap<>();
            data.put(Constants.PAN_PARAM, masked);
            data.put(FIO, String.valueOf(prepayHandler.getCheckResponseJson().get(FIO)));

            data.put(REFNUM, creditSimpleResult.getData().getPaymentId());
            data.put(STATUS, String.valueOf(creditSimpleResult.getData().getAction()));
            data.put(MERCHANT_NAME, creditSimpleResult.getData().getMerchantName());

            gatewayResult
                .responseJson(data)
                .responseTime(Instant.now())
                .responseText(creditSimpleResult.getData().getAction() == 4 ? "Success" : "Failed")
                .responseCode(String.valueOf(creditSimpleResult.getData().getAction()))
                .extId(creditSimpleResult.getData().getPaymentId())
                .success(creditSimpleResult.getData().getAction() == 4);
        } catch (Exception e) {
            return getErrorGatewayResult(gatewayResult, e.getLocalizedMessage());
        }

        return gatewayResult;
    }

    @Override
    public Long getDefaultCheckAmountIfNull() {
        return 0L;
    }

    public GatewayResult cancel(@NotNull SharedCancelDTO sharedCancelDTO, @NotNull CancelHandler cancelHandler) {
        log.info("do cancel for sharedCancelDTO: {}", sharedCancelDTO);

        HasData<ReverseResultDTO> resultData = humoReversService.cancel(sharedCancelDTO, cancelHandler);

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

    private boolean isRealPan(String pan) {
        return realPanPattern.matcher(pan).matches();
    }

    private boolean isPseudoPan(String pan) {
        return pseudoPanPattern.matcher(pan).matches();
    }

    private boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
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

    private final HumoSenderService humoSenderService;
    private final HumoP2pHelperService humoHelperService;
    private final HumoGatewayService humoGatewayService;
    private final ApplicationProperties applicationProperties;
    private final GenerateNumberService generateNumberService;
    private final TransactionPrefixGenerator transactionPrefixUtil;
    private final BankUtilService bankUtilService;

    public HumoCreditGatewayServiceV3(
        HumoReversService humoReversService,
        HumoMiddlewareHelperService humoMiddlewareHelperService,
        PcHelper pcHelper,
        HumoSenderService humoSenderService,
        HumoP2pHelperService humoHelperService,
        HumoGatewayService humoGatewayService,
        ApplicationProperties applicationProperties,
        GenerateNumberService generateNumberService,
        TransactionPrefixGenerator transactionPrefixUtil,
        BankUtilService bankUtilService
    ) {
        this.humoReversService = humoReversService;
        this.humoMiddlewareHelperService = humoMiddlewareHelperService;
        this.pcHelper = pcHelper;
        this.humoHelperService = humoHelperService;
        this.transactionPrefixUtil = transactionPrefixUtil;

        realPanPattern = Pattern.compile("9860\\d{12}");
        pseudoPanPattern = Pattern.compile(Constants.HUMO_PSEUDO_PAN_PATTERN);

        this.humoSenderService = humoSenderService;
        this.humoGatewayService = humoGatewayService;
        this.applicationProperties = applicationProperties;
        this.generateNumberService = generateNumberService;
        this.bankUtilService = bankUtilService;
    }
}
