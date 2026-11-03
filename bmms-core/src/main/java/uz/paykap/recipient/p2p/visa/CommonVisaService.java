package uz.paykap.recipient.p2p.visa;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uz.devops.bm.ncitrans.dto.enums.NciTransResponseJsonKeys;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.devops.cardSuite.config.CardSuiteTietoProperties;
import uz.devops.cardSuite.issuing_v_01_02_xsd.RowType_ExecTransaction_Request;
import uz.devops.cardSuite.service.CardSuiteTietoService;
import uz.devops.cardSuite.service.dto.CardAndAccount;
import uz.devops.cardSuite.service.dto.CardAndHolder;
import uz.javlon.commons.result.CommonResultData;
import uz.paykap.config.Constants;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.repository.BillRepository;
import uz.paykap.service.PcHelper;
import uz.paykap.service.abs.AbstractAbsService;
import uz.paykap.service.abs.request.AbsForexTransactionRequest;
import uz.paykap.service.abs.response.AbsTransactionResult;
import uz.paykap.service.dto.PurposeDTO;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.p2p.P2pAccount;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.p2p.dto.P2pDTO;
import uz.paykap.service.p2p.processor.IsApiForexParam;
import uz.paykap.service.p2p.support.BillHandler;
import uz.paykap.service.p2p.support.P2pHelperService;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Profile("davr")
@Slf4j
@Service
public class CommonVisaService {
    public static final String RESPONSE = "response";
    public static final String VISA_REVERSE = "visa_reverse_";
    public static final String REQUEST = "request";
    public List<String> debitBillNames;
    public List<String> creditBillNames;

//    public static final String SENDER_AMOUNT_IS_NULL = "sender amount is null";

    private final BillRepository billRepository;
    private final AbstractAbsService nciHelperService;
    private final CardSuiteTietoService cardSuiteTietoService;
    private final P2pHelperService p2pHelperService;
    private final PcHelper pcHelper;
    private final CardSuiteTietoProperties cardSuiteTietoProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CommonVisaService(BillRepository billRepository, AbstractAbsService nciHelperService, CardSuiteTietoService cardSuiteTietoService, P2pHelperService p2pHelperService, PcHelper pcHelper, CardSuiteTietoProperties cardSuiteTietoProperties) {
        this.billRepository = billRepository;
        this.nciHelperService = nciHelperService;
        this.cardSuiteTietoService = cardSuiteTietoService;
        this.p2pHelperService = p2pHelperService;
        this.pcHelper = pcHelper;
        this.cardSuiteTietoProperties = cardSuiteTietoProperties;

        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        creditBillNames = new ArrayList<>();
        //visa_reverse_p2p_credit
        //visa-credit
        creditBillNames.add("visa-credit");
        creditBillNames.add("visa_reverse_p2p_credit");

        debitBillNames = new ArrayList<>();
        //visa-debit
        //visa_reverse_p2p_debit
        debitBillNames.add("visa-debit");
        debitBillNames.add("visa_reverse_p2p_debit");
    }

    protected BmResultData<String> isSupported(String visaPanOrToken, P2pAccount p2pAccount) {

        boolean matches = pcHelper.isValidPan(visaPanOrToken, P2pProcessorType.VISA) || pcHelper.isValidToken(visaPanOrToken, P2pProcessorType.VISA);

        if (!matches) {
            log.info("Visa not matched : {}", visaPanOrToken);
            return new BmResultData<>(BmResultStatus.INVALID_RECIPIENT_PARAMS, "Invalid visa card visaPanOrToken");
        }

        if (p2pAccount.getCurrency() != 840) {
            return new BmResultData<>(BmResultStatus.INVALID_PARAMS, "Currency " + p2pAccount.getCurrency() + " not supported by VISA");
        }

        log.info("Found Visa visaPanOrToken : {}", visaPanOrToken);
        return new BmResultData<>(visaPanOrToken);
    }

