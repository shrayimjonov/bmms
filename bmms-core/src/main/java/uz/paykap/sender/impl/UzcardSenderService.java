package uz.paykap.sender.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.dto.request.CommonPayRequest;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.javlon.commons.base.ResultData;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.javlon.commons.result.CommonResult;
import uz.javlon.commons.result.CommonResultData;
import uz.javlon.commons.result.HasData;
import uz.javlon.commons.result.HasResult;
import uz.javlon.svgate.dto.BaseCardDTO;
import uz.javlon.svgate.dto.TranDTO;
import uz.javlon.svgate.dto.enumeration.PurposeType;
import uz.javlon.svgate.dto.enumeration.TranStatus;
import uz.javlon.svgate.dto.enumeration.TranType;
import uz.javlon.svgate.dto.rpc.request.ConfirmHoldRequestDTO;
import uz.javlon.svgate.dto.rpc.request.DismissHoldRequestDTO;
import uz.javlon.svgate.dto.rpc.response.SvHoldResponse;
import uz.javlon.svgate.dto.sv.SvCardV2DTO;
import uz.javlon.svgate.service.SvGateCardServiceV2;
import uz.javlon.svgate.service.SvGateHoldService;
import uz.javlon.svgate.service.SvGatePaymentServiceV2;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Recipient;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.event.dto.PartnerBillingErrorEvent;
import uz.paykap.gateway.dto.PcSetting;
import uz.paykap.gateway.uzcard.UzcardDebitGatewayServiceV3;
import uz.paykap.repository.BillRepository;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.sender.SenderService;
import uz.paykap.sender.dto.SenderOtpParams;
import uz.paykap.sender.dto.SenderStatusResponse;
import uz.paykap.sender.dto.SvgateParam;
import uz.paykap.service.MapService;
import uz.paykap.service.PcHelper;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.uzcard.UzcardHelperService;
import uz.paykap.utils.TransactionPrefixGenerator;

import java.time.Instant;
import java.util.*;

import static uz.paykap.config.Constants.*;

@Service(UzcardSenderService.UZCARD_SENDER_BEAN_NAME)
@Slf4j
@AllArgsConstructor
public class UzcardSenderService extends SenderService {

    public static final String UZCARD_SENDER_BEAN_NAME = "uzcardSenderServiceImpl";
    public static final String UZ_CARD_HOLD_REVERSE = "uzcard-hold-reverse";
    public static final int HOLD_REVERSED = 0;
    public static final String RECIPIENT_SHORT_NAME = "uzcard-hold-confirm";
    public static final String IS_NULL_MESSAGE = "sv response is null";
    public static final String TIMEOUT = "-408";
    public static final String PAYMENT_PURPOSE = "paymentPurpose";
    public static final String SVGATE = "svgate";
    private final RecipientRepository recipientRepository;
    private final ObjectMapper objectMapper;

    @Override
    public HasResult canAgentProceed(PayType payType, String agentLogin, CommonPayRequest payDTO, Long amount) {

//        final PaymentProperties.Validation uzcardValidation = paymentProperties.getValidations().get(payType);
//
//        if (uzcardValidation.getCheckExpiry()) {
//            if (payDTO.getParams().containsKey(PAN_PARAM) && payDTO.getParams().containsKey(EXPIRY_PARAM)) {
//                // TODO
//            }
//        }

        return new CommonResult(payType == PayType.UZCARD ? HasResult.SUCCESS : HasResult.UNKNOWN_ERROR); // TODO - Javlon, add normal logic, like, check balance of card
    }

