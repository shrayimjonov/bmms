package uz.paykap.gateway.uzcard;

import lombok.AllArgsConstructor;
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
import uz.devops.bmcore.gateway.CancelHandler;
import uz.devops.bmcore.gateway.GatewayBillingService;
import uz.devops.bmcore.gateway.GatewayResultHandler;
import uz.devops.bmcore.gateway.PrepayHandler;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.javlon.commons.util.CardUtil;
import uz.javlon.svgate.config.SvgateProperties;
import uz.javlon.svgate.dto.DebitTranDTO;
import uz.javlon.svgate.dto.enumeration.CardStatus;
import uz.javlon.svgate.dto.sv.SvCardV2DTO;
import uz.javlon.svgate.service.SvGateCardServiceV2;
import uz.javlon.svgate.service.SvGatePaymentServiceV2;
import uz.paykap.config.Constants;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.gateway.BaseGatewayBillingService;
import uz.paykap.security.SecurityUtils;
import uz.paykap.service.PcHelper;
import uz.paykap.utils.ExtIdGeneratorInterface;
import uz.paykap.utils.MapUtil;
import uz.paykap.utils.TransactionPrefixGenerator;
import uz.paykap.utils.UzcardExtIdGenerator;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service(UzcardDebitGatewayServiceV3.NAME)
@AllArgsConstructor
public class UzcardDebitGatewayServiceV3 extends BaseGatewayBillingService implements GatewayBillingService {

    public static final String NAME = "uzcardDebitV3";

    private final PcHelper pcHelper;
    private final TransactionPrefixGenerator transactionPrefixGenerator;
    private final SvGateHelperService svGateHelperService;

    @Override
    public GatewayResult check(@NotNull SharedCheckDTO sharedCheckDTO, @NotNull GatewayResultHandler gatewayResultHandler) {

        log.info("Uzcard debit check : {}", sharedCheckDTO);

        if (sharedCheckDTO.getCheckDTO() == null || StringUtils.isEmpty(sharedCheckDTO.getCheckDTO().getAccount()) || sharedCheckDTO.getCheckDTO().getAmount() == null || sharedCheckDTO.getCheckDTO().getAmount() < 1) {
            return getErrorGatewayResult("Invalid account or amount");
        }

        String pan = sharedCheckDTO.getCheckDTO().getAccount();

        if (pcHelper.isValidToken(pan, P2pProcessorType.UZCARD)) {
            return getCardInfo(pan, sharedCheckDTO, gatewayResultHandler);
        }

        if (!pcHelper.isValidPan(pan, P2pProcessorType.UZCARD)) {
            return getErrorGatewayResult("uzCard pan is nod valid");
        }

        if (MapUtil.keyNotFoundOrEmpty(sharedCheckDTO.getCheckDTO().getParams(), Constants.EXPIRY_PARAM)) {
            return getErrorGatewayResult("params.exp is null or empty");
        }

//        if (!cardPanPattern.matcher(sharedCheckDTO.getCheckDTO().getAccount()).matches()) {
        /*if (!pan.matches(svgateProperties.getPanPattern())) {
            return getErrorGatewayResult("Invalid uzcard pan");
        }*/

        String maskedPan = CardUtil.mask(pan);

        HashMap<String, Object> map = new HashMap<>(sharedCheckDTO.getCheckDTO().getParams());
        map.put(Constants.PAN_PARAM, pan);
        gatewayResultHandler.preSaveRequestParams(map, Instant.now());

        GatewayResult gatewayResult = new GatewayResult();

        String expiry = String.valueOf(sharedCheckDTO.getCheckDTO().getParams().get(Constants.EXPIRY_PARAM));

        try {

            var validCardResponse = svGateHelperService.getSvCard(pan);

            if (validCardResponse == null || !validCardResponse.isSuccess() || validCardResponse.getData() == null || validCardResponse.getData().isEmpty()) {
                return getErrorGatewayResult(gatewayResult, "createCard failed : " + maskedPan);
            }

            SvCardV2DTO validCard = validCardResponse.getData().get(0);

            if (!validCard.getSms() || validCard.getStatus() != CardStatus.VALID) {
                log.warn("uzcard card status not valid or sms is off for pan={} and expiry={}", maskedPan, expiry);
                return getErrorGatewayResult("invalid or sms is off : " + maskedPan);
            }

            HashMap<String, Object> data = new HashMap<>();
            data.put(Constants.PAN_PARAM, maskedPan);
            data.put(Constants.PHONE_PARAM, validCard.getPhone());
            data.put(Constants.TOKEN_PARAM, validCard.getToken());

            gatewayResult
                    .responseJson(data)
                    .responseTime(Instant.now())
                    .responseText(Constants.OK)
                    .responseCode(Constants.OK)
                    .success(true)
            ;

        } catch (Exception e) {
            gatewayResult = getErrorGatewayResult(gatewayResult, e.getLocalizedMessage());
        }

        return gatewayResult;
    }

