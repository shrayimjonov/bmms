package uz.paykap.config;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import lombok.AllArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@AllArgsConstructor
public class MbmsRestConfiguration {

    @Bean("mbmsRestTemplate")
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
           .setConnectTimeout(Duration.of(10, ChronoUnit.SECONDS))
           .setReadTimeout(Duration.of(10, ChronoUnit.SECONDS))
           .build();
    }
}
