package uz.paykap.agent.tran.dto;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uz.devops.cbu.service.dto.json.ICurrency;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.agent.tran.CachedCbuService;
import uz.paykap.domain.enumeration.Currency;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
@ConditionalOnProperty(prefix = "rateService", name = "impl", havingValue = "cbuRateService", matchIfMissing = true)
public class CbuRateService implements RateService {

    private final CachedCbuService cbuService;

    @Override
    public Optional<BigDecimal> sellRate(Currency sellCcy) {
        ICurrency fromCurrency = cbuService.getAllActual().get(sellCcy.name());

        if (fromCurrency == null) {
            return Optional.empty();
        }

        return Optional.of(fromCurrency.getRate());
    }

    @Override
    public Optional<BigDecimal> buyRate(Currency buyCcy) {
        return sellRate(buyCcy);
    }

    @Override
    public Optional<BigDecimal> sellRate(Currency sellCcy, String ccyGroup) {
        return sellRate(sellCcy);
    }

    @Override
    public Optional<BigDecimal> buyRate(Currency buyCcy, String ccyGroup) {
        return buyRate(buyCcy);
    }

    public BigDecimal convert(Currency from, Long fromAmountCents, Currency to) {
        log.debug("Converting {} from {} to {}", fromAmountCents, from, to);

        if (from == to) {
            return BigDecimal.valueOf(fromAmountCents).setScale(8, RoundingMode.CEILING);
        }

        return convert(from, BigDecimal.valueOf(fromAmountCents), to);
    }

    public BigDecimal convert(Currency from, BigDecimal fromAmountCents, Currency to) {
        log.debug("Converting {} from {} to {}", fromAmountCents, from, to);

        if (from == to) {
            return fromAmountCents.setScale(8, RoundingMode.CEILING);
        }

        ICurrency fromCurrency = cbuService.getAllActual().get(from.name());

        if (fromCurrency == null) {
            throw new CodedException(ProblemEnum.OBJECT_NOT_FOUND, "not supported currency " + from);
        }

        ICurrency toCurrency = cbuService.getAllActual().get(to.name());

        if (toCurrency == null) {
            throw new CodedException(ProblemEnum.OBJECT_NOT_FOUND, "not supported currency " + to);
        }

//        BigDecimal fromRate = cbuService.getAllActual().get(from).getRate();
//        BigDecimal toRate = cbuService.getAllActual().get(to).getRate();

        return fromCurrency.getRate().multiply(fromAmountCents)
//            .divide(BigDecimal.valueOf(100), 8, BigDecimal.ROUND_FLOOR)
            .divide(toCurrency.getRate(), 8, RoundingMode.CEILING);
    }

    public BigDecimal convert(Integer fromCurrency, Long fromAmountCents, Integer toCurrency) {
        log.debug("Converting {} from {} to {}", fromAmountCents, fromCurrency, toCurrency);
        return convert(Currency.get(fromCurrency), fromAmountCents, Currency.get(toCurrency));
    }

//
//    public BigDecimal convert(Integer fromCurrency, Long fromAmountCents, Currency toCurrency) {
//        log.debug("Converting {} from {} to {}", fromAmountCents, fromCurrency, toCurrency);
//        return convert(Currency.get(fromCurrency), fromAmountCents, toCurrency);
//    }
//
//    public BigDecimal convert(Currency fromCurrency, Long fromAmountCents, Integer toCurrency) {
//        log.debug("Converting {} from {} to {}", fromAmountCents, fromCurrency, toCurrency);
//        return convert(fromCurrency, fromAmountCents, Currency.get(toCurrency));
//    }

}
