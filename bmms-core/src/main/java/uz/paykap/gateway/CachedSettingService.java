package uz.paykap.gateway;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.humo.config.HumoProperties;
import uz.javlon.paynet.config.PaynetProperties;
import uz.javlon.paynet.dto.PaynetCredential;
import uz.javlon.svgate.config.SvgateProperties;
import uz.paykap.domain.Setting;
import uz.paykap.domain.SettingProperty;
import uz.paykap.domain.enumeration.Prop;
import uz.paykap.gateway.dto.HumoSetting;
import uz.paykap.gateway.dto.PcSetting;
import uz.paykap.repository.SettingPropertyRepository;
import uz.paykap.repository.SettingRepository;
import uz.devops.bmcore.cache.CacheEvictable;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@AllArgsConstructor
public class CachedSettingService implements CacheEvictable {

    public static final String GET_PAYNET_CREDENTIAL_CACHE_NAME = "getPaynetCredential";
    public static final String GET_UZCARD_SETTING_CACHE_NAME = "GET_UZCARD_SETTING_CACHE_NAME";
    public static final String GET_HUMO_SETTING_CACHE_NAME = "GET_HUMO_SETTING_CACHE_NAME";

    //All method params as key
    @Cacheable(value = GET_PAYNET_CREDENTIAL_CACHE_NAME, key = "''.concat(#p0).concat('-').concat(#p1).concat('-').concat(#p2)", sync = true)
    public PaynetCredential getPaynetCredential(Long recipientId, String login, String requestGroup) {

        log.debug("Getting PaynetCredential from SettingProperty table : {}, {}, {}", recipientId, login, requestGroup);

        Set<SettingProperty> props = settingPropertyRepository.findPropsByRecipientId(recipientId, login, requestGroup);

        log.debug("Found Props in table : {}", props);

        PaynetCredential credential = new PaynetCredential();

        int propsCount = 0;

        for (SettingProperty prop : props) {
            if (prop.getKey() == Prop.PAYNET_USERNAME) {
                log.debug("Found {} in Prop table", Prop.PAYNET_USERNAME);
                credential.setUsername(prop.getValue());
                propsCount++;
            } else if (prop.getKey() == Prop.PAYNET_PASSWORD) {
                log.debug("Found {} in Prop table", Prop.PAYNET_PASSWORD);
                credential.setPassword(prop.getValue());
                propsCount++;
            } else if (prop.getKey() == Prop.PAYNET_TERMINAL) {
                log.debug("Found {} in Prop table", Prop.PAYNET_TERMINAL);
                credential.setTerminalId(prop.getValue());
                propsCount++;
            }
        }

        if (propsCount == 3) {
            log.info("Found PaynetCredential : username : {} : terminalId : {}", credential.getUsername(), credential.getTerminalId());
            return credential;
        }

        return getGlobalPaynetCredential();
    }

    public PcSetting getEposSetting(Long settingId, PayType payType) {
        if (payType == PayType.HUMO) {
            return getHumoSetting(settingId);
        } else if (payType == PayType.UZCARD) {
            return getUzcardSetting(settingId);
        } else {
            log.info("Epos params NOT resolved from Expression, Humo and Uzcard, return 'null'");
            return null;
        }
    }

