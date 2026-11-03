package uz.paykap.metric;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.Search;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import uz.paykap.config.http.HttpMetricsTracker;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>JHipsterMetricsEndpoint class.</p>
 */
@Slf4j
@WebEndpoint(id = "httppool")
public class HttpPoolMetricsEndpoint {

    private final MeterRegistry meterRegistry;

    public HttpPoolMetricsEndpoint(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * GET /management/httpPoolMetrics
     * <p>
     * Give metrics displayed on Http Poll Metrics page
     *
     * @return a Map with a String defining a category of metrics as Key and
     * another Map containing metrics related to this category as Value
     */
    @ReadOperation
    public Map<String, Map<?, ?>> allMetrics() {

        Map<String, Map<?, ?>> results = new HashMap<>();
        // HTTP requests stats
        results.put("http.pool", this.httpRequestsMetrics());

        return results;
    }

    private Map<String, Object> httpRequestsMetrics() {

        Map<String, Object> resultsGarbageCollector = new HashMap<>();

        Collection<Gauge> gauges = Search.in(this.meterRegistry).name(s -> s.contains(HttpMetricsTracker.METRIC_CATEGORY)).gauges();
        gauges.forEach(gauge -> resultsGarbageCollector.put(gauge.getId().getName(), gauge.value()));

        return resultsGarbageCollector;
    }

}
