package uz.paykap.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.domain.enumeration.P2pProcessorType;

import java.util.Arrays;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PcHelper {

    private final ApplicationProperties properties;


    public Boolean isValidPan(String pan, P2pProcessorType... pcTypes) {
        var isValid = false;
        for (var pcType : pcTypes) {
            isValid |= isValidPan(pan, pcType);
        }
        return isValid;
    }

    public P2pProcessorType getPcTypeByPan(String pan) {
        if (pan == null) {
            return null;
        }
        return properties.getPcRegex()
                .entrySet()
                .stream()
                .filter(it -> isValidRegex(pan, it.getValue().getPan()))
                .findFirst()
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public Boolean isValidPan(String pan, P2pProcessorType pcType) {
        log.trace("isValidPan started for pc type : {}", pcType);
        return isValidCredentials(pan, pcType) &&
                isValidRegex(pan, properties.getPcRegex().get(pcType).getPan());
    }

    public Boolean isValidToken(String token, P2pProcessorType pcType) {
        log.trace("isValidToken started for pc type : {}", pcType);
        return isValidCredentials(token, pcType) &&
                isValidRegex(token, properties.getPcRegex().get(pcType).getToken());
    }

    public Boolean isValidAccount(String account, P2pProcessorType pcType) {
        log.trace("isValidAccount started for pc type : {}", pcType);
        log.debug("properties.getPcRegex() :{}", properties.getPcRegex());
        log.debug("account {}", account);
        return isValidCredentials(account, pcType) &&
                isValidRegex(account, properties.getPcRegex().get(pcType).getAccount());
    }

    private boolean isValidCredentials(String token, P2pProcessorType pcType) {
        log.trace("isValidCredentials started");
        return isValidArgs(token, pcType) && isNotEmptyCollection() && containsPc(pcType);
    }

    private Boolean isValidRegex(String value, String regex) {
        log.debug("isValidRegex started");
        var result = value.matches(regex);
        log.debug("result of validation is : {}", result);
        return result;
    }

    private boolean isNotEmptyCollection() {
        log.trace("isEmptyCollection started");

        if (properties.getPcRegex() == null || properties.getPcRegex().isEmpty()) {
            log.debug("pc type is unknown");
            return false;
        }
        return true;
    }

    private boolean isValidArgs(String value, P2pProcessorType pcType) {
        log.trace("isInvalidArgs started");

        if (value == null || pcType == null) {
            log.debug("one or all of args are null");
            return false;
        }
        return true;
    }

    private boolean containsPc(P2pProcessorType pcType) {
        log.trace("containsPc started");

        if (!properties.getPcRegex().containsKey(pcType)) {
            log.debug("predefined regex collection doesn't contains specified PC : {}", pcType);
            return false;
        }
        return true;
    }

    /**
     * method actual to 2099 year!
     *
     * @param expDate
     * @return
     */
    public String validateAndFixUzcardExp(String expDate) {
        log.debug("begin validate uzcard-expDate: {}", expDate);
        if (expDate == null || expDate.length() != 4 || !isWholePositiveNumber(expDate)) {
            log.debug("return: {}", expDate);
            return expDate;
        }
        String firstPart = expDate.substring(0, 2);
        String lastPart = expDate.substring(2);
        if (Integer.parseInt(firstPart) < Integer.parseInt(lastPart)) {
            String formattedExpDate = lastPart + firstPart;
            log.debug("return: {}", formattedExpDate);
            return formattedExpDate;
        }
        log.debug("return: {}", expDate);
        return expDate;
    }

    private boolean isWholePositiveNumber(String str) {
        return str.chars().allMatch(Character::isDigit);
    }
}