    protected BmResultData<Map<String, Object>> getInfo(String visaPanOrToken, BillHandler handler) {

        try {
            log.info("cardSuiteTietoService.getInfo request pan {}", visaPanOrToken);
            handler.setRequest(Collections.singletonMap(Constants.PAN_PARAM, visaPanOrToken));
            CommonResultData<CardAndHolder> cardInfo = cardSuiteTietoService.getInfo(visaPanOrToken);
            log.info("cardSuiteTietoService.getInfo response {}", cardInfo);

            if (!cardInfo.isSuccess()) {
                // Error
                return errorResult(handler, BmResultStatus.PC_ERROR, cardInfo.getStatus().getMessage());
            }
            if (!Objects.equals(cardInfo.getData().getCard().getStopCause(), "0")) {
                return errorResult(handler, BmResultStatus.INSUFFICIENT_FUNDS_ON_SENDER, BmResultStatus.INSUFFICIENT_FUNDS_ON_SENDER.getMessage());
            }
            // Success

            Map<String, Object> responseJson = Collections.singletonMap(Constants.FIO_PARAM, cardInfo.getData().getCardHolderName());
            handler.success(responseJson);

            log.info("getInfo return success response {}", responseJson);
            return new BmResultData<>(responseJson);
        } catch (Exception e) {
            log.info("getInfo method handle exception {}", e.getMessage());
            return errorResult(handler, e.getMessage());
        }
    }

//    protected BmResultData<Map<String, String>> getInfoSender(String visaPanOrToken, Long senderAmount, BillHandler handler) {
//        if (senderAmount == null) {
//            return errorResult(handler, BmResultStatus.INTERNAL_ERROR, SENDER_AMOUNT_IS_NULL);
//        }
//        try {
//            return canSendMoney(visaPanOrToken, senderAmount);
//        } catch (Exception e) {
//            log.info("getAccountByCard method handle exception {}", e.getMessage());
//            return errorResult(handler, BmResultStatus.INTERNAL_ERROR, e.getMessage());
//        }
//    }

    private BmResultData<Map<String, String>> canSendMoney(String visaPanOrToken, Long senderAmount) {
        log.info("cardSuiteTietoService.getAccountByCard request pan {}", visaPanOrToken);
        CommonResultData<CardAndAccount> accountByCard = cardSuiteTietoService.getAccountByCard(visaPanOrToken);
        log.info("cardSuiteTietoService.getAccountByCard response {}", accountByCard);

        if (!accountByCard.isSuccess()) {
            // Error
            return errorResult(BmResultStatus.SENDER_PC_ERROR, accountByCard.getStatus().getMessage());
        }

        if (accountByCard.getData().getAvailAmount() < senderAmount || !Objects.equals(accountByCard.getData().getCard().getStopCause(), "0")) {
            return errorResult(BmResultStatus.INSUFFICIENT_FUNDS_ON_SENDER, BmResultStatus.INSUFFICIENT_FUNDS_ON_SENDER.getMessage());
        }

        Map<String, String> responseJson = Collections.singletonMap(Constants.FIO_PARAM, accountByCard.getData().getAccount());

        log.info("getAccountByCard return success response {}", responseJson);
        return new BmResultData<>(responseJson);
    }

    public void setPurpose(Object o, AbsForexTransactionRequest forexTransactionRequest, String purpose) {
        if (purpose.contains("${")) {
            forexTransactionRequest.setPurpose(new StringSubstitutor(objectMapper.convertValue(o, Map.class)).replace(purpose));
        } else {
            forexTransactionRequest.setPurpose(purpose);
        }
    }

