package uz.paykap.aop.unpaged;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

//@Aspect
@Slf4j
//@Component
public class UnPagedAspect {

//    @Pointcut("@annotation(org.springframework.web.bind.annotation.GetMapping) && args(..,pageable,unPaged))")
    public void handleCheckRequest(Pageable pageable, Boolean unPaged) {
    }


    @Around(value = "handleCheckRequest(pageable, unPaged)", argNames = "pjp,pageable,unPaged")
    public Object handledCheck(ProceedingJoinPoint pjp, Pageable pageable, Boolean unPaged) throws Throwable {
        log.info("Handling check method");
        if (unPaged != null && unPaged) {
            return pjp.proceed(replaceArray(pjp.getArgs()));
        }
        return pjp.proceed();
    }

    private Object[] replaceArray(Object[] array) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] instanceof Pageable){
                array[i] = Pageable.unpaged();
            }
        }
        return array;
    }
}