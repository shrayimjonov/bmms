package uz.paykap.gateway.nci.forex;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.dto.GatewayResult;
import uz.devops.bmcore.dto.request.SharedCancelDTO;
import uz.devops.bmcore.dto.request.SharedCheckDTO;
import uz.devops.bmcore.dto.request.SharedPayDTO;
import uz.devops.bmcore.gateway.CancelHandler;
import uz.devops.bmcore.gateway.GatewayBillingService;
import uz.devops.bmcore.gateway.GatewayResultHandler;
import uz.devops.bmcore.gateway.PrepayHandler;
import uz.paykap.gateway.BaseGatewayBillingService;
import uz.paykap.service.DepositService;
import uz.paykap.service.abs.AbstractAbsService;
import uz.paykap.service.abs.request.AbsBlackListRequest;
import uz.paykap.service.abs.request.AbsClientRequest;
import uz.paykap.service.abs.request.AbsForexTransactionRequest;
import uz.paykap.service.abs.request.NciForexClientResultDTO;
import uz.paykap.service.abs.response.*;
import uz.paykap.service.dto.result.BmResultData;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service(NciForexTransactionGatewayServiceV3.NAME)
@AllArgsConstructor
public class NciForexTransactionGatewayServiceV3 extends BaseGatewayBillingService implements GatewayBillingService {

    public static final String NAME = "NciForexTransactionGatewayServiceV3";
    public static final String WALLET_PAN = "WALLET_PAN";
    public static final String WALLET_HOLDER = "WALLET_HOLDER";
    public static final int TINKOFF_TRANSFER = 228;
    public static final String TINKOFF_PURPOSE = "Зачисление перевода на счет %s, %s с карты Тинькоффбанк";
    public static final String SUCCESS = "0";
    private final AbstractAbsService nciHelperService;
    private final DepositService depositService;