    @Override
    public HasData<Transaction> debit(CommonPayRequest payDTO, Transaction transaction, String agentLogin) {
        // Kiosk sends debit request separately, just save as debited successfully
        //        if (Constants.KIOSK_LOGINS.contains(agentLogin)) {
        //            return getResult4Kiosk(payDTO, transaction);
        //        }

        // TODO - check, done for ignore duplicated debits
        if (Objects.equals(transaction.getSenderGateway(), UzcardDebitGatewayServiceV3.NAME) && transaction.isSenderSuccess()) {
            log.info("Already sent successfully, just return transaction");
            return new CommonResultData<>(transaction);
        }

        Optional<PcSetting> pcSetting = setEpos(payDTO, transaction);

        if (pcSetting.isEmpty()) {
            transaction.setSenderStatusText("Epos setting not found");
            return new CommonResultData<>(BmResultStatus.NOT_FOUND, transaction, "Epos setting not found");
        }

        HasData<Bill> senderBillResultData = innerDebit(payDTO, transaction, agentLogin);

        if (!senderBillResultData.isSuccess()) {
            return new CommonResultData<>(BmResultStatus.SENDER_PC_ERROR, null, senderBillResultData.getMessage());
        }

        Bill senderBill = senderBillResultData.getData();

        if (!senderBill.isSuccess()) {
            return new CommonResultData<>(
                BmResultStatus.SENDER_PC_ERROR,
                null,
                senderBill != null ? senderBill.getResponseText() : "unknown error"
            );
        }

        transaction
            .senderAccount(senderBill.getAccount())
            .senderAmount(senderBill.getAmount())
            .senderCcy(transaction.getRecipientCcy())
            .senderDate(Instant.now())
            .senderGateway(PayType.UZCARD.name())
            .senderStatus(senderBill.getResponseCode())
            .senderStatusText(senderBill.getResponseText())
            .senderSuccess(senderBill.isSuccess())
            .senderTrId(senderBill.getExtId())
            .senderBillId(senderBill.getId());

        return new CommonResultData<>(transaction);
    }

    @Override
    public ResultData<Transaction> reverse(PayType payType, Transaction transaction, String agentLogin) {
        log.info("\n#### Uzcard reverse ####: \n{}\n", payType);

        BmResultData<Bill> reverse = uzcardHelperService.reverse(transaction, true);

        if (reverse == null) {
            log.warn(
                "Error occurred while reversing uzcard transaction={} with card={}",
                transaction.getId(),
                transaction.getSenderAccount()
            );
            return new ResultData<>(false, transaction, new CodedException(ProblemEnum.BILLING_ERROR_REVERSED, "Uzcard response is null"));
        } else if (!reverse.isSuccess()) {
            log.warn(
                "Error occurred while reversing uzcard transaction={} with card={}",
                transaction.getId(),
                transaction.getSenderAccount()
            );
            return new ResultData<>(false, transaction, new CodedException(ProblemEnum.BILLING_ERROR_REVERSED, reverse.getMessage()));
        }

        return new ResultData<>(transaction);
    }

    @Override
    public HasData<Transaction> debitConfirm(CommonPayRequest payDTO, Transaction transaction, String agentLogin) {
        return new CommonResultData<>(transaction);
    }

    @Override
    public HasData<Bill> reverseHold(Transaction transaction, String login) {
        DismissHoldRequestDTO request = new DismissHoldRequestDTO(
            transaction.getSenderTrId(),
            transaction.getEposMerchant(),
            transaction.getEposTerminal(),
            transaction.getSenderAmount()
        );
        Bill bill = createReverseHoldBill(transaction, request);
        var reverseHoldData = svGateHoldService.dismissHold(request);
        return updateReverseHoldBill(bill, reverseHoldData);
    }

    private HasData<Bill> updateReverseHoldBill(Bill bill, HasData<SvHoldResponse> reverseHoldData) {
        bill
            .responseJson(mapService.convert(reverseHoldData))
            .responseTime(Instant.now())
            .responseText(
                reverseHoldData.getData() != null
                    ? reverseHoldData.getData().getDescription()
                    : (!StringUtils.isEmpty(reverseHoldData.getMessage()) ? reverseHoldData.getMessage() : "reverse hold error")
            )
            .responseCode(
                String.valueOf(reverseHoldData.getData() != null ? reverseHoldData.getData().getStatus() : reverseHoldData.getCode())
            )
            .extId(reverseHoldData.getData() != null ? String.valueOf(reverseHoldData.getData().getId()) : "-1")
            .success(
                reverseHoldData.isSuccess() &&
                reverseHoldData.getData() != null &&
                Objects.equals(reverseHoldData.getData().getStatus(), HOLD_REVERSED)
            );
        return new CommonResultData<>(billRepository.save(bill));
    }

    private Bill createReverseHoldBill(Transaction transaction, DismissHoldRequestDTO request) {
        Bill bill = new Bill()
            .requestJson(mapService.convert(request))
            .recipientShortName(UZ_CARD_HOLD_REVERSE)
            .account(transaction.getSenderAccount())
            .amount(transaction.getSenderAmount())
            .amountRatio(100)
            .currency(UZS_CURRENCY)
            .type(RequestType.CANCEL)
            .login(transaction.getRequestLogin())
            .requestTime(Instant.now())
            .responseTime(Instant.now())
            .transactionId(transaction.getId());
        return billRepository.save(bill);
    }

