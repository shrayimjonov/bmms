package uz.paykap.service.uzcard;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.dto.request.CommonPayRequest;
import uz.devops.bmcore.enumeration.RequestType;
import uz.javlon.commons.result.CommonResultData;
import uz.javlon.commons.result.HasData;
import uz.javlon.commons.util.CardUtil;
import uz.javlon.svgate.config.SvgateResult;
import uz.javlon.svgate.dto.DebitTranDTO;
import uz.javlon.svgate.dto.DebitTranWithPurposeDTO;
import uz.javlon.svgate.dto.TranDTO;
import uz.javlon.svgate.dto.enumeration.CardStatus;
import uz.javlon.svgate.dto.enumeration.PurposeType;
import uz.javlon.svgate.dto.enumeration.TranType;
import uz.javlon.svgate.dto.rpc.request.CreateHoldRequestDTO;
import uz.javlon.svgate.dto.rpc.request.DismissHoldRequestDTO;
import uz.javlon.svgate.dto.rpc.response.HoldResponseDTO;
import uz.javlon.svgate.dto.rpc.response.HoldStatus;
import uz.javlon.svgate.dto.rpc.response.SvHoldResponse;
import uz.javlon.svgate.dto.sv.SvCardV2DTO;
import uz.javlon.svgate.service.SvGateCardServiceV2;
import uz.javlon.svgate.service.SvGateHoldService;
import uz.javlon.svgate.service.SvGatePaymentServiceV2;
import uz.javlon.svgate.service.impl.DefaultStanSequence;
import uz.paykap.adapter.verification.pc.PcTranType;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.config.Constants;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.gateway.uzcard.SvGateHelperService;
import uz.paykap.gateway.uzcard.UzcardUtils;
import uz.paykap.repository.BillRepository;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.security.SecurityUtils;
import uz.paykap.service.MapService;
import uz.paykap.service.PcHelper;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.humo.P2pRecoSummaryResult;
import uz.paykap.service.p2p.dto.P2pParamResult;
import uz.paykap.service.p2p.processor.UzcardParam;
import uz.paykap.service.p2p.support.P2pHelperService;
import uz.paykap.utils.TransactionPrefixGenerator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

import static uz.paykap.config.Constants.UZS_CURRENCY;

@Slf4j
@Service
@AllArgsConstructor
public class UzcardHelperService {

    public static final String UZ_CARD_HOLD_CREATE = "uzcard-hold-create";
    public static final String UZ_CARD_HOLD_DISMISS = "uzcard-hold-dismiss";
    public static final long ONE_MONTH_IN_MINUTE = 43200L;
    private final MapService mapService;
    private final BillRepository billRepository;
    private final TransactionRepository transactionRepository;
    private final SvGateCardServiceV2 cardService;
    private final SvGateHoldService svGateHoldService;
    private final DefaultStanSequence stanSequence;
    private final P2pHelperService p2pHelperService;
    private final SvGatePaymentServiceV2 paymentService;
    private final UzcardTerminalRegistrar uzcardTerminalRegistrar;
    private final TransactionPrefixGenerator transactionPrefixGenerator;
    private final PcHelper pcHelper;
    private UzCardP2pReconcileHelper uzcardP2pReconcileHelper;
    private final SvGateHelperService svGateHelperService;
    private final ApplicationProperties properties;

    @SneakyThrows
    public void init() {
        uzcardTerminalRegistrar.registerTerminals();
    }

    public Optional<String> getSenderCardTokenOrCreate(String pan) {
        return getSenderCardTokenOrCreate(pan, null);
    }

