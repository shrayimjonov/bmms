package uz.paykap.gateway.uzcard;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.CommonBillService;
import uz.devops.bmcore.domain.CommonBill;
import uz.devops.bmcore.dto.GatewayResult;
import uz.devops.bmcore.dto.request.CommonStatusRequest;
import uz.devops.bmcore.dto.request.SharedCancelDTO;
import uz.devops.bmcore.dto.request.SharedCheckDTO;
import uz.devops.bmcore.dto.request.SharedPayDTO;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.devops.bmcore.gateway.CancelHandler;
import uz.devops.bmcore.gateway.GatewayBillingService;
import uz.devops.bmcore.gateway.GatewayResultHandler;
import uz.devops.bmcore.gateway.PrepayHandler;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.javlon.commons.result.HasData;
import uz.javlon.commons.util.CardUtil;
import uz.javlon.svgate.config.SvgateProperties;
import uz.javlon.svgate.config.SvgateResult;
import uz.javlon.svgate.domain.Bank;
import uz.javlon.svgate.dto.DebitTranDTO;
import uz.javlon.svgate.dto.TranDTO;
import uz.javlon.svgate.dto.sv.SvCardV2DTO;
import uz.javlon.svgate.service.SvGateCardServiceV2;
import uz.javlon.svgate.service.SvGatePaymentServiceV2;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.config.Constants;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.gateway.BankUtilService;
import uz.paykap.gateway.BaseGatewayBillingService;
import uz.paykap.security.SecurityUtils;
import uz.paykap.service.PcHelper;
import uz.paykap.utils.ExtIdGeneratorInterface;
import uz.paykap.utils.TransactionPrefixGenerator;
import uz.paykap.utils.UzcardExtIdGenerator;

import javax.validation.constraints.NotNull;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Service(UzcardCreditGatewayServiceV3.NAME)
public class UzcardCreditGatewayServiceV3 extends BaseGatewayBillingService implements GatewayBillingService {

    public static final String NAME = "uzcardCreditV3";
    public static final String COMMISSION = "commission";
    public static final String EXISTS = "exists";

    private final Pattern cardPanPattern;
    private final PcHelper pcHelper;
    private final TransactionPrefixGenerator transactionPrefixGenerator;
    private final ExtIdGeneratorInterface extIdGenerator = new UzcardExtIdGenerator();
    private final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