    @Override
    public GatewayResult check(@NotNull @NotNull SharedCheckDTO sharedCheckDTO, @NotNull @NotNull GatewayResultHandler gatewayResultHandler) {
        if (sharedCheckDTO == null || sharedCheckDTO.getCheckDTO() == null || StringUtils.isEmpty(sharedCheckDTO.getCheckDTO().getAccount())) {
            //return invalid params
            log.warn("invalid params");
            return errorResult(NciForexErrorCodes.INVALID_PARAMS.getCodeString(), NciForexErrorCodes.INVALID_PARAMS.getDesc());
        }

        if (!Objects.equals(sharedCheckDTO.getCheckDTO().getCurrency(), 860) || sharedCheckDTO.getCheckDTO().getParams() == null || !Objects.equals(sharedCheckDTO.getCheckDTO().getParams().get("RCV_C"), "UZS") || !Objects.equals(sharedCheckDTO.getCheckDTO().getParams().get("SNT_C"), "RUB")){
            log.warn("CURRENCY NOT SUPPORTED");
            return errorResult(NciForexErrorCodes.CURRENCY_NOT_SUPPORTED.getCodeString(), NciForexErrorCodes.CURRENCY_NOT_SUPPORTED.getDesc());
        }
        /*//verify agent deposit
        Long amount = sharedCheckDTO.getRecipientParams().getRecipientMinAmount();
        if (sharedCheckDTO.getGatewayAmount() != null) {
            amount = sharedCheckDTO.getGatewayAmount().longValue();
        }
        Boolean canAgentProceed = canAgentProceed(amount);
        if (!canAgentProceed) {
            log.warn("insufficient funds BM agent deposit");
            return errorResult(NciForexErrorCodes.INSUFFICIENT_FUNDS_DEPOSIT.getCodeString(), NciForexErrorCodes.INSUFFICIENT_FUNDS_DEPOSIT.getDesc());
        }

        //get sender account from recipient serviceId
        String recipientServiceId = sharedCheckDTO.getRecipientParams().getRecipientServiceId();
        if (StringUtils.isEmpty(recipientServiceId)) {
            log.warn("sender's account not found");
            return errorResult(NciForexErrorCodes.SENDER_ACCOUNT_IS_EMPTY.getCodeString(), NciForexErrorCodes.SENDER_ACCOUNT_IS_EMPTY.getDesc());
        }

        Long settlementLAmount = null;
        if (sharedCheckDTO.getCheckDTO().getParams().containsKey(TinkofRequestKey.SETTLEMENT_AMOUNT_VALUE.getStringValue())) {
            String settlementAmount = sharedCheckDTO.getCheckDTO().getParams().get(TinkofRequestKey.SETTLEMENT_AMOUNT_VALUE.getStringValue());

            try {
                settlementLAmount = Long.parseLong(settlementAmount);
            } catch (Exception e) {
                log.warn("double.parseDouble(settlementAmount) throw exception: {}", e.getMessage());
                settlementLAmount = null;
            }
        }

        //verify nci credit account balance - sendGetAccountRequest
        Boolean senderAccountYesDeposit = verifySenderAccountDeposit(settlementLAmount, sharedCheckDTO.getTranId(), recipientServiceId);
        if (senderAccountYesDeposit != null && !senderAccountYesDeposit) {
            log.warn("sender account not found or not balance");
            return errorResult(NciForexErrorCodes.SENDER_ACCOUNT_NOT_FOUND.getCodeString(), NciForexErrorCodes.SENDER_ACCOUNT_NOT_FOUND.getDesc());
        }*/

        //get Nci Client id by phone number
        NciForexClientResultDTO client = getClient(sharedCheckDTO.getCheckDTO().getAccount(), sharedCheckDTO.getTranId());
        if (client == null) {
            log.warn("client not found");
            return errorResult(NciForexErrorCodes.CLIENT_NOT_FOUND.getCodeString(), NciForexErrorCodes.CLIENT_NOT_FOUND.getDesc());
        }
        if (!Objects.equals(client.getResponseCode(), NciForexErrorCodes.SUCCESS.getCodeString())){
            log.warn(client.getResponseText());
            return errorResult(client.getResponseCode(), client.getResponseText());
        }

        // verify account - sendGetAccountRequest
        AbsAccountResult account = getAccount(sharedCheckDTO.getTranId(), generateUzsCashBackAccountId(client.getClient()));
        if (account == null) {
            log.warn("receiver account not found");
            return errorResult(NciForexErrorCodes.ACCOUNT_NOT_FOUND.getCodeString(), NciForexErrorCodes.ACCOUNT_NOT_FOUND.getDesc());
        }

        //check client for black List
        AbsBlackListRequest clientInBlackList = getBlackListRequest(sharedCheckDTO, client.getClient());
        BmResultData<List<AbsBlackListResult>> blackListData = nciHelperService.getBlackList(clientInBlackList);
        if (!blackListData.isSuccess()) {
            String message = blackListData.getStatus() != null && !StringUtils.isEmpty(blackListData.getStatus().getMessage()) ? blackListData.getStatus().getMessage() : NciForexErrorCodes.NCI_GET_BLACK_LIST_ERROR.getDesc();
            log.warn("get black list error: {}", message);
            return errorResult(NciForexErrorCodes.NCI_GET_BLACK_LIST_ERROR.getCodeString(), message);
        }
        if (blackListData.getData() != null && !blackListData.getData().isEmpty()) {
            log.warn(">>> CLIENT IN BLACK LIST! <<<");
            return errorResult(NciForexErrorCodes.CLIENT_IN_BLACK_LIST.getCodeString(), NciForexErrorCodes.CLIENT_IN_BLACK_LIST.getDesc());
        }

        String fio = client.getClient().getFamily() + " " + client.getClient().getFirstName() + " " + client.getClient().getPatronymic();
        Map<String, Object> responseJson = new HashMap<>();
        responseJson.put(WALLET_HOLDER, fio);
        responseJson.put(WALLET_PAN, account.getId());

        log.info("return success result: {}", responseJson);
        return new GatewayResult()
            .success(true)
            .responseText("SUCCESS")
            .responseCode("0")
            .responseTime(Instant.now())
            .responseJson(responseJson);
    }