    /* UZCARD/HUMO -> VISA,  9-покупка */
    protected BmResultData<Bill> credit(Transaction transaction, IsApiForexParam isApiForexParam, @NotNull @Pattern(regexp = "\\d{16}") String visaPanOrToken, String comment, P2pDTO senderDTO) {
        try {

            AbsForexTransactionRequest forexTransactionDTO = new AbsForexTransactionRequest();
            forexTransactionDTO.setRequestId("BILL_MASTER_" + System.currentTimeMillis() + "_" + transaction.getId());
            forexTransactionDTO.setMfo(isApiForexParam.getMfo());
            forexTransactionDTO.setAccCl(isApiForexParam.getAccCl());
            forexTransactionDTO.setAccCo(isApiForexParam.getAccCo());
            forexTransactionDTO.setAmountCo(transaction.getRecipientAmount());
            forexTransactionDTO.setDeal(9); //todo 8/9  тип сделки (8-покупка 9 -продажа) nujno utochnit
            forexTransactionDTO.setFxDeal(isApiForexParam.getFxDeal());
            log.info("doForexTransaction request: {}", forexTransactionDTO);

            PurposeDTO purposeDTO = createPurpose(transaction, comment, isApiForexParam.getMfo(), senderDTO, null,false);
            setPurpose(purposeDTO, forexTransactionDTO, isApiForexParam.getPurpose());

            Bill forexBill = p2pHelperService.createP2pBill(transaction, RequestType.P2P_CREDIT, P2pProcessorType.FOREX, forexTransactionDTO, forexTransactionDTO.getRequestId());

            BmResultData<AbsTransactionResult> resultData = nciHelperService.doForexTransaction(forexTransactionDTO);

            if (!resultData.isSuccess()) {
                log.warn("doForexTransaction response is not success : {}", resultData);
                forexBill
                    .responseTime(Instant.now())
                    .responseText(String.valueOf(resultData))
                    .success(false);
                billRepository.save(forexBill);

                // Error
                log.error("nciHelperService.doForexTransaction throw exception : {}", resultData);
                return this.errorResult(forexBill, resultData.getMessage());
            }

            forexBill
                .responseJson(Collections.singletonMap(NciTransResponseJsonKeys.TRANS_ID.name(), String.valueOf(resultData.getData().getTransId())))
                .responseTime(Instant.now())
                .extId(resultData.getData().getTransId())
                .success(true)
                .responseText(resultData.getData().getErrorNote());

            billRepository.save(forexBill);
            log.info("forex forexBill {}", forexBill);

            // Success, inform VISA

            log.info("cardSuiteTietoService.credit pan : {} and amount {}", visaPanOrToken, transaction.getRecipientAmount());
            final RowType_ExecTransaction_Request creditRequest = cardSuiteTietoService.getCreditRequest(visaPanOrToken, transaction.getRecipientAmount());

            Bill visaBill = p2pHelperService.createP2pBill(transaction, RequestType.P2P_CREDIT, P2pProcessorType.VISA, objectMapper.convertValue(creditRequest, Map.class));

            CommonResultData<BigDecimal> credit = cardSuiteTietoService.credit(forexTransactionDTO.getRequestId(), creditRequest);

            if (credit != null && credit.isSuccess()) {
                log.info("cardSuiteTietoService.credit success response {}", credit);

                visaBill
                    .responseJson(Collections.singletonMap(NciTransResponseJsonKeys.TRANS_ID.name(), String.valueOf(resultData.getData().getTransId())))
                    .responseTime(Instant.now())
                    .extId(resultData.getData().getTransId())
                    .success(true)
                    .responseText(resultData.getData().getErrorNote());

                visaBill = billRepository.save(visaBill);

                log.debug("Final save bill: {}", visaBill);

                return new BmResultData<>(visaBill);
            }
            // Error
            log.error("credit response is not success ...");
            return this.errorResult(visaBill, Objects.requireNonNull(credit).getMessage());
        } catch (Exception e) {
            log.error("credit throw exception : {}", e.getMessage());
            return this.errorResult(e.getMessage());
        }
    }

