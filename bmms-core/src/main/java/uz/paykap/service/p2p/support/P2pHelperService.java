package uz.paykap.service.p2p.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uz.devops.bmcore.enumeration.RequestType;
import uz.paykap.config.Constants;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.repository.BillRepository;
import uz.paykap.service.MapService;
import uz.paykap.service.abs.request.AbsTransactionRequest;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.p2p.processor.BaseParam;
import uz.paykap.service.p2p.processor.EposParam;
import uz.paykap.service.p2p.processor.HasEposParam;
import uz.paykap.service.p2p.processor.ProcessorParam;

import javax.validation.ConstraintViolation;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@AllArgsConstructor
public class P2pHelperService {

    private final ObjectMapper objectMapper;
    private final MapService mapService;
    private final LocalValidatorFactoryBean mvcValidator;
    private final BillRepository billRepository;

    public <TParam extends ProcessorParam> BmResultData<TParam> getSenderParams(String params, Class<? extends ProcessorParam> aClass) {
        return getParam2(params, true, aClass);
    }

    public <TParam extends ProcessorParam> BmResultData<TParam> getRecipientParams(String params, Class<? extends ProcessorParam> aClass) {
        return getParam2(params, false, aClass);
    }

    public <TParam extends ProcessorParam> BmResultData<TParam> getParam2(String params, boolean forSender, Class<? extends ProcessorParam> paramClass) {

        ProcessorParam processorParam;

        try {

            log.debug("Parsing {} params from p2pParam.value : \n{} \n{}", forSender ? "sender" : "recipient", params, paramClass.getSimpleName());

            BaseParam baseParam = objectMapper.readValue(params, BaseParam.class);

            log.debug("Parsed param : {}", baseParam);

            processorParam = objectMapper.treeToValue(forSender ? baseParam.getSender() : baseParam.getRecipient(), paramClass);
        } catch (IOException e) {
            // INTERNAL_ERROR
            log.warn("Param not parsed to {}, {}", paramClass.getSimpleName(), e.getLocalizedMessage());
            return new BmResultData<>(forSender ? BmResultStatus.INVALID_SENDER_PARAMS : BmResultStatus.INVALID_RECIPIENT_PARAMS, e.getLocalizedMessage());
        }

        if (processorParam == null) {
            return new BmResultData<>(forSender ? BmResultStatus.INVALID_SENDER_PARAMS : BmResultStatus.INVALID_RECIPIENT_PARAMS, "set p2p_param." + (forSender ? "sender" : "recipient") + " in DB");
        }

        TParam paramDto = (TParam) processorParam;

        log.debug("Validating param object : {}", paramDto);

        BmResultData<TParam> validateResult = validateDto(paramDto);

        if (!validateResult.isSuccess()) {
            log.warn("Validation error : {}", validateResult.getMessage());
            return validateResult;
        }

        log.debug("Validated successfully : {}", paramDto);

        return new BmResultData<>(paramDto);
    }

    private <TDto> BmResultData<TDto> validateDto(TDto dto) {
        Set<ConstraintViolation<Object>> violations = mvcValidator.validate(dto);

        if (violations.isEmpty()) {
            // All OK return parsed DTO
            return new BmResultData<>(dto);
        }

        List<String> collect = violations.stream()
                .map(violation -> String.format("%s : '%s' %s", StreamSupport.stream(violation.getPropertyPath().spliterator(), false).reduce((first, second) -> second).orElse(null),
                        violation.getInvalidValue(), violation.getMessage())).collect(Collectors.toList());

        // INVALID_PARAMS
        return new BmResultData<>(BmResultStatus.INVALID_REQUEST, String.valueOf(collect));
    }

    public BmResultData<HasEposParam> getSenderEposParams(Transaction transaction) {

        log.debug("Get sender epos params, by eposMerchant: {}, eposTerminal: {}", transaction.getEposMerchant(), transaction.getEposTerminal());

        var merchantArray = StringUtils.split(transaction.getEposMerchant(), ">");
        var terminalArray = StringUtils.split(transaction.getEposTerminal(), ">");

        return new BmResultData<>(new EposParam(merchantArray[0], terminalArray[0]));

    }

    public BmResultData<HasEposParam> getRecipientEposParams(Transaction transaction) {
        log.debug("Get sender epos params, by eposMerchant: {}, eposTerminal: {}", transaction.getEposMerchant(), transaction.getEposTerminal());

        var merchantArray = StringUtils.split(transaction.getEposMerchant(), ">");
        var terminalArray = StringUtils.split(transaction.getEposTerminal(), ">");

        EposParam eposParam = new EposParam();

        if (merchantArray.length == 2) {
            eposParam.setMerchantId(merchantArray[1]);
        } else {
            return new BmResultData<>(BmResultStatus.INVALID_RECIPIENT_PARAMS, "epos merchant id not found in " + transaction.getEposMerchant());
        }

        if (terminalArray.length == 2) {
            eposParam.setTerminalId(terminalArray[1]);
        } else {
            return new BmResultData<>(BmResultStatus.INVALID_RECIPIENT_PARAMS, "epos terminal id not found in " + transaction.getEposTerminal());
        }

        return new BmResultData<>(eposParam);
    }

