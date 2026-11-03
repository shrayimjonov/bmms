package uz.paykap.sender.impl;

import java.time.Instant;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.dto.request.CommonPayRequest;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.devops.humo.service.HumoGatewayService;
import uz.devops.humo.service.dto.DebitConfirmRequestDTO;
import uz.devops.humo.service.dto.DebitCreateRequestDTO;
import uz.devops.humo.service.dto.DebitResultDTO;
import uz.devops.humo.service.result.HumoResult;
import uz.devops.humoMiddleware.dto.response.RetrieveCardV3ResponseDTO;
import uz.devops.humoMiddleware.dto.basic.CardDataInfo;
import uz.devops.humoMiddleware.dto.basic.RecordInfo;
import uz.devops.humoMiddleware.dto.request.GetCardInfoRequestDTO;
import uz.devops.humoMiddleware.dto.response.GetCardInfoResponseDTO;
import uz.devops.humoMiddleware.service.HumoMiddlewareService;
import uz.javlon.commons.base.ResultData;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.javlon.commons.result.CommonResult;
import uz.javlon.commons.result.CommonResultData;
import uz.javlon.commons.result.HasData;
import uz.javlon.commons.result.HasResult;
import uz.javlon.commons.util.CardUtil;
import uz.javlon.commons.util.PhoneUtil;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.config.Constants;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.gateway.dto.PcSetting;
import uz.paykap.gateway.humo.HumoUtils;
import uz.paykap.repository.BillRepository;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.sender.SenderService;
import uz.paykap.sender.dto.SenderOtpParams;
import uz.paykap.service.GenerateNumberService;
import uz.paykap.service.HumoMiddlewareHelperService;
import uz.paykap.service.MapService;
import uz.paykap.service.PcHelper;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.humo.HumoP2pHelperService;
import uz.paykap.utils.TransactionPrefixGenerator;

@Slf4j
@Service(HumoSenderService.NAME)
@AllArgsConstructor
public class HumoSenderService extends SenderService {

    public static final String NAME = "humoSenderServiceImpl";
    public static final int DEBIT_CONFIRM_SUCCESS_ACTION = 10;
    public static final String SUCCESS_AUTH_ACTION_CODE = "000";
    public static final String HUMO_DEBIT_CREATE = "humo-debit-create";
    public static final String HUMO_HOLD_CREATE = "humo-hold-create";
    public static final String HUMO_DEBIT_CONFIRM = "humo-debit-confirm";
    public static final String CONFIRM_REF_PREFIX = "Confirm_";
    public static final String CREATE_REF_PREFIX = "Create_";

    @Override
    public HasData<Transaction> debit(CommonPayRequest payDTO, Transaction transaction, String agentLogin) {
        log.info("Humo debit begin, payDTO: {}, transaction: {}, agentLogin: {}", payDTO, transaction, agentLogin);

        //TODO - check, done for ignore duplicated debits
        if (Objects.equals(transaction.getSenderGateway(), HUMO_DEBIT_CREATE) && transaction.isSenderSuccess()) {
            return new CommonResultData<>(transaction);
        }

        if (payDTO.getParams() == null) {
            return new CommonResultData<>(BmResultStatus.NOT_FOUND, null, "params");
        }

        HasData<Transaction> OBJECT_NOT_FOUND = findAndSetEpos(payDTO, transaction);
        if (OBJECT_NOT_FOUND != null) return OBJECT_NOT_FOUND;

        ResultData<Bill> holdResult = hold(payDTO, transaction, agentLogin, HUMO_DEBIT_CREATE);
        log.info("Inner debit-hold result: {}", holdResult);

        if (!holdResult.isSuccess()) {
            if (holdResult.getData() != null) {
                Bill holdBill = holdResult.getData();
                transaction
                    .senderAccount(holdBill.getAccount())
                    .senderAmount(holdBill.getAmount())
                    .senderCcy(transaction.getRecipientCcy())
                    .senderDate(Instant.now())
                    .senderGateway(PayType.HUMO.name())
                    .senderStatus(holdBill.getResponseCode())
                    //TODO new field for centreId from humo
                    .senderStatusText(String.valueOf(holdBill.getResponseJson().get(Constants.REFERENCE_NUMBER_PARAM)))
                    .senderTrId(holdBill.getExtId())
                    .senderBillId(holdBill.getId());
            }
            //            return new ResultData<>(holdResult.getException());
            return new CommonResultData<>(
                BmResultStatus.SENDER_PC_ERROR,
                null,
                holdResult.getException() != null ? holdResult.getException().getLocalizedMessage() : "unknown error"
            );
        }

        Bill holdBill = holdResult.getData();

        transaction
            .senderAccount(holdBill.getAccount())
            .senderAmount(holdBill.getAmount())
            .senderCcy(transaction.getRecipientCcy())
            .senderDate(Instant.now())
            .senderGateway(PayType.HUMO.name())
            .senderStatus(holdBill.getResponseCode())
            //TODO new field for centreId from humo
            .senderStatusText(String.valueOf(holdBill.getResponseJson().get(Constants.REFERENCE_NUMBER_PARAM)))
            .senderTrId(holdBill.getExtId())
            .senderBillId(holdBill.getId());
        if (!holdBill.isSuccess()) {
            transaction.setStatus(TransactionStatus.FAILED);
        }

        return new CommonResultData<>(transaction);
    }

