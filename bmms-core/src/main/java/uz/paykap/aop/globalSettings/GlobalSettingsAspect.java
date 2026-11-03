package uz.paykap.aop.globalSettings;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uz.devops.bmcore.dto.request.CheckRequest;
import uz.devops.bmcore.dto.request.CommonPayRequest;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.utils.SimpleBillUtil;

import static uz.paykap.utils.SimpleBillUtil.checkDisabledResult;
import static uz.paykap.utils.SimpleBillUtil.payDisabledResult;

/**
 * Aspect for toggling check/pay execution.
 */
@Slf4j
@Aspect
@Component
public class GlobalSettingsAspect {

    private final ApplicationProperties applicationProperties;

    public GlobalSettingsAspect(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        log.info("##### GlobalSettings Aspect : ON #####");
    }

    @Pointcut("@annotation(uz.paykap.aop.globalSettings.EnableCheck) && args(simpleCheckDTO,..))")
    public void handleCheckRequest(CheckRequest simpleCheckDTO) {
    }

    @Pointcut("@annotation(uz.paykap.aop.globalSettings.EnablePay) && args(payDTO,..))")
    public void handlePayRequest(CommonPayRequest payDTO) {
    }

    @Pointcut("within(@uz.paykap.aop.globalSettings.PreHandleRequest *)")
    public void beanAnnotatedWithPreHandleRequest() {
    }

    @Pointcut("execution(public * *(..))")
    public void publicMethod() {
    }

    @Around(value = "beanAnnotatedWithPreHandleRequest() && publicMethod()")
    public Object preHandleRequest(ProceedingJoinPoint pjp) throws Throwable {
        log.debug("Pre Handling method");

        if (!applicationProperties.getAllowOnPreHandle()) {
            log.debug("Returning unavailable response...");
            return ResponseEntity.ok(SimpleBillUtil.serviceUnavailableErrorResponse());
        }

        return pjp.proceed();
    }

    @Around(value = "handleCheckRequest(simpleCheckDTO)", argNames = "pjp,simpleCheckDTO")
    public Object handledCheck(ProceedingJoinPoint pjp, CheckRequest simpleCheckDTO) throws Throwable {
        log.debug("Handling check method");

        if (!applicationProperties.getCheckEnabled()) {
            return checkDisabledResult(simpleCheckDTO);
        }
        return pjp.proceed();
    }

    @Around(value = "handlePayRequest(payDTO)", argNames = "pjp,payDTO")
    public Object handledPay(ProceedingJoinPoint pjp, CommonPayRequest payDTO) throws Throwable {
        log.debug("Handling pay method");

        if (!applicationProperties.getPayEnabled()) {
            return payDisabledResult(payDTO);
        }
        return pjp.proceed();
    }


}