    public void getEposParams(String eposMerchant, String eposTerminal, EposParam senderParam, EposParam recipientParam) {
        log.debug("Get epos params, by eposMerchant: {}, eposTerminal: {}", eposMerchant, eposTerminal);

        if (eposMerchant == null || eposTerminal == null || eposMerchant.isEmpty() || eposTerminal.isEmpty()) {
            log.warn("Epos merchant or terminal null");
            return;
        }

        var merchantArray = StringUtils.split(eposMerchant, ">");
        var terminalArray = StringUtils.split(eposTerminal, ">");

        senderParam.setMerchantId(merchantArray[0]);
        senderParam.setTerminalId(terminalArray[0]);
        if (merchantArray.length == 2) {
            recipientParam.setMerchantId(merchantArray[1]);
        }

        if (terminalArray.length == 2) {
            recipientParam.setTerminalId(terminalArray[1]);
        }

    }

    public void setSenderEpos(Transaction transaction, String merchant, String terminal) {
        transaction.setEposMerchant(merchant);
        transaction.setEposTerminal(terminal);

        log.debug("Set sender Epos merchant : {}", transaction.getEposMerchant());
        log.debug("Set sender Epos terminal : {}", transaction.getEposTerminal());
    }

    public void setRecipientEpos(Transaction transaction, String merchant, String terminal) {
        transaction.setEposMerchant(transaction.getEposMerchant() + ">" + merchant);
        transaction.setEposTerminal(transaction.getEposTerminal() + ">" + terminal);

        log.debug("Set recipient Epos merchant : {}", transaction.getEposMerchant());
        log.debug("Set recipient Epos terminal : {}", transaction.getEposTerminal());
    }

    public Bill createP2pBill(Transaction transaction, RequestType requestType, P2pProcessorType processor, Object requestJson) {
        return createP2pBill(transaction, requestType, processor, requestJson, null);
    }

    public Bill createP2pBill(Transaction transaction, RequestType requestType, P2pProcessorType processor, Object requestJson, String checkStatusId) {

        Map<String, Object> map = null;

        if (requestJson != null) {
            if (requestJson instanceof Map) {
                map = (Map<String, Object>) requestJson;
            } else {
                map = mapService.convert(requestJson);
            }
        }

        if (map == null && checkStatusId != null) {
            map = new HashMap<>(1);
            map.put(Constants.CHECK_STATUS_ID, checkStatusId);
        }

        Bill bill = new Bill()
                .type(requestType)
                .amountRatio(transaction.getAmountRatio())
                .requestTime(Instant.now())
                .responseTime(Instant.now())
                .login(transaction.getRequestLogin())
                .success(false)
                .requestJson(map)
                .transactionId(transaction.getId())
                .outExtId(checkStatusId);

        if (requestType == RequestType.P2P_DEBIT) {
            bill
                    .recipientShortName(processor.name().toLowerCase() + "-debit")
                    .amount(transaction.getSenderAmount())
                    .account(transaction.getSenderAccount())
                    .currency(transaction.getSenderCcy());
        } else if (requestType == RequestType.P2P_CREDIT) {
            bill
                    .recipientShortName(processor.name().toLowerCase() + "-credit")
                    .amount(transaction.getRecipientAmount())
                    .account(transaction.getRecipientAccount())
                    .currency(transaction.getRecipientCcy());
        } else if (requestType == RequestType.CONFIRM) {
            bill
                    .recipientShortName(processor.name().toLowerCase() + "-confirm")
                    .amount(transaction.getRequestAmount())
                    .account(transaction.getRecipientAccount())
                    .currency(transaction.getRecipientCcy());
        } else {
            log.warn("Using request params, bcz {} given instead of {} or {} or {}", requestType, RequestType.P2P_DEBIT, RequestType.P2P_CREDIT, RequestType.CONFIRM);
            bill
                    .recipientShortName(processor.name().toLowerCase())
                    .amount(transaction.getRequestAmount())
                    .account(transaction.getRecipientAccount())
                    .currency(transaction.getRequestCcy());
        }

        bill = billRepository.save(bill);

        log.debug("{} bill saved : {}", requestType, bill);

        return bill;
    }

    public Bill createCommissionBill(Transaction transaction, Integer currency, String recipientShortName, RequestType requestType, AbsTransactionRequest transactionDto, String recipientAccount) {
        Bill bill = new Bill()
                .recipientShortName(recipientShortName)
                .type(requestType)
                .amountRatio(transaction.getAmountRatio())
                .amount(transaction.getSystemCommissionUpAmount())
                .account(recipientAccount)
                .currency(currency)
                .requestTime(Instant.now())
                .responseTime(Instant.now())
                .login(transaction.getRequestLogin())
                .success(false)
                .requestJson(mapService.convert(transactionDto))
                .transactionId(transaction.getId())
                .outExtId(transactionDto.getRequestId());

        bill = billRepository.save(bill);

        log.debug("Bill saved: {}", bill);

        return bill;
    }

    public BmResultData<Bill> saveAndReturnError(Bill bill, String responseText) {
        return saveAndReturnError(bill, responseText, null);
    }

    public BmResultData<Bill> saveAndReturnError(Bill bill, String responseText, Map<String, Object> responseParams) {
        return saveAndReturnError(bill, "FAILED", responseText, responseParams);
    }

    public BmResultData<Bill> saveAndReturnError(Bill bill, String responseCode, String responseText, Map<String, Object> responseParams) {

        bill
                .responseJson(responseParams)
                .responseTime(Instant.now())
                .responseText(responseText)
                .responseCode(responseCode)
                .success(false)
        ;

        return new BmResultData<>(BmResultStatus.SENDER_PC_ERROR, billRepository.save(bill));
    }

    public Double convertToSum(Integer amountInCents) {
        BigDecimal amountInDollars = BigDecimal.valueOf(amountInCents).divide(BigDecimal.valueOf(100L), 2, RoundingMode.FLOOR);
        return amountInDollars.doubleValue();
    }

}
