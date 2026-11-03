package uz.paykap.service.abs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uz.devops.bm.ncitrans.dto.enums.NciTransResponseJsonKeys;
import uz.devops.bmcore.enumeration.RequestType;
import uz.paykap.agent.tran.dto.CalculationService;
import uz.paykap.aop.common.LogRequest;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.config.Constants;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.recipient.p2p.NciAccountRecipientService;
import uz.paykap.repository.BillRepository;
import uz.paykap.service.abs.request.*;
import uz.paykap.service.abs.response.*;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.dto.result.HasCodeAndMessage;
import uz.paykap.service.p2p.dto.P2pAccountDTO;
import uz.paykap.service.p2p.dto.P2pAnorDTO;
import uz.paykap.service.p2p.dto.P2pDTO;
import uz.paykap.service.p2p.dto.P2pForexDTO;
import uz.paykap.service.p2p.dto.response.ConfirmResult;
import uz.paykap.service.p2p.processor.AnorParam;
import uz.paykap.service.p2p.processor.NciForexParam;
import uz.paykap.service.p2p.processor.NciParam;
import uz.paykap.service.p2p.support.BillHandler;
import uz.paykap.service.p2p.support.P2pHelperService;
import uz.paykap.utils.TransactionPrefixGenerator;

import javax.validation.ConstraintViolation;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static uz.paykap.utils.ValueUtil.getFirstValid;

@Slf4j
public abstract class AbstractAbsService {

    public static final String DEFAULT_INN_CL = "999999999";

    public static String TRANSACTION_URL = "/api/bm/gateway/v3/nci/transaction";
    public static String MULTI_TRANSACTION_URL = "/api/bm/gateway/v3/nci/multi-transaction";
    public static String GET_ACCOUNT_URL = "/api/bm/gateway/v3/nci/get/account";
    protected static String GET_CLIENT_URL = "/api/bm/gateway/v3/nci/get/client";
    protected static String GET_BLACK_LIST_URL = "/api/bm/gateway/v3/nci/get/black-list";

    @Autowired
    protected P2pHelperService p2pHelperService;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected BillRepository billRepository;
    @Autowired
    protected ApplicationProperties properties;
    @Autowired
    private LocalValidatorFactoryBean validator;
    @Autowired
    private TransactionPrefixGenerator prefixGenerator;

    public BmResultData<Bill> transfer(Transaction transaction, AbsTransactionRequest nciTranRequest, boolean isSender) {

        log.debug("Creating NCI bill from : {}", nciTranRequest);

        Bill mainBill = p2pHelperService.createP2pBill(transaction, isSender ? RequestType.P2P_DEBIT : RequestType.P2P_CREDIT, P2pProcessorType.NCI, nciTranRequest, nciTranRequest.getRequestId());

        log.debug("Nci p2p bill saved : {}", mainBill);

        BmResultData<AbsTransactionResult> mainAbsTranResult = doTransaction(nciTranRequest);

        mainBill = updateResultIntoBill(mainBill, mainAbsTranResult);

        log.debug("Main bill saved : {}", mainBill);

        return new BmResultData<>(mainAbsTranResult.getStatus(), mainBill, mainAbsTranResult.getDetails());
    }

    public BmResultData<Bill> anorPayTransfer(Transaction transaction, AnorPayTransactionRequest anorPayTransactionRequest) {

        log.debug("Creating AnorPay bill from : {}", anorPayTransactionRequest);

        Bill mainBill = p2pHelperService.createP2pBill(transaction, RequestType.P2P_CREDIT, P2pProcessorType.ANOR, anorPayTransactionRequest, anorPayTransactionRequest.getRequestId());

        log.debug("AnorPay p2p bill saved : {}", mainBill);

        BmResultData<AnorPayTransactionResult> mainAbsTranResult = doAnorPayTransaction(anorPayTransactionRequest);

        mainBill = updateAnorPayResultIntoBill(mainBill, mainAbsTranResult);

        log.debug("Main AnorPay bill saved : {}", mainBill);

        return confirmAnorPayTransaction(transaction, anorPayTransactionRequest, mainAbsTranResult.getData());
    }

    protected Bill updateResultIntoBill(Bill bill, BmResultData<AbsTransactionResult> nciTranResult) {

        bill.responseTime(Instant.now());

        if (nciTranResult == null) {
            bill
                    .success(false)
                    .responseJson(Collections.singletonMap("error", "nciTranResult is null"))
                    .responseCode("FAILED")
            //
            ;
            return billRepository.save(bill);
        }

        if (nciTranResult.getData() != null) {
            Map<String, Object> map = objectMapper.convertValue(nciTranResult.getData(), new TypeReference<>() {
            });
            map.put(NciTransResponseJsonKeys.TRANS_ID.name(), String.valueOf(nciTranResult.getData().getTransId()));

            bill.responseJson(map);
        }

        if (!nciTranResult.isSuccess()) {

            // Error

            if (bill.getRequestJson() == null) {
                bill.responseJson(Collections.singletonMap("error", nciTranResult.getMessage()));
            }

            bill
                    .responseText(StringUtils.substring(nciTranResult.getMessage(), 0, 126))
                    .responseCode("FAILED")
                    .extId(nciTranResult.getData().getTransId())
                    .success(false);

            log.error("nci.doTransaction throw exception...");

        } else {

            // Success

            log.debug("Success, updating bill : {}", bill.getId());

            bill
                    .extId(nciTranResult.getData().getTransId())
                    .responseCode(nciTranResult.getData().getErrorCode())
                    .responseText(nciTranResult.getData().getErrorNote())
                    .success(true);
        }

        return billRepository.save(bill);
    }