    public Optional<String> getSenderCardTokenOrCreate(String pan, String expiry) {

        if (pan == null) {
            log.warn("Pan is null");
            return Optional.empty();
        }

        if (pcHelper.isValidToken(pan, P2pProcessorType.UZCARD)) {
            log.debug("Returning self, bcz it is TOKEN : {}", pan);
            return Optional.of(pan);
        }

        if (expiry == null) {


            log.debug("Card expiry is null, calling p2pInfo...");
            var p2pInfoDTO = paymentService.p2pInfoResult(pan);

            if (p2pInfoDTO == null) {
                log.warn("uzcard p2p info request failed");
                return Optional.empty();
            }

            if (!p2pInfoDTO.isSuccess()) {
                log.warn(p2pInfoDTO.getMessage());
                return Optional.empty();
            }

            if (p2pInfoDTO.getData().getExp() == null) {
                log.warn("uzcard p2p.exp is null");
                return Optional.empty();
            }

            expiry = p2pInfoDTO.getData().getExp();

            if (expiry.length() > 4) {
                log.debug("Card expiry is more than 4, need substring : {}", expiry);
                expiry = StringUtils.substring(expiry, 2);
                log.debug("Result card expiry after substring : {}", expiry);
            }
        }

        BmResultData<SvCardV2DTO> cardV2DTOResultData = getSvCard(pan, expiry);

        if (!cardV2DTOResultData.isSuccess() || cardV2DTOResultData.getData() == null) {
            log.warn("Get uzcard failed for " + CardUtil.maskIfBankCard(pan));
            return Optional.empty();
        }

        log.debug("Returning card TOKEN {} for pan {}", cardV2DTOResultData.getData().getToken(), pan);

        return Optional.of(cardV2DTOResultData.getData().getToken());
    }

    public Optional<String> getRecipientCardTokenOrCreate(String panOrToken) {

        if (panOrToken == null) {
            log.warn("Pan is null");
            return Optional.empty();
        }

        if (pcHelper.isValidToken(panOrToken, P2pProcessorType.UZCARD)) {
            log.debug("Returning self, bcz it is TOKEN : {}", panOrToken);
            return Optional.of(panOrToken);
        }


//        try {
        log.debug("Card expiry is null, calling p2pInfo...");
        var p2pInfoDTO = paymentService.p2pInfoResult(panOrToken);


        if (p2pInfoDTO == null) {
            log.warn("uzcard p2p info request failed");
            return Optional.empty();
        }

        if (!p2pInfoDTO.isSuccess()) {
            log.warn(p2pInfoDTO.getMessage());
            return Optional.empty();
        }

        if (p2pInfoDTO.getData().getCardToken() == null) {
            log.warn("uzcard p2p.cardToken is null");
            return Optional.empty();
        }

        log.debug("Returning card TOKEN {} for pan {}", p2pInfoDTO.getData().getCardToken(), panOrToken);

        return Optional.of(p2pInfoDTO.getData().getCardToken());
    }

    public BmResultData<SvCardV2DTO> getSvCardByToken(String cardToken) {

        var cardResult = cardService.getCards(Collections.singletonList(cardToken));

        if (!cardResult.isSuccess() || cardResult.getData() == null || cardResult.getData().isEmpty()) {
            return new BmResultData<>(BmResultStatus.SENDER_NOT_FOUND, "Uzcard not found by given token : " + cardResult.getMessage());
        }

        return new BmResultData<>(cardResult.getData().get(0));
    }

    public BmResultData<SvCardV2DTO> getSvCard(String pan, String expiry) {

        if (expiry == null) {

            log.info("Card expiry is null, calling p2pInfo({})", pan);

            var p2pInfoDTO = paymentService.p2pInfoResult(pan);

            if (!p2pInfoDTO.isSuccess()) {
                return new BmResultData<>(BmResultStatus.PC_ERROR, "Empty p2p info response");
//                throw new CodedException(ProblemEnum.INTERNAL_ERROR, "Empty p2p info response");
            }

            if (p2pInfoDTO.getData() == null || p2pInfoDTO.getData().getExp() == null) {
                return new BmResultData<>(BmResultStatus.INVALID_PARAMS, "p2p.exp is null");
//                throw new CodedException(ProblemEnum.INTERNAL_ERROR, "p2p.exp is null");
            }

            if (p2pInfoDTO.getData().getExp().length() > 4) {
                expiry = StringUtils.right(p2pInfoDTO.getData().getExp(), 4);
            }
        }

        var cardResponse = svGateHelperService.getSvCard(pan);

        if (cardResponse == null || !cardResponse.isSuccess() || cardResponse.getData() == null) {
            log.warn("createCard failed for pan={} and expiry={}", pan, expiry);
            return new BmResultData<>(BmResultStatus.PC_ERROR, "Uzcard could not create card : " + CardUtil.mask(pan) + " : " + expiry);
        }

        return new BmResultData<>(cardResponse.getData().get(0));
    }

