package uz.paykap.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uz.paykap.gateway.common.CommonSender;
import uz.paykap.gateway.common.impl.CommonSenderImpl;
import uz.paykap.gateway.common.impl.CommonSenderSimulation;

@Slf4j
@Configuration
public class SimulationConfig {

    private final RestTemplate restTemplate;

    public SimulationConfig(@Qualifier(DynamicGatewayServiceConfig.DYNAMIC_REST_TEMPLATE) RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Bean("commonSender")
    @ConditionalOnProperty(prefix = "application", name = "simulate-gateway-requests", havingValue = "false")
    public CommonSender commonSender() {
        log.info("############ Simulation OFF ############");
        return new CommonSenderImpl(restTemplate);
    }

    @Bean("commonSender")
    @ConditionalOnProperty(prefix = "application", name = "simulate-gateway-requests", havingValue = "true", matchIfMissing = true)
    public CommonSender CommonSenderSimulation(ObjectMapper objectMapper) {
        log.info("############ Simulation ON ############");
        return new CommonSenderSimulation(objectMapper);
    }

}
