package uz.paykap.config.http;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Data
@ConfigurationProperties("http-client")
public class HttpClientProperties {

    private int connectionRequestTimeoutInMillis = 20000;

    /*
     * Determines the timeout in milliseconds until a connection is established.
     * */
    private int connectTimeoutInMillis = 20000;

    /*
     * The timeout for waiting for data
     * */
    private int socketTimeoutInMillis = 20000;
    private int defaultKeepAliveTimeInMillis = 50;
    private int closeIdleConnectionWaitTimeInMillis = 50;

    /**
     * Used to configure both the default max per route as well as the maximum total connections.
     */
    private int maxTotalConnections = 200;
    private int defaultMaxPerRoute = 50;

    private Map<String, Integer> hostnames = new HashMap<>();

    /**
     * Configures a LoggingClientHttpRequestInterceptor and a BufferingClientHttpRequestFactory,
     * so requests and responses can be logged.
     */
    private boolean loggingEnabled = false;
    private boolean trackerLoggingEnabled = true;
}