    public UzcardCreditGatewayServiceV3(PcHelper pcHelper, SvGateCardServiceV2 svGateCardService, SvGatePaymentServiceV2 uzcardService, SvgateProperties svgateProperties, TransactionPrefixGenerator transactionPrefixGenerator, CommonBillService commonBillService, BankUtilService bankUtilService, ApplicationProperties applicationProperties) {
        this.pcHelper = pcHelper;
        this.svGateCardService = svGateCardService;
        this.uzcardService = uzcardService;
        this.transactionPrefixGenerator = transactionPrefixGenerator;
        this.commonBillService = commonBillService;
        cardPanPattern = Pattern.compile(svgateProperties.getPanPattern());
        this.bankUtilService = bankUtilService;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public GatewayResult check(@NotNull SharedCheckDTO sharedCheckDTO, @NotNull GatewayResultHandler gatewayResultHandler) {

        log.info("Uzcard credit check : {}", sharedCheckDTO);

        if (sharedCheckDTO.getCheckDTO().getAccount() == null || sharedCheckDTO.getCheckDTO().getAccount().isEmpty() || sharedCheckDTO.getCheckDTO().getAmount() == null || sharedCheckDTO.getCheckDTO().getAmount() < 1) {
            return getErrorGatewayResult("Invalid account or amount");
        }

        if (pcHelper.isValidToken(sharedCheckDTO.getCheckDTO().getAccount(), P2pProcessorType.UZCARD)) {
            return getResultByToken(sharedCheckDTO, gatewayResultHandler);
        }
        boolean isValidPan = cardPanPattern.matcher(sharedCheckDTO.getCheckDTO().getAccount()).matches();

        if (!isValidPan) {
            return getErrorGatewayResult("Pan not valid");
        }

//        String bin = StringUtils.substring(sharedCheckDTO.getCheckDTO().getAccount(), 0, 6);
//        Bank bank = validateBinFromProperty(bin);

        // Check if bank supported
//        bankUtilService.validateAccountAndGetBank(sharedCheckDTO.getCheckDTO().getAccount());

        log.debug("Bank is supported for : {}", CardUtil.mask(sharedCheckDTO.getCheckDTO().getAccount()));

        gatewayResultHandler.preSaveRequestParams(sharedCheckDTO.getCheckDTO().getParams(), Instant.now());

        GatewayResult gatewayResult = new GatewayResult();


        String fullPan = sharedCheckDTO.getCheckDTO().getAccount();


        var p2pInfoDTO = uzcardService.p2pInfoResult(fullPan);

        log.debug("p2pInfo result : {}", p2pInfoDTO);

        String maskedPan = CardUtil.maskIfBankCard(fullPan);

//            CommonResultData<SvCardV2DTO> card = svGateCardService.createCard(new BaseCardDTO(fullPan, StringUtils.substring(p2pInfoDTO.getExp(), 2, 6)));

        if (!p2pInfoDTO.isSuccess() || p2pInfoDTO.getData() == null) {
            return getErrorGatewayResult(gatewayResult, "createCard failed : " + maskedPan);
        }

        HashMap<String, Object> data = new HashMap<>();
        data.put(Constants.PAN_PARAM, maskedPan);
        data.put(Constants.FIO_PARAM, p2pInfoDTO.getData().getFio());
        data.put(Constants.TOKEN_PARAM, p2pInfoDTO.getData().getCardToken());
        data.put(COMMISSION, DECIMAL_FORMAT.format(sharedCheckDTO.getGatewayAmount() * sharedCheckDTO.getRecipientParams().getSystemCommissionUp() / 100d));
        data.put(EXISTS, String.valueOf(p2pInfoDTO.getData().getFio() != null));

        gatewayResult
                .responseJson(data)
                .responseTime(Instant.now())
                .responseText(Constants.OK)
                .responseCode(Constants.OK)
                .success(true)
        ;
//        return getErrorGatewayResult(gatewayResult, e.getLocalizedMessage());

        return gatewayResult;
    }

    @Override
    public Long getDefaultCheckAmountIfNull() {
        return 0L;
    }

    // TODO - fix, move to V3
    public SimpleBillResponse status(CommonStatusRequest dto) {

        log.info("Status uzcard : {}", dto);

        if (dto.getBillId() == null) {
            throw new CodedException(ProblemEnum.INVALID_PARAMS);
        }

        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

        CommonBill bill = commonBillService.findTop1ByIdAndLoginAndType(dto.getBillId(), login, RequestType.PAY)
                .orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));

        if (true) {
            return commonBillService.toSimple(bill);
        }

        // TODO - fix, add new bill with

//        HasData<List<TranDTO>> svTranDTOS = uzcardService.statusBySvId(bill.getExtId());
//
//        if (svTranDTOS == null || svTranDTOS.getData() == null || !svTranDTOS.isSuccess()) {
//            return commonBillService.toSimple(bill);
//        }
//
//        if (svTranDTOS != null && svTranDTOS.getData().size() > 0) {
//            boolean a = !Objects.equals(bill.getResponseText(), svTranDTOS.getData().get(0).getStatus().name());
//            boolean b = !Objects.equals(bill.getResponseCode(), String.valueOf(svTranDTOS.getData().get(0).getResp()));
//            if (a) {
//                bill.setResponseText(svTranDTOS.getData().get(0).getStatus().name());
//            }
//
//            if (b) {
//                bill.setResponseCode(String.valueOf(svTranDTOS.getData().get(0).getResp()));
//            }
//
//            if (a || b) {
//                bill.setSuccess(svTranDTOS.getData().get(0).getResp() == 0);
//                bill = commonBillService.save(bill);
//            }
//        }