    @Override
    public GatewayResult pay(@NotNull @NotNull SharedPayDTO sharedPayDTO, @NotNull @NotNull PrepayHandler prepayHandler) {
        log.info("nci forex transaction pay : {}", sharedPayDTO);
        boolean paramsValidated = validatePayParams(sharedPayDTO, prepayHandler);
        if (!paramsValidated) {
            log.warn("invalid params");
            return errorResult(NciForexErrorCodes.INVALID_PARAMS.getCodeString(), NciForexErrorCodes.INVALID_PARAMS.getDesc());
        }

        if (sharedPayDTO.getPayDTO().getParams() == null || !Objects.equals(sharedPayDTO.getPayDTO().getParams().get("RCV_C"), "UZS") || !Objects.equals(sharedPayDTO.getPayDTO().getParams().get("SNT_C"), "RUB")){
            log.warn("CURRENCY NOT SUPPORTED");
            return errorResult(NciForexErrorCodes.CURRENCY_NOT_SUPPORTED.getCodeString(), NciForexErrorCodes.CURRENCY_NOT_SUPPORTED.getDesc());
        }

        AbsForexTransactionRequest transactionDTO = getNciForexTransactionDTO(sharedPayDTO, prepayHandler);
        log.info("send nci doForexTransaction(): {}", transactionDTO);
        BmResultData<AbsTransactionResult> resultData = nciHelperService.doForexTransaction(transactionDTO);
        log.info("nci return result: {}", resultData);

        if (resultData == null || !resultData.isSuccess() || resultData.getData() == null){
            log.warn("nci do forex transaction error: {}", resultData);
            return errorResult(NciForexErrorCodes.TRANSACTION_FAILED.getCodeString(), NciForexErrorCodes.TRANSACTION_FAILED.getCodeString());
        }

        AbsTransactionResult transactionResult = resultData.getData();
        if (!Objects.equals(transactionResult.getErrorCode(), SUCCESS)){
            String message = StringUtils.isEmpty(transactionResult.getErrorNote()) ? transactionResult.getErrorNote() : NciForexErrorCodes.TRANSACTION_FAILED.getDesc();
            log.warn("nci doForexTransaction error: {}", message);
            return errorResult(NciForexErrorCodes.TRANSACTION_FAILED.getCodeString(), message);
        }

        Map<String, Object> responseJson = new HashMap<>();
        responseJson.put("NCI_CHECK_REQUEST_ID", transactionResult.getCheckRequestId());
        responseJson.put("NCI_TRANS_ID", transactionResult.getTransId());
        responseJson.put("NCI_ERROR_CODE", transactionResult.getErrorCode());
        responseJson.put("NCI_ERROR_NOTE", transactionResult.getErrorNote());

        log.info("return success result: {}", responseJson);
        return new GatewayResult()
            .success(true)
            .responseText("SUCCESS")
            .responseCode("0")
            .extId(transactionResult.getTransId())
            .responseTime(Instant.now())
            .responseJson(responseJson);
    }

    @Override
    public GatewayResult cancel(@NotNull @NotNull SharedCancelDTO sharedCancelDTO, @NotNull @NotNull CancelHandler cancelHandler) {
        log.info("do cancel for sharedCancelDTO: {}", sharedCancelDTO);
        boolean paramsVerified = verifyCancelParams(sharedCancelDTO);
        if (!paramsVerified){
            log.warn("invalid params");
            return errorResult(NciForexErrorCodes.INVALID_PARAMS.getCodeString(), NciForexErrorCodes.INVALID_PARAMS.getDesc());
        }

        Long transId = Long.parseLong(sharedCancelDTO.getRecipientTrId());
        BmResultData<AbsTransactionResult> cancelResultData = nciHelperService.sendCancelRequest(transId, "BILL_MASTER_" + System.currentTimeMillis() + "_" + sharedCancelDTO.getTranId(),"", NciUpdateTranGroupType.FOREX);

        if (cancelResultData == null || !cancelResultData.isSuccess() || cancelResultData.getData() == null){
            log.warn("nci cancel transaction error. Result data is null");
            return errorResult(NciForexErrorCodes.CANCEL_TRANSACTION_FAILED.getCodeString(), NciForexErrorCodes.CANCEL_TRANSACTION_FAILED.getDesc());
        }

        AbsTransactionResult transactionResult = cancelResultData.getData();
        if (!Objects.equals(transactionResult.getErrorCode(), SUCCESS)){
            log.warn("nci cancel transaction error. Result is NOT SUCCESS");
            String message = !StringUtils.isEmpty(transactionResult.getErrorNote()) ? transactionResult.getErrorNote() : NciForexErrorCodes.CANCEL_TRANSACTION_FAILED.getDesc();
            return errorResult(NciForexErrorCodes.CANCEL_TRANSACTION_FAILED.getCodeString(), message);
        }

        Map<String, Object> responseJson = new HashMap<>();
        responseJson.put("CHECK_REQUEST_ID", transactionResult.getCheckRequestId());
        responseJson.put("TRANS_ID", transactionResult.getTransId());
        responseJson.put("ERROR_CODE", transactionResult.getErrorCode());
        responseJson.put("ERROR_NOTE", transactionResult.getErrorNote());

        GatewayResult result = new GatewayResult();
        result.setSuccess(true);
        result.setResponseText("SUCCESS");
        result.setResponseCode("0");
        result.setResponseTime(Instant.now());
        result.setResponseJson(responseJson);

        log.info("return reverse result: {}", result);
        return result;
    }