    @Override
    public ResultData<Transaction> reverse(PayType payType, Transaction transaction, String agentLogin) {
        humoHelperService.reverse(transaction, agentLogin, transaction.getEposMerchant(), transaction.getEposTerminal(), true, false);

        return new ResultData<>(transaction);
    }

    @Override
    public HasData<Transaction> debitConfirm(CommonPayRequest payDTO, Transaction transaction, String agentLogin) {
        //TODO - check, done for ignore duplicated debits
        if (Objects.equals(transaction.getSenderGateway(), HUMO_DEBIT_CONFIRM) && transaction.isSenderSuccess()) {
            return new CommonResultData<>(transaction);
        }

        if (payDTO.getParams() == null) {
            return new CommonResultData<>(BmResultStatus.NOT_FOUND, null, "params");
        }

        ResultData<Bill> billResultData = confirm(payDTO, transaction);

        if (!billResultData.isSuccess()) {
            transaction.setSenderSuccess(false);
            return new CommonResultData<>(
                BmResultStatus.SENDER_PC_ERROR,
                null,
                billResultData.getException() != null ? billResultData.getException().getLocalizedMessage() : "unknown error"
            );
        }

        Bill senderBill = billResultData.getData();

        transaction
            .senderAccount(senderBill.getAccount())
            .senderAmount(senderBill.getAmount())
            .senderCcy(transaction.getRecipientCcy())
            .senderDate(Instant.now())
            .senderGateway(PayType.HUMO.name())
            .senderStatus(senderBill.getResponseCode())
            //TODO new field for centreId from humo
            //            .senderStatusText(senderBill.getRequestJson().get(CENTRE_ID))
            .senderSuccess(senderBill.isSuccess())
            .senderTrId(senderBill.getExtId())
            .senderBillId(senderBill.getId()); //            .sender
        if (!senderBill.isSuccess()) {
            transaction.setStatus(TransactionStatus.FAILED);
        }
        transaction.setSenderStatusText(TransactionStatus.SUCCESS.name());
        return new CommonResultData<>(transaction);
    }

    @Override
    public HasData<Transaction> holdForAgent(CommonPayRequest payDTO, Transaction transaction, String agentLogin) {
        HasData<Transaction> OBJECT_NOT_FOUND = findAndSetEpos(payDTO, transaction);
        if (OBJECT_NOT_FOUND != null) return OBJECT_NOT_FOUND;

        if (
            payDTO.getParams() == null ||
            payDTO.getParams().get(Constants.PAN_PARAM) == null ||
            payDTO.getParams().get(Constants.EXPIRY_PARAM) == null
        ) {
            throw new CodedException(ProblemEnum.INVALID_PARAMS, "Не найден номер карты или срок");
        }
        transaction.setSenderAccount(String.valueOf(payDTO.getParams().get(Constants.PAN_PARAM)));
        String centreId = humoP2pHelperService.getCentreId(transaction.getSenderAccount());
        payDTO.getParams().put(Constants.CENTRE_ID_PARAM, centreId);

        ResultData<Bill> hold = hold(payDTO, transaction, agentLogin, HUMO_HOLD_CREATE);
        if (!hold.isSuccess()) {
            return new CommonResultData<>(
                BmResultStatus.SENDER_PC_ERROR,
                null,
                hold.getException() != null ? hold.getException().getLocalizedMessage() : "unknown error"
            );
        }
        Bill holdBill = hold.getData();

        transaction
            .senderAccount(holdBill.getAccount())
            .senderAmount(holdBill.getAmount())
            .senderDate(Instant.now())
            .senderGateway(PayType.HUMO.name())
            .senderStatus(holdBill.getResponseCode())
            //TODO new field for centreId from humo
            .senderStatusText(String.valueOf(holdBill.getResponseJson().get(Constants.REFERENCE_NUMBER_PARAM)))
            .senderTrId(holdBill.getExtId())
            .senderBillId(holdBill.getId())
            .status(TransactionStatus.HOLD_SUCCESS);
        if (!holdBill.isSuccess()) {
            transaction.setStatus(TransactionStatus.HOLD_ERROR);
        }

        transactionRepository.save(transaction);
        return new CommonResultData<>(transaction);
    }

