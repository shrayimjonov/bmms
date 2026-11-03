package uz.paykap.aop.common;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import uz.paykap.config.ApplicationProperties;

@Slf4j
//@Aspect
//@Component
public class LoggingRequestAspect {

    private final ApplicationProperties applicationProperties;

    public LoggingRequestAspect(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        log.debug("##### LoggingRequestAspect : ON #####");
    }

//    @Pointcut("@annotation(uz.paykap.aop.common.LogRequest) " +
//        "&& execution(* uz.paykap..*(uz.paykap.service.dto.Billed,..)) " +
//        "&& args(billed,..)"
//    )
//    public void handleBilledRequest(Billed billed) {
//    }

//    @Pointcut("execution(* uz.paykap.service.abs.AbsSimulationService.doTransaction(..))")
//    public void handleBilledRequest() {
//    }

//    @Around(value = "handleBilledRequest(billed)", argNames = "pjp,billed")
//    public Object handledRequest(ProceedingJoinPoint pjp, Billed billed) throws Throwable {
//        log.debug("Handling request to external service : {}", billed);
//
//        return pjp.proceed();
//    }

    @Around(value = "execution(* uz.paykap.service.abs.AbsSimulationService.doTransaction(..))", argNames = "pjp")
    public Object handledRequest(ProceedingJoinPoint pjp) throws Throwable {
        log.debug("Handling request to external service");

        return pjp.proceed();
    }

    @Before("execution(* uz.paykap.service.abs.AbsSimulationService.doTransaction(..))")
    public void logBeforeMethodCall() {
        log.info("Inside Before Advice");
    }

}
