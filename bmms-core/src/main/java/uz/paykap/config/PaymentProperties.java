package uz.paykap.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import uz.devops.bmcore.enumeration.PayType;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "payment", ignoreUnknownFields = false)
public class PaymentProperties {

    @Getter
    private final Map<PayType, Validation> validations = new HashMap<>(5);

    @Getter
    @Setter
    public static class Validation {
        private Long checkBalanceLimit = 10000000L;
        private Boolean checkBalance = true;
        private Boolean checkExpiry = true;
        private Boolean checkActive = true;

        public Validation setDefaults(boolean checkActive, boolean checkBalance, boolean checkExpiry) {
            setCheckActive(checkActive);
            setCheckBalance(checkBalance);
            setCheckExpiry(checkExpiry);
            return this;
        }
    }

}
