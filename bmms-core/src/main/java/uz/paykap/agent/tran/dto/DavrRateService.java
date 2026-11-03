package uz.paykap.agent.tran.dto;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uz.devops.nci.isApi.dto.response.DavrRateResponseDTO;
import uz.devops.nci.isApi.service.NciIsApiService;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.agent.tran.CachedCbuService;
import uz.paykap.domain.enumeration.Currency;
import uz.devops.bmcore.cache.CacheEvictable;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
@ConditionalOnProperty(prefix = "rateService", name = "impl", havingValue = "davrRateService")
public class DavrRateService implements RateService, CacheEvictable {

    private final NciIsApiService nciIsApiService;

    public Optional<BigDecimal> sellRate(Currency sellCcy) {

        DavrRateResponseDTO sellRate = getAllActual().get(sellCcy.name());

        if (sellRate == null || sellRate.getMobileSellRate() == null) {
            return Optional.empty();
        }

        return Optional.of(BigDecimal.valueOf(sellRate.getMobileSellRate()).setScale(8, RoundingMode.CEILING));
    }

    public Optional<BigDecimal> buyRate(Currency buyCcy) {

        DavrRateResponseDTO buyRate = getAllActual().get(buyCcy.name());

        if (buyRate == null || buyRate.getMobilePurchaseRate() == null) {
            return Optional.empty();
        }

        return Optional.of(BigDecimal.valueOf(buyRate.getMobilePurchaseRate()).setScale(8, RoundingMode.CEILING));
    }

    @Override
    public Optional<BigDecimal> sellRate(Currency sellCcy, String ccyGroup) {
        return sellRate(sellCcy);
    }

    @Override
    public Optional<BigDecimal> buyRate(Currency buyCcy, String ccyGroup) {
        return buyRate(buyCcy);
    }

    @Override
    public BigDecimal convert(Currency from, Long fromAmountCents, Currency to) {

        if (from == to) {
            log.debug("Do not convert, bca ccy codes are same : {} : {}", from, to);
            return BigDecimal.valueOf(fromAmountCents);
        }

        DavrRateResponseDTO davrFromCcy = getAllActual().get(from.name());
        DavrRateResponseDTO davrToCcy = getAllActual().get(to.name());

        Long mobilePurchaseRate = davrToCcy.getMobilePurchaseRate();
        Long mobileSellRate = davrFromCcy.getMobileSellRate();

        // TODO -

        return BigDecimal.valueOf(mobileSellRate).multiply(BigDecimal.valueOf(fromAmountCents)).divide(BigDecimal.valueOf(mobilePurchaseRate), 2, RoundingMode.CEILING);
    }

    @Override
    public BigDecimal convert(Currency from, BigDecimal fromAmountCents, Currency to) {

        if (Objects.equals(from, to)) {
            log.debug("Do not convert, bca ccy codes are same : {} : {}", from, to);
            return fromAmountCents;
        }

        return convert(from, fromAmountCents.longValue(), to);
    }

    @Override
    public BigDecimal convert(Integer from, Long fromAmountCents, Integer to) {

        if (Objects.equals(from, to)) {
            log.debug("Do not convert, bca ccy codes are same : {} : {}", from, to);
            return BigDecimal.valueOf(fromAmountCents);
        }

        return convert(Currency.get(from), fromAmountCents, Currency.get(to));
    }

    @PostConstruct
    public void init() {
        getAllActual();
    }

    @Override
    @CacheEvict(value = {CachedCbuService.GET_ALL_ACTUAL}, allEntries = true)
    public void evictAll() {
        log.info("Evicting Davr rates...");
    }

    @Cacheable(value = CachedCbuService.GET_ALL_ACTUAL, sync = true)
    public Map<String, DavrRateResponseDTO> getAllActual() {

        Map<String, DavrRateResponseDTO> rates = nciIsApiService.getExchangeRate();

        if (rates == null || rates.isEmpty()) {
            log.debug("Got null or empty rates");
            return Collections.emptyMap();
        }

        log.debug("Got response from Davr exchange rates service : {}", rates.size());

        return rates;
    }

    @Scheduled(cron = "0 */15 * * * *")
    public void updateAllCurrency() {

        log.debug("Updating Davr rates");

        Map<String, DavrRateResponseDTO> allActual = getAllActual();

        if (allActual != null && !allActual.isEmpty()) {
            evictAll();
            getAllActual();
            log.info("Rates updated from Davr");
        } else {
            log.warn("Could not update rates from Davr");
        }

    }
}
