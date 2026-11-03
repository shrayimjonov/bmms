package uz.paykap.agent.tran.dto;

import uz.paykap.domain.enumeration.Currency;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public interface RateService {

    enum CcyGroupNames {
        DEFAULT_GROUP(""),
        ATM("ATM"),
        MOBILE_APP("MOBILE_APP"),
        EXCHANGE_OFFICE("EXCHANGE_OFFICE"),
        ABS_FOREX("ABS_FOREX"),
        TKB("TKB"),
        TINKOFF("TINKOFF"),
        CONTACT("CONTACT"),
        OTHER("OTHER")
        ;
        private String group;

        CcyGroupNames(String group) {
            this.group = group;
        }

        public String getGroup() {
            return group;
        }
    }

    Optional<BigDecimal> sellRate(Currency sellCcy);

    Optional<BigDecimal> buyRate(Currency buyCcy);

    Optional<BigDecimal> sellRate(Currency sellCcy, String ccyGroup);

    Optional<BigDecimal> buyRate(Currency buyCcy, String ccyGroup);

    BigDecimal convert(Currency from, Long fromAmountCents, Currency to);

    BigDecimal convert(Currency from, BigDecimal fromAmountCents, Currency to);

    BigDecimal convert(Integer fromCurrency, Long fromAmountCents, Integer toCurrency);

    default BigDecimal convertCentToDollar(BigDecimal amountInCents) {
        return amountInCents.divide(BigDecimal.valueOf(100), 8, RoundingMode.FLOOR);
    }

}
