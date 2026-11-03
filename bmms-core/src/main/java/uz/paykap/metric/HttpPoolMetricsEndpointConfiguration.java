package uz.paykap.metric;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsEndpointAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>JHipsterMetricsEndpointConfiguration class.</p>
 */
@Configuration
@AutoConfigureAfter(MetricsEndpointAutoConfiguration.class)
public class HttpPoolMetricsEndpointConfiguration {

    /**
     * <p>httpPoolMetricsEndpoint.</p>
     *
     * @param meterRegistry a {@link MeterRegistry} object.
     * @return a {@link HttpPoolMetricsEndpoint} object.
     */
    @Bean
    @ConditionalOnBean({MeterRegistry.class})
    @ConditionalOnMissingBean
    @ConditionalOnAvailableEndpoint
    public HttpPoolMetricsEndpoint httpPoolMetricsEndpoint(MeterRegistry meterRegistry) {
        return new HttpPoolMetricsEndpoint(meterRegistry);
    }
}