    @Override
    public GatewayResult pay(@NotNull SharedPayDTO sharedPayDTO, @NotNull PrepayHandler prepayHandler) {
        //TODO - Javlon, fix gateway amount

        log.debug("Uzcard debit pay : {}", sharedPayDTO.getPayDTO());

        if (MapUtil.keyNotFoundOrEmpty(sharedPayDTO.getPayDTO().getParams(), Constants.EPOS_MERCHANT_PARAM)
                || MapUtil.keyNotFoundOrEmpty(sharedPayDTO.getPayDTO().getParams(), Constants.EPOS_TERMINAL_PARAM)) {
            return getErrorGatewayResult("epos is null or empty");
        }

        GatewayResult gatewayResult = new GatewayResult();

        log.info("Bank cardBin: {}", StringUtils.substring(prepayHandler.getAccount(), 0, 6));
//        Bank bank = bankUtilService.validateAccountAndGetBank(prepayHandler.getAccount());

        // Uzcard-credit recipient tiyinda bo'lgani uchun va
        // PayDto.gatewayAmount tiyinda double bo'lib kelgani uchun
        // gatewayAmount.longValue() olinmoqda
        if (sharedPayDTO.getGatewayAmount() == null) {
            String errorText = "Uzcard debit gateway amount is null";
            return getErrorGatewayResult(gatewayResult, errorText);
        }

        String cardToken = String.valueOf(prepayHandler.getCheckResponseJson().get(Constants.TOKEN_PARAM));
        Long amount = sharedPayDTO.getGatewayAmount().longValue();

        log.debug("Debiting from card token : {}", cardToken);


        // 1. Debit
        DebitTranDTO debitTranDTO = new DebitTranDTO();
        debitTranDTO.setAmount(amount);
        debitTranDTO.setCardId(String.valueOf(prepayHandler.getCheckResponseJson().get(Constants.TOKEN_PARAM)));
        debitTranDTO.setExt(transactionPrefixGenerator.getGatewayDebitPrefix(sharedPayDTO.getTranId() != null ? sharedPayDTO.getTranId() : sharedPayDTO.getPayDTO().getBillId()));
        debitTranDTO.setMerchantId(String.valueOf(sharedPayDTO.getPayDTO().getParams().get(Constants.EPOS_MERCHANT_PARAM)));
        debitTranDTO.setTerminalId(String.valueOf(sharedPayDTO.getPayDTO().getParams().get(Constants.EPOS_TERMINAL_PARAM)));
        debitTranDTO.setPort(1);

        var tranDTO = uzcardService.debit(debitTranDTO);

        if (!tranDTO.isSuccess() || tranDTO.getData() == null) {
            return getErrorGatewayResult(gatewayResult, tranDTO.getMessage());
        }

        String masked = CardUtil.mask(prepayHandler.getAccount());

        log.debug("Uzcard Debit result : {}", debitTranDTO);

        HashMap<String, Object> data = new HashMap<>();
        data.put(Constants.PAN_PARAM, masked);
//            data.put(Constants.FIO_PARAM, prepayHandler.getCheckResponseJson().get(Constants.FIO_PARAM));

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

    @Override
    public Long getDefaultCheckAmountIfNull() {
        return 0L;
    }

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
//        if (svTranDTOS != null && svTranDTOS.size() > 0) {
//            boolean a = !Objects.equals(bill.getResponseText(), svTranDTOS.get(0).getStatus().name());
//            boolean b = !Objects.equals(bill.getResponseCode(), String.valueOf(svTranDTOS.get(0).getResp()));
//            if (a) {
//                bill.setResponseText(svTranDTOS.get(0).getStatus().name());
//            }
//
//            if (b) {
//                bill.setResponseCode(String.valueOf(svTranDTOS.get(0).getResp()));
//            }
//
//            if (a || b) {
//                bill.setSuccess(svTranDTOS.get(0).getResp() == 0);
//                bill = commonBillService.save(bill);
//            }
//        }
//
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

    private GatewayResult getCardInfo(String pan, SharedCheckDTO sharedCheckDTO, GatewayResultHandler gatewayResultHandler) {
        var cards = uzcardCardService.getCards(Collections.singletonList(pan));

        if (!cards.isSuccess()) {
            return errorResult(String.valueOf(cards.getCode()), cards.getMessage());
        }

        if (cards == null) {
            log.error("getCards error");
            return errorResult("-9999", "get cards error for token: " + pan);
        }

        if (!cards.isSuccess() || cards.getData() == null || cards.getData().get(0) == null) {
            log.error(cards.getMessage());
            return errorResult(String.valueOf(cards.getCode()), cards.getMessage());
        }

        SvCardV2DTO cardData = cards.getData().get(0);

        String cardPan = cardData.getPan();
        String maskedPan = CardUtil.mask(cardPan);

        GatewayResult gatewayResult = new GatewayResult();
        Map<String, Object> data = new HashMap<>();

        if (sharedCheckDTO.getCheckDTO().getParams() != null) {
            data = sharedCheckDTO.getCheckDTO().getParams();
        }

        data.put(Constants.PAN_PARAM, maskedPan);
        data.put(Constants.PHONE_PARAM, cardData.getPhone());
        data.put(Constants.TOKEN_PARAM, cardData.getToken());
        gatewayResultHandler.preSaveRequestParams(data, Instant.now());

        gatewayResult
                .responseJson(data)
                .responseTime(Instant.now())
                .responseText(Constants.OK)
                .responseCode(Constants.OK)
                .success(true)
        ;

        return gatewayResult;
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

    /*private SvgateProperties.Bank getBank(String cardBinOrPan) {
        if (cardBinOrPan == null || cardBinOrPan.isEmpty()) {
            throw new CodedException(ProblemEnum.BANK_NOT_SUPPORTED);
        }

        if (cardBinOrPan.length() > 6) {
            cardBinOrPan = cardBinOrPan.substring(0, 6);
        }

        SvgateProperties.Bank bank = svgateProperties.getBanks().get(cardBinOrPan);

        if (bank == null || !bank.getP2p() || bank.getMerchantId() == null || bank.getMerchantId().isEmpty() || bank.getTerminalId() == null || bank.getTerminalId().isEmpty() || bank.getPort() == null) {
            throw new CodedException(ProblemEnum.BANK_NOT_SUPPORTED);
        }

        return bank;
    }*/

    private final SvGatePaymentServiceV2 uzcardService;
    private final SvGateCardServiceV2 uzcardCardService;
    private final SvgateProperties svgateProperties;
    private final CommonBillService commonBillService;
    private final ExtIdGeneratorInterface extIdGenerator = new UzcardExtIdGenerator();

}

