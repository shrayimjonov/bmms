package uz.paykap.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Configuration
public class DynamicGatewayServiceConfig {

//    public static final int DEFAULT_CONNECT_TIMEOUT = 10000;
//    public static final int DEFAULT_READ_TIMEOUT = 10000;
    public static final String DYNAMIC_REST_TEMPLATE = "dynamicRestTemplate";

        @Bean(DYNAMIC_REST_TEMPLATE)
    public RestTemplate getRestTemplate(ApplicationProperties properties) {

        log.info("Common RestTemplate Logging enabled : {}", properties.getCommon().getEnabledRestTemplateLogging());
        log.info("Common RestTemplate ReadTimeout : {}", properties.getCommon().getDefaultReadTimeout());
        log.info("Common RestTemplate ConnectTimeout : {}", properties.getCommon().getDefaultConnectTimeout());

        TrustStrategy acceptingTrustStrategy = (x509Certificates, s) -> true;
        SSLContext sslContext = null;
        try {
            sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
        }

        SSLConnectionSocketFactory csf = null;
        if (sslContext != null) {
            csf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
        }

        CloseableHttpClient httpClient;

        if (properties.getCommon().getProxyEnabled()) {
            httpClient = HttpClients.custom()
                    .setSSLSocketFactory(csf)
                    .setProxy(new HttpHost(properties.getCommon().getProxyHost(), properties.getCommon().getProxyPort()))
                    .build();

        } else {
            httpClient = HttpClients.custom()
                    .setSSLSocketFactory(csf)
                    .build();
        }

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

        requestFactory.setHttpClient(httpClient);
        requestFactory.setReadTimeout(properties.getCommon().getDefaultReadTimeout());
        requestFactory.setConnectTimeout(properties.getCommon().getDefaultConnectTimeout());

        RestTemplate restTemplate;

        if (!Objects.equals(properties.getCommon().getEnabledRestTemplateLogging(), Boolean.FALSE)) {
            restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(requestFactory));
            List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
            interceptors.add(new LoggingRequestInterceptor());
            restTemplate.setInterceptors(interceptors);
        } else {
            restTemplate = new RestTemplate(requestFactory);
        }

        for (HttpMessageConverter converter : restTemplate.getMessageConverters()) {
            if (converter instanceof StringHttpMessageConverter) {
                ((StringHttpMessageConverter) converter).setWriteAcceptCharset(false);
            }
        }

        return restTemplate;
    }

}