    public boolean hasSufficientFunds(Long tranAmount, SvCardV2DTO card) {
        return tranAmount != null && card.getBalance() != null && tranAmount <= card.getBalance();
    }

    public DebitTranDTO getRequest(String cardToken, Long amount, String extId, String merchantId, String terminalId) {

        DebitTranDTO tranDTO = new DebitTranDTO();
        tranDTO.setCardId(cardToken);
        tranDTO.setStan(stanSequence.getNextStan());
        tranDTO.setCurrencyCode("860");
        tranDTO.setAmount(amount);
        tranDTO.setExt(extId);

        tranDTO.setMerchantId(merchantId);
        tranDTO.setTerminalId(terminalId);
        tranDTO.setPort(1);

        return tranDTO;
    }

    public DebitTranWithPurposeDTO getDebitRequest(String cardToken, Long amount, String extId, String merchantId, String terminalId, PurposeType purposeType, String recipientAccount, Long commissionAmount) {

        DebitTranWithPurposeDTO tranDTO = new DebitTranWithPurposeDTO();
        tranDTO.setPurpose(purposeType);
        tranDTO.setReceiverId(recipientAccount);
        tranDTO.setAmount(amount - commissionAmount);
        tranDTO.setCardId(cardToken);
        tranDTO.setCommission(commissionAmount);
        tranDTO.setCurrency("860");
        tranDTO.setExt(extId);
        tranDTO.setMerchantId(merchantId);
        tranDTO.setTerminalId(terminalId);
        log.info("Uzcard request dto : {}", tranDTO);
        return tranDTO;
    }

    public BmResultData<Bill> debit(Transaction transaction, String cardToken, P2pParamResult p2pParam) {

        BmResultData<UzcardParam> paramResult = p2pHelperService.getSenderParams(p2pParam.getValue(), UzcardParam.class);

        if (!paramResult.isSuccess()) {
            return new BmResultData<>(paramResult.getStatus());
        }

        String eposMerchant = paramResult.getData().getMerchantId();
        String eposTerminal = paramResult.getData().getTerminalId();

        p2pHelperService.setSenderEpos(transaction, eposMerchant, eposTerminal);

        return debit(transaction, cardToken, transactionPrefixGenerator.getP2pDebitPrefix(transaction.getId()), "p2p-uzcard-debit", paramResult.getData().getPaymentPurpose());

    }