    private boolean verifyCancelParams(SharedCancelDTO sharedCancelDTO) {
        if (sharedCancelDTO == null){
            return false;
        }
        if (StringUtils.isEmpty(sharedCancelDTO.getRecipientTrId()) || !NumberUtils.isNumber(sharedCancelDTO.getRecipientTrId())){
            return false;
        }

        try {
            Long.parseLong(sharedCancelDTO.getRecipientTrId());
        } catch (Exception e){
            log.warn("Long.parseLong(sharedCancelDTO.getRecipientTrId()) throw exception: {}", e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public Long getDefaultCheckAmountIfNull() {
        return 0L;
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

    /*private Boolean canAgentProceed(Long amount) {
        try {
            SimpleDepositDTO agentDeposit = depositService.getAgentDeposit();
            if (agentDeposit != null && amount != null) {
                return agentDeposit.getBalance() >= amount;
            }
            return false;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }*/

    private AbsBlackListRequest getBlackListRequest(SharedCheckDTO sharedCheckDTO, AbsClientResult client) {
        AbsBlackListRequest nciHelperBlackListRequest = new AbsBlackListRequest();
        nciHelperBlackListRequest.setPhoneMobile(sharedCheckDTO.getCheckDTO().getAccount());
        nciHelperBlackListRequest.setPassportNumber(client.getPassportNumber());
        nciHelperBlackListRequest.setPassportSerial(client.getPassportSerial());
        nciHelperBlackListRequest.setPinFl(client.getPinFl());
        nciHelperBlackListRequest.setNumberTaxRegistration(client.getNumberTaxRegistration());
        nciHelperBlackListRequest.setFirstName(client.getFirstName());
        nciHelperBlackListRequest.setFamily(client.getFamily());
        nciHelperBlackListRequest.setPatronymic(client.getPatronymic());
        nciHelperBlackListRequest.setBlackCode("3");
        nciHelperBlackListRequest.setTypeBlack("1");
        nciHelperBlackListRequest.setRequestId("BILL_MS_" + System.currentTimeMillis() + "_" + sharedCheckDTO.getTranId());

        return nciHelperBlackListRequest;
    }

    /*private Boolean verifySenderAccountDeposit(Long settlementLAmount, Long requestId, String senderAccount) {
        if (settlementLAmount == null) {
            return null;
        }

        // verify sender account - sendGetAccountRequest
        NciHelperAccountResult account = getAccount(requestId, senderAccount);
        if (account == null || account.getSOutTmp() == null) {
            return null;
        }
        long balanceAmount = 0L;
        try {
            balanceAmount = Long.parseLong(account.getSOutTmp());
        } catch (Exception e) {
            log.warn("Long.parseLong(s_out_tmp) throw exception: {}", e.getMessage());
            return null;
        }
        return balanceAmount >= settlementLAmount;
    }*/

    private String generateUzsCashBackAccountId(AbsClientResult client) {
        String cashBackAccount = "22616";
        cashBackAccount = cashBackAccount + "000";
        cashBackAccount = cashBackAccount + "%";
        cashBackAccount = cashBackAccount + client.getTransId();
        cashBackAccount = cashBackAccount + "001";

        return cashBackAccount;
    }

    private AbsAccountResult getAccount(Long requestId, String account) {
        BmResultData<AbsAccountResult> accountData = nciHelperService.sendGetAccountRequest(requestId, account, null);
        if (!accountData.isSuccess() || accountData.getData() == null) {
            log.warn("account not found");
            return null;
        }
        AbsAccountResult accountResult = accountData.getData();
        if (StringUtils.isEmpty(accountResult.getId())) {
            log.warn("account transId not found");
            return null;
        }

        log.debug("return account: {}", accountData.getData());
        return accountData.getData();
    }

    private NciForexClientResultDTO getClient(String account, Long requestId) {
        AbsClientRequest clientDTO = new AbsClientRequest();
        clientDTO.setPhoneMobile(account);
        clientDTO.setRequestId(requestId);
        BmResultData<List<AbsClientResult>> clientData = nciHelperService.getClient(clientDTO);

        if (!clientData.isSuccess() || clientData.getData() == null) {
            log.warn("client not found");
            return new NciForexClientResultDTO(NciForexErrorCodes.CLIENT_NOT_FOUND.getCodeString(), NciForexErrorCodes.CLIENT_NOT_FOUND.getDesc(), null);
        }
        List<AbsClientResult> clientList = clientData.getData();
        if (clientList.size() != 1) {
            log.warn("found many clients");
            return new NciForexClientResultDTO(NciForexErrorCodes.FIND_MANY_CLIENTS.getCodeString(), NciForexErrorCodes.FIND_MANY_CLIENTS.getDesc(), null);
        }
        AbsClientResult clientResult = clientList.get(0);
        if (StringUtils.isEmpty(clientResult.getTransId())) {
            log.warn("client id not found");
            return new NciForexClientResultDTO(NciForexErrorCodes.CLIENT_ID_NOT_FOUND.getCodeString(), NciForexErrorCodes.CLIENT_ID_NOT_FOUND.getDesc(), null);
        }

        log.debug("return client: {}", clientResult);
        return new NciForexClientResultDTO(NciForexErrorCodes.SUCCESS.getCodeString(), NciForexErrorCodes.SUCCESS.getDesc(), clientResult);
    }
    private Boolean validatePayParams(SharedPayDTO sharedPayDTO, PrepayHandler prepayHandler) {
        if (sharedPayDTO == null) {
            return false;
        }
        if (sharedPayDTO.getPayDTO() == null) {
            return false;
        }
        if (sharedPayDTO.getPayDTO().getParams() == null) {
            return false;
        }
        if (sharedPayDTO.getPayDTO().getParams().get(TinkofRequestKey.SETTLEMENT_AMOUNT_VALUE.getStringValue()) == null) {
            return false;
        }
        if (sharedPayDTO.getPayDTO().getParams().get(TinkofRequestKey.RECEIVING_AMOUNT_VALUE.getStringValue()) == null) {
            return false;
        }
        if (sharedPayDTO.getPayDTO().getParams().get(TinkofRequestKey.SELL_RATE_VALUE.getStringValue()) == null) {
            return false;
        }
        if (sharedPayDTO.getRecipientParams() == null || StringUtils.isEmpty(sharedPayDTO.getRecipientParams().getRecipientServiceId())){
            return false;
        }
        try {
            Long.parseLong(String.valueOf(sharedPayDTO.getPayDTO().getParams().get(TinkofRequestKey.SETTLEMENT_AMOUNT_VALUE.getStringValue())));
            Long.parseLong(String.valueOf(sharedPayDTO.getPayDTO().getParams().get(TinkofRequestKey.RECEIVING_AMOUNT_VALUE.getStringValue())));
            Double.parseDouble(String.valueOf(sharedPayDTO.getPayDTO().getParams().get(TinkofRequestKey.SELL_RATE_VALUE.getStringValue())));
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    private AbsForexTransactionRequest getNciForexTransactionDTO(SharedPayDTO sharedPayDTO, PrepayHandler prepayHandler) {
        Long amountCo = Long.parseLong(String.valueOf(sharedPayDTO.getPayDTO().getParams().get(TinkofRequestKey.SETTLEMENT_AMOUNT_VALUE.getStringValue())));

        Long amountCl = Long.parseLong(String.valueOf(sharedPayDTO.getPayDTO().getParams().get(TinkofRequestKey.RECEIVING_AMOUNT_VALUE.getStringValue())));

        Double rate = Double.parseDouble(String.valueOf(sharedPayDTO.getPayDTO().getParams().get(TinkofRequestKey.SELL_RATE_VALUE.getStringValue())));

        AbsForexTransactionRequest transactionDTO = new AbsForexTransactionRequest();
        transactionDTO.setAccCl(sharedPayDTO.getRecipientParams().getRecipientServiceId());
        transactionDTO.setAmountCl(amountCo);
        transactionDTO.setAccCo(String.valueOf(prepayHandler.getCheckResponseJson().get(WALLET_PAN)));
        transactionDTO.setAmountCo(amountCl);
        transactionDTO.setExchangeRate(rate);
        transactionDTO.setFxDeal(TINKOFF_TRANSFER);

        transactionDTO.setDocNum(String.valueOf(sharedPayDTO.getTranId()));
        transactionDTO.setRequestId("BILL_MASTER_" + System.currentTimeMillis() + "_" + sharedPayDTO.getTranId());

        String account = prepayHandler.getCheckResponseJson() != null && !StringUtils.isEmpty(String.valueOf(prepayHandler.getCheckResponseJson().get(WALLET_PAN))) ? String.valueOf(prepayHandler.getCheckResponseJson().get(WALLET_PAN)) :  "22616000%";
        String fio = prepayHandler.getCheckResponseJson() != null && !StringUtils.isEmpty(String.valueOf(prepayHandler.getCheckResponseJson().get(WALLET_HOLDER))) ? String.valueOf(prepayHandler.getCheckResponseJson().get(WALLET_HOLDER)) : "ФИО";
        transactionDTO.setPurpose(String.format("Зачисление перевода на счет %s, %s с карты Тинькоффбанк", account, fio));

        return transactionDTO;
    }
}

