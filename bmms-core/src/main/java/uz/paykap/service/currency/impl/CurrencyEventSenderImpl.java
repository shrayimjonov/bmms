package uz.paykap.service.currency.impl;

import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;
import tech.jhipster.config.JHipsterDefaults;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.service.currency.CurrencyEventSender;
import uz.paykap.service.currency.CurrencyUpdateEvent;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service("currencyEventSender")
@ConditionalOnProperty(prefix = "application.currencyEventSenderProperties", name = "currencySenderSimulate", havingValue = "false")
public class CurrencyEventSenderImpl implements CurrencyEventSender {

    public static final String API_CACHE_MANAGER_CLEAR_URI = "/api/cache-manager/clear";
    private final ApplicationProperties properties;
    private final RestTemplate restTemplate;

    public CurrencyEventSenderImpl(ApplicationProperties properties, @Qualifier("mbmsRestTemplate") RestTemplate restTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    @Override
    public void send(CurrencyUpdateEvent currencyUpdateEvent) {
        log.info("send cache evict request");
        try {
            HttpEntity<Void> httpEntity = new HttpEntity<Void>(getHeaders());
            restTemplate.exchange(properties.getCurrencyEventSenderProperties().getUrl() + API_CACHE_MANAGER_CLEAR_URI, HttpMethod.DELETE, httpEntity, Void.class);
            log.info("send success");
        } catch (Exception e) {
            log.error("send error");
            e.printStackTrace();
        }
    }

    public HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String authString = properties.getCurrencyEventSenderProperties().getUsername() + ":" + properties.getCurrencyEventSenderProperties().getPassword();
        String authStringEnc = new String(Base64Utils.encode(authString.getBytes()), StandardCharsets.UTF_8);
        headers.add("BASIC-AUTHORIZATION", authStringEnc);

        return headers;
    }
}