    public BmResultData<Bill> debit(Transaction transaction, String cardToken, String outExtId, String recipientShortName, PurposeType purposeType) {

        log.info("#### Uzcard debit ####: \n{} : {}\n", cardToken, outExtId);

        Long commissionAmount = 0L;
        if (transaction.getSystemCommissionDownAmount() != null) {
            commissionAmount = transaction.getSystemCommissionDownAmount();
        }
        if (transaction.getSystemCommissionUpAmount() != null) {
            commissionAmount += transaction.getSystemCommissionUpAmount();
        }
        log.info("Commission amount : {}", commissionAmount);
        final DebitTranWithPurposeDTO debitTranDTO = getDebitRequest(cardToken, transaction.getSenderAmount(), outExtId, transaction.getEposMerchant(), transaction.getEposTerminal(), purposeType, transaction.getRecipientAccount(), commissionAmount);

        Bill bill = new Bill()
                .requestJson(mapService.convert(debitTranDTO))
                .recipientShortName(recipientShortName)
                .account(cardToken)
                .amount(transaction.getSenderAmount())
                .amountRatio(100)
                .currency(UZS_CURRENCY)
                .type(RequestType.P2P_DEBIT)
                .login(transaction.getRequestLogin())
                .requestTime(Instant.now())
                .responseTime(Instant.now())
                .transactionId(transaction.getId())
                .outExtId(debitTranDTO.getExt())
                //
                ;

        bill = billRepository.save(bill);

        log.debug("Debiting from card : {}", cardToken);

        // Debit

        var tranDTO = paymentService.debitWithPurpose(debitTranDTO);

        log.debug("Debit result : {}", tranDTO);

        if (!tranDTO.isSuccess()) {
            log.warn("#### Uzcard Error Response : {}", tranDTO.getMessage());
            bill
                    .responseJson(Collections.singletonMap("error", tranDTO.getMessage()))
                    .responseTime(Instant.now())
                    .responseText(tranDTO.getMessage())
                    .responseCode(String.valueOf(tranDTO.getCode()))
                    .success(false)
            ;

            billRepository.save(bill);

            if (Objects.equals(tranDTO.getCode(), SvgateResult.TIMEOUT.getCode())) {
                return new BmResultData<>(BmResultStatus.TIMEOUT, tranDTO.getMessage());
            }

            return new BmResultData<>(BmResultStatus.SENDER_PC_ERROR, tranDTO.getMessage());
        }

        // Success

        bill
                .responseJson(mapService.convert(tranDTO))
                .responseTime(Instant.now())
                .responseText(tranDTO.getData().getRespDesc())
                .responseCode(String.valueOf(tranDTO.getData().getResp()))
                .extId(tranDTO.getData().getRefNum())
                .success(tranDTO.getData().getResp() == 0)
        ;

        log.info("#### Uzcard Response : \n{}\n", bill);

        return new BmResultData<>(billRepository.save(bill));
    }

    /* Used for p2p */
    public BmResultData<Bill> reverse(Transaction transaction, Boolean isSender) {

        if (transaction.getSenderTrId() == null) {
            return new BmResultData<>(BmResultStatus.INTERNAL_ERROR, "SenderTrId is null");
        }

        Optional<String> login = SecurityUtils.getCurrentUserLogin();

        if (login.isEmpty()) {
            return new BmResultData<>(BmResultStatus.P2P_NOT_PERMITTED, "Invalid login");
        }

        Bill bill = new Bill()
                .recipientShortName("uzcard-debit-reverse")
                .account((isSender) ? transaction.getSenderAccount() : transaction.getRecipientAccount())
                .amount((isSender) ? transaction.getSenderAmount() : transaction.getRecipientAmount())
                .amountRatio(transaction.getAmountRatio())
                .currency((isSender) ? transaction.getSenderCcy() : transaction.getRecipientCcy())
                .type(RequestType.CANCEL)
                .login(login.get())
                .requestTime(Instant.now())
                .transactionId(transaction.getId())
                .extId((isSender) ? transaction.getSenderTrId() : transaction.getRecipientTrId())
                //
                ;


        var reverseResult = paymentService.reverse((isSender) ? transaction.getSenderTrId() : transaction.getRecipientTrId());

        if (!reverseResult.isSuccess()) {
            bill
//                    .responseJson(Collections.singletonMap("error", reverseResult.getMessage()))
                    .responseTime(Instant.now())
                    .responseText(reverseResult.getMessage())
                    .responseCode(String.valueOf(reverseResult.getCode()))
                    .success(false)
            ;

            billRepository.save(bill);

            return new BmResultData<>((isSender) ? BmResultStatus.SENDER_PC_ERROR : BmResultStatus.RECIPIENT_PC_ERROR, bill, reverseResult.getMessage());
        }

        if (reverseResult.getData() == null || reverseResult.getData().isEmpty()) {

            String reversalErrorText = "Reversal result is null";

            bill
                    .responseJson(Collections.singletonMap("error", reversalErrorText))
                    .responseTime(Instant.now())
                    .responseText(reversalErrorText)
                    .responseCode("FAILED")
                    .success(false);

            billRepository.save(bill);

            return new BmResultData<>((isSender) ? BmResultStatus.SENDER_PC_ERROR : BmResultStatus.RECIPIENT_PC_ERROR, bill, reversalErrorText);
        }

        TranDTO reversalTran = null;

        for (TranDTO tran : reverseResult.getData()) {
            if (tran.getTranType() == TranType.REVERSAL) {
                reversalTran = tran;
            }
        }

        if (reversalTran == null) {

            String reversalErrorText = "Reversal tran not found : " + reverseResult.getData().size();

            bill
                    .responseJson(Collections.singletonMap("error", reversalErrorText))
                    .responseTime(Instant.now())
                    .responseText(reversalErrorText)
                    .responseCode("FAILED")
                    .success(false);

            billRepository.save(bill);

            return new BmResultData<>(BmResultStatus.RECIPIENT_PC_ERROR, bill, reversalErrorText);
        }

        log.debug("Found reverse from list size : {} : {}", reverseResult.getData().size(), reversalTran);

        String pan = (isSender) ? transaction.getSenderAccount() : transaction.getRecipientAccount();
        String masked = CardUtil.mask(pan);

        HashMap<String, Object> data = new HashMap<>();
        data.put(Constants.PAN_PARAM, masked);
        data.put(Constants.AMOUNT_PARAM, String.valueOf(reversalTran.getAmount()));

        UzcardUtils.setTranData(reversalTran, data);

        bill
                .responseJson(data)
                .responseTime(Instant.now())
                .responseText(reversalTran.getRespDesc())
                .responseCode(String.valueOf(reversalTran.getResp()))
                .extId(reversalTran.getRefNum())
                .success(reversalTran.isSuccess())
        ;

        return new BmResultData<>(billRepository.save(bill));

    }

