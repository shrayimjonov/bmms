package uz.paykap.gateway.nci;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import uz.paykap.config.ApplicationProperties;
import uz.paykap.gateway.BaseGatewayBillingService;
import uz.paykap.gateway.nci.forex.NciForexErrorCodes;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.service.abs.AbstractAbsService;
import uz.paykap.service.abs.request.AbsMultiTransactionDTO;
import uz.paykap.service.abs.response.AbsTransactionResult;
import uz.paykap.service.abs.response.MultiTransactionResultDTO;
import uz.paykap.service.abs.response.NciUpdateTranGroupType;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.p2p.dto.NciTranDTO;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service(NciMultiTransactionGatewayService.NAME)
@AllArgsConstructor
public class NciMultiTransactionGatewayService extends BaseGatewayBillingService implements GatewayBillingService {

    public static final String NAME = "nciMultiTransactionGatewayServiceV3";
    public static final String SUCCESS = "0";
    private final AbstractAbsService nciHelperService;
    private final ApplicationProperties applicationProperties;
    private final ObjectMapper objectMapper;
    private final TransactionRepository transactionRepository;

    @Override
    public GatewayResult check(@NotNull @NotNull SharedCheckDTO sharedCheckDTO, @NotNull @NotNull GatewayResultHandler gatewayResultHandler) {
        log.info("Check skip");
        Map<String, Object> responseJson = new HashMap<>();
        responseJson.put("SUCCESS", "TRUE");
        responseJson.put("SKIP_CHECK", "OK");

        GatewayResult result = new GatewayResult();
        result.setSuccess(true);
        result.setResponseText("OK");
        result.setResponseCode("200");
        result.setResponseTime(Instant.now());
        result.setResponseJson(responseJson);
        result.setExtId("-1");
        log.info("return success result: {}", result);
        return result;
    }

