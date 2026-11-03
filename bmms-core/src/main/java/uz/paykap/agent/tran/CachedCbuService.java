package uz.paykap.agent.tran;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uz.devops.cbu.service.CbuService;
import uz.devops.cbu.service.dto.json.ICurrency;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.domain.Currency;
import uz.paykap.repository.CurrencyRepository;
import uz.devops.bmcore.cache.CacheEvictable;
import uz.paykap.service.mapper.CurrencyMapper;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
@ConditionalOnProperty(prefix = "rateService", name = "impl", havingValue = "cbuRateService", matchIfMissing = true)
public class CachedCbuService implements CacheEvictable {

    public static final String GET_CURRENCY_RATE_CACHE_NAME = "getCurrencyRate";
    public static final String GET_ALL_ACTUAL = "getAllActual";

    private final CbuService cbuService;
    private final CurrencyRepository currencyRepository;
    private final CurrencyMapper currencyMapper;

    @PostConstruct
    public void init() {
        syncCurrencies();
        getAllActual();
    }

    @Cacheable(value = GET_CURRENCY_RATE_CACHE_NAME, key = "#code", condition = "#code != null", sync = true)
    public Integer getCurrencyRate(Integer code) {
        return getCurrencyRate(StringUtils.leftPad(String.valueOf(code), 3));
    }

    @Cacheable(value = GET_CURRENCY_RATE_CACHE_NAME, key = "#code", condition = "#code != null", sync = true)
    public Integer getCurrencyRate(String code) {

        log.debug("getCurrencyRate by alfa: {}", code);

        if (code == null) {
            throw new CodedException(ProblemEnum.INVALID_PARAMS);
        }

        ICurrency currencyInfo = getAllActual().get(code);

        if (currencyInfo == null) {
            throw new CodedException(ProblemEnum.INTERNAL_ERROR, code + " no supported");
        }

        return currencyInfo.getRate().multiply(BigDecimal.valueOf(100L)).intValue();
    }

    @Cacheable(value = GET_ALL_ACTUAL, sync = true)
    public Map<String, ICurrency> getAllActual() {

        List<Currency> dbCurrencies = currencyRepository.findByActiveTrue();

        if (dbCurrencies == null || dbCurrencies.isEmpty()) {
            log.info("Syncing and returning direct from CBU");
            return syncCurrencies();
        }

        Map<String, ICurrency> result = new HashMap<>();

        for (Currency dbCurrency : dbCurrencies) {
            result.put(dbCurrency.getCcy(), dbCurrency);
            result.put(StringUtils.leftPad(String.valueOf(dbCurrency.getCode()), 3, "0"), dbCurrency);
        }

        log.info("Getting all actual currencies from DB");
        return result;
    }

    public Map<String, ICurrency> fetchAllActual() {
        log.info("Fetching all actual currencies from CBU");
        return cbuService.getAllActual().orElse(Collections.emptyMap());
    }

    public Map<String, ICurrency> syncCurrencies() {
        log.info("Updating currencies in DB");

        Map<String, ICurrency> allActual = fetchAllActual();
        Map<String, ICurrency> result = new HashMap<>();

        for (ICurrency value : allActual.values()) {
            Optional<Currency> currencyOptional = currencyRepository.findTop1ByActiveTrueAndCode(value.getCode());

            ICurrency savedCurrency;

            if (currencyOptional.isPresent()) {
                currencyOptional.get().setRate(value.getRate());
                currencyOptional.get().setDate(value.getDate());
                savedCurrency = currencyRepository.save(currencyOptional.get());
                log.debug("Currency {} updated in DB", value.getCcy());
            } else {
                Currency fromCbu = currencyMapper.fromCbu(value);
                savedCurrency = currencyRepository.save(fromCbu);
                log.debug("New currency {} added into DB", value.getCcy());
            }

            result.put(savedCurrency.getCcy(), savedCurrency);
            result.put(StringUtils.leftPad(String.valueOf(savedCurrency.getCode()), 3, "0"), savedCurrency);
        }

        return result;
    }

    @Scheduled(cron = "0 0 */1 * * *")
    public void updateAllCurrency() {
        log.debug("Updating currencies");

        Optional<Map<String, ICurrency>> allActual = cbuService.getAllActual();

        if (allActual.isPresent()) {
            syncCurrencies();
            evictAll();
            log.info("Currencies updated from CBU");
        } else {
            log.warn("Could not update currencies from CBU");
        }

    }

    @Override
    @CacheEvict(value = {GET_CURRENCY_RATE_CACHE_NAME, GET_ALL_ACTUAL}, allEntries = true)
    public void evictAll() {
        log.info("Evicting CBU rates...");
    }
}