    @Deprecated
    public BmResultData<Bill> creditSvGateV2(Transaction transaction, P2pParamResult p2pParam, UzcardParam recipientParam, String recipientCardToken) {

        log.info("Crediting to card token : {}", recipientCardToken);

        return new BmResultData<>(BmResultStatus.NOT_IMPLEMENTED);
    }

    public HasData<Bill> hold(Transaction transaction, CommonPayRequest payDTO) {
        CreateHoldRequestDTO request = createHoldRequest(transaction);
        log.debug("hold request: {}", request);
        Bill holdBill = initHoldBill(transaction, request, payDTO);
        log.debug("hold Bill: {}", holdBill);

        var holdResponseData = svGateHoldService.createHold(request);
        log.debug("hold result: {}", holdResponseData);

        return new CommonResultData<>(updateHoldBill(holdBill, holdResponseData, transaction));
    }

    public HasData<Bill> reHold(Transaction transaction, CommonPayRequest payDTO) {

        final String oldHoldId = transaction.getSenderTrId();
        final String oldHoldCardToken = transaction.getSenderAccount();
        log.info("Old hold id : {} and oldHoldCardToken : {}", oldHoldId, oldHoldCardToken);

        final HasData<HoldResponseDTO> firstHoldData = svGateHoldService.getHoldById(oldHoldId);
        log.info("Found hold : {}", firstHoldData);
        if (!firstHoldData.isSuccess()) {
            return new CommonResultData<>(firstHoldData, null, firstHoldData.getMessage());
        }
        if (firstHoldData.getData().getStatus() == HoldStatus.EXPIRED || firstHoldData.getData().getStatus() == HoldStatus.CANCELED) {
            return new CommonResultData<>(BmResultStatus.INVALID_STATUS, null, "First hold in invalid status");
        }

        if (Objects.equals(firstHoldData.getData().getReqamt(), payDTO.getAmount())) {
            return new CommonResultData<>(BmResultStatus.INVALID_REQUEST, null, "Cannot reHold same amount");
        }

        if (!Objects.equals(firstHoldData.getData().getReqamt(), transaction.getSenderAmount())) {
            return new CommonResultData<>(BmResultStatus.INVALID_REQUEST, null, "Hold and tran amounts differ");
        }

        final HasData<List<SvCardV2DTO>> cards = cardService.getCards(List.of(oldHoldCardToken));
        log.info("Found client card : {}", cards);
        if (!cards.isSuccess() || cards.getData().isEmpty()) {
            return new CommonResultData<>(cards, null, cards.getMessage());
        }

        final SvCardV2DTO firstCard = cards.getData().get(0);
        if (firstCard.getStatus() != CardStatus.VALID) {
            return new CommonResultData<>(BmResultStatus.INVALID_STATUS, null, "Card not active");
        }

        Long oldHoldAmount = transaction.getSenderAmount();
        Long requestAmount = payDTO.getAmount();
        Long cardBalance = firstCard.getBalance();

        if (requestAmount < oldHoldAmount) {
            log.debug("Request amount less than hold amount : {} : {}", requestAmount, oldHoldAmount);

            final Bill oldHoldDismiss = sendHoldDismiss(oldHoldId, oldHoldAmount, transaction);

            if (!oldHoldDismiss.isSuccess()) {
                return new CommonResultData<>(BmResultStatus.SENDER_PC_ERROR, oldHoldDismiss, oldHoldDismiss.getResponseText());
            }

            log.debug("Old hold {}:{} dismissed OK", oldHoldId, oldHoldAmount);

            final Bill newHoldBill = sendHoldCreate(transaction, requestAmount);

            if (!newHoldBill.isSuccess()) {
                log.warn("New hold failed create, second try...");

                final Bill newHoldBill2 = sendHoldCreate(transaction, requestAmount);

                if (!newHoldBill2.isSuccess()) {
                    log.warn("Second New hold create failed, missed old hold, need manual recover");
                    return new CommonResultData<>(BmResultStatus.SENDER_PC_ERROR, newHoldBill2, newHoldBill2.getResponseText());
                }

            }

            return new CommonResultData<>(newHoldBill);
        } else {
            log.debug("Request amount grater than hold amount : {} : {}", requestAmount, oldHoldAmount);

            final BigDecimal reHoldUpPercent = BigDecimal.valueOf(requestAmount * 100).divide(BigDecimal.valueOf(oldHoldAmount), 0, RoundingMode.CEILING).subtract(BigDecimal.valueOf(100));
            final Integer maxReHoldPercent = properties.getHold().getMaxReholdPercent();

            if (reHoldUpPercent.longValue() > maxReHoldPercent) {
                log.warn("Request amount grater than held amount more than {}% : req {} : held {}", maxReHoldPercent, requestAmount, oldHoldAmount);
                return new CommonResultData<>(BmResultStatus.INVALID_REQUEST, null, "Request amount more than " + maxReHoldPercent + "%");
            }

            if (cardBalance >= requestAmount) {
                log.debug("Request amount grater than balance : {} : {}, do new hold then dismiss old", requestAmount, cardBalance);

                final var reHoldRequest = createHoldRequest(transaction, requestAmount);
                final Bill newHoldBill = initHoldBill(transaction, reHoldRequest, payDTO, requestAmount);

                var holdResponseData = svGateHoldService.createHold(reHoldRequest);

                log.debug("re hold result: {}", holdResponseData);

                updateHoldBill(newHoldBill, holdResponseData, transaction);

                if (!newHoldBill.isSuccess()) {
                    log.warn("Re hold failed, just fail");
                    transaction.nextSenderStatusText(newHoldBill.getResponseText());
                    return new CommonResultData<>(BmResultStatus.FAILED, null, newHoldBill.getResponseText());
                }

                log.debug("Re hold {}:{} success, dismiss old hold {}:{}", newHoldBill.getExtId(), newHoldBill.getAmount(), oldHoldId, oldHoldAmount);

                final Bill oldHoldDismissBill = sendHoldDismiss(oldHoldId, oldHoldAmount, transaction);

                log.debug("Dismiss old hold result : {}", oldHoldDismissBill);

                if (!oldHoldDismissBill.isSuccess()) {
                    final Bill newHoldDismissBill = sendHoldDismiss(newHoldBill.getExtId(), requestAmount, transaction);

                    if (!newHoldDismissBill.isSuccess()) {
                        log.warn("Double hold occurred: New hold ok -> old hold dismiss failed -> new hold dismiss failed");
                        return new CommonResultData<>(BmResultStatus.SENDER_PC_ERROR, null, newHoldDismissBill.getResponseText());
                    }
                }

                log.debug("All is OK : New hold ok -> old hold dismiss ok");

                return new CommonResultData<>(newHoldBill);

            } else {

                log.debug("Request amount less than balance : {} : {}, dismiss old then rehold more amount", requestAmount, cardBalance);

                if (cardBalance + oldHoldAmount < requestAmount) {
                    return new CommonResultData<>(BmResultStatus.INSUFFICIENT_FUNDS_ON_SENDER, null, "Insufficient funds in card");
                }

                final Bill oldHoldDismiss = sendHoldDismiss(oldHoldId, oldHoldAmount, transaction);

                if (!oldHoldDismiss.isSuccess()) {
                    return new CommonResultData<>(BmResultStatus.SENDER_PC_ERROR, oldHoldDismiss, oldHoldDismiss.getResponseText());
                }

                log.debug("Old hold {}:{} dismissed OK", oldHoldId, oldHoldAmount);

                final Bill newHoldBill = sendHoldCreate(transaction, requestAmount);

                if (!newHoldBill.isSuccess()) {
                    log.warn("New hold failed create, second try...");

                    final Bill newHoldBill2 = sendHoldCreate(transaction, requestAmount);

                    if (!newHoldBill2.isSuccess()) {
                        log.warn("Second New hold create failed, missed old hold, need manual recover");
                        return new CommonResultData<>(BmResultStatus.SENDER_PC_ERROR, newHoldBill2, newHoldBill2.getResponseText());
                    }

                }

                return new CommonResultData<>(newHoldBill);
            }
        }

    }