    /* VISA -> UZCARD/HUMO, 8-продажа */
    protected BmResultData<Bill> debit(Transaction transaction, IsApiForexParam isApiForexParam, @NotNull @Pattern(regexp = "\\d{16}") String visaPanOrToken, String comment, P2pDTO senderDTO, P2pDTO recipientDTO) {
        try {

            log.info("cardSuiteTietoService.debit pan : {} and amount {}", visaPanOrToken, transaction.getRecipientAmount());

            RowType_ExecTransaction_Request debitRequest = cardSuiteTietoService.getDebitRequest(visaPanOrToken, transaction.getSenderAmount());

            Bill visaBill = p2pHelperService.createP2pBill(transaction, RequestType.P2P_DEBIT, P2pProcessorType.VISA, objectMapper.convertValue(debitRequest, Map.class));

            BmResultData<Map<String, String>> canSendMoney = canSendMoney(visaPanOrToken, transaction.getSenderAmount());

            if (!canSendMoney.isSuccess()) {
                return errorResult(visaBill, canSendMoney.getMessage());
            }

            CommonResultData<BigDecimal> visaDebitResult = cardSuiteTietoService.debit(String.valueOf(transaction.getId()), debitRequest);

            if (visaDebitResult == null) {
                String emptyResultText = "Visa debit returned empty result";
                log.warn(emptyResultText);
                return errorResult(visaBill, emptyResultText);
            }

            if (visaDebitResult.getData() == null || !visaDebitResult.isSuccess()) {
                log.warn("Visa debit response is NOT success : {}", visaDebitResult);
                return this.errorResult(visaBill, visaDebitResult.getMessage());
            }


            log.info("Visa debit success response {}", visaDebitResult);

            visaBill
                .responseJson(Collections.emptyMap())
                .responseTime(Instant.now())
                .extId(String.valueOf(visaDebitResult.getData().longValue()))
                .success(true)
                .responseText("Success");

            visaBill = billRepository.save(visaBill);

            log.debug("Visa debit bill saved : {}", visaBill);

            // Start doing Forex

            AbsForexTransactionRequest forexTransactionDTO = new AbsForexTransactionRequest();
            forexTransactionDTO.setRequestId("BILL_MASTER_" + System.currentTimeMillis() + "_" + transaction.getId());
            forexTransactionDTO.setMfo(isApiForexParam.getMfo());
            forexTransactionDTO.setAccCl(isApiForexParam.getAccCl());
            forexTransactionDTO.setAccCo(isApiForexParam.getAccCo());
            forexTransactionDTO.setAmountCo(transaction.getSenderAmount());
            forexTransactionDTO.setDeal(8);
            forexTransactionDTO.setFxDeal(isApiForexParam.getFxDeal());

            Bill forexBill = p2pHelperService.createP2pBill(transaction, RequestType.P2P_DEBIT, P2pProcessorType.FOREX, forexTransactionDTO, forexTransactionDTO.getRequestId());
            transaction.setSenderBillId(forexBill.getId());

            String purpose = isApiForexParam.getPurpose();
            PurposeDTO purposeDTO = createPurpose(transaction, comment, isApiForexParam.getMfo(), senderDTO, recipientDTO,true);
            setPurpose(purposeDTO, forexTransactionDTO, purpose);

            log.info("Forex request: {}", forexTransactionDTO);

            BmResultData<AbsTransactionResult> forexResultData = nciHelperService.doForexTransaction(forexTransactionDTO);

            if (!forexResultData.isSuccess()) {
                log.warn("Forex response is not success : {}", forexResultData);
                forexBill
                    .responseTime(Instant.now())
                    .responseText(String.valueOf(forexResultData.getMessage()))
                    .success(false);
                billRepository.save(forexBill);

                // Error
                log.error("Forex debit throw exception : {}", forexResultData);

                RowType_ExecTransaction_Request reverseParameters = new RowType_ExecTransaction_Request();
                reverseParameters.setCARD(visaPanOrToken);
                reverseParameters.setBANK_C(this.cardSuiteTietoProperties.getDefaultBankC());
                reverseParameters.setGROUPC(this.cardSuiteTietoProperties.getDefaultGroupC());
                reverseParameters.setPAYMENT_MODE("1");
                reverseParameters.setCARD_ACCT_CCY("USD");
                reverseParameters.setTRAN_TYPE("113");
                reverseParameters.setTRAN_CCY("USD");
                reverseParameters.setTRAN_AMNT(BigDecimal.valueOf(transaction.getSenderAmount()));

                Bill visaReverseBill = p2pHelperService.createP2pBill(transaction, RequestType.P2P_CREDIT, P2pProcessorType.VISA, reverseParameters);

                CommonResultData<BigDecimal> visaReverseResult = cardSuiteTietoService.credit(String.valueOf(visaReverseBill.getId()), visaPanOrToken, transaction.getSenderAmount());

                if (visaReverseResult == null) {
                    String emptyResultText = "Visa reverse returned empty result";
                    log.warn(emptyResultText);
                    transaction.setStatus(TransactionStatus.S_CANCEL_FAILED);
                    return errorResult(visaReverseBill, emptyResultText);
                }

                if (visaReverseResult.getData() == null || !visaReverseResult.isSuccess()) {
                    log.warn("Visa reverse response is NOT success : {}", visaReverseResult);
                    transaction.setStatus(TransactionStatus.S_CANCEL_FAILED);
                    return this.errorResult(visaReverseBill, visaReverseResult.getMessage());
                }

                log.info("Visa reverse success response {}", visaReverseResult);

                visaReverseBill
                    .responseJson(Collections.emptyMap())
                    .responseTime(Instant.now())
                    .extId(String.valueOf(visaReverseResult.getData().longValue()))
                    .success(true)
                    .responseText("Success");

                visaReverseBill = billRepository.save(visaBill);

                return new BmResultData<>(forexResultData);
            }

            forexBill
                .responseJson(Collections.singletonMap(NciTransResponseJsonKeys.TRANS_ID.name(), String.valueOf(forexResultData.getData().getTransId())))
                .responseTime(Instant.now())
                .extId(forexResultData.getData().getTransId())
                .success(true)
                .responseText(forexResultData.getData().getErrorNote());

            billRepository.save(forexBill);
            log.info("forex forexBill {}", forexBill);

            return new BmResultData<>(BmResultStatus.SUCCESS, forexBill);
        } catch (Exception e) {
            log.error("credit throw exception : {}", e.getMessage());
            return this.errorResult(e.getMessage());
        }
    }