    @Cacheable(value = GET_UZCARD_SETTING_CACHE_NAME, key = "#p0", sync = true)
    public PcSetting getUzcardSetting(Long settingId) {

        if (settingId == null) {
            return new PcSetting()
                .withEposMerchant("123")
                .withEposTerminal("123");
        }

        log.debug("Getting UzcardSetting from Setting table : {}", settingId);

        Optional<Setting> optionalSetting = settingRepository.findTop1ById(settingId);

        if (optionalSetting.isEmpty()) {
            return new PcSetting()
                .withEposMerchant("123")
                .withEposTerminal("123");
        }

        log.debug("Found Setting in table : {}", optionalSetting.get());

        PcSetting uzcardSetting = new PcSetting();

        SettingProperty merchantProperty = optionalSetting.get().getProps().get(Prop.UZCARD_MERCHANT);

        if (merchantProperty != null) {
            log.debug("Found {}={} in Prop table", Prop.UZCARD_MERCHANT, merchantProperty.getValue());
            uzcardSetting.setEposMerchant(merchantProperty.getValue());
        } else {
            log.debug("{} got from SvgateProperties.merchantId", Prop.UZCARD_MERCHANT);
            uzcardSetting.setEposMerchant("123");
        }

        SettingProperty terminalProperty = optionalSetting.get().getProps().get(Prop.UZCARD_TERMINAL);

        if (terminalProperty != null) {
            log.debug("Found {}={} in Prop table", Prop.UZCARD_TERMINAL, terminalProperty.getValue());
            uzcardSetting.setEposTerminal(terminalProperty.getValue());
        } else {
            log.debug("{} got from SvgateProperties.terminalId", Prop.UZCARD_TERMINAL);
            uzcardSetting.setEposTerminal("123");
        }

        log.debug("Uzcard Setting : {}", uzcardSetting);

        return uzcardSetting;
    }

    @Cacheable(value = GET_HUMO_SETTING_CACHE_NAME, key = "#p0", sync = true)
    public PcSetting getHumoSetting(Long settingId) {

        if (settingId == null) {
            return new HumoSetting()
                .withEposMerchant(humoProperties.getMerchantId())
                .withEposTerminal(humoProperties.getTerminalId());
        }

        log.debug("Getting HumoSetting from Setting table : {}", settingId);

        Optional<Setting> optionalSetting = settingRepository.findTop1ById(settingId);

        if (optionalSetting.isEmpty()) {
            return new HumoSetting()
                .withEposMerchant(humoProperties.getMerchantId())
                .withEposTerminal(humoProperties.getTerminalId());
        }

        log.debug("Found Setting in table : {}", optionalSetting.get());

        HumoSetting humoSetting = new HumoSetting();

        SettingProperty merchantProperty = optionalSetting.get().getProps().get(Prop.HUMO_MERCHANT);

        if (merchantProperty != null) {
            log.debug("Found {} in Prop table", Prop.HUMO_MERCHANT);
            humoSetting.setEposMerchant(merchantProperty.getValue());
        } else {
            log.debug("{} got from HumoProperties.merchantId", Prop.HUMO_MERCHANT);
            humoSetting.setEposMerchant(humoProperties.getMerchantId());
        }

        SettingProperty terminalProperty = optionalSetting.get().getProps().get(Prop.HUMO_TERMINAL);

        if (terminalProperty != null) {
            log.debug("Found {} in Prop table", Prop.HUMO_TERMINAL);
            humoSetting.setEposTerminal(terminalProperty.getValue());
        } else {
            log.debug("{} got from HumoProperties.terminalId", Prop.HUMO_TERMINAL);
            humoSetting.setEposTerminal(humoProperties.getTerminalId());
        }

        return humoSetting;
    }

    private PaynetCredential getGlobalPaynetCredential() {
        log.debug("Getting PaynetCredential from PaynetProperties");
        PaynetCredential credential = new PaynetCredential(paynetProperties.getUsername(), paynetProperties.getPassword(), paynetProperties.getTerminalId());
        log.info("Found Global PaynetCredential : username : {} : terminalId : {}", credential.getUsername(), credential.getTerminalId());
        return credential;
    }

    public void evictAllSettings() {

    }

    @CacheEvict(value = {GET_UZCARD_SETTING_CACHE_NAME, GET_HUMO_SETTING_CACHE_NAME}, key = "#p0")
    public void evictSettingById(Long settingId) {
    }

    private final SettingPropertyRepository settingPropertyRepository;
    private final PaynetProperties paynetProperties;
    private final SettingRepository settingRepository;
    private final HumoProperties humoProperties;

    @Override
    @CacheEvict(value = {GET_PAYNET_CREDENTIAL_CACHE_NAME, GET_UZCARD_SETTING_CACHE_NAME, GET_HUMO_SETTING_CACHE_NAME}, allEntries = true)
    public void evictAll() {

    }
}