    @Override
    public HasData<SenderStatusResponse> status(final Transaction transaction, String agentLogin) {
        var tranList = paymentService.statusByExtId(String.valueOf(transaction.getId()));

        if (!tranList.isSuccess() || tranList.getData() == null) {
            return new CommonResultData<>(tranList, null, tranList.getMessage());
        }

        boolean paid = false;
        boolean cancelled = false;

        for (TranDTO tranDTO : tranList.getData()) {
            if (tranDTO.getTranType() == TranType.REVERSAL && tranDTO.getStatus() == TranStatus.ROK) {
                cancelled = true;
            }
            if (
                (
                    tranDTO.getTranType() == TranType.CREDIT ||
                    tranDTO.getTranType() == TranType.DEBIT ||
                    tranDTO.getTranType() == TranType.P2P_CREDIT ||
                    tranDTO.getTranType() == TranType.P2P_DEBIT ||
                    tranDTO.getTranType() == TranType.P2P
                ) &&
                tranDTO.getStatus() == TranStatus.OK
            ) {
                paid = true;
            }
        }

        // If paid in SV and not cancelled in SV but transaction.senderSuccess marked as not success, make it success
        if (!cancelled && paid && !transaction.isSenderSuccess()) {
            transaction.senderSuccess(true);
        }

        if (cancelled && Objects.equals(transaction.isSenderSuccess(), true)) {
            transaction.senderSuccess(true);
            transaction.senderStatus(transaction.getSenderStatus() + "->" + "canceled in SV");
            transaction.status(TransactionStatus.SENDER_CANCELLED);
        }

        //        TransactionStatus status;
        //
        //        if (cancelled) {
        //            status = TransactionStatus.SENDER_CANCELLED;
        //        } else if (paid) {
        //            status = TransactionStatus.SUCCESS;
        //        } else {
        //            status = TransactionStatus.FAILED;
        //        }

        return new CommonResultData<>(
            new SenderStatusResponse(
                paid,
                cancelled,
                cancelled && Objects.equals(transaction.isSenderSuccess(), true) ? "canceled in SV but not in DB" : ""
            )
        );
    }

    @Override
    public HasData<Transaction> holdForAgent(CommonPayRequest payDTO, Transaction transaction, String agentLogin) {
        prepareTransactionToHoldRequest(payDTO, transaction, agentLogin);
        var holdResponseData = uzcardHelperService.hold(transaction, payDTO);
        return updateTransaction(transaction, holdResponseData);
    }

    @Override
    public HasData<Transaction> reHold(CommonPayRequest payDTO, Transaction transaction, String agentLogin) {
        final HasData<Bill> reHold = uzcardHelperService.reHold(transaction, payDTO);

        return updateTransaction(transaction, reHold);
    }

    private HasData<Transaction> updateTransaction(Transaction transaction, HasData<Bill> holdResponseData) {
        if (holdResponseData.getData() != null && !StringUtils.isEmpty(holdResponseData.getData().getResponseText())) {
            transaction.setSenderStatusText(holdResponseData.getData().getResponseText());
        }

        if (holdResponseData.isSuccess() && holdResponseData.getData() != null && holdResponseData.getData().isSuccess()) {
            transaction.setSenderStatus(TransactionStatus.HOLD_SUCCESS.name());
            transaction.setStatus(TransactionStatus.HOLD_SUCCESS);
            transaction.setSenderTrId(holdResponseData.getData().getExtId());
            return new CommonResultData<>(transaction);
        }
        if (holdResponseData.getData() != null && Objects.equals(holdResponseData.getData().getResponseCode(), TIMEOUT)) {
            transaction.setStatus(TransactionStatus.HOLD_WAITING);
            transaction.setSenderStatus(TransactionStatus.HOLD_WAITING.name());
            return new CommonResultData<>(BmResultStatus.SENDER_PC_ERROR, transaction);
        }

        transaction.setStatus(TransactionStatus.HOLD_ERROR);
        transaction.setSenderStatus(TransactionStatus.HOLD_ERROR.name());
        return new CommonResultData<>(BmResultStatus.SENDER_PC_ERROR, transaction);
    }

    private Transaction prepareTransactionToHoldRequest(CommonPayRequest payDTO, Transaction transaction, String agentLogin) {
        findAndSetEpos(payDTO, transaction);
        setAmount(payDTO, transaction);
        setSenderAccount(transaction, payDTO);
        return transaction;
    }