    private HasData<Transaction> findAndSetEpos(CommonPayRequest payDTO, Transaction transaction) {
        Optional<PcSetting> pcSetting = setEpos(payDTO, transaction);
        if (pcSetting.isEmpty()) {
            transaction.setSenderStatusText("Epos setting not found");
            return new CommonResultData<>(BmResultStatus.NOT_FOUND, null, "Epos setting not found");
        }
        return null;
    }

    @Override
    public HasData<Bill> reverseHold(Transaction transaction, String login) {
        return humoHelperService.reverse(transaction, login, transaction.getEposMerchant(), transaction.getEposTerminal(), true, true);
    }

    @Override
    public HasData<Transaction> holdConfirm(CommonPayRequest payDTO, Transaction transaction, String agentLogin) {
        return debitConfirm(payDTO, transaction, agentLogin);
    }

    @Override
    public PayType getSupportedPayType() {
        return PayType.HUMO;
    }

    @Override
    public HasData<SenderOtpParams> getOtpParams(String pan, String expiry) {

        var otpParamsData = humoMiddlewareHelperService.retrieveCardV3(pan);
        if (!otpParamsData.isSuccess() || otpParamsData.getData() == null || otpParamsData.getData() == null) {
            return new CommonResultData<>(BmResultStatus.SENDER_PC_ERROR, null, "get humo card error");
        }

        RetrieveCardV3ResponseDTO result = otpParamsData.getData();

        if (result == null || result.getMb() == null || StringUtils.isEmpty(result.getMb().getPhone())) {
            return new CommonResultData<>(BmResultStatus.INVALID_STATUS, null, "SMS_INFO_IS_OFF for card");
        }

        SenderOtpParams otpParams = new SenderOtpParams();
        otpParams.setMaskedCardInfo(CardUtil.maskIfBankCard(pan));
        otpParams.setPhone(PhoneUtil.getValidPhone(result.getMb().getPhone()));

        return new CommonResultData<>(otpParams);
    }

    @Override
    public HasResult canAgentProceed(PayType payType, String agentLogin, CommonPayRequest payDTO, Long amount) {

        log.info("Can agent proceed, payType: {}, agentLogin: {}, payDTO: {}, amount: {}", payType, agentLogin, payDTO, amount);

        if (payDTO.getParams() == null) {
            return new CommonResult(BmResultStatus.INVALID_PARAMS, "params is null");
        }

        String pan = String.valueOf(payDTO.getParams().getOrDefault(Constants.PAN_PARAM, payDTO.getParams().get(Constants.TOKEN_PARAM)));

        if (pan == null || pan.isEmpty()) {
            return new CommonResult(BmResultStatus.INVALID_PARAMS, "params.pan is null or empty");
        }

        //        try {
        // Accepts real or pseudo pan
        String realPan;

        if (pcHelper.isValidPan(pan, P2pProcessorType.HUMO)) {
            realPan = pan;
        } else if (pcHelper.isValidToken(pan, P2pProcessorType.HUMO)) {
            final BmResultData<String> realPanResult = humoHelperService.getRealPan(pan);

            if (realPanResult == null) {
                return new CommonResult(BmResultStatus.SENDER_PC_ERROR, "Humo getRealPan failed, bmResult is null");
            }

            if (
                !realPanResult.isSuccess() ||
                Objects.isNull(realPanResult.getData()) ||
                !pcHelper.isValidPan(realPanResult.getData(), P2pProcessorType.HUMO)
            ) {
                log.warn("Error when getting realPan from Humo : " + realPanResult.getMessage());
                return new CommonResult(BmResultStatus.SENDER_PC_ERROR, realPanResult.getDetails());
            }

            realPan = realPanResult.getData();
        } else {
            return new CommonResult(BmResultStatus.INVALID_PARAMS, "Invalid humo pan or token given");
        }

        var middlewareSimpleResultData = humoMiddlewareHelperService.retrieveCardV3(realPan);

        var retrieveCardV3Response = middlewareSimpleResultData.getData();
        if (
            retrieveCardV3Response != null &&
            retrieveCardV3Response.getBalance() != null &&
            amount != null &&
            retrieveCardV3Response.getBalance().getAvailableAmount() >= amount
        ) {
            payDTO.getParams().put(Constants.CENTRE_ID_PARAM, retrieveCardV3Response.getCard().getInstitutionId());
            payDTO.getParams().put(Constants.EXPIRY_PARAM, retrieveCardV3Response.getCard().getExpiry());
            payDTO.getParams().put(Constants.PAN_PARAM, realPan);
            return HasResult.SUCCESS;
        } else {
            return new CommonResult(BmResultStatus.INSUFFICIENT_FUNDS_ON_SENDER, "Недостаточно средств на депозите");
        }
    }

