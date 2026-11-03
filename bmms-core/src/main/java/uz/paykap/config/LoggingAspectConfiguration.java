package uz.paykap.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import tech.jhipster.config.JHipsterConstants;
import uz.javlon.pusher.service.PusherService;
import uz.paykap.aop.logging.LoggingAspect;
import uz.paykap.aop.pusher.PusherAspect;

@Configuration
@EnableAspectJAutoProxy
public class LoggingAspectConfiguration {

    @Bean
    @Profile(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT)
    public LoggingAspect loggingAspect(Environment env) {
        return new LoggingAspect(env);
    }

    @Bean
    @ConditionalOnBean(value = {PusherService.class})
    @ConditionalOnProperty(
        prefix = "application.pusher",
        name = {"enabled"},
        havingValue = "true"
    )
    public PusherAspect pusherAspect(PusherService pusherService) {
        return new PusherAspect(pusherService);
    }
}
