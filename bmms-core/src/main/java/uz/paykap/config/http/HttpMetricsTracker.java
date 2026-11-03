package uz.paykap.config.http;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class HttpMetricsTracker {

    private static final String HTTP_METRIC_NAME_PREFIX = "httpcp";
    private static final String METRIC_NAME_LEASED_CONNECTIONS = HTTP_METRIC_NAME_PREFIX + ".connections.leased";
    public static final String METRIC_CATEGORY = "http_pool";
    private static final String METRIC_NAME_AVAILABLE_CONNECTIONS = HTTP_METRIC_NAME_PREFIX + ".connections.available";
    private static final String METRIC_NAME_PENDING_CONNECTIONS = HTTP_METRIC_NAME_PREFIX + ".connections.pending";
    private static final String METRIC_NAME_MAX_CONNECTIONS = HTTP_METRIC_NAME_PREFIX + ".connections.max";

    private final Map<String, HttpPoolStats> poolStatsMap = new ConcurrentHashMap<>();
    private final MeterRegistry meterRegistry;
    private final HttpClientProperties properties;

    @Autowired
    public HttpMetricsTracker(MeterRegistry meterRegistry, HttpClientProperties properties) {
        this.meterRegistry = meterRegistry;
        this.properties = properties;
    }

    // todo - read metrics like in JHipsterMetricsEndpoint

    private void createGauge(String poolName, HttpPoolStats poolStats, MeterRegistry meterRegistry) {

        Gauge.builder(METRIC_NAME_AVAILABLE_CONNECTIONS, poolStats, HttpPoolStats::getAvailable)
                .description("Available connections")
                .tags(METRIC_CATEGORY, poolName)
                .register(meterRegistry);

        Gauge.builder(METRIC_NAME_LEASED_CONNECTIONS, poolStats, HttpPoolStats::getLeased)
                .description("Leased connections")
                .tags(METRIC_CATEGORY, poolName)
                .register(meterRegistry);

        Gauge.builder(METRIC_NAME_PENDING_CONNECTIONS, poolStats, HttpPoolStats::getPending)
                .description("Pending connections")
                .tags(METRIC_CATEGORY, poolName)
                .register(meterRegistry);

        Gauge.builder(METRIC_NAME_MAX_CONNECTIONS, poolStats, HttpPoolStats::getMax)
                .description("Max connections")
                .tags(METRIC_CATEGORY, poolName)
                .register(meterRegistry);

        if (properties.isTrackerLoggingEnabled()) {
            log.info("Http Available connections : {}", poolStats.getAvailable());
            log.info("Http Leased connections : {}", poolStats.getLeased());
            log.info("Http Pending connections : {}", poolStats.getPending());
            log.info("Http Max connections : {}", poolStats.getMax());
        }
    }

    public void add(HttpRoute route, PoolingHttpClientConnectionManager poolingHttpClientConnectionManager) {
        String hostName = route.getTargetHost().getHostName();
        if (!poolStatsMap.containsKey(hostName)) {
            HttpPoolStats httpPoolStats = new HttpPoolStats(route, poolingHttpClientConnectionManager);
            poolStatsMap.put(hostName, httpPoolStats);
            createGauge(hostName, httpPoolStats, meterRegistry);
        }
    }


}
