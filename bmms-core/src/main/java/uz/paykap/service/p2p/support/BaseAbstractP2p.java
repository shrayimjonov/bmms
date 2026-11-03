package uz.paykap.service.p2p.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uz.devops.bmcore.enumeration.RequestType;
import uz.paykap.domain.Bill;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.repository.BillRepository;
import uz.paykap.security.SecurityUtils;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.p2p.P2pAccount;
import uz.paykap.service.dto.p2p.P2pRequestScopedContext;
import uz.paykap.service.dto.result.BmResult;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.p2p.dto.P2pDTO;
import uz.paykap.utils.BillUtil;

import javax.validation.ConstraintViolation;
import java.lang.reflect.ParameterizedType;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
public abstract class BaseAbstractP2p<T extends P2pDTO> {

    @Autowired
    protected P2pHelperService p2PHelperService;
    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    private BillRepository billRepository;
    @Autowired
    private P2pRequestScopedContext p2pRequestScopedContext;
    @Autowired
    private LocalValidatorFactoryBean mvcValidator;

    private <TDto> BmResultData<TDto> validateDto(TDto dto) {

//        Set<ConstraintViolation<Object>> violations;
//
//        if (groups == null || groups.length == 0) {
//            violations = mvcValidator.validate(dto);
//        } else {
//            violations = mvcValidator.validate(dto, groups);
//        }

        log.debug("Validating {} with data {}", dto.getClass().getSimpleName(), dto);

        Set<ConstraintViolation<Object>> violations = mvcValidator.validate(dto);

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

    public BmResultData<T> parseAndValidate(JsonNode jsonNode) {

        log.debug("Started parse and validate : {}", jsonNode.toString());

        BmResultData<T> parse = parse(jsonNode);

        if (parse.isSuccess()) {
            log.debug("Parsed OK : {}", parse.getData());
            return validateDto(parse.getData());
        }

        log.debug("Validated error : {}", parse.getMessage());

        return parse;
    }

    public BmResultData<T> parse(JsonNode jsonNode) {

        if (jsonNode == null) {
            // INVALID_PARAMS
            return new BmResultData<>(BmResultStatus.INVALID_REQUEST, "request params is null");
        }

        var validatorClazz = this.getClass();
        var validatorInterface = (ParameterizedType) validatorClazz.getGenericSuperclass();
        var validatorParamsType = validatorInterface.getActualTypeArguments()[0];
        var clazzLoader = applicationContext.getClassLoader();

        if (clazzLoader == null) {
            // INTERNAL_ERROR
            return new BmResultData<>(BmResultStatus.INTERNAL_ERROR, "classLoader is null");
        }

        T dto;
        try {
            var validatorParamsClazz = clazzLoader.loadClass(validatorParamsType.getTypeName());

            dto = (T) objectMapper.treeToValue(jsonNode, validatorParamsClazz);

        } catch (ClassNotFoundException | JsonProcessingException e) {
            // INTERNAL_ERROR
            return new BmResultData<>(BmResultStatus.INTERNAL_ERROR, e.getLocalizedMessage());
        }

        return new BmResultData<>(dto);
    }

    public abstract BmResult isSupported(T dto, P2pAccount p2pAccount);

    public abstract P2pProcessorType getProcessorType();

    protected abstract BmResultData<Map<String, Object>> getInfo(T dto, BillHandler handler);

    public BmResultData<Map<String, Object>> getP2pInfo(T dto) {

        String username;

        if (p2pRequestScopedContext.getUsername() != null) {
            username = p2pRequestScopedContext.getUsername();
        } else {
            username = SecurityUtils.getCurrentUserLogin().orElse("no found");
        }

        P2pAccount recipientAccount = p2pRequestScopedContext.getRecipientAccount();

        Bill bill = createInitialBill(username, recipientAccount.getCurrency(), dto.getAccount(), String.valueOf(getProcessorType()), 0L, 0L);

        return getInfo(dto, new BillHandler() {
            @Override
            public void setRequest(Map<String, Object> request) {
                billRepository.save(
                        bill
                                .requestJson(request)
                                .requestTime(Instant.now())
                );
            }

            @Override
            public void error() {
                billRepository.save(
                        bill
                                .responseJson(Collections.singletonMap("error", "no data"))
                                .responseTime(Instant.now())
                                .success(false)
                );
            }

            @Override
            public void error(String responseCode, String responseText) {
                billRepository.save(
                        bill
                                .responseText(responseText)
                                .responseCode(responseCode)
                                .responseTime(Instant.now())
                                .success(false)
                );
            }

            @Override
            public void error(Map<String, Object> response) {
                billRepository.save(
                        bill
                                .responseJson(response)
                                .responseTime(Instant.now())
                                .success(false)
                );
            }

            @Override
            public void success() {
                billRepository.save(
                        bill
//                        .responseJson(Collections.singletonMap("", "empty response"))
                                .responseTime(Instant.now())
                                .success(true)
                );
            }

            @Override
            public void success(Map<String, Object> response) {
                billRepository.save(
                        bill
                                .responseJson(response)
                                .responseTime(Instant.now())
                                .success(true)
                );
            }

            @Override
            public Long getBillId() {
                return bill.getId();
            }
        });
    }

    protected Bill createInitialBill(String username, Integer currency, String account, String recipientShortName, Long transactionId, Long amount) {

        Bill bill = BillUtil.getInitialBill(username, currency, account, recipientShortName, transactionId, amount, RequestType.CHECK);

        return billRepository.save(bill);
    }

//    protected Double convertToSum(Integer amountInCents) {
//        BigDecimal amountInDollars = BigDecimal.valueOf(amountInCents).divide(BigDecimal.valueOf(100L), 2, RoundingMode.FLOOR);
//        return amountInDollars.doubleValue();
//    }

    protected BillRepository getBillRepository() {
        return billRepository;
    }
}
