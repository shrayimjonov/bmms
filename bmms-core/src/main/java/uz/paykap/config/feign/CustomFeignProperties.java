package uz.paykap.config.feign;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
public class CustomFeignProperties {

    private String uaaUrl;
    private String paymsUrl;
    private String contractmsUrl;
}