    private ResultData<Bill> hold(CommonPayRequest payDTO, Transaction transaction, String agentLogin, String recipientShortName) {
        log.info("Hold resources in humo, payDTO: {},\n transaction: {},\n agentLogin: {}", payDTO, transaction, agentLogin);
        if (StringUtils.isEmpty(transaction.getEposMerchant())) {
            throw new CodedException(ProblemEnum.INVALID_PARAMS, "transaction.eposMerchant=null");
        }

        if (StringUtils.isEmpty(transaction.getEposTerminal())) {
            throw new CodedException(ProblemEnum.INVALID_PARAMS, "transaction.eposTerminal=null");
        }

        if (payDTO.getParams() == null || payDTO.getParams().get(Constants.PAN_PARAM) == null) {
            throw new CodedException(ProblemEnum.INVALID_PARAMS, Constants.PAN_PARAM);
        }
        String pan = String.valueOf(payDTO.getParams().get(Constants.PAN_PARAM));
        log.debug("Pan resolved for hold: {}", pan);

        if (payDTO.getParams().get(Constants.EXPIRY_PARAM) == null) {
            throw new CodedException(ProblemEnum.INVALID_PARAMS, Constants.EXPIRY_PARAM);
        }
        String expire = String.valueOf(payDTO.getParams().get(Constants.EXPIRY_PARAM));
        log.debug("Expire: {}", expire);

        if (payDTO.getParams().get(Constants.CENTRE_ID_PARAM) == null) {
            throw new CodedException(ProblemEnum.INVALID_PARAMS, Constants.CENTRE_ID_PARAM);
        }
        String centreId = String.valueOf(payDTO.getParams().get(Constants.CENTRE_ID_PARAM));
        log.debug("Centre id: {}", centreId);

        log.info("#### Humo debit create####: \n{}\n", payDTO);

        String paymentRef = transactionPrefixUtil.getSenderDebitPrefix(transaction.getId());

        Map<String, Object> requestJson = new HashMap<>();
        requestJson.put(Constants.AMOUNT_PARAM, String.valueOf(transaction.getSenderAmount()));
        requestJson.put(Constants.CCY_PARAM, String.valueOf(Constants.UZS_CURRENCY));
        requestJson.put(Constants.CENTRE_ID_PARAM, centreId);
        requestJson.put(Constants.EXPIRY_PARAM, expire);
        requestJson.put(Constants.PAN_PARAM, pan);
        requestJson.put(Constants.POINT_CODE_PARAM, applicationProperties.getHumo().getVirtualPointCode());
        requestJson.put(Constants.PAYMENT_REF, paymentRef);

        Bill bill = new Bill()
            .recipientShortName(recipientShortName)
            .account(String.valueOf(payDTO.getParams().get(Constants.PAN_PARAM)))
            .amount(transaction.getSenderAmount())
            .amountRatio(100)
            .currency(Constants.UZS_CURRENCY)
            .requestJson(requestJson)
            .type(RequestType.PAY)
            .login(agentLogin)
            .requestTime(Instant.now())
            .responseTime(Instant.now())
            .transactionId(transaction.getId())
            .outExtId(transactionPrefixUtil.getSenderDebitPrefix(transaction.getId())); //

        bill = billRepository.save(bill);
        transaction.setSenderTrId(paymentRef);
        transactionRepository.save(transaction);

        log.debug("Debit creating from card : {}", HumoUtils.mask(pan));

        try {
            // 1. Debit create
            DebitCreateRequestDTO debitCreateRequestDTO = new DebitCreateRequestDTO();
            debitCreateRequestDTO.setAmount(transaction.getSenderAmount());
            debitCreateRequestDTO.setCcyCode(Constants.UZS_CURRENCY);
            //centreID == institutionId"
            debitCreateRequestDTO.setCentreID(centreId);
            debitCreateRequestDTO.setExpDate(expire);
            debitCreateRequestDTO.setPan(pan);
            debitCreateRequestDTO.setPointCode(applicationProperties.getHumo().getVirtualPointCode());
            debitCreateRequestDTO.setTerminalId(transaction.getEposTerminal());
            debitCreateRequestDTO.setMerchantId(transaction.getEposMerchant());
            debitCreateRequestDTO.setSessionId(generateNumberService.humoSessionId(NAME, CREATE_REF_PREFIX + bill.getId()));
            debitCreateRequestDTO.setPaymentRef(bill.getOutExtId());

            //todo HUMO-DEBIT-OPTIMIZE: HumoSenderService.innerDebit(), HumoSenderService.hold(), HumoDebitPayService.debit(), HumoP2pHelperService.hold() methodlari bir xil ish qilyapdi
            HasData<DebitResultDTO> simpleResultData = humoGatewayService.createDualMessageDebit(debitCreateRequestDTO);
            log.debug("Debit create result : {}", simpleResultData);

            if (!simpleResultData.isSuccess()) {
                String detailedException = simpleResultData.getMessage();

                bill
                    .responseJson(Collections.singletonMap("error", detailedException))
                    .responseTime(Instant.now())
                    .responseText(detailedException)
                    .responseCode("FAILED")
                    .success(false);

                bill = billRepository.save(bill);
                if (simpleResultData.isTimeout()) {
                    ResultData<Bill> timeoutResultData = new ResultData<>(false, bill, new CodedException(ProblemEnum.TIMEOUT));
                    timeoutResultData.getData().setResponseText(BmResultStatus.TIMEOUT.name());
                    timeoutResultData.getData().setResponseCode(String.valueOf(BmResultStatus.TIMEOUT.getCode()));
                    return timeoutResultData;
                }
                return new ResultData<>(false, bill, new CodedException(ProblemEnum.BILLING_ERROR, detailedException));
            }

            DebitResultDTO debitCreateResultDTO = simpleResultData.getData();
            // если ответ неуспешен
            if (debitCreateResultDTO.getAction() != 4 || debitCreateResultDTO.getPaymentId() == null) {
                log.warn("#### Humo error when creating debit, response : {}", debitCreateResultDTO);
                bill
                    .responseJson(Collections.singletonMap(Constants.ACTION_CODE_PARAM, String.valueOf(debitCreateResultDTO.getAction())))
                    .responseTime(Instant.now())
                    .responseText("payment id: " + debitCreateResultDTO.getPaymentId())
                    .responseCode(String.valueOf(debitCreateResultDTO.getAction()))
                    .success(false);

                return new ResultData<>(billRepository.save(bill));
            }

            final Map<String, Object> responseJson = mapService.convert(debitCreateResultDTO);
            responseJson.put(Constants.REFERENCE_NUMBER_PARAM, debitCreateResultDTO.getAuthRefNumber());

            bill
                .responseJson(responseJson)
                .responseTime(Instant.now())
                .responseText("Success")
                .responseCode(String.valueOf(debitCreateResultDTO.getAction()))
                .extId(String.valueOf(debitCreateResultDTO.getPaymentId()))
                .success(true);

            transaction.setSenderTrId(debitCreateResultDTO.getPaymentId());
            transactionRepository.save(transaction);
        } catch (Exception e) {
            log.warn("#### Humo Hold Error Response : {}", e.getLocalizedMessage());

            bill
                .responseText(StringUtils.substring(e.getLocalizedMessage(), 0, 126))
                .responseJson(Collections.singletonMap("error", e.getLocalizedMessage()))
                .responseTime(Instant.now())
                .responseCode("FAILED")
                .success(false);
        }

        return new ResultData<>(billRepository.save(bill));
    }