    protected Bill updateAnorPayResultIntoBill(Bill bill, BmResultData<AnorPayTransactionResult> anorPayResult) {

        bill.responseTime(Instant.now());

        if (anorPayResult == null) {
            bill
                    .success(false)
                    .responseJson(Collections.singletonMap("error", "anorPayResult is null"))
                    .responseCode("FAILED")
            //
            ;
            return billRepository.save(bill);
        }

        if (anorPayResult.getData() != null) {
            // todo - optimaize
            Map<String, Object> map = objectMapper.convertValue(anorPayResult.getData(), new TypeReference<Map<String, Object>>() {
            });
            map.put(NciTransResponseJsonKeys.TRANS_ID.name(), String.valueOf(anorPayResult.getData().getId()));

            bill.responseJson(map);
        }

        if (!anorPayResult.isSuccess()) {

            // Error

            if (bill.getRequestJson() == null) {
                bill.responseJson(Collections.singletonMap("error", anorPayResult.getMessage()));
            }

            bill
                    .responseText(StringUtils.substring(anorPayResult.getMessage(), 0, 126))
                    .responseCode("FAILED")
                    .extId(anorPayResult.getData().getId())
                    .success(false);

            log.error("nci.doTransaction throw exception...");

        } else {

            // Success

            log.debug("Success, updating bill : {}", bill.getId());

            bill
                    .extId(anorPayResult.getData().getId())
                    .responseCode(anorPayResult.getData().getResultCode())
                    .responseText(anorPayResult.getData().getResultNote())
                    .success(true);
        }

        return billRepository.save(bill);
    }

    @LogRequest
    public abstract BmResultData<AbsTransactionResult> doTransaction(AbsTransactionRequest transactionDTO);

    @LogRequest
    public abstract BmResultData<List<MultiTransactionResultDTO>> doMultiTransaction(AbsMultiTransactionDTO transactionDTO);

    public abstract BmResultData<AnorPayTransactionResult> doAnorPayTransaction(AnorPayTransactionRequest requisitesRequest);

    public BmResultData<String> getPcTransactionIdByExternalId(String id) {
        return new BmResultData<>(BmResultStatus.NOT_IMPLEMENTED);
    }

//    public abstract BmResultData<AbsTransactionResult> checkStatus(AbsStatusRequest absStatusRequest);

    public BmResultData<ConfirmResult> confirm(Transaction transaction, Bill confirmBill) {
        return new BmResultData<>(BmResultStatus.NOT_IMPLEMENTED, "Confirm not implemented");
    }

    public BmResultData<Bill> confirmAnorPayTransaction(Transaction transaction, AnorPayTransactionRequest mainRequest, AnorPayTransactionResult mainResult) {
        return new BmResultData<>(BmResultStatus.NOT_IMPLEMENTED, "Confirm not implemented");
    }

    protected boolean hasConfirm() {
        return false;
    }

    public abstract BmResultData<AbsAccountResult> sendGetAccountRequest(Long requestId, String account, String mfo);

    public abstract BmResultData<AbsAccountResult> sendGetAccountRequest(AbsAccountDTO accountRequestDto);

    public abstract BmResultData<AbsTransactionResult> sendCancelRequest(Long tranId, String requestId, String eposMerchant, NciUpdateTranGroupType tranGroupType);

    public BmResultData<AbsTransactionResult> sendCancelRequest(String tranId) {
        return new BmResultData<>(BmResultStatus.NOT_IMPLEMENTED);
    }

    public abstract BmResultData<AbsTransactionResult> sendCancelRequest(Long tranId, String requestId, NciUpdateTranGroupType groupType);

    public BmResultData<AbsTransactionResult> reverseForex(Long billId) {
        return new BmResultData<>(BmResultStatus.NOT_IMPLEMENTED);
    }

    public BmResultData<Map<String, Object>> getInfo(String account, String mfo, BillHandler handler) {

        try {

//            String account = dto.getAccount();

            log.debug("Get info by account: {}", account);

//            String mfo = dto.getMfo();

            if (mfo == null || mfo.isEmpty()) {
                mfo = properties.getAbs().getMfo();
                log.info("Default MFO {} used", mfo);
            }

            BmResultData<AbsAccountResult> accountResult = this.sendGetAccountRequest(handler.getBillId(), account, mfo);

            log.debug("Get info accountResult : {}", accountResult);

            if (!accountResult.isSuccess()) {
                return errorResult(handler, accountResult.getStatus());
            }

            // Success

            Map<String, Object> responseJson = new HashMap<>();

            responseJson.put(Constants.ACCOUNT_EXISTS_PARAM, "true");
            responseJson.put(Constants.CLIENT_NUMBER_PARAM, accountResult.getData().getClient());
            responseJson.put(Constants.ACCOUNT_NAME_PARAM, accountResult.getData().getName());
            responseJson.put(Constants.FIO_PARAM, accountResult.getData().getName());
            responseJson.put(Constants.ACCOUNT_PARAM, accountResult.getData().getTransId());
            handler.success(responseJson);

            return new BmResultData<>(responseJson);
        } catch (Exception e) {
            handler.error(Collections.singletonMap("error", e.getLocalizedMessage()));
            return new BmResultData<>(BmResultStatus.SENDER_PC_ERROR, e.getLocalizedMessage());
        }
    }

    private BmResultData<Map<String, Object>> errorResult(BillHandler handler, HasCodeAndMessage p2pResultStatus) {
        log.error("Response status: {}", p2pResultStatus);
        handler.error(Collections.singletonMap("error", p2pResultStatus.getMessage()));
        return new BmResultData<>(p2pResultStatus);
    }

    public String getRequestId(String tranId, String requestIdSuffix) {
        return Constants.DEFAULT_BM_PREFIX + Constants.DEFAULT_BM_DELIMITER + System.currentTimeMillis() + Constants.DEFAULT_BM_DELIMITER + tranId + (requestIdSuffix == null || requestIdSuffix.isEmpty() ? "" : Constants.DEFAULT_BM_DELIMITER + requestIdSuffix);
    }

    public AbsTransactionRequest getTransactionDto2(Long tranId, String accCl, String bankCl, String accCo, String requestId, Long amount, String nameCo, String bankCo, String purposeId, String purpose, NciParam nciParam, P2pDTO senderDto, P2pDTO recipientDto) {
        return getTransactionDto2(tranId, accCl, bankCl, "", accCo, requestId, amount, nameCo, bankCo, purposeId, purpose, "", "", "", "", "", nciParam, senderDto, recipientDto);
    }

