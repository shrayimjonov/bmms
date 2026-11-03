package uz.paykap.service.humo;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.integration.xml.xpath.XPathUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import uz.devops.bmcore.cache.CacheEvictable;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.util.MapUtil;
import uz.devops.humo.service.HumoGatewayService;
import uz.devops.humo.service.dto.*;
import uz.devops.humo.service.dto.v3.getpayment.GetPaymentStatusCodes;
import uz.devops.humo.service.result.HumoResult;
import uz.devops.humoMiddleware.dto.request.GetRealPanRequestDTO;
import uz.devops.humoMiddleware.dto.response.RetrieveCardV3ResponseDTO;
import uz.devops.humoMiddleware.service.HumoMiddlewareService;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.javlon.commons.result.CommonResultData;
import uz.javlon.commons.result.HasData;
import uz.javlon.commons.util.CardUtil;
import uz.paykap.adapter.verification.pc.PcTranType;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.config.Constants;
import uz.paykap.domain.Bill;
import uz.paykap.domain.PcBin;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.BinPcType;
import uz.paykap.domain.enumeration.HumoCardType;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.gateway.CommonGatewaySender;
import uz.paykap.gateway.ResultChecker;
import uz.paykap.repository.BillRepository;
import uz.paykap.repository.PcBinRepository;
import uz.paykap.security.SecurityUtils;
import uz.paykap.sender.impl.HumoSenderService;
import uz.paykap.service.GenerateNumberService;
import uz.paykap.service.HumoMiddlewareHelperService;
import uz.paykap.service.MapService;
import uz.paykap.service.PcHelper;
import uz.paykap.service.dto.BankCentreIdList;
import uz.paykap.service.dto.CardCreditRequest;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.p2p.dto.P2pParamResult;
import uz.paykap.service.p2p.processor.HumoParam;
import uz.paykap.service.p2p.support.P2pHelperService;
import uz.paykap.utils.TransactionPrefixGenerator;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class HumoP2pHelperService implements CacheEvictable {

    public static final String MASTER_CARD_BIN = "555536";
    public static final int DEBIT_CONFIRM_SUCCESS_ACTION = 10;
    public static final String SUCCESS_AUTH_ACTION_CODE = "000";
    public static final String CONFIRM_SUCCESS = "Confirm success";
    public static final String ADDITIONAL_PARAM = "ADDITIONAL_PARAM";

    public static final String CENTRE_ID = "CENTRE_ID";
    public static final String SESSION_ID = "SESSION_ID";
    public static final String HUMO_CREDIT_RECIPIENT_NAME = "humo-credit";
    public static final String HUMO_P2P = "humo-p2p";
    public static final String HUMO_P2P_CONFIRM = "humo-p2p-confirm";

    private final PcHelper pcHelper;
    private final MapService mapService;
    private final BillRepository billRepository;
    private final P2pHelperService p2pHelperService;
    private final HumoGatewayService humoGatewayService;
    private final ApplicationProperties applicationProperties;
    private final GenerateNumberService generateNumberService;
    private final TransactionPrefixGenerator transactionPrefixUtil;
    private final HumoMiddlewareHelperService humoMiddlewareHelperService;
    private final HumoMiddlewareService humoMiddlewareService;
    private final HumoReconcileHelperService reconcileHelperService;
    private final PcBinRepository pcBinRepository;
    private final CommonGatewaySender commonGatewaySender;

    private static final Map<String, String> centreIds = new HashMap<>() {
        {
            put("986001", "Ipoteka");
            put("986002", "UzPSB");
            put("986003", "Agrobank");
            put("986004", "Asaka");
            put("986006", "KishloqKB");
            put("986008", "Xalqbank");
            put("986009", "AsiaAllianc");
            put("986010", "Kapital");
            put("986012", "NBU");
            put("986013", "Mkredit");
            put("986014", "Savdogar");
            put("986015", "Turon");
            put("986016", "Hamkor");
            put("986017", "IpakYuli");
            put("986018", "Trastbank");
            put("986019", "Aloqa");
            put("986020", "KDB");
            put("986021", "Turkiston");
            put("986023", "Universal");
            put("986024", "Ravnaq");
            put("986025", "Davr");
            put("986026", "Infin");
            put("986027", "OFB");
            put("986028", "HiTech");
            put("986029", "UTBank");
            put("986030", "Saderat");
            put("986031", "AgroExpBank");
            put("986032", "MadadInvest");
            put("986033", "Poytaxtbank");
            put("986034", "Tengebank");
            put("986035", "TBCBank");
            put("986060", "AnorBank");
            put("55553601", "Ipoteka");
            put("55553602", "UzPSB");
            put("55553603", "Agrobank");
            put("55553604", "Asaka");
            put("55553606", "KishloqKB");
            put("55553608", "Xalqbank");
            put("55553609", "AsiaAllianc");
            put("55553610", "Kapital");
            put("55553612", "NBU");
            put("55553613", "Mkredit");
            put("55553614", "Savdogar");
            put("55553615", "Turon");
            put("55553616", "Hamkor");
            put("55553617", "IpakYuli");
            put("55553618", "Trastbank");
            put("55553619", "Aloqa");
            put("55553620", "KDB");
            put("55553621", "Turkiston");
            put("55553623", "Universal");
            put("55553624", "Ravnaq");
            put("55553625", "Davr");
            put("55553626", "Infin");
            put("55553627", "OFB");
            put("55553628", "HiTech");
            put("55553629", "UTBank");
            put("55553630", "Saderat");
            put("55553631", "AgroExpBank");
            put("55553632", "MadadInvest");
            put("55553633", "Poytaxtbank");
            put("55553634", "Tengebank");
            put("55553635", "TBCBank");
            put("55553660", "AnorBank");
            put("55553661", "AnorBank");
        }
    };

    @PostConstruct
    public void evictAll() {
        final Map<String, String> humoCentreIds = getHumoCentreIds();
        centreIds.clear();
        centreIds.putAll(humoCentreIds);
    }

    private Map<String, String> getHumoCentreIds() {
        List<PcBin> humoBins = pcBinRepository.findAllByPcType(BinPcType.HUMO);
        log.debug("humoBins: {}", humoBins);

        if (humoBins == null || humoBins.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, String> result = new HashMap<>();
        humoBins
                .stream()
                .filter(bin -> !StringUtils.isEmpty(bin.getBankName()))
                .forEach(bin -> result.put(bin.getBin(), bin.getBankName()));
        log.info("initial Map: {}", result);

        return result;
    }

    public BmResultData<String> getRealPan(String fakePan) {
        log.info("Get real pan from fake pan: {}", fakePan);

        try {
            var realCardResponse = humoMiddlewareService.getRealPan(new GetRealPanRequestDTO(fakePan));

            if (!realCardResponse.isSuccess()) {
                return new BmResultData<>(BmResultStatus.PC_ERROR, realCardResponse.getMessage());
            }

            return new BmResultData<>(BmResultStatus.SUCCESS, realCardResponse.getData().getPan(), "Success");
        } catch (Exception e) {
            log.warn("Exception when get real pan : {}", e.getMessage());
            return new BmResultData<>(BmResultStatus.PC_ERROR, e.getLocalizedMessage());
        }
    }

    public BmResultData<Bill> credit(
            Long transactionId,
            String pan,
            Long amount,
            String merchantId,
            String terminalId,
            SenderAdditionalParam additionalParam
    ) {
        CardCreditRequest cardCreditRequest = new CardCreditRequest()
                .withAmount(amount)
                .withPan(pan)
                .withMerchantId(merchantId)
                .withTerminalId(terminalId)
                .withFakePan(pcHelper.isValidPan(pan, P2pProcessorType.HUMO) != Boolean.TRUE)
                .withTransactionId(transactionId)//
                ;

        if (
                additionalParam != null &&
                        !StringUtils.isEmpty(additionalParam.getFirstName()) &&
                        !StringUtils.isEmpty(additionalParam.getLastName()) &&
                        !StringUtils.isEmpty(additionalParam.getMiddleName())
        ) {
            cardCreditRequest.setAdditionalParam(additionalParam);
        }
        return credit(
                cardCreditRequest,
                SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND))
        );
    }

    public String returnException(Exception e) {
        if (e == null) {
            return "Unknown exception";
        }

        log.warn("Result from humo not success: {}", e.getLocalizedMessage());
        if (e instanceof HttpServerErrorException) {
            HttpServerErrorException httpServerErrorException = (HttpServerErrorException) e;

            try {
                Object evaluate = XPathUtils.evaluate(httpServerErrorException.getResponseBodyAsString(), "//error/text()");

                String errorCode = String.valueOf(evaluate);

                log.debug("Humo error code parsed : {}", errorCode);

                GetPaymentStatusCodes byActionCode = GetPaymentStatusCodes.getByActionCode(errorCode);

                if (byActionCode == GetPaymentStatusCodes.UNKNOWN) {
                    log.warn("'error' tag not found in Humo exception or in codes");
                    return httpServerErrorException.getResponseBodyAsString();
                }

                log.warn("Humo error message : {}", errorCode + " : " + byActionCode.getMsgFull());

                return byActionCode.getActionCode() + ":" + byActionCode.getMsgFull();
            } catch (Exception parseException) {
                log.warn("Cannot parse humo exception : {}", parseException.getMessage());
                return httpServerErrorException.getResponseBodyAsString();
            }
        }

        return e.getLocalizedMessage();
    }

    public BmResultData<Bill> debitConfirm(Transaction transaction) {

        DebitConfirmRequestDTO debitConfirmRequestDTO = new DebitConfirmRequestDTO();
        debitConfirmRequestDTO.setPaymentId(transaction.getSenderTrId());
        debitConfirmRequestDTO.setPaymentRef(generateNumberService.humoPaymentRef(String.valueOf(transaction.getId())));

        Map<String, Object> requestJson = mapService.convert(debitConfirmRequestDTO);

        requestJson.put(Constants.AMOUNT_PARAM, String.valueOf(transaction.getSenderAmount()));
        requestJson.put(Constants.CCY_PARAM, String.valueOf(Constants.UZS_CURRENCY));
        requestJson.put(Constants.POINT_CODE_PARAM, applicationProperties.getHumo().getVirtualPointCode());

        Bill bill = new Bill()
                .recipientShortName(HumoSenderService.HUMO_DEBIT_CONFIRM)
                .account(transaction.getSenderAccount())
                .amount(transaction.getSenderAmount())
                .amountRatio(100)
                .currency(Constants.UZS_CURRENCY)
                .type(RequestType.CONFIRM)
                .login(transaction.getRequestLogin())
                .requestJson(requestJson)
                .requestTime(Instant.now())
                .responseTime(Instant.now())
                .transactionId(transaction.getId());

        bill = billRepository.save(bill);
        log.info("Humo debit confirm bill saved: {}", bill);

        HasData<DebitResultDTO> simpleResultDataConfirm;

        try {
            simpleResultDataConfirm = humoGatewayService.confirmDualMessageDebit(debitConfirmRequestDTO);
        } catch (Exception e) {
            String exceptionText = returnException(e);

            saveErrorResponse(bill, exceptionText);

            log.error("Exception when confirming humo payment : {}", e.getLocalizedMessage());
            return new BmResultData<>(BmResultStatus.SENDER_PC_ERROR, exceptionText);
        }

        log.debug("Humo debit confirm result: {}", simpleResultDataConfirm);

        if (!simpleResultDataConfirm.isSuccess()) {
            String exceptionText = simpleResultDataConfirm.getMessage();

            saveErrorResponse(bill, exceptionText);

            return new BmResultData<>(BmResultStatus.SENDER_PC_ERROR, exceptionText);
        }

        DebitResultDTO debitConfirmResult = simpleResultDataConfirm.getData();

        if (
                !Objects.equals(debitConfirmResult.getAuthActionCode(), SUCCESS_AUTH_ACTION_CODE) ||
                        !(debitConfirmResult.getAction() == DEBIT_CONFIRM_SUCCESS_ACTION)
        ) {
            log.warn("#### Humo error when confirming debit, response : {}", debitConfirmResult);

            bill
                    .responseJson(Collections.singletonMap(Constants.ACTION_CODE_PARAM, String.valueOf(debitConfirmResult.getAction())))
                    .responseTime(Instant.now())
                    .responseText("auth action debit: " + debitConfirmResult.getPaymentId())
                    .responseCode("FAILED")
                    .success(false);

            return new BmResultData<>(billRepository.save(bill));
        }

        /* Success */

        bill
                .responseJson(mapService.convert(debitConfirmResult))
                .responseTime(Instant.now())
                .responseText(CONFIRM_SUCCESS)
                .responseCode(String.valueOf(debitConfirmResult.getAction()))
                .extId(String.valueOf(debitConfirmRequestDTO.getPaymentId()))
                .success(true);

        billRepository.save(bill);

        transaction
                .senderDate(bill.getRequestTime())
                .senderStatus(bill.getResponseCode())
                .senderStatusText(bill.getResponseText())
                .senderBillId(bill.getId());

        return new BmResultData<>(bill.isSuccess() ? BmResultStatus.SUCCESS : BmResultStatus.PC_ERROR, bill);
    }

    private void saveErrorResponse(Bill bill, String exceptionText) {
        bill
                .responseJson(Collections.singletonMap("error", exceptionText))
                .responseTime(Instant.now())
                .responseText(exceptionText)
                .responseCode("FAILED")
                .success(false);

        billRepository.save(bill);
    }

    public HasData<Bill> reverse(Transaction transaction, String merchantId, String terminalId, Boolean isSender) {
        return reverse(transaction, SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND)), merchantId, terminalId, isSender, false);
    }

    public HasData<Bill> reverse(Transaction transaction, String agentLogin, String eposMerchant, String eposTerminal, Boolean isSender, Boolean isHoldReverse) {
        log.info("\n#### Humo reverse #### login : {} \n", agentLogin);

        String pan = (isSender) ? transaction.getSenderAccount() : transaction.getRecipientAccount();

        Bill bill = new Bill()
                .recipientShortName(isHoldReverse != null && isHoldReverse ? "humo-hold-reverse" : "humo-reverse")
                .account(pan)
                .amount((isSender) ? transaction.getSenderAmount() : transaction.getRecipientAmount())
                .amountRatio(100)
                .currency(Constants.UZS_CURRENCY)
                .type(RequestType.CANCEL)
                .login(agentLogin)
                .requestTime(Instant.now())
                .responseTime(Instant.now())
                .transactionId(transaction.getId())
                .success(false)//
                ;

//        bill = billRepository.save(bill);

        String centreId = getCentreId(pan);

        if (centreId == null || centreId.isEmpty()) {
            final String msg = "Centre id for pan: " + pan + " not found";

            log.error(msg);

            saveErrorResponse(bill, msg);

            return new CommonResultData<>(BmResultStatus.INTERNAL_ERROR, null, msg);
        }

//        try {
        ReverseRequestDTO reverseRequestDTO = new ReverseRequestDTO();
        reverseRequestDTO.setCentreID(centreId);
        reverseRequestDTO.setPaymentId((isSender) ? transaction.getSenderTrId() : transaction.getRecipientTrId());
        reverseRequestDTO.setPaymentRef(generateNumberService.humoPaymentRef(String.valueOf(bill.getId())));
        reverseRequestDTO.setSessionId(generateNumberService.humoSessionId(this.getClass().getSimpleName(), String.valueOf(bill.getId())));
        reverseRequestDTO.setTerminalId(eposTerminal);
        reverseRequestDTO.setMerchantId(eposMerchant);

//            HasData<ReverseResultDTO> simpleResultData = humoGatewayService.reverse(reverseRequestDTO);

        bill.requestJson(mapService.convert(reverseRequestDTO));
        billRepository.save(bill);

        HasData<ReverseResultDTO> simpleResultData = commonGatewaySender.sendAndReceive(reverseRequestDTO, bill, humoGatewayService::reverse, reverseResultDTO -> true);

        if (!simpleResultData.isSuccess()) {

            log.warn("Reverse request is not success: {}", simpleResultData.getMessage());
            return new CommonResultData<>(simpleResultData, null, simpleResultData.getMessage());
        }

        ReverseResultDTO reverseResultDTO = simpleResultData.getData();

        bill
                .responseJson(Map.of(Constants.PAN_PARAM, (isSender) ? transaction.getSenderAccount() : transaction.getRecipientAccount()))
                .responseTime(Instant.now())
                .responseText(reverseResultDTO.isSuccess() ? "success" : "error")
                .extId((isSender) ? transaction.getSenderTrId() : transaction.getRecipientTrId())
                .success(reverseResultDTO.isSuccess());

        log.debug("#### Humo reverse response : {}", reverseResultDTO.isSuccess());

        if (reverseResultDTO.isSuccess()) {
            log.info("transaction reversed");
            if (isSender) {
                transaction.setSenderSuccess(false);
                transaction.setSenderStatus("reversed");
            } else {
                transaction.setRecipientSuccess(false);
                transaction.setRecipientStatus("reversed");
            }
        }
//        } catch (Exception e) {
//            log.warn("#### Humo Error Response : {}", e.getLocalizedMessage());
//
//            bill
//                    .responseJson(Collections.singletonMap("error", e.getLocalizedMessage()))
//                    .responseTime(Instant.now())
//                    .responseText(StringUtils.substring(e.getLocalizedMessage(), 0, 126))
//                    .responseCode("FAILED")
//                    .success(false);
//        }

        bill = billRepository.save(bill);

        log.info("Bill after reverse: {}", bill);

        return new CommonResultData<>(bill.isSuccess() ? BmResultStatus.SUCCESS : BmResultStatus.PC_ERROR, bill);
    }

    public String getVirtualPointCode() {
        return applicationProperties.getHumo().getVirtualPointCode();
    }

    public BmResultData<Bill> hold(String pan, Transaction transaction, P2pParamResult p2pParam) {
        log.debug("Requested debit hold : {}", pan);

        BmResultData<HumoParam> paramResult = p2pHelperService.getSenderParams(p2pParam.getValue(), HumoParam.class);

        if (!paramResult.isSuccess()) {
            log.warn("Could not parse Humo Param from db : {}", paramResult.getMessage());
            return new BmResultData<>(paramResult);
        }

        log.debug("Humo param parsed : {}", paramResult.getData());

        String eposMerchant = paramResult.getData().getMerchantId();
        String eposTerminal = paramResult.getData().getTerminalId();

        p2pHelperService.setSenderEpos(transaction, eposMerchant, eposTerminal);

        log.info("#### Humo debit hold ####: \n{}\n", CardUtil.mask(pan));

        String expire;
        String centreId;

        try {
            BalanceRequestDTO balanceRequestDTO = new BalanceRequestDTO();
            balanceRequestDTO.setPan(pan);

            var middlewareSimpleResultData = humoMiddlewareHelperService.retrieveCardV3(pan);

            RetrieveCardV3ResponseDTO balanceResultDTO = middlewareSimpleResultData.getData();
            log.debug("BalanceResultDTO: {}", balanceResultDTO);
            //TODO getAvailableAmount in tiyin , amount in ????
            if (balanceResultDTO.getBalance().getAvailableAmount() >= transaction.getSenderAmount()) {
                centreId = balanceResultDTO.getCard().getInstitutionId();
                expire = balanceResultDTO.getCard().getExpiry();
            } else {
                log.warn("Humo debit hold INSUFFICIENT_FUNDS_ON_SENDER : {}", pan);
                return new BmResultData<>(BmResultStatus.INSUFFICIENT_FUNDS_ON_SENDER, CardUtil.mask(pan));
            }
        } catch (Exception e) {
            log.warn("Error when get balance or get realPan from humo: {}", e.getLocalizedMessage());
            return new BmResultData<>(BmResultStatus.SENDER_PC_ERROR, e.getMessage());
        }

        log.debug("Pan: {}", CardUtil.mask(pan));

        if (expire == null) {
            throw new CodedException(ProblemEnum.INVALID_PARAMS, "expire");
        }
        log.debug("Expire: {}", expire);

        if (centreId == null || centreId.isEmpty()) {
            throw new CodedException(ProblemEnum.INVALID_PARAMS, "centre_id");
        }

        log.debug("Centre id: {}", centreId);

        log.info("\n#### Humo debit hold #### \n");

        Map<String, Object> requestJson = new HashMap<>();
        requestJson.put(Constants.AMOUNT_PARAM, String.valueOf(transaction.getSenderAmount()));
        requestJson.put(Constants.CCY_PARAM, String.valueOf(Constants.UZS_CURRENCY));
        requestJson.put(Constants.CENTRE_ID_PARAM, centreId);
        requestJson.put(Constants.EXPIRY_PARAM, expire);
        requestJson.put(Constants.PAN_PARAM, pan);
        requestJson.put(Constants.POINT_CODE_PARAM, getVirtualPointCode());

        final String paymentRef = transactionPrefixUtil.getP2pDebitPrefix(transaction.getId());

        Bill bill = new Bill()
                .recipientShortName(HumoSenderService.HUMO_DEBIT_CREATE)
                .account(pan)
                .amount(transaction.getSenderAmount())
                .amountRatio(100)
                .currency(Constants.UZS_CURRENCY)
                .requestJson(requestJson)
                .type(RequestType.P2P_DEBIT)
                .login(transaction.getRequestLogin())
                .requestTime(Instant.now())
                .responseTime(Instant.now())
                .transactionId(transaction.getId())
                .outExtId(paymentRef)//
                ;

        bill = billRepository.save(bill);

        log.debug("Debiting from card : {}", CardUtil.mask(pan));

        try {
            // 1. Debit create

            DebitCreateRequestDTO debitCreateRequestDTO = new DebitCreateRequestDTO();
            debitCreateRequestDTO.setAmount(transaction.getSenderAmount());
            debitCreateRequestDTO.setCcyCode(Constants.UZS_CURRENCY);
            //centreID == institutionId"
            debitCreateRequestDTO.setCentreID(centreId);
            debitCreateRequestDTO.setExpDate(expire); //todo exp to long
            debitCreateRequestDTO.setPan(pan);
            debitCreateRequestDTO.setPointCode(getVirtualPointCode());
            debitCreateRequestDTO.setTerminalId(eposTerminal);
            debitCreateRequestDTO.setMerchantId(eposMerchant);
            debitCreateRequestDTO.setSessionId(
                    generateNumberService.humoSessionId(this.getClass().getSimpleName(), String.valueOf(bill.getId()))
            );
            debitCreateRequestDTO.setPaymentRef(paymentRef);

            //todo HUMO-DEBIT-OPTIMIZE: HumoSenderService.innerDebit(), HumoSenderService.hold(), HumoDebitPayService.debit(), HumoP2pHelperService.hold() methodlari bir xil ish qilyapdi
            HasData<DebitResultDTO> simpleResultData = humoGatewayService.createDualMessageDebit(debitCreateRequestDTO);
            log.debug("Debit create result : {}", simpleResultData);

            if (!simpleResultData.isSuccess()) {
                if (simpleResultData.isTimeout()) {
                    BmResultData<Bill> timeoutData = p2pHelperService.saveAndReturnError(bill, simpleResultData.getMessage());
                    timeoutData.setStatus(BmResultStatus.TIMEOUT);
                    timeoutData.getData().setResponseText(BmResultStatus.TIMEOUT.name());
                    return timeoutData;
                }
                return p2pHelperService.saveAndReturnError(bill, simpleResultData.getMessage());
            }

            DebitResultDTO debitCreateResultDTO = simpleResultData.getData();
            // если ответ неуспешен
            if (debitCreateResultDTO.getAction() != 4 || debitCreateResultDTO.getPaymentId() == null) {
                log.warn("#### Humo error when creating debit, response : {}", debitCreateResultDTO);
                return p2pHelperService.saveAndReturnError(
                        bill,
                        "FAILED",
                        "payment id: " + debitCreateResultDTO.getPaymentId(),
                        Collections.singletonMap(Constants.ACTION_CODE_PARAM, String.valueOf(debitCreateResultDTO.getAction()))
                );
            }

            bill
                    .responseJson(mapService.convert(debitCreateResultDTO))
                    .responseTime(Instant.now())
                    .responseText("Success")
                    .responseCode(String.valueOf(debitCreateResultDTO.getAction()))
                    .extId(String.valueOf(debitCreateResultDTO.getPaymentId()))
                    .success(true);

            return new BmResultData<>(billRepository.save(bill));
        } catch (Exception e) {
            log.warn("#### Humo Error Response : {}", e.getLocalizedMessage());

            return p2pHelperService.saveAndReturnError(
                    bill,
                    e.getLocalizedMessage(),
                    Collections.singletonMap("error", e.getLocalizedMessage())
            );
        }
    }

    public BmResultData<Bill> credit(CardCreditRequest cardCreditRequest, String agentLogin) {
        log.info("Card credit request: {}", cardCreditRequest);

        String pan = cardCreditRequest.getPan();

        final String paymentRef = transactionPrefixUtil.getP2pCreditPrefix(cardCreditRequest.getTransactionId());

        Bill bill = new Bill()
                .recipientShortName(HUMO_CREDIT_RECIPIENT_NAME)
                .type(RequestType.P2P_CREDIT)
                .amountRatio(100)
                .amount(cardCreditRequest.getAmount())
                .account(pan)
                .currency(Constants.UZS_CURRENCY)
                .requestTime(Instant.now())
                .requestJson(mapService.convert(cardCreditRequest))
                .responseTime(Instant.now())
                .login(agentLogin)
                .success(false)
                .transactionId(cardCreditRequest.getTransactionId())
                .outExtId(paymentRef);

        bill = billRepository.save(bill);
        log.debug("Bill saved: {}", bill);

        if (pcHelper.isValidToken(pan, P2pProcessorType.HUMO)) {
            try {
                final BmResultData<String> realPan = getRealPan(cardCreditRequest.getPan());

                if (realPan == null) {
                    return new BmResultData<>(BmResultStatus.PC_ERROR, "Humo getRealPan failed, bmResult is null");
                }

                if (
                        !realPan.isSuccess() ||
                                Objects.isNull(realPan.getData()) ||
                                !pcHelper.isValidPan(realPan.getData(), P2pProcessorType.HUMO)
                ) {
                    log.warn("Error when getting realPan from Humo : " + realPan.getMessage());
                    return new BmResultData<>(realPan);
                }

                pan = realPan.getData();
                bill.account(pan);
                log.debug("Fetched humo.realPan: {}", pan);
            } catch (Exception e) {
                log.warn("Exception when getting real pan: {}", e.getLocalizedMessage());

                bill.responseTime(Instant.now()).responseText("Exception when getting real pan");
                bill = billRepository.save(bill);
                return new BmResultData<>(BmResultStatus.RECIPIENT_PC_ERROR, bill, e.getLocalizedMessage());
            }
        }

        var simpleResultData = humoMiddlewareHelperService.retrieveCardV3(pan);

        if (!simpleResultData.isSuccess()) {
            log.error("Stack Trace: ");

            bill.responseTime(Instant.now()).responseText("Error when get balance").success(false);
            bill = billRepository.save(bill);

            return new BmResultData<>(BmResultStatus.RECIPIENT_PC_ERROR, bill);
        }

        if (
                simpleResultData.getData() == null ||
                        simpleResultData.getData().getBalance() == null ||
                        simpleResultData.getData().getCard() == null ||
                        simpleResultData.getData().getAccount() == null
        ) {
            log.warn("Balance result invalid");

            bill.responseTime(Instant.now()).responseText("Balance result invalid");
            bill = billRepository.save(bill);

            return new BmResultData<>(BmResultStatus.RECIPIENT_PC_ERROR, bill, "Balance result invalid");
        }

        CreditRequestDTO creditRequestDTO = new CreditRequestDTO();
        creditRequestDTO.setPaymentRef(paymentRef);
        creditRequestDTO.setSessionId(generateNumberService.humoSessionId(this.getClass().getSimpleName(), String.valueOf(bill.getId())));
        creditRequestDTO.setCentreId(simpleResultData.getData().getCard().getInstitutionId());
        creditRequestDTO.setPointCode(applicationProperties.getHumo().getVirtualPointCode());
        creditRequestDTO.setTerminalId(cardCreditRequest.getTerminalId());
        creditRequestDTO.setMerchantId(cardCreditRequest.getMerchantId());
        creditRequestDTO.setAmount(cardCreditRequest.getAmount());
        creditRequestDTO.setExpiry(simpleResultData.getData().getCard().getExpiry());
        creditRequestDTO.setPan(pan);

        if (cardCreditRequest.getAdditionalParam() != null) {
            creditRequestDTO.setAdditionalParam(cardCreditRequest.getAdditionalParam());
        }

        bill.requestJson(mapService.convert(creditRequestDTO));

        bill = billRepository.save(bill);
        log.debug("Saved request json: {}", bill);

        HasData<CreditResultDTO> creditSimpleResult;

        creditSimpleResult = humoGatewayService.credit(creditRequestDTO);
        log.info("Credit result: {}", creditSimpleResult);

        if (!creditSimpleResult.isSuccess()) {
            log.warn("Error when credit card: {}", creditSimpleResult.getMessage());
            log.error("Stack Trace: ");

            bill.responseTime(Instant.now()).responseText("Error when credit card");
            bill = billRepository.save(bill);
            if (creditSimpleResult.isTimeout()) {
                BmResultData<Bill> timeoutData = p2pHelperService.saveAndReturnError(bill, creditSimpleResult.getMessage());
                timeoutData.setStatus(BmResultStatus.TIMEOUT);
                timeoutData.getData().setResponseText(BmResultStatus.TIMEOUT.name());
                return new BmResultData<>(BmResultStatus.TIMEOUT, timeoutData.getData(), creditSimpleResult.getMessage());
            }
            return new BmResultData<>(BmResultStatus.RECIPIENT_PC_ERROR, bill, creditSimpleResult.getMessage());
        }

        if (creditSimpleResult.getData() == null) {
            log.warn("Credit result invalid");

            bill.responseTime(Instant.now()).responseText("Credit result invalid");
            bill = billRepository.save(bill);

            return new BmResultData<>(BmResultStatus.RECIPIENT_PC_ERROR, bill, "Credit result invalid");
        }

        boolean success = creditSimpleResult.getData().getAction() == 4;

        bill
                .responseJson(mapService.convert(creditSimpleResult.getData()))
                .responseTime(Instant.now())
                .success(success)
                .responseText(success ? "Success" : "Failed")
                .responseCode(String.valueOf(creditSimpleResult.getData().getAction()))
                .extId(creditSimpleResult.getData().getPaymentId());

        bill = billRepository.save(bill);
        log.info("Final save bill: {}", bill);

        return new BmResultData<>(success ? BmResultStatus.SUCCESS : BmResultStatus.RECIPIENT_PC_ERROR, bill, bill.getResponseText());
    }

    public BmResultData<Bill> p2p(Transaction transaction, String senderPan, String senderExpiry, String recipientPan, Long amount, Integer switchingId, String language) {

        log.info("Humo direct p2p request: {} -> {}", senderPan, recipientPan);

        final HasData<String> senderHumoPanData = getHumoPan(senderPan);

        if (!senderHumoPanData.isSuccess()) {
            return new BmResultData<>(BmResultStatus.SENDER_PC_ERROR, null, senderHumoPanData.getMessage());
        }

        final HasData<String> recipientHumoPanData = getHumoPan(recipientPan);

        if (!recipientHumoPanData.isSuccess()) {
            return new BmResultData<>(BmResultStatus.RECIPIENT_PC_ERROR, null, recipientHumoPanData.getMessage());
        }

        final P2PRequestDTO innerP2PRequestDTO = new P2PRequestDTO();
        innerP2PRequestDTO.setCcyCode(transaction.getSenderCcy());
        innerP2PRequestDTO.setFromPan(senderHumoPanData.getData());
        innerP2PRequestDTO.setFromExpiry(senderExpiry);
        innerP2PRequestDTO.setToPan(recipientHumoPanData.getData());
        innerP2PRequestDTO.setAmount(transaction.getSenderAmount());
        innerP2PRequestDTO.setMerchantId(transaction.getEposMerchant());
        innerP2PRequestDTO.setTerminalId(transaction.getEposTerminal());
        innerP2PRequestDTO.setLanguage((language == null || language.isEmpty()) ? "en" : language);
        innerP2PRequestDTO.setAutoSwitch(1);
        innerP2PRequestDTO.setSwitchingId(String.valueOf(switchingId));
        final String paymentRef = transactionPrefixUtil.getP2pPrefix(transaction.getId());
        innerP2PRequestDTO.setPaymentRef(paymentRef);

        setP2pPaymentRef(transaction, paymentRef);

        Bill bill = new Bill()
                .recipientShortName(HUMO_P2P)
                .type(RequestType.P2P_CREDIT)
                .amountRatio(Constants.AMOUNT_RATIO_TIYIN)
                .amount(amount)
                .account(senderHumoPanData.getData() + "->" + recipientHumoPanData.getData())
                .currency(transaction.getSenderCcy())
                .requestTime(Instant.now())
                .requestJson(mapService.convert(innerP2PRequestDTO))
                .responseTime(Instant.now())
                .login(transaction.getRequestLogin())
                .success(false)
                .transactionId(transaction.getId())
                .outExtId(paymentRef);

        bill = billRepository.save(bill);

        log.debug("Humo.p2p bill saved: {}", bill);

        var p2pResult = humoGatewayService.p2p(innerP2PRequestDTO);

        log.info("Humo.p2p result: {}", p2pResult);

        /////

        if (p2pResult == null) {
            final String details = "No response from SvGate";
            updateErrorBill(transaction.getId(), bill, details);
            return new BmResultData<>(BmResultStatus.PC_ERROR, details);
        } else if (p2pResult.getData() == null) {
            String responseIsEmptyText = updateErrorBill(transaction, bill, p2pResult);
            return new BmResultData<>(BmResultStatus.PC_ERROR, responseIsEmptyText);
        }

        setP2pPaymentId(transaction, p2pResult.getData().getPaymentId());

        if (!p2pResult.isSuccess()) {

            log.warn("#### Humo.p2p Error Response : {}", p2pResult.getMessage());

            if (Objects.equals(p2pResult.getCode(), HumoResult.TIMEOUT.getCode()) || p2pResult.isTimeout()) {
                bill.setResponseTime(Instant.now());
                bill.setResponseText(BmResultStatus.TIMEOUT.name());
                bill.setResponseCode(String.valueOf(BmResultStatus.TIMEOUT.getCode()));
                bill.setSuccess(false);
                billRepository.save(bill);
                return new BmResultData<>(BmResultStatus.TIMEOUT, bill);
            }

            billRepository.save(fillBill(bill, p2pResult.getData()));

            return new BmResultData<>(BmResultStatus.PC_ERROR, bill, p2pResult.getMessage());
        }

        if (!p2pResult.getData().isSuccess()) {
            billRepository.save(fillBill(bill, p2pResult.getData()));

            return new BmResultData<>(BmResultStatus.PC_ERROR, bill, "Wrong action code");
        }

        log.info("#### Humo direct p2p Success Response : \n{}\n", p2pResult.getData());

        billRepository.save(fillBill(bill, p2pResult.getData()));

        return new BmResultData<>(bill);
    }

    public BmResultData<Bill> p2pConfirm(Transaction transaction) {

        DebitConfirmRequestDTO debitConfirmRequestDTO = new DebitConfirmRequestDTO();
        debitConfirmRequestDTO.setPaymentId(getP2pPaymentId(transaction));
        debitConfirmRequestDTO.setPaymentRef(getP2pPaymentRef(transaction));

        Map<String, Object> requestJson = mapService.convert(debitConfirmRequestDTO);

        requestJson.put(Constants.AMOUNT_PARAM, String.valueOf(transaction.getSenderAmount()));
        requestJson.put(Constants.CCY_PARAM, String.valueOf(Constants.UZS_CURRENCY));
        requestJson.put(Constants.POINT_CODE_PARAM, applicationProperties.getHumo().getVirtualPointCode());

        Bill bill = new Bill()
                .recipientShortName(HUMO_P2P_CONFIRM)
                .account(transaction.getSenderAccount())
                .amount(transaction.getSenderAmount())
                .amountRatio(Constants.AMOUNT_RATIO_TIYIN)
                .currency(Constants.UZS_CURRENCY)
                .type(RequestType.CONFIRM)
                .login(transaction.getRequestLogin())
                .requestJson(requestJson)
                .requestTime(Instant.now())
                .responseTime(Instant.now())
                .transactionId(transaction.getId());

        bill = billRepository.save(bill);
        log.info("Humo p2p confirm bill saved: {}", bill);

        HasData<DebitResultDTO> simpleResultDataConfirm = commonGatewaySender.sendAndReceive(debitConfirmRequestDTO, bill, humoGatewayService::confirmDualMessageDebit, new ResultChecker<>() {
            @Override
            public boolean success(DebitResultDTO debitConfirmResult) {
                return Objects.equals(debitConfirmResult.getAuthActionCode(), SUCCESS_AUTH_ACTION_CODE) && (debitConfirmResult.getAction() == DEBIT_CONFIRM_SUCCESS_ACTION);
            }

            @Override
            public void beforeSaveBillResult(Bill bill, DebitResultDTO result) {
                bill
                        .responseCode(String.valueOf(result.getAction()))
                        .extId(String.valueOf(result.getPaymentId()));
            }
        }, this::returnException);

        transaction
                .senderDate(bill.getRequestTime())
                .senderStatus(bill.getResponseCode())
                .senderStatusText(bill.getResponseText())
                .senderBillId(bill.getId());

        return new BmResultData<>(bill.isSuccess() ? BmResultStatus.SUCCESS : BmResultStatus.PC_ERROR, bill);
    }

    public String getP2pPaymentRef(Transaction transaction) {
        return transaction.getSenderTrId();
    }

    public String getP2pPaymentId(Transaction transaction) {
        return transaction.getRecipientTrId();
    }

    public Transaction setP2pPaymentRef(Transaction transaction, String paymentRef) {
        return transaction.senderTrId(paymentRef);
    }

    public Transaction setP2pPaymentId(Transaction transaction, String paymentId) {
        return transaction.recipientTrId(paymentId);
    }

    protected Bill fillBill(Bill bill, P2PResultDTO p2PResultDTO) {
        if (p2PResultDTO == null) {
            return bill
                    .responseJson(Collections.singletonMap("error", "p2PResultDTO is null"))
                    .responseTime(Instant.now())
                    .responseText("p2PResultDTO is null")
                    .responseCode("-1")
                    .extId("-1")
                    .success(false);
        }

        return bill
                .responseJson(MapUtil.convert(p2PResultDTO))
                .responseTime(Instant.now())
                .responseText(String.valueOf(p2PResultDTO.getAction()))
                .responseCode(String.valueOf(p2PResultDTO.getAction()))
                .extId(p2PResultDTO.getPaymentId())
                .success(true);
    }

    private String updateErrorBill(Transaction transaction, Bill bill, HasData<?> bmResult) {

        log.warn("#### Humo Response is NULL for transaction : {}", transaction.getId());

        String responseIsEmptyText = "Humo p2p response is empty" + (bmResult != null ? " : " + bmResult.getDetails() : "");

        bill
                //                    .responseJson(Collections.singletonMap("error", responseIsEmptyText))
                .responseTime(Instant.now())
                .responseText(responseIsEmptyText)
                .responseCode("Empty response")
                .success(false);

        billRepository.save(bill);
        return responseIsEmptyText;
    }

    private void updateErrorBill(Long tranId, Bill bill, String errorText) {

        log.warn("#### Humo.p2p response error : {} : {}", tranId, errorText);

        bill
                //                    .responseJson(Collections.singletonMap("error", responseIsEmptyText))
                .responseTime(Instant.now())
                .responseText(errorText)
                .responseCode("FAILED")
                .success(false);

        billRepository.save(bill);
    }

    public HasData<String> getHumoPan(String tokenOrPan) {

        if (pcHelper.isValidPan(tokenOrPan, P2pProcessorType.HUMO)) {

            return new BmResultData<>(tokenOrPan);
        } else if (pcHelper.isValidToken(tokenOrPan, P2pProcessorType.HUMO)) {
            try {
                final BmResultData<String> realPan = getRealPan(tokenOrPan);

                if (realPan == null) {
                    return new BmResultData<>(BmResultStatus.PC_ERROR, "Humo getRealPan failed, bmResult is null");
                }

                if (!realPan.isSuccess()
                        || Objects.isNull(realPan.getData())
                        || !pcHelper.isValidPan(realPan.getData(), P2pProcessorType.HUMO)
                ) {
                    log.warn("Error when getting realPan from Humo : " + realPan.getMessage());
                    return new BmResultData<>(realPan);
                }
                log.debug("Fetched humo.realPan: {}", realPan.getData());
                return new BmResultData<>(realPan.getData());
            } catch (Exception e) {
                log.warn("Exception when getting real senderPan: {}", e.getLocalizedMessage());
                return new BmResultData<>(BmResultStatus.RECIPIENT_PC_ERROR, null, e.getLocalizedMessage());
            }
        }

        return new BmResultData<>(BmResultStatus.NOT_FOUND, null, "Real pan not found for : " + tokenOrPan);
    }

    private String getStringParam(SenderAdditionalParam additionalParam) {
        return mapService.convertToString(additionalParam, true);
    }

    public BankCentreIdList getCentreIdList() {
        log.info("!!! Get centre id list !!!");
        var path = System.getProperty("user.dir") + File.separator + "docs" + File.separator + "humo" + File.separator + "BIN.xml";
        log.info("path to simulation file : {}", path);

        var file = new File(path);

        BankCentreIdList bankCentreIdList;
        try {
            var jaxbContext = JAXBContext.newInstance(BankCentreIdList.class);
            Unmarshaller unmarshaller;
            unmarshaller = jaxbContext.createUnmarshaller();

            bankCentreIdList = (BankCentreIdList) unmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            log.error("Exception when unmarshalling: {}", e.getMessage());
            return new BankCentreIdList();
        }

        log.info("Centre list size: {}", bankCentreIdList.getBankCentreIds().size());
        return bankCentreIdList;
    }

    public String getCentreId(String pan) {
        log.debug("Get centre id by card bin: {}", pan);

        HumoCardType humoCardType = getHumoCardType(pan);
        String cardBin = "";
        switch (humoCardType) {
            case MASTER_CARD: {
                if (StringUtils.isNumeric(StringUtils.substring(pan, 6, 8))) {
                    //If 'pan' already real pan, return 8 symbols
                    cardBin = StringUtils.substring(pan, 0, 8);
                    break;
                }

                //if 'pan' is token, get real pan
                BmResultData<String> realPanResult = getRealPan(pan);

                if (!realPanResult.isSuccess()) {
                    log.debug("Get real pan error: {}", realPanResult.getMessage());
                    break;
                }

                cardBin = StringUtils.substring(realPanResult.getData(), 0, 8);
                break;
            }
            case DEFAULT: {
                cardBin = StringUtils.substring(pan, 0, 6);
                break;
            }
            default: {
                log.warn("Unknown card bin");
                cardBin = StringUtils.substring(pan, 0, 6);
            }
        }

        return centreIds.get(cardBin);
    }

    public HumoCardType getHumoCardType(String pan) {
        if (MASTER_CARD_BIN.equals(StringUtils.substring(pan, 0, 6))) {
            log.debug("Card type MASTER_CARD: {}", pan);
            return HumoCardType.MASTER_CARD;
        }
        log.debug("Card type DEFAULT: {}", pan);
        return HumoCardType.DEFAULT;
    }

    public P2pRecoSummaryResult summaryReconcile(PcTranType pcTranType, Instant fromDate, Instant toDate) {
        return reconcileHelperService.reconcileSummary(pcTranType, fromDate, toDate);
    }
}