    private ResultData<Bill> confirm(CommonPayRequest payDTO, Transaction transaction) {
        log.info("Confirm humo debit, payDTO: {},\n transaction: {},\n agentLogin: {}", payDTO, transaction, transaction.getRequestLogin());

        Map<String, Object> requestJson = new HashMap<>();
        requestJson.put(Constants.AMOUNT_PARAM, String.valueOf(transaction.getSenderAmount()));
        requestJson.put(Constants.CCY_PARAM, String.valueOf(Constants.UZS_CURRENCY));
        requestJson.put(Constants.POINT_CODE_PARAM, applicationProperties.getHumo().getVirtualPointCode());

        Bill bill = new Bill()
            .recipientShortName(HUMO_DEBIT_CONFIRM)
            .account(HumoUtils.mask(String.valueOf(payDTO.getParams().get(Constants.PAN_PARAM))))
            .amount(transaction.getSenderAmount())
            .amountRatio(100)
            .currency(Constants.UZS_CURRENCY)
            .requestJson(requestJson)
            .type(RequestType.PAY)
            .login(transaction.getRequestLogin())
            .requestTime(Instant.now())
            .responseTime(Instant.now())
            .transactionId(transaction.getId()); //

        DebitConfirmRequestDTO debitConfirmRequestDTO = new DebitConfirmRequestDTO();
        debitConfirmRequestDTO.setSessionId(generateNumberService.humoSessionId(NAME, CONFIRM_REF_PREFIX + bill.getId()));
        debitConfirmRequestDTO.setPaymentRef(generateNumberService.humoPaymentRef(CREATE_REF_PREFIX + transaction.getId()));
        debitConfirmRequestDTO.setPaymentId(transaction.getSenderTrId());

        requestJson.putAll(mapService.convert(debitConfirmRequestDTO));

        bill = billRepository.save(bill);
        log.info("Bill saved: {}", bill);

        try {
            HasData<DebitResultDTO> simpleResultDataConfirm = humoGatewayService.confirmDualMessageDebit(debitConfirmRequestDTO);
            log.debug("Debit confirm result: {}", simpleResultDataConfirm);

            if (!simpleResultDataConfirm.isSuccess()) {
                String detailedException = simpleResultDataConfirm.getMessage();

                bill
                    .responseJson(Collections.singletonMap("error", detailedException))
                    .responseTime(Instant.now())
                    .responseText(detailedException)
                    .responseCode("FAILED")
                    .success(false);

                bill = billRepository.save(bill);

                return new ResultData<>(new CodedException(ProblemEnum.BILLING_ERROR, detailedException));
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

                return new ResultData<>(billRepository.save(bill));
            }

            bill
                .responseJson(mapService.convert(debitConfirmResult))
                .responseTime(Instant.now())
                .responseText(debitConfirmResult.getPaymentId())
                .responseCode(debitConfirmResult.getAuthActionCode())
                .extId(String.valueOf(debitConfirmResult.getPaymentId()))
                .success(true);
        } catch (Exception e) {
            log.warn("#### Humo Error Response : {}", e.getLocalizedMessage());

            bill
                //                    .responseText(StringUtils.substring(e.getLocalizedMessage(), 0, 126))
                .responseText(e != null ? e.getLocalizedMessage() : "unknown error")
                .responseJson(Collections.singletonMap("error", e.getLocalizedMessage()))
                .responseTime(Instant.now())
                .responseCode("FAILED")
                .success(false);
        }

        return new ResultData<>(billRepository.save(bill));
    }

    private final PcHelper pcHelper;
    private final MapService mapService;
    private final BillRepository billRepository;
    private final HumoP2pHelperService humoHelperService;
    private final HumoGatewayService humoGatewayService;
    private final ApplicationProperties applicationProperties;
    private final GenerateNumberService generateNumberService;
    private final TransactionPrefixGenerator transactionPrefixUtil;
    private final TransactionRepository transactionRepository;
    private final HumoP2pHelperService humoP2pHelperService;
    private final HumoMiddlewareHelperService humoMiddlewareHelperService;

    @Override
    public String getDescription() {
        return "Снятие средств с карт HUMO";
    }
}
