package uz.paykap.aop.pusher;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import uz.javlon.pusher.Constants;
import uz.javlon.pusher.dto.PushType;
import uz.javlon.pusher.service.PusherService;
import uz.paykap.service.dto.AgentDTO;

/**
 * Aspect for logging execution of service and repository Spring components.
 * <p>
 * By default, it only runs with the "dev" profile.
 */
@Slf4j
@Aspect
@ConditionalOnBean(value = {PusherService.class})
public class PusherAspect {

    private final PusherService pusherService;

    public PusherAspect(PusherService pusherService) {
        this.pusherService = pusherService;
        log.info("##### Pusher Aspect : ON #####");
    }

    /**
     * Pointcut that matches all Spring beans in the application's main packages.
     */
    @Pointcut("within(uz.paykap.repository..*)" +
        " || within(uz.paykap.service..*)" +
        " || within(uz.paykap.web.rest..*)")
    public void applicationPackagePointcut() {
        // Method is empty as this is just a Pointcut, the implementations are in the advices.
    }

    /**
     * Advice that logs methods throwing exceptions.
     *
     * @param joinPoint join point for advice
     * @param e         exception
     */
    @AfterThrowing(pointcut = "applicationPackagePointcut()", throwing = "e")
    public void pushAfterThrowing(JoinPoint joinPoint, Throwable e) {
        pusherService.sendAsync("Exception in " + joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName() + "() with cause = \'" + (e.getCause() != null ? e.getCause() : "NULL") + "\'", PushType.GROUP, Constants.ERRORS);
    }

    @AfterReturning(pointcut = "execution(* uz.paykap.service.AgentService.save(..))", returning = "agent")
    public void afterAgentCreate(JoinPoint jp, AgentDTO agent) {
        pusherService.sendAsync("Agent updated : " + agent.toString(), PushType.GROUP, Constants.ADMINS, Constants.ADMINS + ".agent");
    }
}