    private void setAmount(CommonPayRequest payDTO, Transaction transaction) {
        if (transaction.getSenderAmount() == null) {
            if (payDTO.getAmount() == null) {
                throw new CodedException(ProblemEnum.NOT_FOUND, "sender amount not found");
            }
            transaction.setSenderAmount(payDTO.getAmount());
        }
    }

    private void findAndSetEpos(CommonPayRequest payDTO, Transaction transaction) {
        Optional<PcSetting> pcSetting = setEpos(payDTO, transaction);
        if (pcSetting.isEmpty()) {
            transaction.setSenderStatusText("Epos setting not found");
            throw new CodedException(ProblemEnum.NOT_FOUND, "epos merchant-terminal not found");
        }
        if (StringUtils.isEmpty(transaction.getEposMerchant()) || StringUtils.isEmpty(transaction.getEposTerminal())) {
            throw new CodedException(ProblemEnum.NOT_FOUND, "merchant or terminal not found");
        }
    }

    private void setSenderAccount(Transaction transaction, CommonPayRequest payDTO) {
        if (payDTO.getParams() != null && payDTO.getParams().get(TOKEN_PARAM) != null) {
            transaction.setSenderAccount(String.valueOf(payDTO.getParams().get(TOKEN_PARAM)));
            return;
        }
        if (payDTO.getParams() == null || payDTO.getParams().get(PAN_PARAM) == null || payDTO.getParams().get(EXPIRY_PARAM) == null) {
            throw new CodedException(ProblemEnum.INVALID_PARAMS, "Не найден номер карты или срок");
        }
        var cardInfo = cardService.createCard(
            new BaseCardDTO(String.valueOf(payDTO.getParams().get(PAN_PARAM)), String.valueOf(payDTO.getParams().get(EXPIRY_PARAM)))
        );
        if (!cardInfo.isSuccess() || cardInfo.getData() == null) {
            // todo
            throw new CodedException(ProblemEnum.BILLING_ERROR, "Ошибка в получение токена УзКарт. Описание : " + cardInfo.getMessage());
        }
        transaction.setSenderAccount(cardInfo.getData().getToken());
    }

    @SneakyThrows
    @Override
    public HasData<Transaction> holdConfirm(CommonPayRequest payDTO, Transaction transaction, String agentLogin) {
        if (transaction.getSenderTrId() == null || transaction.getSenderTrId().isEmpty()) {
            transaction.setSenderSuccess(false);
            transaction.setSenderStatusText("Sender hold id is null or empty");
            transaction.setSenderStatus("failed");
            transaction.setSenderDate(Instant.now());
            return new CommonResultData<>(BmResultStatus.INVALID_PARAMS, transaction, "Sender hold id is null or empty");
        }

        ConfirmHoldRequestDTO requestDTO = new ConfirmHoldRequestDTO();
        requestDTO.setHoldId(transaction.getSenderTrId());
        requestDTO.setExt(transactionPrefixGenerator.getSenderDebitPrefix(transaction.getId()));
        requestDTO.setMerchantId(transaction.getEposMerchant());
        requestDTO.setTerminalId(transaction.getEposTerminal());
        //        requestDTO.setPort(1234);
        //        requestDTO.setStan(stanSequence.getNextStan());
        requestDTO.setAmount(transaction.getSenderAmount());
        //        requestDTO.setDate12(ZonedDateTime.now());

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("request", objectMapper.writeValueAsString(requestDTO));

        Bill bill = new Bill()
            .recipientShortName(RECIPIENT_SHORT_NAME)
            .account(transaction.getSenderAccount())
            .amount(transaction.getSenderAmount())
            .amountRatio(100)
            .requestJson(requestMap)
            .currency(transaction.getSenderCcy())
            .type(RequestType.CONFIRM)
            .login(agentLogin)
            .requestTime(Instant.now())
            .responseTime(Instant.now())
            .success(false)
            .transactionId(transaction.getId());

        bill = billRepository.save(bill);

        var svResponseWithData = svGateHoldService.confirmHold(requestDTO);
        boolean isSuccess = svResponseWithData != null && svResponseWithData.isSuccess();
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("response", objectMapper.writeValueAsString(svResponseWithData));

        bill
            .responseJson(responseMap)
            .responseTime(Instant.now())
            .responseText(svResponseWithData != null ? svResponseWithData.getMessage() : IS_NULL_MESSAGE)
            .responseCode(String.valueOf(svResponseWithData != null ? svResponseWithData.getCode() : "sv response code is null"))
            .extId(svResponseWithData != null && svResponseWithData.getData() != null ? svResponseWithData.getData().getExternalId() : "-1")
            .success(svResponseWithData != null && svResponseWithData.isSuccess());

        transaction.setStatus(isSuccess ? TransactionStatus.SUCCESS : TransactionStatus.HOLD_CONFIRM_ERROR);
        transaction.setSenderSuccess(isSuccess);
        transaction.setSenderStatusText(svResponseWithData != null ? svResponseWithData.getMessage() : IS_NULL_MESSAGE);
        transaction.setSenderStatus(svResponseWithData != null ? String.valueOf(svResponseWithData.isSuccess()) : IS_NULL_MESSAGE);
        transaction.setSenderDate(Instant.now());

        billRepository.save(bill);

        return new CommonResultData<>(
            isSuccess ? BmResultStatus.SUCCESS : BmResultStatus.SENDER_PC_ERROR,
            transaction,
            isSuccess ? null : svResponseWithData != null ? svResponseWithData.getMessage() : IS_NULL_MESSAGE
        );
    }