    private Bill updateHoldBill(Bill holdBill, HasData<SvHoldResponse> holdResponseData, Transaction transaction) {

        holdBill
                .responseJson(mapService.convert(holdResponseData))
                .responseTime(Instant.now())
                .responseText(holdResponseData.getData() != null ? holdResponseData.getData().getDescription() : (!StringUtils.isEmpty(holdResponseData.getMessage()) ? holdResponseData.getMessage() : "hold error"))
                .responseCode(String.valueOf(holdResponseData.getData() != null ? holdResponseData.getData().getStatus() : holdResponseData.getCode()))
                .extId(holdResponseData.getData() != null ? String.valueOf(holdResponseData.getData().getId()) : "-1")
                .success(holdResponseData.isSuccess() && holdResponseData.getData() != null && Objects.equals(holdResponseData.getData().getStatus(), 0));

        billRepository.save(holdBill);

        transaction.setSenderTrId(holdBill.getExtId());
        transactionRepository.save(transaction);
        return holdBill;
    }

    private Bill initHoldBill(Transaction transaction, CreateHoldRequestDTO request, CommonPayRequest payRequest) {
        return initHoldBill(transaction, request, payRequest, transaction.getSenderAmount());
    }

    private Bill initHoldBill(Transaction transaction, CreateHoldRequestDTO request, CommonPayRequest payRequest, Long senderAmount) {
        Map<String, Object> requestJson = mapService.convert(request);
        if (requestJson == null) {
            requestJson = new HashMap<>();
        }
        if (payRequest.getParams() != null) {
            requestJson.putAll(payRequest.getParams());
        }
        Bill bill = new Bill()
                .requestJson(requestJson)
                .recipientShortName(UZ_CARD_HOLD_CREATE)
                .account(transaction.getSenderAccount())
                .amount(senderAmount)
                .amountRatio(100)
                .currency(UZS_CURRENCY)
                .type(RequestType.PAY)
                .login(transaction.getRequestLogin())
                .requestTime(Instant.now())
                .responseTime(Instant.now())
                .transactionId(transaction.getId())
                //
                ;

        return billRepository.save(bill);
    }

