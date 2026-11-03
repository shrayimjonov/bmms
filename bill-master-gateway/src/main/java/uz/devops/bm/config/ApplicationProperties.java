package uz.devops.bm.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to Bill Master Gw.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 * See {@link tech.jhipster.config.JHipsterProperties} for a good example.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

    private String userDefaultPassword = "12345";

    private ContentProperties content;

    @Getter
    @Setter
    public static class ContentProperties {

        private String uri;
        private String protocol;
        private Integer port;
        private String path;
    }
}