    public AbsTransactionRequest getTransactionDto2(
            Long tranId,
            String accCl, //  accCl=19997000500001183021, // from account (param)
            String bankCl,
            String nameCl, //  nameCl=IVAN IVANOV, // from name (request if other, else from nci)
            String accCo, //  accCo=22616000990000582001, // to account (request)
            String requestId, //  docNum=24506, // (gen)
            Long amount, //  summa=10000, // amount (request)
            String nameCo, //  nameCo=GRAVITY BUSINESS, // to name (request if other, else from nci)
            String bankCo, //  bankCo=01183, // to mfo (request)
            String purposeId, //  purposeId=00668, // (param)
            String purpose, //  purpose=Комиссия от обработки транзакций за чек № 24506от суммы 10000 // (param + gen),
            String requestIdSuffix,
            String senderName,
            String recipientName,
            String senderAccount,
            String recipientAccount, NciParam nciParam, P2pDTO senderDto,
            P2pDTO recipientDto) {

        return getTransactionDto2(tranId, accCl, bankCl, nameCl, accCo, requestId, amount, nameCo, bankCo, purposeId, purpose, requestIdSuffix, senderName, recipientName, senderAccount, recipientAccount, null, nciParam, senderDto, recipientDto);
    }

    public AbsTransactionRequest getTransactionDto2(
            Long tranId,
            String accCl, //  accCl=19997000500001183021, // from account (param)
            String bankCl,
            String nameCl, //  nameCl=IVAN IVANOV, // from name (request if other, else from nci)
            String accCo, //  accCo=22616000990000582001, // to account (request)
            String requestId, //  docNum=24506, // (gen)
            Long amount, //  summa=10000, // amount (request)
            String nameCo, //  nameCo=GRAVITY BUSINESS, // to name (request if other, else from nci)
            String bankCo, //  bankCo=01183, // to mfo (request)
            String purposeId, //  purposeId=00668, // (param)
            String purpose, //  purpose=Комиссия от обработки транзакций за чек № 24506от суммы 10000 // (param + gen),
            String requestIdSuffix,
            String senderName,
            String recipientName,
            String senderAccount,
            String recipientAccount, Map<String, String> additionalPurposeVariables,
            NciParam nciParam,
            P2pDTO senderDto,
            P2pDTO recipientDto) {

        //  date=2021-01-13 16:59:52.449, // (gen) // TODO - check openDate in nci
        //  typeDoc=06 // (static)

        AbsTransactionRequest transactionDTO = new AbsTransactionRequest();

        //used for checkStatus method, get from bill.getRequestJson.get("checkRequestId");
        transactionDTO.setRequestId(requestId);

        transactionDTO.setAccCl(accCl); // From account
        transactionDTO.setNameCl(nameCl); // From name
        transactionDTO.setBankCl(bankCl); // From mfo
        transactionDTO.setAccCo(accCo); // To account
        transactionDTO.setNameCo(nameCo);
        transactionDTO.setBankCo(bankCo);
        transactionDTO.setSumma(amount);
        transactionDTO.setDocNum(String.valueOf(tranId));
        transactionDTO.setDate(Timestamp.from(Instant.now()));
        transactionDTO.setPurposeId(purposeId);
        transactionDTO.setTransactionId(String.valueOf(tranId)); // TODO - replace with docNum, optional
        transactionDTO.setSenderName(senderName);
        transactionDTO.setRecipientName(recipientName);

        transactionDTO.setSenderAccount(senderAccount);
        transactionDTO.setRecipientAccount(recipientAccount);

        if (senderDto != null) {
            transactionDTO.setSenderAccount(senderDto.getAccount());
        }
        if (recipientDto != null) {
            transactionDTO.setRecipientAccount(recipientDto.getAccount());
        }
        if (nciParam != null) {
            transactionDTO.setTranType(nciParam.getTranType());
            transactionDTO.setTax(nciParam.getTax());
        }

        if (purpose.contains("${")) {
            Map variables = objectMapper.convertValue(transactionDTO, Map.class);
            if (additionalPurposeVariables != null) {
                variables.putAll(additionalPurposeVariables);
            }
            transactionDTO.setPurpose(new StringSubstitutor(variables).replace(purpose));
        } else {
            transactionDTO.setPurpose(purpose);
        }

        printLog(transactionDTO);

        return transactionDTO;
    }