        return commonBillService.toSimple(bill);
    }

    public GatewayResult cancel(@NotNull SharedCancelDTO sharedCancelDTO, @NotNull CancelHandler cancelHandler) {
        log.info("do cancel for sharedCancelDTO: {}", sharedCancelDTO);

        var svResponseWithData = uzcardService.reverse(cancelHandler.getExtId());

        if (!svResponseWithData.isSuccess()) {
            log.warn("reverse result is not success");

            String responseCode = ProblemEnum.INTERNAL_ERROR.getStatus() != null ? String.valueOf(ProblemEnum.INTERNAL_ERROR.getStatus().getStatusCode()) : "-1";
            return errorResult(responseCode, "Reverse result is not success");
        }

        if (svResponseWithData.getData() == null || svResponseWithData.getData().size() == 0) {
            log.warn("empty result from PC");
            String responseCode = ProblemEnum.BILLING_ERROR.getStatus() != null ? String.valueOf(ProblemEnum.BILLING_ERROR.getStatus().getStatusCode()) : "-1";
            return errorResult(responseCode, "empty result from PC");
        }

        Map<String, Object> responseJson = new HashMap<>();
        responseJson.put("pan", cancelHandler.getAccount());
        responseJson.put("success", String.valueOf(svResponseWithData.isSuccess()));

        GatewayResult result = new GatewayResult();
        result.setSuccess(true);
        result.setResponseText(svResponseWithData.isSuccess() ? "SUCCESS" : "ERROR");
        result.setResponseCode(svResponseWithData.isSuccess() ? "0" : "-1");
        result.setResponseTime(Instant.now());
        result.setResponseJson(responseJson);

        log.info("return reverse result: {}", result);
        return result;
    }

    private GatewayResult getResultByToken(SharedCheckDTO sharedCheckDTO, GatewayResultHandler gatewayResultHandler) {
        String token = sharedCheckDTO.getCheckDTO().getAccount();

        var cards = svGateCardService.getCards(Collections.singletonList(token));

        if (!cards.isSuccess()) {
            return errorResult(String.valueOf(cards.getCode()), cards.getMessage());
        }

        if (cards == null) {
            log.error("getCards error");
            return errorResult("-9999", "get cards error for token: " + token);
        }

        if (!cards.isSuccess() || cards.getData() == null || cards.getData().get(0) == null) {
            log.error(cards.getMessage());
            return errorResult(String.valueOf(cards.getCode()), cards.getMessage());
        }

        SvCardV2DTO cardData = cards.getData().get(0);
        String fullPan = cardData.getPan();

//        Bank bank = validateBinFromProperty(StringUtils.substring(fullPan, 0, 6));
        // Check if bank supported
//        bankUtilService.validateAccountAndGetBank(fullPan);

        log.debug("Bank is supported for : {}", CardUtil.mask(fullPan));

//        gatewayResultHandler.preSaveRequestParams(sharedCheckDTO.getCheckDTO().getParams(), Instant.now());
        var p2pInfoDTO = uzcardService.p2pInfoResult(fullPan);

        if (p2pInfoDTO == null || p2pInfoDTO.getData() == null || !p2pInfoDTO.isSuccess()) {
            return errorResult(String.valueOf(p2pInfoDTO.getCode()), p2pInfoDTO.getMessage());
        }


        log.debug("p2pInfo result : {}", p2pInfoDTO);

        Map<String, Object> data = new HashMap<>();
        if (sharedCheckDTO.getCheckDTO().getParams() == null) {
            data = sharedCheckDTO.getCheckDTO().getParams();
        }

        data.put(Constants.PAN_PARAM, fullPan);
        data.put(Constants.FIO_PARAM, p2pInfoDTO.getData().getFio());
        data.put(Constants.TOKEN_PARAM, token);
        data.put(COMMISSION, DECIMAL_FORMAT.format(sharedCheckDTO.getGatewayAmount() * sharedCheckDTO.getRecipientParams().getSystemCommissionUp() / 100d));
        data.put(EXISTS, String.valueOf(p2pInfoDTO.getData().getFio() != null));
        gatewayResultHandler.preSaveRequestParams(data, Instant.now());

        return new GatewayResult()
                .responseJson(data)
                .responseTime(Instant.now())
                .responseText(Constants.OK)
                .responseCode(Constants.OK)
                .success(true)
                ;
    }

    private Bank validateBinFromProperty(String bin) {
        Map<String, List<Bank>> pcCreditSettings = applicationProperties.getPcCreditSettings();
        String username = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));
        if (pcCreditSettings == null) {
            log.warn("bank settings not found");
            throw new CodedException(ProblemEnum.BANK_NOT_SUPPORTED, "application.pcCreditSetting property is null");
        }
        if (pcCreditSettings.get(username) == null) {
            log.warn("bank settings property for user: {} is null", username);
            throw new CodedException(ProblemEnum.BANK_NOT_SUPPORTED, "application.pcCreditSetting." + username + " property is null");
        }

        List<Bank> banks = pcCreditSettings.get(username);
        Optional<Bank> optionalBank = banks.stream().filter(bank -> Objects.equals(bank.getCardBin(), bin)).findAny();
        if (optionalBank.isEmpty()) {
            log.warn("bank not found");
            throw new CodedException(ProblemEnum.BANK_NOT_SUPPORTED, "application.pcCreditSetting." + username + " banks is null");
        }
        return optionalBank.get();
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

    private final SvGatePaymentServiceV2 uzcardService;
    private final SvGateCardServiceV2 svGateCardService;
    private final CommonBillService commonBillService;
    private final BankUtilService bankUtilService;
    private final ApplicationProperties applicationProperties;

    @Override
    public GatewayResult pay(@NotNull SharedPayDTO sharedPayDTO, @NotNull PrepayHandler prepayHandler) {
        //TODO - Javlon, fix gateway amount

        log.info("Pay uzcard : {}", sharedPayDTO);

        GatewayResult gatewayResult = new GatewayResult();


        String pan = "";
        if (pcHelper.isValidPan(prepayHandler.getAccount(), P2pProcessorType.UZCARD)) {
            pan = prepayHandler.getAccount();
        }
        if (pcHelper.isValidToken(prepayHandler.getAccount(), P2pProcessorType.UZCARD)) {
            if (prepayHandler.getCheckResponseJson() != null && prepayHandler.getCheckResponseJson().get(Constants.PAN_PARAM) != null) {
                pan = String.valueOf(prepayHandler.getCheckResponseJson().get(Constants.PAN_PARAM));
            }
        }
        if (StringUtils.isEmpty(pan)) {
            log.warn("full pan not found...");
            throw new CodedException(ProblemEnum.INVALID_PARAMS, "open PAN not found");
        }
        String bin = StringUtils.substring(pan, 0, 6);
        log.info("Bank cardBin: {}", bin);

            /*Bank bank = null;
            if (sharedPayDTO.getPayDTO().getParams() == null || (sharedPayDTO.getPayDTO().getParams().get(Constants.EPOS_MERCHANT_PARAM) == null && sharedPayDTO.getPayDTO().getParams().get(Constants.EPOS_TERMINAL_PARAM) == null)) {
                bank = bankUtilService.validateAccountAndGetBank(pan);
            }*/

        String merchantId;
        String terminalId;
        Integer port = null;

        if (sharedPayDTO.getPayDTO() != null &&
                sharedPayDTO.getPayDTO().getParams() != null &&
                sharedPayDTO.getPayDTO().getParams().get(Constants.EPOS_MERCHANT_PARAM) != null &&
                sharedPayDTO.getPayDTO().getParams().get(Constants.EPOS_TERMINAL_PARAM) != null) {

            merchantId = String.valueOf(sharedPayDTO.getPayDTO().getParams().get(Constants.EPOS_MERCHANT_PARAM));
            terminalId = String.valueOf(sharedPayDTO.getPayDTO().getParams().get(Constants.EPOS_TERMINAL_PARAM));

            String sPort = sharedPayDTO.getPayDTO().getParams().get("port") != null ? String.valueOf(sharedPayDTO.getPayDTO().getParams().get("port")) : "1";
            try {
                port = Integer.parseInt(sPort);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        } else {

            Map<String, List<Bank>> pcCreditSettings = applicationProperties.getPcCreditSettings();
            String username = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));
            if (pcCreditSettings == null) {
                log.warn("bank settings not found");
                throw new CodedException(ProblemEnum.BANK_NOT_SUPPORTED, "application.pcCreditSetting property is null");
            }
            if (pcCreditSettings.get(username) == null) {
                log.warn("bank settings property for user: {} is null", username);
                throw new CodedException(ProblemEnum.BANK_NOT_SUPPORTED, "application.pcCreditSetting." + username + " property is null");
            }

            List<Bank> banks = pcCreditSettings.get(username);
            Optional<Bank> optionalBank = banks.stream().filter(bank -> Objects.equals(bank.getCardBin(), bin)).findAny();
            if (optionalBank.isEmpty()) {
                log.warn("bank not found");
                throw new CodedException(ProblemEnum.BANK_NOT_SUPPORTED, "application.pcCreditSetting." + username + " banks is null");
            }
            Bank bank = optionalBank.get();
            merchantId = bank.getMerchantId();
            terminalId = bank.getTerminalId();

            if (bank.getPort() != null) {
                port = bank.getPort();
            }
        }

        if (StringUtils.isEmpty(merchantId) || StringUtils.isEmpty(terminalId)) {
            log.warn("merchantId or/and terminalId not found");
            return getErrorGatewayResult("merchantId or/and terminalId not found");
        }

        // Uzcard-credit recipient tiyinda bo'lgani uchun va
        // PayDto.gatewayAmount tiyinda double bo'lib kelgani uchun
        // gatewayAmount.longValue() olinmoqda

        if (sharedPayDTO.getGatewayAmount() == null) {
            String errorText = "Uzcard credit gateway amount is null";
            return getErrorGatewayResult(gatewayResult, errorText);
        }

        Long amount = sharedPayDTO.getGatewayAmount().longValue();

        DebitTranDTO debitTranDTO = new DebitTranDTO();
        debitTranDTO.setAmount(amount);
        debitTranDTO.setCardId(String.valueOf(prepayHandler.getCheckResponseJson().get(Constants.TOKEN_PARAM)));
        debitTranDTO.setExt(transactionPrefixGenerator.getGatewayCreditPrefix(sharedPayDTO.getTranId() != null ? sharedPayDTO.getTranId() : sharedPayDTO.getPayDTO().getBillId()));
        debitTranDTO.setMerchantId(merchantId);
        debitTranDTO.setTerminalId(terminalId);
        debitTranDTO.setPort(port);

        var tranDTO = uzcardService.credit(debitTranDTO);

        if (!tranDTO.isSuccess() || tranDTO.getData() == null) {
            if (Objects.equals(tranDTO.getCode(), SvgateResult.TIMEOUT.getCode())) {
                return getErrorGatewayResult(gatewayResult, tranDTO.getMessage()).withStatus(TransactionStatus.WAITING);
            }
            return getErrorGatewayResult(gatewayResult, tranDTO.getMessage());
        }

        String masked = CardUtil.mask(pan);

        HashMap<String, Object> data = new HashMap<>();
        data.put(Constants.PAN_PARAM, masked);
        data.put(Constants.FIO_PARAM, prepayHandler.getCheckResponseJson().get(Constants.FIO_PARAM));

        UzcardUtils.setTranData(tranDTO.getData(), data);

        gatewayResult
                .responseJson(data)
                .responseTime(Instant.now())
                .responseText(StringUtils.substring(tranDTO.getData().getRespDesc(), 0, 128))
                .responseCode(String.valueOf(tranDTO.getData().getResp()))
                .extId(tranDTO.getData().getRefNum())
                .success(tranDTO.getData().getResp() == 0)
        ;

        return gatewayResult;
    }

}