    private BmResultData<Map<String, Object>> errorResult(BillHandler handler, String exceptionMessage) {
        return errorResult(handler, BmResultStatus.INSUFFICIENT_FUNDS_ON_SENDER, exceptionMessage);
    }

    private BmResultData<Map<String, Object>> errorResult(BillHandler handler, BmResultStatus error, String exceptionMessage) {
        log.error("return errorResult: {}", exceptionMessage);
        handler.error(Collections.singletonMap("error", exceptionMessage));
        return new BmResultData<>(error != null ? error : BmResultStatus.PC_ERROR, Collections.singletonMap("error", exceptionMessage));
    }

    private BmResultData<Map<String, String>> errorResult(BmResultStatus error, String exceptionMessage) {
        log.error("return errorResult: {}", exceptionMessage);
        return new BmResultData<>(error != null ? error : BmResultStatus.PC_ERROR, Collections.singletonMap("error", exceptionMessage));
    }

    private BmResultData<Bill> errorResult(Bill bill, String exceptionMessage) {

        log.warn("Exception when crediting : {}", exceptionMessage);

        bill
            .responseTime(Instant.now())
            .responseText(exceptionMessage)
            .success(false)
        ;
        billRepository.save(bill);

        if (bill.getType() == RequestType.P2P_CREDIT) {
            return new BmResultData<>(BmResultStatus.RECIPIENT_PC_ERROR, bill);
        } else if (bill.getType() == RequestType.P2P_DEBIT) {
            return new BmResultData<>(BmResultStatus.SENDER_PC_ERROR, bill);
        } else {
            return new BmResultData<>(BmResultStatus.PC_ERROR, bill);
        }
    }