    @Override
    public PayType getSupportedPayType() {
        return PayType.UZCARD;
    }

    private HasData<Bill> innerDebit(CommonPayRequest payDTO, Transaction transaction, String agentLogin) {
        if (transaction.getEposMerchant() == null || transaction.getEposMerchant().isEmpty()) {
            return new CommonResultData<>(BmResultStatus.INVALID_PARAMS, null, "transaction.epos_merchant=null");
        }

        if (transaction.getEposTerminal() == null || transaction.getEposTerminal().isEmpty()) {
            return new CommonResultData<>(BmResultStatus.INVALID_PARAMS, null, "transaction.epos_terminal=null");
        }

        log.info("#### Uzcard debit ####: \n{}\n", payDTO);
        String extId = transactionPrefixGenerator.getSenderDebitPrefix(transaction.getId());

        final String account = String.valueOf(
            payDTO.getParams().getOrDefault(PAN_PARAM, payDTO.getParams().getOrDefault(TOKEN_PARAM, "-"))
        );

        transaction.setSenderAccount(account);

        String cardToken;

        if (payDTO.getParams().containsKey(TOKEN_PARAM) && payDTO.getParams().get(TOKEN_PARAM) != null) {
            cardToken = String.valueOf(payDTO.getParams().get(TOKEN_PARAM));
        } else if (payDTO.getParams().containsKey(PAN_PARAM) && payDTO.getParams().containsKey(EXPIRY_PARAM)) {
            // TODO - remove, uzcard deprecated pay by pan, use only token

            final String pan = String.valueOf(payDTO.getParams().get(PAN_PARAM));
            final String expiry = String.valueOf(payDTO.getParams().get(EXPIRY_PARAM));

            log.debug("Getting Uzcard token from Pan and Exp : {} : {}", pan, expiry);
            BmResultData<SvCardV2DTO> cardV2DTOResultData = uzcardHelperService.getSvCard(pan, expiry);

            if (!cardV2DTOResultData.isSuccess()) {
                String errorText = "token not found with given pan and exp";

                log.warn(errorText);
                log.warn(cardV2DTOResultData.getMessage());

                Bill bill = new Bill()
                    .recipientShortName("uzcard-debit")
                    .account(account)
                    .amount(transaction.getSenderAmount())
                    .amountRatio(100)
                    .requestJson(payDTO.getParams())
                    .currency(transaction.getSenderCcy())
                    .type(RequestType.PAY)
                    .login(agentLogin)
                    .requestTime(Instant.now())
                    //                    .responseTime(Instant.now())
                    //                    .success(false)
                    .transactionId(transaction.getId())
                    .outExtId(extId)//
                ;

                bill
                    .responseJson(Collections.singletonMap("error", cardV2DTOResultData.getMessage()))
                    .responseTime(Instant.now())
                    .responseText(errorText)
                    .responseCode("invalid_params")
                    .success(false);

                billRepository.save(bill);

                return new BmResultData<>(BmResultStatus.NOT_FOUND, cardV2DTOResultData.getMessage());
            }

            cardToken = cardV2DTOResultData.getData().getToken();

            log.debug("Got Uzcard token from Pan and Exp : {}", cardToken);
            //            if (!uzcardHelperService.hasSufficientFunds(payDTO.getAmount(), cardV2DTOResultData.getData())) {
            //                return new ResultData<>(new CodedException(ProblemEnum.INSUFFICIENT_FUNDS, cardToken));
            //            }
        } else {
            String errorText = "token or pair of pan and exp is not given";

            log.warn(errorText);

            Bill bill = new Bill()
                .recipientShortName("uzcard-debit")
                .account(account)
                .amount(transaction.getSenderAmount())
                .amountRatio(100)
                .requestJson(payDTO.getParams())
                .currency(transaction.getSenderCcy())
                .type(RequestType.PAY)
                .login(agentLogin)
                .requestTime(Instant.now())
                //                    .responseTime(Instant.now())
                //                    .success(false)
                .transactionId(transaction.getId())
                .outExtId(extId)//
            ;
            bill
                .responseJson(Collections.singletonMap("error", errorText))
                .responseTime(Instant.now())
                .responseText(errorText)
                .responseCode("invalid_params")
                .success(false);

            billRepository.save(bill);

            return new CommonResultData<>(BmResultStatus.INVALID_PARAMS, null, errorText);
        }
        log.debug("Debiting from card : {}", cardToken);

        PurposeType purposeType = getPurposeType(payDTO, transaction, cardToken);

        final var debitResult = uzcardHelperService.debit(transaction, cardToken, extId, "uzcard-debit", purposeType);

        if (!debitResult.isSuccess()) {
            applicationEventPublisher.publishEvent(
                    new PartnerBillingErrorEvent(this, "Uzcard", debitResult.getMessage(), transaction.getId())
            );
        }

        return debitResult;
    }

