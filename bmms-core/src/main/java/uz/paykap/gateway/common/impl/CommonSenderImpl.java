package uz.paykap.gateway.common.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import uz.devops.bmcore.enumeration.RequestType;
import uz.paykap.config.DynamicGatewayServiceConfig;
import uz.paykap.domain.enumeration.MethodType;
import uz.paykap.gateway.common.CommonSender;

import java.util.Map;

//@Profile("prod")
//@Service("commonSender")
@Slf4j
public class CommonSenderImpl implements CommonSender {

    private final RestTemplate restTemplate;

    public CommonSenderImpl(@Qualifier(DynamicGatewayServiceConfig.DYNAMIC_REST_TEMPLATE) RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<String> sendAndReceive(RequestType requestType, String recipientShortName, MethodType httpMethod, String url, HttpHeaders headers, Map<String, Object> params) {
        return sendAndReceive(requestType, recipientShortName, httpMethod, url, null, headers, params);
    }

    @Override
    public ResponseEntity<String> sendAndReceive(RequestType requestType, String recipientShortName, MethodType httpMethod, String url, String body, HttpHeaders headers, Map<String, Object> params) {
        log.info("\n\n### Sending request to {} by common gateway service!!\n", recipientShortName);
        log.debug("Method         : " + httpMethod);
        log.debug("Built URL      : {}", url);
        log.debug("Built Headers  : {}", headers);
        log.debug("Built Body     : {}", body);
        return restTemplate.exchange(url, resolve(String.valueOf(httpMethod), HttpMethod.POST), new HttpEntity<>(body, headers), String.class);
    }

    @Override
    public ResponseEntity<String> sendAndReceive(RequestType requestType, String recipientShortName, String url, MultiValueMap<String, String> body, HttpHeaders headers, Map<String, Object> params) {
        log.info("### Sending request to {} by common gateway service!!", recipientShortName);
        log.debug("Method         : POST");
        log.debug("Built URL      : {}", url);
        log.debug("Built Headers  : {}", headers);
        log.debug("Built Body     : {}", body);
        return restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
    }

    @Override
    public ResponseEntity<String> sendAndReceive(RequestType requestType, String recipientShortName, String url, HttpHeaders headers, MultiValueMap<String, Object> body, Map<String, Object> params) {
        log.info("### Sending request to {} by common gateway service!!", recipientShortName);
        log.debug("Method         : POST");
        log.debug("Built URL      : {}", url);
        log.debug("Built Headers  : {}", headers);
        log.debug("Built Body     : {}", body);
        return restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
    }

    private HttpMethod resolve(String httpMethod, HttpMethod defaultHttpMethod) {

        if (httpMethod == null) {

            if (defaultHttpMethod == null) {
                return HttpMethod.GET;
            }

            return defaultHttpMethod;
        }

        HttpMethod result = HttpMethod.resolve(httpMethod);

        if (result == null) {

            if (defaultHttpMethod == null) {
                return HttpMethod.GET;
            }

            return defaultHttpMethod;
        }

        return result;
    }


}