    private BmResultData<Bill> errorResult(String exceptionMessage) {
        log.warn("Exception when crediting : {}", exceptionMessage);
        return new BmResultData<>(BmResultStatus.UNKNOWN_ERROR, exceptionMessage);
    }

    public BmResultData<Bill> reverse(Transaction transaction, RequestType type) {
        log.info("Visa reverse with type : {}", type);
        //visa-debit
        //visa_reverse_p2p_debit

        //visa_reverse_p2p_credit
        //visa-credit
        boolean isDebit = RequestType.P2P_DEBIT == type;
        String extId = UUID.randomUUID().toString();
        Bill visaBill = new Bill()
            .recipientShortName(VISA_REVERSE + type.name().toLowerCase(Locale.ROOT))
            .type(RequestType.CANCEL)
            .amountRatio(transaction.getAmountRatio())
            .requestTime(Instant.now())
            .responseTime(Instant.now())
            .login(transaction.getRequestLogin())
            .success(false)
            .transactionId(transaction.getId())
            .outExtId(extId)
            .account(isDebit ? transaction.getSenderAccount() : transaction.getRecipientAccount());
        billRepository.save(visaBill);

        CommonResultData<BigDecimal> resultData;

        if (isDebit) {
            List<Bill> bills = billRepository.findAllBillsWithShortname(debitBillNames, transaction.getId());
            if (bills.size() == 1 && bills.get(0).getType() == RequestType.P2P_DEBIT) {
                visaBill
                    .requestJson(Collections.singletonMap(REQUEST, transaction.getSenderAccount()));

                log.info("visa credit request : extId {} , pan {}, amount in cents {}", extId, transaction.getSenderAccount(), transaction.getSenderAmount());
                resultData = cardSuiteTietoService.credit(extId, transaction.getSenderAccount(), transaction.getSenderAmount());
                log.info("visa response : {}", resultData);
                return validateVisaResultAndReturn(extId, visaBill, resultData);
            } else {
                return new BmResultData<>(BmResultStatus.PC_ERROR, "In db found :" + bills.size());
            }
        } else {

            List<Bill> bills = billRepository.findAllBillsWithShortname(creditBillNames, transaction.getId());
            if (bills.size() == 1 && bills.get(0).getType() == RequestType.P2P_CREDIT) {
                visaBill
                    .requestJson(Collections.singletonMap(REQUEST, transaction.getRecipientAccount()));
                log.info("visa debit request : extId {} , pan {}, amount in cents {}", extId, transaction.getRecipientAccount(), transaction.getRecipientAmount());
                resultData = cardSuiteTietoService.debit(extId, transaction.getRecipientAccount(), transaction.getRecipientAmount());
                log.info("visa response : {}", resultData);
                return validateVisaResultAndReturn(extId, visaBill, resultData);
            } else {
                return new BmResultData<>(BmResultStatus.PC_ERROR, "In db found :" + bills.size());
            }
        }
    }

    private BmResultData<Bill> validateVisaResultAndReturn(String extId, Bill visaBill, CommonResultData<BigDecimal> creditResult) {
        if (creditResult != null && creditResult.isSuccess()) {

            visaBill
                .responseJson(Collections.singletonMap(RESPONSE, creditResult))
                .responseTime(Instant.now())
                .extId(extId)
                .success(true)
                .responseText(creditResult.getMessage());

            visaBill = billRepository.save(visaBill);

            log.debug("Final save bill: {}", visaBill);

            return new BmResultData<>(visaBill);
        }
        visaBill
            .responseTime(Instant.now())
            .responseText(creditResult != null ? creditResult.getMessage() : "visa return null")
            .success(false);

        log.debug("Final save bill: {}", visaBill);

        billRepository.save(visaBill);
        return new BmResultData<>(BmResultStatus.PC_ERROR, visaBill);
    }