    private Bill sendHoldCreate(Transaction transaction, Long senderAmount) {

        CreateHoldRequestDTO request = createHoldRequest(transaction);

        Map<String, Object> requestJson = mapService.convert(request);

        Bill holdBill = new Bill()
                .requestJson(requestJson)
                .recipientShortName(UZ_CARD_HOLD_CREATE)
                .account(transaction.getSenderAccount())
                .amount(senderAmount)
                .amountRatio(100)
                .currency(UZS_CURRENCY)
                .type(RequestType.PAY)
                .login(transaction.getRequestLogin())
                .requestTime(Instant.now())
                .responseTime(Instant.now())
                .transactionId(transaction.getId())
                //
                ;

        final var holdResponseData = svGateHoldService.createHold(request);

        holdBill
                .responseJson(mapService.convert(holdResponseData))
                .responseTime(Instant.now())
                .responseText(holdResponseData.getData() != null ? holdResponseData.getData().getDescription() : (!StringUtils.isEmpty(holdResponseData.getMessage()) ? holdResponseData.getMessage() : "hold error"))
                .responseCode(String.valueOf(holdResponseData.getData() != null ? holdResponseData.getData().getStatus() : holdResponseData.getCode()))
                .extId(holdResponseData.getData() != null ? String.valueOf(holdResponseData.getData().getId()) : "-1")
                .success(holdResponseData.isSuccess() && holdResponseData.getData() != null && Objects.equals(holdResponseData.getData().getStatus(), 0));

        billRepository.save(holdBill);

        if (holdBill.isSuccess()) {
            transaction.setSenderTrId(holdBill.getExtId());
            transactionRepository.save(transaction);
        }

        return holdBill;
    }