    private void printLog(Object transactionDTO) {
        if (log.isDebugEnabled()) {
            try {
                log.debug("\n\nBuilt ABS transaction :\n {}\n", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(transactionDTO));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }

    public BmResultData<AbsTransactionRequest> getSenderTran(P2pAccountDTO senderDto, P2pDTO recipientDto, NciParam nciParam, Long tranId, Long amount) {

        String accCl = senderDto.getAccount();
        String bankCl = senderDto.getMfo();
        String nameCl = senderDto.getName();
        String accCo = nciParam.getBankAccount();

//        String bankCo = nciParam.getBankMfo() != null ? nciParam.getBankMfo() : properties.getDefaultMfo();
        Optional<String> bankCoOptional = getFirstValid(nciParam.getBankMfo(), senderDto.getMfo(), properties.getDefaultMfo());
        if (bankCoOptional.isEmpty()) {
            return new BmResultData<>(BmResultStatus.INVALID_PARAMS, "bankCo should be given in p2p_params.bankMfo, dto.mfo or application.defaultMfo");
        }
        String bankCo = bankCoOptional.get();

//        String nameCo = nciParam.getAccName();
        Optional<String> nameCoOptional = getFirstValid(nciParam.getAccName(), senderDto.getName());
        if (nameCoOptional.isEmpty()) {
            return new BmResultData<>(BmResultStatus.INVALID_PARAMS, "nameCo should be given in p2p_params.accName or dto.name");
        }
        String nameCo = nameCoOptional.get();

//        String purposeId = nciParam.getPurposeId();
        Optional<String> purposeIdOptional = getFirstValid(nciParam.getPurposeId(), senderDto.getPurposeId());
        if (purposeIdOptional.isEmpty()) {
            return new BmResultData<>(BmResultStatus.INVALID_PARAMS, "purposeId should be given in p2p_params.purposeId or dto.purposeId");
        }
        String purposeId = purposeIdOptional.get();

        AbsTransactionRequest transactionDTO = new AbsTransactionRequest();

        //used for checkStatus method, get from bill.getRequestJson.get("checkRequestId");
        transactionDTO.setRequestId(prefixGenerator.getP2pDebitPrefix(tranId));

        transactionDTO.setAccCl(accCl); // From account
        transactionDTO.setBankCl(bankCl); // From mfo
        transactionDTO.setNameCl(nameCl); // From name
        transactionDTO.setAccCo(accCo); // To account
        transactionDTO.setNameCo(nameCo);
        transactionDTO.setBankCo(bankCo);
        transactionDTO.setSumma(amount);
        transactionDTO.setDocNum(String.valueOf(tranId));
        transactionDTO.setDate(Timestamp.from(Instant.now()));
        transactionDTO.setPurposeId(purposeId);
        transactionDTO.setTransactionId(String.valueOf(tranId));

        transactionDTO.setSenderName(senderDto.getName());
        transactionDTO.setRecipientName(recipientDto.getName());

        transactionDTO.setSenderAccount(senderDto.getAccount());
        transactionDTO.setRecipientAccount(recipientDto.getAccount());

        transactionDTO.setTranType(nciParam.getTranType());
        transactionDTO.setTax(nciParam.getTax());
        transactionDTO.setPurposeComment(senderDto.getPurpose());

        Optional<String> purposeOptional = getFirstValid(nciParam.getPurpose(), senderDto.getPurpose());
        if (purposeOptional.isEmpty()) {
            return new BmResultData<>(BmResultStatus.INVALID_PARAMS, "purpose should be given in p2p_params.purpose or dto.purpose");
        }
        String purpose = purposeOptional.get();

        if (purpose.contains("${")) {
            transactionDTO.setPurpose(new StringSubstitutor(objectMapper.convertValue(transactionDTO, Map.class)).replace(purpose));
            ;
        } else {
            transactionDTO.setPurpose(purpose);
        }

        printLog(transactionDTO);

        return new BmResultData<>(BmResultStatus.SUCCESS, transactionDTO);
    }

    public BmResultData<AbsTransactionRequest> getRecipientTran(P2pDTO senderDto, P2pAccountDTO recipientDto, NciParam nciParam, Long tranId, Long amount) {

        String accCl = nciParam.getBankAccount(); // As recipient is NCI, accCl must be a transit account, like 17403.....001

        Optional<String> nameClOptional = getFirstValid(nciParam.getAccName(), recipientDto.getName());
        if (nameClOptional.isEmpty()) {
            return new BmResultData<>(BmResultStatus.INVALID_PARAMS, "nameCl should be given in p2p_params.bankMfo or dto.mfo");
        }
        String nameCl = nameClOptional.get(); // As recipient is NCI, nameCl must be a transit account name, like 'Транзитный счет ***'

        Optional<String> bankClOptional = getFirstValid(nciParam.getBankMfo(), recipientDto.getMfo());
        if (bankClOptional.isEmpty()) {
            return new BmResultData<>(BmResultStatus.INVALID_PARAMS, "bankCl should be given in p2p_params.bankMfo or dto.mfo");
        }
        String bankCl = bankClOptional.get();

        String accCo = recipientDto.getAccount();

        String bankCo = recipientDto.getMfo();

        Optional<String> nameCoOptional = getFirstValid(recipientDto.getName());
        if (nameCoOptional.isEmpty()) {
            return new BmResultData<>(BmResultStatus.INVALID_PARAMS, "nameCo should be given in dto.name");
        }
        String nameCo = nameCoOptional.get();

        Optional<String> purposeIdOptional = getFirstValid(nciParam.getPurposeId(), recipientDto.getPurposeId());
        if (purposeIdOptional.isEmpty()) {
            return new BmResultData<>(BmResultStatus.INVALID_PARAMS, "purposeId should be given in p2p_params.purposeId or dto.purposeId");
        }
        String purposeId = purposeIdOptional.get();

        AbsTransactionRequest transactionDTO = new AbsTransactionRequest();

        //used for checkStatus method, get from bill.getRequestJson.get("checkRequestId");
        transactionDTO.setRequestId(prefixGenerator.getP2pCreditPrefix(tranId));

        transactionDTO.setAccCl(accCl); // From account
        transactionDTO.setBankCl(bankCl); // From mfo
        transactionDTO.setNameCl(nameCl); // From accName
        transactionDTO.setAccCo(accCo); // To account
        transactionDTO.setNameCo(nameCo);
        transactionDTO.setBankCo(bankCo);
        transactionDTO.setSumma(amount);
        transactionDTO.setDocNum(String.valueOf(tranId));
        transactionDTO.setDate(Timestamp.from(Instant.now()));
        transactionDTO.setPurposeId(purposeId);
        transactionDTO.setTransactionId(String.valueOf(tranId));

        transactionDTO.setSenderName(senderDto.getName());
        transactionDTO.setRecipientName(recipientDto.getName());

        transactionDTO.setSenderAccount(senderDto.getAccount());
        transactionDTO.setRecipientAccount(recipientDto.getAccount());

        transactionDTO.setTranType(nciParam.getTranType());
        transactionDTO.setTax(nciParam.getTax());
        transactionDTO.setPurposeComment(recipientDto.getPurpose());

        Optional<String> purposeOptional = getFirstValid(nciParam.getPurpose(), recipientDto.getPurpose());
        if (purposeOptional.isEmpty()) {
            return new BmResultData<>(BmResultStatus.INVALID_PARAMS, "purpose should be given in p2p_params.purpose or dto.purpose");
        }
        String purpose = purposeOptional.get();

        if (purpose.contains("${")) {
            purpose = new StringSubstitutor(objectMapper.convertValue(transactionDTO, Map.class)).replace(purpose);
        }

        transactionDTO.setPurpose(purpose);

        printLog(transactionDTO);

        return new BmResultData<>(BmResultStatus.SUCCESS, transactionDTO);
    }

    public AnorPayTransactionRequest getAnorPayRecipientTran(P2pAnorDTO recipientDto, P2pDTO senderDto, AnorParam anorParam, String tranId, Long amount, String requestIdSuffix) {

        String accCl = anorParam.getBankAccount();
        String nameCl = anorParam.getAccName();
        String bankCl = anorParam.getBankMfo();

        String accCo = recipientDto.getAccount();
        String bankCo = recipientDto.getMfo();
        String nameCo = recipientDto.getName();
        String purpose = recipientDto.getPurpose();
        String purposeId = recipientDto.getPurposeId();

        AnorPayTransactionRequest transactionDTO = new AnorPayTransactionRequest();

        //used for checkStatus method, get from bill.getRequestJson.get("checkRequestId");
        transactionDTO.setRequestId("BILL_MASTER_" + System.currentTimeMillis() + "_" + tranId + requestIdSuffix);

        transactionDTO.setAccCl(accCl); // From account
        transactionDTO.setNameCl(nameCl); // From accName
        transactionDTO.setBankCl(bankCl); // From mfo
        transactionDTO.setInnCl(DEFAULT_INN_CL); // todo - fix, remove static

        transactionDTO.setAccCo(accCo); // To account
        transactionDTO.setNameCo(nameCo);
        transactionDTO.setBankCo(bankCo);
        transactionDTO.setInnCo(recipientDto.getInn());

        transactionDTO.setSumma(amount);
//        transactionDTO.setPurpose(purpose);
        transactionDTO.setPurposeId(purposeId);
        transactionDTO.setDocNum(tranId);
        transactionDTO.setDate(Timestamp.from(Instant.now()));
        transactionDTO.setTransactionId(tranId);

        transactionDTO.setSenderName(senderDto.getName());
        transactionDTO.setRecipientName(recipientDto.getName());

        transactionDTO.setSenderAccount(senderDto.getAccount());
        transactionDTO.setRecipientAccount(recipientDto.getAccount());

        transactionDTO.setRequestType(RequestType.P2P_CREDIT);
        transactionDTO.setPurposeComment(recipientDto.getPurpose());

        if (purpose.contains("${")) {
            transactionDTO.setPurpose(new StringSubstitutor(objectMapper.convertValue(transactionDTO, Map.class)).replace(purpose));
            ;
        } else {
            transactionDTO.setPurpose(purpose);
        }

        printLog(transactionDTO);

        return transactionDTO;
    }

    public BmResultData<AbsTransactionRequest> getDirectTran(P2pAccountDTO senderDto, P2pAccountDTO recipientDto, NciParam recipientNciParam, String tranId, Long amount, String requestIdSuffix) {

        String accCl = senderDto.getAccount();
        String nameCl = senderDto.getName();
        String bankCl = senderDto.getMfo();

        String accCo = recipientDto.getAccount();

        Optional<String> bankCoOptional = getFirstValid(recipientDto.getMfo());
        if (bankCoOptional.isEmpty()) {
            return new BmResultData<>(BmResultStatus.INVALID_PARAMS, "bankCo should be given in p2p_params.bankMfo or dto.mfo");
        }
        String bankCo = bankCoOptional.get();

        Optional<String> nameCoOptional = getFirstValid(recipientDto.getName());
        if (nameCoOptional.isEmpty()) {
            return new BmResultData<>(BmResultStatus.INVALID_PARAMS, "nameCo should be given in p2p_params.accName or dto.name");
        }
        String nameCo = nameCoOptional.get();

        Optional<String> purposeIdOptional = getFirstValid(recipientNciParam.getPurposeId(), recipientDto.getPurposeId());
        if (purposeIdOptional.isEmpty()) {
            return new BmResultData<>(BmResultStatus.INVALID_PARAMS, "purposeId should be given in p2p_params.purposeId or dto.purposeId");
        }
        String purposeId = purposeIdOptional.get();

        AbsTransactionRequest transactionDTO = new AbsTransactionRequest();

        //used for checkStatus method, get from bill.getRequestJson.get("checkRequestId");
        transactionDTO.setRequestId("BILL_MASTER_" + System.currentTimeMillis() + "_" + tranId + requestIdSuffix);

        transactionDTO.setAccCl(accCl); // From account
        transactionDTO.setBankCl(bankCl); // From mfo
        transactionDTO.setNameCl(nameCl); // From accName
//        transactionDTO.setInnCl(senderDto.getInn());
        transactionDTO.setAccCo(accCo); // To account
        transactionDTO.setNameCo(nameCo);
        transactionDTO.setBankCo(bankCo);
//        transactionDTO.setInnCo(recipientDto.getInn());
        transactionDTO.setSumma(amount);
        transactionDTO.setDocNum(tranId);
        transactionDTO.setDate(Timestamp.from(Instant.now()));
        transactionDTO.setPurposeId(purposeId);
        transactionDTO.setTransactionId(tranId);

        transactionDTO.setTranType(recipientNciParam.getTranType());
        transactionDTO.setTax(recipientNciParam.getTax());
        transactionDTO.setSenderName(senderDto.getName());
        transactionDTO.setSenderAccount(senderDto.getAccount());
        transactionDTO.setRecipientName(recipientDto.getName());
        transactionDTO.setRecipientAccount(recipientDto.getAccount());
        transactionDTO.setPurposeComment(recipientDto.getPurpose());

        Optional<String> purposeOptional = getFirstValid(recipientNciParam.getPurpose(), recipientDto.getPurpose());
        if (purposeOptional.isEmpty()) {
            return new BmResultData<>(BmResultStatus.INVALID_PARAMS, "purpose should be given in p2p_params.purpose or dto.purpose");
        }
        String purpose = purposeOptional.get();

        if (purpose.contains("${")) {
            purpose = new StringSubstitutor(objectMapper.convertValue(transactionDTO, Map.class)).replace(purpose);
        }

        transactionDTO.setPurpose(purpose);

        log.debug("Built ABS transaction : {}", transactionDTO);

        return new BmResultData<>(BmResultStatus.SUCCESS, transactionDTO);
    }

    public abstract BmResultData<AbsTransactionResult> doForexTransaction(AbsForexTransactionRequest transactionDTO);

    protected AbsForexTransactionRequest getForexRequest(Long requestId, String senderBankAccount, Long senderAmount, String recipientBankAccount, Long recipientAmount, Double exchangeRate,
                                                         Integer fxDeal, String purpose, String dollars, NciForexParam nciForexParam, P2pForexDTO p2pForexDTO, Transaction transaction, String exp) {

        AbsForexTransactionRequest forexTransaction = new AbsForexTransactionRequest();
        forexTransaction.setRequestId(prefixGenerator.getP2pCreditPrefix(requestId));
        forexTransaction.setAccCl(senderBankAccount);
        forexTransaction.setSenderPan(transaction.getSenderAccount());
        forexTransaction.setAmountCl(senderAmount);
        forexTransaction.setAccCo(recipientBankAccount);
        forexTransaction.setAmountCo(recipientAmount);
        forexTransaction.setDocNum(String.valueOf(requestId));
        forexTransaction.setExchangeRate(exchangeRate);
//        forexTransaction.setPurpose(purpose);

        forexTransaction.setCodeFilial(nciForexParam.getCodeFilial());
        forexTransaction.setCon(nciForexParam.getCon());
        forexTransaction.setDateExp(exp);
//        forexTransaction.setPaymentType(nciForexParam.getPaymentType());
//        forexTransaction.setIndicator(nciForexParam.getIndicator());
        forexTransaction.setClientId(p2pForexDTO.getClientId());
        forexTransaction.setRollbackIs(nciForexParam.getRollbackIs());
        forexTransaction.setIsMasked(nciForexParam.getIsMasked());
        forexTransaction.setMfo(p2pForexDTO.getMfo());
//        forexTransaction.setOperationCode(fidoValParam.getOperationCode());
//        forexTransaction.setPan(p2pFidoForexDTO.getPan());
//        forexTransaction.setMaskedPan(CardUtil.mask(p2pForexDTO.getPan()));

        if (purpose.contains("${")) {
            forexTransaction.setDollars(dollars);
            forexTransaction.setPurpose(new StringSubstitutor(objectMapper.convertValue(forexTransaction, Map.class)).replace(purpose));
        } else {
            forexTransaction.setPurpose(purpose);
        }

        return forexTransaction;
    }

//    public AbsForexTransactionRequest getForexDebitRequestDTO(P2pFidoForexDTO p2pFidoForexDTO, Transaction transaction, Double exchangeRate) {
//
//        AbsForexTransactionRequest forexTransaction = new AbsForexTransactionRequest();
//        forexTransaction.setRequestId(prefixGenerator.getP2pCreditPrefix(transaction.getId()));
//        forexTransaction.setAccCl(fidoValParam.getAccount());
//        forexTransaction.setMfo(fidoValParam.getCodeFilial());
//
//        forexTransaction.setSenderPan(transaction.getSenderAccount());
//        forexTransaction.setAmountCl(transaction.getSenderAmount());
//
//        forexTransaction.setAccCo(transaction.getRecipientAccount());
//        forexTransaction.setAmountCo(transaction.getRecipientAmount());
//        forexTransaction.setDocNum(String.valueOf(transaction.getId()));
//        forexTransaction.setExchangeRate(p2pFidoForexDTO.getMobileCourse() == null ? transaction.getCcyRate().doubleValue() : p2pFidoForexDTO.getMobileCourse().doubleValue());
//        forexTransaction.setRequestAmount(transaction.getRequestAmount());
//
////        forexTransaction.setCodeFilial(fidoValParam.getCodeFilial());
////        forexTransaction.setCon(fidoValParam.getCon());
//        forexTransaction.setDateExp(p2pFidoForexDTO.getExp());
////        forexTransaction.setPaymentType(fidoValParam.getPaymentType());
////        forexTransaction.setIndicator(fidoValParam.getIndicator());
//        forexTransaction.setClientId(p2pFidoForexDTO.getClientId());
////        forexTransaction.setRollbackIs(fidoValParam.getRollbackIs());
////        forexTransaction.setIsMasked(fidoValParam.getIsMasked());
////        forexTransaction.setOperationCode(fidoValParam.getOperationCode());
//        forexTransaction.setPan(p2pFidoForexDTO.getPan());
//        forexTransaction.setMaskedPan(CardUtil.mask(p2pFidoForexDTO.getPan()));
//
////        if (fidoValParam.getPurpose().contains("${")) {
////            forexTransaction.setDollars(parseCentToDollar(transaction.getSenderAmount()));
////            forexTransaction.setPurpose(new StringSubstitutor(objectMapper.convertValue(forexTransaction, Map.class)).replace(fidoValParam.getPurpose()));
////        } else {
////            forexTransaction.setPurpose(fidoValParam.getPurpose());
////        }
//        return forexTransaction;
//    }

//    public AbsForexTransactionRequest getForexCreditRequestDTO(FidoValParam fidoValParam, P2pFidoForexDTO p2pFidoForexDTO, Transaction transaction, Double exchangeRate) {
//
//        AbsForexTransactionRequest forexTransaction = new AbsForexTransactionRequest();
//        forexTransaction.setRequestId(prefixGenerator.getP2pCreditPrefix(transaction.getId()));
//        forexTransaction.setAccCl(fidoValParam.getAccount());
//        forexTransaction.setMfo(fidoValParam.getCodeFilial());
//
//        forexTransaction.setSenderPan(transaction.getRecipientAccount());
//        forexTransaction.setAmountCl(transaction.getRecipientAmount());
//
//        forexTransaction.setAccCo(transaction.getSenderAccount());
//        forexTransaction.setAmountCo(transaction.getSenderAmount());
//        forexTransaction.setDocNum(String.valueOf(transaction.getId()));
//        forexTransaction.setExchangeRate(p2pFidoForexDTO.getMobileCourse() == null ? transaction.getCcyRate().doubleValue() : p2pFidoForexDTO.getMobileCourse().doubleValue());
//        forexTransaction.setRequestAmount(transaction.getRequestAmount());
//
////        forexTransaction.setCodeFilial(fidoValParam.getCodeFilial());
////        forexTransaction.setCon(fidoValParam.getCon());
////        forexTransaction.setDateExp(p2pFidoForexDTO.getExp());
////        forexTransaction.setPaymentType(fidoValParam.getPaymentType());
////        forexTransaction.setIndicator(fidoValParam.getIndicator());
////        forexTransaction.setClientId(p2pFidoForexDTO.getClientId());
////        forexTransaction.setRollbackIs(fidoValParam.getRollbackIs());
////        forexTransaction.setIsMasked(fidoValParam.getIsMasked());
////        forexTransaction.setOperationCode(fidoValParam.getOperationCode());
//        forexTransaction.setPan(p2pFidoForexDTO.getPan());
//        forexTransaction.setMaskedPan(CardUtil.mask(p2pFidoForexDTO.getPan()));
//
////        if (fidoValParam.getPurpose().contains("${")) {
////            forexTransaction.setDollars(parseCentToDollar(transaction.getSenderAmount()));
////            forexTransaction.setPurpose(new StringSubstitutor(objectMapper.convertValue(forexTransaction, Map.class)).replace(fidoValParam.getPurpose()));
////        } else {
////            forexTransaction.setPurpose(fidoValParam.getPurpose());
////        }
//        return forexTransaction;
//    }

    public AbsForexTransactionRequest getForexDirectRequestDto(NciForexParam nciForexParam, P2pForexDTO p2pForexDTO, Long requestId, String senderAccount, Long senderAmount, String recipientAccount, Long recipientAmount, Double exchangeRate, boolean forexIsSender, Transaction transaction, String exp) {

        return getForexRequest(
                requestId,
                senderAccount,
                senderAmount,
                recipientAccount,
                recipientAmount,
                exchangeRate,
                !Objects.isNull(nciForexParam.getFxDeal()) ? nciForexParam.getFxDeal() : p2pForexDTO.getFxDeal(),
                getFirstValid(nciForexParam.getPurpose(), p2pForexDTO.getComment()).orElse(forexIsSender ? "61061 покупка " + parseCentToDollar(senderAmount) + " USD по курсу " + exchangeRate + " UZS" : "71041 продажа " + parseCentToDollar(recipientAmount) + " USD по курсу " + exchangeRate + " UZS"),
                parseCentToDollar(recipientAmount),
                nciForexParam,
                p2pForexDTO,
                transaction,
                exp
        );
    }

    public AbsForexTransactionRequest getForexRecipientRequestDto(NciForexParam nciForexParam, P2pForexDTO p2pForexDTO, Long requestId, Long senderAmount, String recipientAccount, Long recipientAmount, Double exchangeRate, Transaction transaction, String exp) {

        return getForexRequest(
                requestId,
                nciForexParam.getBankAccount(),
                senderAmount,
                recipientAccount,
                recipientAmount,
                exchangeRate,
                !Objects.isNull(nciForexParam.getFxDeal()) ? nciForexParam.getFxDeal() : p2pForexDTO.getFxDeal(),
                getFirstValid(nciForexParam.getPurpose(), p2pForexDTO.getComment()).orElse("71041 продажа " + parseCentToDollar(recipientAmount) + " USD по курсу " + exchangeRate + " UZS"),
                parseCentToDollar(recipientAmount),
                nciForexParam,
                p2pForexDTO,
                transaction,
                exp
        );

//        AbsForexTransactionRequest forexTransaction = new AbsForexTransactionRequest();
//        forexTransaction.setRequestId(prefixGenerator.getP2pCreditPrefix(requestId));
//        forexTransaction.setAccCl(nciForexParam.getBankAccount());
//        forexTransaction.setAmountCl(senderAmount);
//        forexTransaction.setAccCo(recipientAccount);
//        forexTransaction.setAmountCo(recipientAmount);
//        forexTransaction.setDocNum(String.valueOf(requestId));
//        forexTransaction.setExchangeRate(exchangeRate);
//        forexTransaction.setPurpose("71041 продажа " + parseCentToDollar(recipientAmount) + " USD по курсу " + exchangeRate + " UZS");
//        forexTransaction.setFxDeal(!Objects.isNull(nciForexParam.getFxDeal()) ? nciForexParam.getFxDeal() : p2pForexDTO.getFxDeal());
//
//        return forexTransaction;
    }

    public AbsForexTransactionRequest getForexSenderRequestDto(NciForexParam nciForexParam, P2pForexDTO p2pForexDTO, Long requestId, String senderAccount, Long senderAmount, Long recipientAmount, Double exchangeRate, Transaction transaction, String exp) {

        return getForexRequest(
                requestId,
                senderAccount,
                senderAmount,
                nciForexParam.getBankAccount(),
                recipientAmount, exchangeRate,
                !Objects.isNull(nciForexParam.getFxDeal()) ? nciForexParam.getFxDeal() : p2pForexDTO.getFxDeal(),
                getFirstValid(nciForexParam.getPurpose(), p2pForexDTO.getComment()).orElse("61061 покупка " + parseCentToDollar(senderAmount) + " USD по курсу " + exchangeRate + " UZS"),
                parseCentToDollar(senderAmount),
                nciForexParam,
                p2pForexDTO,
                transaction,
                exp
        );

//        AbsForexTransactionRequest forexTransaction = new AbsForexTransactionRequest();
//        forexTransaction.setRequestId(prefixGenerator.getP2pDebitPrefix(requestId));
//        forexTransaction.setAccCl(senderAccount);
//        forexTransaction.setAmountCl(senderAmount);
//        forexTransaction.setAccCo(nciForexParam.getBankAccount());
//        forexTransaction.setAmountCo(recipientAmount);
//        forexTransaction.setDocNum(String.valueOf(requestId));
//        forexTransaction.setExchangeRate(exchangeRate);
//        forexTransaction.setPurpose("61061 покупка " + parseCentToDollar(senderAmount) + " USD по курсу " + exchangeRate + " UZS");
//        forexTransaction.setFxDeal(!Objects.isNull(nciForexParam.getFxDeal()) ? nciForexParam.getFxDeal() : p2pForexDTO.getFxDeal());
//
//        return forexTransaction;
    }

    private String parseCentToDollar(Long senderAmount) {
        BigDecimal usd = BigDecimal.valueOf(senderAmount).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN);
        return String.valueOf(usd.doubleValue());
    }

    public BmResultData<Bill> errorResult(Bill bill, HasCodeAndMessage codedException) {

        log.warn("Exception when crediting : {}", codedException.getMessage());

        bill
                .responseTime(Instant.now())
                .responseText(codedException.getMessage())
                .success(false)
        ;
        bill = billRepository.save(bill);

        return new BmResultData<>(codedException, bill);
    }

    protected <TDto> BmResultData<TDto> validateDto(TDto dto) {

        Set<ConstraintViolation<Object>> violations = validator.validate(dto);

        if (violations.isEmpty()) {
            // All OK return parsed DTO
            log.debug("OK - No validation errors, returning success result with data : {}", dto);
            return new BmResultData<>(dto);
        }

        List<String> collect = violations.stream()
                .map(violation -> String.format("%s : '%s' %s", StreamSupport.stream(violation.getPropertyPath().spliterator(), false).reduce((first, second) -> second).orElse(null),
                        violation.getInvalidValue(), violation.getMessage())).collect(Collectors.toList());

        log.debug("Validation errors : {}", collect);

        // INVALID_PARAMS
        return new BmResultData<>(BmResultStatus.INVALID_PARAMS, String.valueOf(collect));
    }

    public abstract BmResultData<List<AbsClientResult>> getClient(AbsClientRequest clientDTO);

    public abstract BmResultData<List<AbsBlackListResult>> getBlackList(AbsBlackListRequest nciHelperBlackListRequest);

    public abstract BmResultData<List<AbsCoursesResult>> getCourses(AbsCoursesRequest request);

    public BmResultData<Bill> doCommissionTran(Transaction transaction, P2pDTO senderDto, P2pDTO recipientDto, NciParam nciParam) {

        log.debug("Doing commission tran to NCI : {}", nciParam.getCommission());

        String accCl = nciParam.getBankAccount();
        String bankCl = nciParam.getBankMfo();
        String nameCl = nciParam.getAccName();

        String accCo = nciParam.getCommission().getBankAccount();
        String nameCo = nciParam.getCommission().getAccName();
        String bankCo = nciParam.getCommission().getBankMfo();

        String purposeId = nciParam.getCommission().getPurposeId();
        String purpose = nciParam.getCommission().getPurpose();

        String senderName = senderDto != null ? senderDto.getName() : "";
        String recipientName = recipientDto != null ? recipientDto.getName() : "";

        /*
         * User want to send from owned X account to any PC
         * We are debiting from X account (accCl) and crediting to transit account (accCo),
         * so we store bankCo as merchantId and accCo as terminalId
         * */
        p2pHelperService.setRecipientEpos(transaction, bankCo, accCo);

        // TODO - impl, multi commission
//        Long commissionUpAmount = nciParam.getCommission().getPercent().multiply(BigDecimal.valueOf(transaction.getSystemCommissionUpAmount())).divide(BigDecimal.valueOf(100L), RoundingMode.FLOOR).longValue();

        Map<String, String> purposeVariables = new HashMap<>() {{
            put(NciAccountRecipientService.COMMISSION_PARAM_NAME, CalculationService.decimal2Format.format(Double.valueOf(transaction.getSystemCommissionUpAmount()) / transaction.getRequestAmount() * 100));
            put(NciAccountRecipientService.REQUEST_AMOUNT_PARAM_NAME, CalculationService.decimal2Format.format(Double.valueOf(transaction.getRequestAmount()) / 100d));
//            putAll();
        }};

        AbsTransactionRequest transactionDto = getTransactionDto2(
                transaction.getId(),
                accCl,
                bankCl,
                nameCl,
                accCo,
                String.valueOf(transaction.getId()),
//            prefixGenerator.getP2pCreditCommission(transaction.getId()),
                transaction.getSystemCommissionUpAmount(),
                nameCo,
                bankCo,
                purposeId,
                purpose,
                "_kom",
                senderName,
                recipientName,
                transaction.getSenderAccount(),
                transaction.getRecipientAccount(),
                purposeVariables,
                nciParam,
                senderDto,
                recipientDto
        );

        Bill bill = p2pHelperService.createCommissionBill(transaction, Constants.UZS_CURRENCY, nameCo, RequestType.DEPOSIT, transactionDto, nciParam.getCommission().getBankAccount());

        //used for checkStatus method
        Map<String, Object> requestJson = new HashMap<>();

        if (bill.getRequestJson() != null) {
            requestJson = bill.getRequestJson();
        }

        requestJson.put(Constants.CHECK_STATUS_ID, transactionDto.getRequestId());
        bill.setRequestJson(requestJson);

        bill = billRepository.save(bill);

        BmResultData<AbsTransactionResult> resultData = doTransaction(transactionDto);

        if (!resultData.isSuccess()) {

            // Error
            log.error("nci.doTransaction exception : {}", resultData.getMessage());
            return this.errorResult(bill, resultData.getStatus());
        }

        // Success

        bill
                .responseJson(Collections.singletonMap(NciTransResponseJsonKeys.TRANS_ID.name(), String.valueOf(resultData.getData().getTransId())))
                .responseTime(Instant.now())
                .extId(resultData.getData().getTransId())
                .success(true)
                .responseText(resultData.getData().getErrorNote())
        ;

        bill = billRepository.save(bill);

        log.debug("Final save bill: {}", bill);

        return new BmResultData<>(bill);
    }


}