    public PurposeDTO createPurpose(Transaction transaction, String comment, String mfo, P2pDTO senderDTO, P2pDTO recipientDTO, boolean debit) {
        PurposeDTO purposeDTO = new PurposeDTO();
        purposeDTO.setCcyRate(transaction.getCcyRate());
        purposeDTO.setTranIdOfSOPC(debit?String.valueOf(transaction.getSenderBillId()):transaction.getSenderTrId());
        purposeDTO.setTranId(transaction.getId());
        purposeDTO.setSenderFio(senderDTO.getName());
        purposeDTO.setRecipientFio(recipientDTO == null ? null : recipientDTO.getName());
        purposeDTO.setSenderAmount(transaction.getSenderAmount());
        purposeDTO.setExtComment(comment);
        purposeDTO.setMfo(mfo);
        purposeDTO.setSenderAccount(transaction.getSenderAccount());
        purposeDTO.setSenderName(transaction.getSenderName());
        purposeDTO.setCategory(transaction.getCategory());
        purposeDTO.setComment(transaction.getComment());
        purposeDTO.setDate(transaction.getDate());
        purposeDTO.setAmountRatio(transaction.getAmountRatio());
        purposeDTO.setEposMerchant(transaction.getEposMerchant());
        purposeDTO.setEposTerminal(transaction.getEposTerminal());
        purposeDTO.setMain(transaction.getMain());
        purposeDTO.setPromo(transaction.getPromo());
        purposeDTO.setRecipientServiceId(transaction.getRecipientServiceId());
        purposeDTO.setPayType(transaction.getPayType());
        purposeDTO.setGatewayAmount(transaction.getGatewayAmount());
        purposeDTO.setSenderAccount(transaction.getSenderAccount());
        purposeDTO.setRecipientAccount(transaction.getRecipientAccount());
        purposeDTO.setSystemFeeAmount(transaction.getSystemFeeAmount());
        purposeDTO.setSystemCurrency(transaction.getSystemCurrency());
        purposeDTO.setSystemCommissionUpAmount(transaction.getSystemCommissionUpAmount());
        purposeDTO.setSystemCommissionDownAmount(transaction.getSystemCommissionDownAmount());
        purposeDTO.setStatus(transaction.getStatus());
        purposeDTO.setSenderSuccess(transaction.getSenderSuccess());
        purposeDTO.setSenderStatusText(transaction.getSenderStatusText());
        purposeDTO.setSenderServiceId(transaction.getSenderServiceId());
        purposeDTO.setSenderGateway(transaction.getSenderGateway());
        purposeDTO.setSenderDate(transaction.getSenderDate());
        purposeDTO.setSenderCcy(transaction.getSenderCcy());
        purposeDTO.setSenderStatus(transaction.getSenderStatus());
        purposeDTO.setSenderBillId(transaction.getSenderBillId());
        purposeDTO.setSenderBean(transaction.getSenderBean());
        purposeDTO.setRoundAmount(transaction.getRoundAmount());
        purposeDTO.setRequestTrId(transaction.getRequestTrId());
        purposeDTO.setRequestSubAccount(transaction.getRequestSubAccount());
        purposeDTO.setRequestLogin(transaction.getRequestLogin());
        purposeDTO.setRequestGroup(transaction.getRequestGroup());
        purposeDTO.setRequestCcy(transaction.getRequestCcy());
        purposeDTO.setRequestAmount(transaction.getRequestAmount());
        purposeDTO.setRecipientTrId(transaction.getRecipientTrId());
        purposeDTO.setRecipientSuccess(transaction.getRecipientSuccess());
        purposeDTO.setRecipientStatusText(transaction.getRecipientStatusText());
        purposeDTO.setRecipientName(transaction.getRecipientName());
        purposeDTO.setRecipientGateway(transaction.getRecipientGateway());
        purposeDTO.setRecipientDate(transaction.getRecipientDate());
        purposeDTO.setRecipientCcy(transaction.getRecipientCcy());
        purposeDTO.setRecipientBillId(transaction.getRecipientBillId());
        purposeDTO.setRecipientAmount(transaction.getRecipientAmount());
        purposeDTO.setAgentFeeAmount(transaction.getAgentFeeAmount());

        return purposeDTO;
    }

}