    private Bill sendHoldDismiss(String holdId, Long holdAmount, Transaction transaction) {

        DismissHoldRequestDTO request = new DismissHoldRequestDTO(holdId, transaction.getEposMerchant(), transaction.getEposTerminal(), holdAmount);

        Map<String, Object> requestJson = mapService.convert(request);

        Bill dismissBill = new Bill()
                .requestJson(requestJson)
                .recipientShortName(UZ_CARD_HOLD_DISMISS)
                .account(transaction.getSenderAccount())
                .amount(holdAmount)
                .amountRatio(100)
                .currency(UZS_CURRENCY)
                .type(RequestType.PAY)
                .login(transaction.getRequestLogin())
                .requestTime(Instant.now())
                .responseTime(Instant.now())
                .transactionId(transaction.getId())
                //
                ;

        var holdResponseData = svGateHoldService.dismissHold(request);

        log.debug("Dismiss hold result: {}", holdResponseData);

        dismissBill
                .responseJson(mapService.convert(holdResponseData.getData()))
                .responseTime(Instant.now())
                .responseText(holdResponseData.getData() != null ? holdResponseData.getData().getDescription() : (!StringUtils.isEmpty(holdResponseData.getMessage()) ? holdResponseData.getMessage() : "hold error"))
                .responseCode(String.valueOf(holdResponseData.getData() != null ? holdResponseData.getData().getStatus() : holdResponseData.getCode()))
                .extId(holdResponseData.getData() != null ? String.valueOf(holdResponseData.getData().getId()) : "-1")
                .success(holdResponseData.isSuccess() && holdResponseData.getData() != null && Objects.equals(holdResponseData.getData().getStatus(), 0));

        return billRepository.save(dismissBill);
    }

    private CreateHoldRequestDTO createHoldRequest(Transaction transaction) {
        return createHoldRequest(transaction, transaction.getSenderAmount());
    }

    private CreateHoldRequestDTO createHoldRequest(Transaction transaction, Long senderAmount) {
        CreateHoldRequestDTO request = new CreateHoldRequestDTO();
        request.setAmount(senderAmount);
        request.setCardId(transaction.getSenderAccount());
        request.setMerchantId(transaction.getEposMerchant());
        request.setTerminalId(transaction.getEposTerminal());
        request.setTime(ONE_MONTH_IN_MINUTE);
        return request;
    }

    public P2pRecoSummaryResult summaryReconcile(PcTranType pcTranType, Instant fromDate, Instant toDate) {
        return uzcardP2pReconcileHelper.summaryReconcile(pcTranType, fromDate, toDate);
    }
}
