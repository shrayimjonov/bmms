package uz.devops.bm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ForwardConfiguration {

    @Bean("forwardRest")
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
