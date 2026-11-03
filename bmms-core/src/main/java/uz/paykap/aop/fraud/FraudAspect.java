package uz.paykap.aop.fraud;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import uz.devops.bmcore.dto.request.CheckRequest;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.javlon.commons.base.ResultData;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.config.Constants;

import java.time.Instant;
import java.util.Objects;

/**
 * Aspect for Oson Fraud.
 */
@Lazy
@Aspect
@Component
@ConditionalOnBean(value = {FraudService.class})
//@ConditionalOnProperty(prefix = "application", name = "fraudEnabled", havingValue = "true")
public class FraudAspect {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ApplicationProperties applicationProperties;
    private final FraudService fraudService;

    public FraudAspect(ApplicationProperties applicationProperties, FraudService fraudService) {
        this.applicationProperties = applicationProperties;
        this.fraudService = fraudService;
        log.info("##### Fraud Aspect : ON #####");
    }

    @Pointcut("@annotation(uz.paykap.aop.fraud.EnableFraud) && args(simpleCheckDTO,..))")
    public void handleCheckRequest4Fraud(CheckRequest simpleCheckDTO) {
    }

    @Around(value = "handleCheckRequest4Fraud(simpleCheckDTO)", argNames = "pjp,simpleCheckDTO")
    public Object handledCheck(ProceedingJoinPoint pjp, CheckRequest simpleCheckDTO) throws Throwable {
        log.debug("Handling check method");

        if (applicationProperties.getFraudEnabled()) {

            long start = System.currentTimeMillis();
            ResultData<String> response = fraudService.getFraudResponse(simpleCheckDTO);
            log.debug("Fraud response : {}", response);
            log.debug("Fraud spent time : {}", System.currentTimeMillis() - start);

            if (!Objects.equals(response.isSuccess(), Boolean.TRUE)) {

                // If fraud detected, return ERROR and message
                return checkDisabledResult(simpleCheckDTO, response.getData());
            }
        }

        // If fraud disabled or fraud not detected continue transaction

        return pjp.proceed();
    }

    private SimpleBillResponse checkDisabledResult(CheckRequest checkDTO, String message) {

        return new SimpleBillResponse()
            .recipientShortName(Constants.SYSTEM_AGENT_NAME)
            .sessionId(checkDTO.getAgentTranId())
            .amountRatio(checkDTO.getAmountRatio())
            .amount(checkDTO.getAmount())
            .account(checkDTO.getAccount())
            .currency(checkDTO.getCurrency())
            .requestTime(Instant.now())
            .responseTime(Instant.now())
            .responseText(message) //Система временна недоступна
            .success(false)
            .status(TransactionStatus.CHECK_FAIL);
    }

}