    @Override
    public GatewayResult pay(@NotNull @NotNull SharedPayDTO sharedPayDTO, @NotNull @NotNull PrepayHandler prepayHandler) {
        try {
            log.info("nci transaction pay : {}", sharedPayDTO);
            boolean paramsValidated = validatePayParams(sharedPayDTO);
            if (!paramsValidated) {
                log.warn("invalid params");
                return errorResult(NciForexErrorCodes.INVALID_PARAMS.getCodeString(), NciForexErrorCodes.INVALID_PARAMS.getDesc());
            }
            log.info("prepayHandler checkRequestJson : {}", prepayHandler.getCheckRequestJson());
            log.info("prepayHandler checkResponseJson : {}", prepayHandler.getCheckResponseJson());

            AbsMultiTransactionDTO nciRequest = new AbsMultiTransactionDTO();
            nciRequest.setTransId(sharedPayDTO.getTranId());
            nciRequest.setStateId(applicationProperties.getAbs().getNciTranStateId());

            if (prepayHandler.getCheckRequestJson() == null || prepayHandler.getCheckRequestJson().isEmpty() || !prepayHandler.getCheckRequestJson().containsKey("transactions")) {
                log.warn("invalid check request json is null or empty or transactions key object not found ");
                return errorResult(NciForexErrorCodes.INVALID_PARAMS.getCodeString(), "invalid check request json is null or empty or transactions key object not found ");
            }
            List<NciTranDTO> list = objectMapper.readValue(objectMapper.writeValueAsString(prepayHandler.getCheckRequestJson().get("transactions")), new TypeReference<List<NciTranDTO>>() {
            });
            var transactionOptional = transactionRepository.findById(sharedPayDTO.getTranId());
            log.info("Found Transaction : {}", transactionOptional);
            Long recipientAmount;
            if (transactionOptional.isPresent()) {
                recipientAmount = transactionOptional.get().getRecipientAmount();
            } else {
                recipientAmount = sharedPayDTO.getGatewayAmount().longValue();
            }
            log.info("list : {}", list);
            Long finalRecipientAmount = recipientAmount;
            var filteredNciTran = list
                    .stream()
                    .filter(nciTranDTO -> nciTranDTO.getSum() != null && !nciTranDTO.getSum().isEmpty() && Long.parseLong(nciTranDTO.getSum()) > 0)
                    .sorted(Comparator.comparing(NciTranDTO::getOrder))
                    .peek(nciTranDTO -> {
                        if (nciTranDTO.getAmountPart() != null && nciTranDTO.getAmountPart() != 0) {
                            Long summa = (nciTranDTO.getAmountPart() * finalRecipientAmount) / 100;
                            nciTranDTO.setSum(String.valueOf(summa));
                        }
                    })
                    .collect(Collectors.toList());

            nciRequest.setData(filteredNciTran);

            log.info("do multi tran request : {}", nciRequest);
            BmResultData<List<MultiTransactionResultDTO>> resultData = nciHelperService.doMultiTransaction(nciRequest);
            log.info("nci return result: {}", resultData);

            if (resultData == null || !resultData.isSuccess() || resultData.getData() == null) {
                log.warn("nci do transaction error: {}", resultData);
                return errorResult(NciForexErrorCodes.TRANSACTION_FAILED.getCodeString(), NciForexErrorCodes.TRANSACTION_FAILED.getCodeString());
            }

            Map<String, Object> responseJson = new HashMap<>();
            responseJson.put("NCI_CHECK_REQUEST_ID", sharedPayDTO.getTranId());
            responseJson.put("NCI_TRANS_ID", sharedPayDTO.getTranId());

            log.info("return success result: {}", responseJson);
            return new GatewayResult()
                    .success(true)
                    .responseText("SUCCESS")
                    .responseCode("0")
                    .extId(String.valueOf(sharedPayDTO.getTranId()))
                    .responseTime(Instant.now())
                    .responseJson(responseJson);
        } catch (Exception e) {
            log.info("Multi tran gateway service : {}", e.getMessage());
            return new GatewayResult()
                    .success(false)
                    .responseText("ERROR")
                    .responseCode("-9999")
                    .extId(String.valueOf(sharedPayDTO.getTranId()))
                    .responseTime(Instant.now())
                    .responseJson(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @Override
    public GatewayResult cancel(@NotNull @NotNull SharedCancelDTO sharedCancelDTO, @NotNull @NotNull CancelHandler cancelHandler) {
        log.info("do cancel for sharedCancelDTO: {}", sharedCancelDTO);
        boolean paramsVerified = verifyCancelParams(sharedCancelDTO);
        if (!paramsVerified) {
            log.warn("invalid params");
            return errorResult(NciForexErrorCodes.INVALID_PARAMS.getCodeString(), NciForexErrorCodes.INVALID_PARAMS.getDesc());
        }

        Long transId = Long.parseLong(sharedCancelDTO.getRecipientTrId());
        BmResultData<AbsTransactionResult> cancelResultData = nciHelperService.sendCancelRequest(transId, "BILL_MASTER_" + System.currentTimeMillis() + "_" + sharedCancelDTO.getTranId(), "", NciUpdateTranGroupType.MULTI_INS);

        if (cancelResultData == null || !cancelResultData.isSuccess() || cancelResultData.getData() == null) {
            log.warn("nci cancel transaction error. Result data is null");
            return errorResult(NciForexErrorCodes.CANCEL_TRANSACTION_FAILED.getCodeString(), NciForexErrorCodes.CANCEL_TRANSACTION_FAILED.getDesc());
        }

        AbsTransactionResult transactionResult = cancelResultData.getData();
        if (!Objects.equals(transactionResult.getErrorCode(), SUCCESS)) {
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
        if (sharedCancelDTO == null) {
            return false;
        }
        if (StringUtils.isEmpty(sharedCancelDTO.getRecipientTrId()) || !NumberUtils.isNumber(sharedCancelDTO.getRecipientTrId())) {
            return false;
        }

        try {
            Long.parseLong(sharedCancelDTO.getRecipientTrId());
        } catch (Exception e) {
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

    private Boolean validatePayParams(SharedPayDTO sharedPayDTO) {
        if (sharedPayDTO == null) {
            return false;
        }
        return sharedPayDTO.getPayDTO() != null;
    }
}

