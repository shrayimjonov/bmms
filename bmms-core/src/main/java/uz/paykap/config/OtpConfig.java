package uz.paykap.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uz.javlon.commons.base.Result;
import uz.javlon.commons.otp.OtpGenerator;
import uz.javlon.commons.otp.SendStrategy;
import uz.javlon.commons.otp.impl.ConsoleSendStrategy;
import uz.javlon.commons.otp.impl.DefaultOtpGenerator;
import uz.javlon.play.sms.service.PlaySmsService;
import uz.paykap.service.dto.p2p.BmResultStatus;

//@Profile("prod")
@Configuration
public class OtpConfig {

    private final PlaySmsService playSmsService;
    private final ApplicationProperties properties;

    public OtpConfig(PlaySmsService playSmsService, ApplicationProperties properties) {
        this.playSmsService = playSmsService;
        this.properties = properties;
    }

    @Bean("otpGenerator")
    @ConditionalOnProperty(prefix = "application.otp", name = "simulation", havingValue = "false")
    public OtpGenerator otpGenerator() {
        return new DefaultOtpGenerator();
    }

    @Bean("sendStrategy")
    @ConditionalOnProperty(prefix = "application.sms", name = "simulation", havingValue = "false")
    public SendStrategy sendStrategy() {
        return (recipient, content) -> {
            final Result result = playSmsService.send(content, recipient);

            if (result == null || !result.isSuccess()) {
                return BmResultStatus.FAILED;
            } else {
                return BmResultStatus.SUCCESS;
            }
        };
    }

    @Bean("otpGenerator")
    @ConditionalOnProperty(prefix = "application.otp", name = "simulation", havingValue = "true", matchIfMissing = true)
    public OtpGenerator otpGeneratorSimulation() {
        return () -> properties.getOtp().getDefaultSimulationCode();
    }

    @Bean("sendStrategy")
    @ConditionalOnProperty(prefix = "application.sms", name = "simulation", havingValue = "true", matchIfMissing = true)
    public SendStrategy sendStrategySimulation() {
        return new ConsoleSendStrategy();
    }
}