    @SneakyThrows
    private PurposeType getPurposeType(CommonPayRequest payDTO, Transaction transaction, String cardToken) {
        if (payDTO.getParams().containsKey(PAYMENT_PURPOSE)) {
            return PurposeType.valueOf(payDTO.getParams().get(PAYMENT_PURPOSE).toString());
        }
        Recipient recipient = recipientRepository.findTop1ByShortName(transaction.getRecipientName()).orElse(null);
        if (recipient == null || recipient.getJsonParams() == null || recipient.getJsonParams().isEmpty()) {
            return PurposeType.PAYMENT;
        }
        var params = objectMapper.readTree(recipient.getJsonParams());
        if (!params.has(SVGATE)) {
            return PurposeType.PAYMENT;
        }
        var svgateParam = objectMapper.convertValue(params.get(SVGATE), SvgateParam.class);
        if (svgateParam.getPaymentPurpose() != null) {
            return svgateParam.getPaymentPurpose();
        }
        return PurposeType.PAYMENT;
    }

    @Override
    public HasData<SenderOtpParams> getOtpParams(String pan, String expiry) {
        SvCardV2DTO result;

        if (pcHelper.isValidPan(pan, P2pProcessorType.UZCARD) && !StringUtils.isEmpty(expiry)) {
            expiry = pcHelper.validateAndFixUzcardExp(expiry);

            var cardData = cardService.createCard(new BaseCardDTO(pan, expiry));
            if (!cardData.isSuccess() || cardData.getData() == null) {
                return new CommonResultData<>(BmResultStatus.SENDER_PC_ERROR, null, "get uzcard card error");
            }
            result = cardData.getData();
            if (
                result.getSms() == null ||
                !result.getSms() ||
                StringUtils.isEmpty(result.getPhone()) ||
                StringUtils.isEmpty(result.getToken())
            ) {
                return new CommonResultData<>(BmResultStatus.SENDER_PC_ERROR, null, "get uzcard card error");
            }

            return new CommonResultData<>(new SenderOtpParams(result.getToken(), result.getPhone()));
        }

        var cardListData = cardService.getCards(Collections.singletonList(pan));
        if (
            !cardListData.isSuccess() ||
            cardListData.getData() == null ||
            cardListData.getData().isEmpty() ||
            cardListData.getData().get(0) == null
        ) {
            throw new CodedException(ProblemEnum.CARD_NOT_FOUND, "create card error");
        }

        result = cardListData.getData().get(0);

        return new CommonResultData<>(new SenderOtpParams(result.getToken(), result.getPhone()));
    }

    private final PcHelper pcHelper;
    private final UzcardHelperService uzcardHelperService;
    private final TransactionPrefixGenerator transactionPrefixGenerator;
    private final SvGatePaymentServiceV2 paymentService;
    private final SvGateCardServiceV2 cardService;
    private final BillRepository billRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final SvGateHoldService svGateHoldService;
    private final MapService mapService;

    @Override
    public String getDescription() {
        return "Снятие средств с карты Uzcard";
    }
}
