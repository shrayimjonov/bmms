package uz.paykap.config.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * - Supports both HTTP and HTTPS
 * - Uses a connection pool to re-use connections and save overhead of creating connections.
 * - Has a custom connection keep-alive strategy (to apply a default keep-alive if one isn't specified)
 * - Starts an idle connection monitor to continuously clean up stale connections.
 */
@Configuration
@Slf4j
@EnableConfigurationProperties(HttpClientProperties.class)
public class HttpClientConfig {

    @Autowired
    private HttpClientProperties properties;

    public HttpClientConfig(HttpClientProperties properties) {
        this.properties = properties;

        log.info("\n{}\n{}\n{}", "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$", properties, "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory());
        return restTemplate;
    }

    // humoMiddlewareRestTemplate
    // anorhubPluginRestTemplate
    // issuingRestTemplate
    // bmNciLoanRestTemplate
    // paynetLogoRestTemplate
    // bmNciLoanRestTemplate
    // nciIsApiRestTemplate
    // nciTransRestTemplate
    // humoMobileRestTemplate
    // bmTkbRestTemplate
    // davrMunisRestTemplate
    // playRestTemplate

//    @Bean("humoMiddlewareRestTemplate")
//    public RestTemplate humoMiddlewareRestTemplate() {
//
//    }

    @Bean
    RestTemplateCustomizer httpClientCustomizer(HttpClient httpClient) {
        return restTemplate -> {
            ClientHttpRequestFactory requestFactory = restTemplate.getRequestFactory();
            // not-so-proudly copied from the TestRestTemplate code:
            if (requestFactory instanceof InterceptingClientHttpRequestFactory) {
                Field requestFactoryField = ReflectionUtils.findField(RestTemplate.class, "requestFactory");
                ReflectionUtils.makeAccessible(requestFactoryField);
                requestFactory = (ClientHttpRequestFactory) ReflectionUtils.getField(requestFactoryField, restTemplate);
            }
            if (requestFactory instanceof HttpComponentsClientHttpRequestFactory) {
                HttpComponentsClientHttpRequestFactory clientRequestFactory =
                        (HttpComponentsClientHttpRequestFactory) requestFactory;
                clientRequestFactory.setHttpClient(httpClient);
                if (properties.isLoggingEnabled()) {
                    restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(requestFactory));
                    restTemplate.getInterceptors().add(new LoggingClientHttpRequestInterceptor());
                }
            }
        };
    }

    @Bean
    public HttpComponentsClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setHttpClient(httpClient());

        return clientHttpRequestFactory;
    }

    @Bean
    public PoolingHttpClientConnectionManager poolingConnectionManager() {
        SSLContextBuilder builder = new SSLContextBuilder();
        try {
//            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            builder.loadTrustMaterial(null, new TrustAllStrategy());
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            log.error("Pooling Connection Manager Initialisation failure because of " + e.getMessage(), e);
        }

        SSLConnectionSocketFactory sslsf = null;
        try {
            sslsf = new SSLConnectionSocketFactory(builder.build(), new NoopHostnameVerifier());
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            log.error("Pooling Connection Manager Initialisation failure because of " + e.getMessage(), e);
        }

        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
                .<ConnectionSocketFactory>create().register("https", sslsf)
                .register("http", new PlainConnectionSocketFactory())
                .build();

        PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        poolingConnectionManager.setMaxTotal(properties.getMaxTotalConnections());
        poolingConnectionManager.setDefaultMaxPerRoute(properties.getDefaultMaxPerRoute());

        final Map<String, Integer> hostnames = properties.getHostnames();

        if (properties.getHostnames() != null && !properties.getHostnames().isEmpty()) {
            for (Map.Entry<String, Integer> entry : hostnames.entrySet()) {
                poolingConnectionManager.setMaxPerRoute(new HttpRoute(new HttpHost(entry.getKey())), entry.getValue());
            }
        }

        return poolingConnectionManager;
    }

    @Bean
    public ConnectionKeepAliveStrategy connectionKeepAliveStrategy() {
        return (response, context) -> {
            HeaderElementIterator it = new BasicHeaderElementIterator
                    (response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (it.hasNext()) {
                HeaderElement he = it.nextElement();
                String param = he.getName();
                String value = he.getValue();

                if (value != null && param.equalsIgnoreCase("timeout")) {
                    return Long.parseLong(value) * 1000;
                }
            }
            return properties.getDefaultKeepAliveTimeInMillis();
        };
    }

    @Bean
    public CloseableHttpClient httpClient() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(properties.getConnectionRequestTimeoutInMillis())
                .setConnectTimeout(properties.getConnectTimeoutInMillis())
                .setSocketTimeout(properties.getSocketTimeoutInMillis())
                .build();

        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(poolingConnectionManager())
                .setKeepAliveStrategy(connectionKeepAliveStrategy())
                .build();
    }

    @Bean
    public Runnable idleConnectionMonitor(final PoolingHttpClientConnectionManager connectionManager, HttpMetricsTracker httpMetricsTracker) {
        return new Runnable() {
            @Override
            @Scheduled(fixedDelay = 20000)
            public void run() {
                try {
                    if (connectionManager != null) {
                        log.debug("run IdleConnectionMonitor - Closing expired and idle connections...");
                        connectionManager.closeExpiredConnections();
                        connectionManager.closeIdleConnections(properties.getCloseIdleConnectionWaitTimeInMillis(), TimeUnit.SECONDS);

                        if (connectionManager.getRoutes() != null) {
                            for (HttpRoute route : connectionManager.getRoutes()) {
                                httpMetricsTracker.add(route, connectionManager);
                            }
                        }
                    } else {
                        log.debug("run IdleConnectionMonitor - Http Client Connection manager is not initialised");
                    }

                } catch (Exception e) {
                    log.error("run IdleConnectionMonitor - Exception occurred. msg={}, e={}", e.getMessage(), e);
                }
            }
        };
    }

}
