package uz.paykap.service.epos;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import uz.devops.bmcore.dto.request.CommonPayRequest;
import uz.paykap.domain.Setting;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.Prop;
import uz.paykap.gateway.CachedSettingService;
import uz.paykap.gateway.dto.PcSetting;
import uz.paykap.repository.SettingRepository;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static uz.paykap.config.Constants.PAN_PARAM;

@Slf4j
@Component
public class EposResolver {

    private final ExpressionParser expressionParser = new SpelExpressionParser();

    private final SettingRepository settingRepository;
    private final CachedSettingService cachedSettingService;

    public EposResolver(SettingRepository settingRepository, CachedSettingService cachedSettingService) {
        this.settingRepository = settingRepository;
        this.cachedSettingService = cachedSettingService;
    }

    protected Optional<PcSetting> resolve(Transaction tran, String expression) {
        return resolve(tran, expression, null);
    }

    public Optional<PcSetting> resolve(Transaction tran, String expression, Map<String, Object> params) {

        log.info("Resolving epos setting : \ntranId = {}\nexpression = {}\nparams = {}", tran != null ? tran.getId() : "null", expression, params);

        StandardEvaluationContext context = new StandardEvaluationContext();

        if (tran != null) {
            context.setVariable("tran", tran);
        }
        if (params != null) {
            context.setVariable("params", params);
            log.debug("Set params into context");
        }

        String epos = expressionParser
//            .parseExpression("#params['pan'] matches '^98606001.*' ? '1111,1111' : (#params['pan'] matches '^98606002.*' ? '2222,2222' : null)")
//            .parseExpression("#params['pan'] matches '^986060(03|66).*' ? '011830505734602,60610019' : (#params['pan'] matches '^986060(01|04|67).*' ? '011830505734603,6061002K' : null)")
                .parseExpression(expression)
                .getValue(context, String.class);

        log.debug("Parsed result : {}", epos);

        Optional<PcSetting> pcSetting = commaStringToSetting(epos);

        if (pcSetting.isPresent()) {
            log.info("Epos setting parsed from expression, result : {}", pcSetting.get());
        } else {
            log.info("Epos setting NOT parsed : {}", expression);
        }

        return pcSetting;
    }

    private Optional<PcSetting> commaStringToSetting(String epos) {

        if (epos == null) {
            log.debug("'epos' param is null, return Optional.empty");
            return Optional.empty();
        }

        String[] split = epos.split(",");

        if (split.length < 2) {
            log.debug("Size of '{}'.split(',') less than 2, return Optional.empty", epos);
            return Optional.empty();
        }

        return Optional.ofNullable(new PcSetting().withEposMerchant(split[0]).withEposTerminal(split[1]));
    }

    public Optional<PcSetting> resolve(CommonPayRequest payDTO, Transaction tran) {

        if (payDTO.getSettingId() == null) {
            log.debug("'payDTO.getSettingId' is null, return Optional.empty");
            return Optional.empty();
        }

        Optional<Setting> setting = settingRepository.findOneWithEagerRelationships(payDTO.getSettingId());

        if (setting.isEmpty()) {
            log.debug("Setting entity not found for payDTO.getSettingId={}, return Optional.empty", payDTO.getSettingId());
            return Optional.empty();
        }

        if (setting.get().getProps().containsKey(Prop.EPOS_EX) && payDTO.getParams().containsKey(PAN_PARAM)) {
            log.debug("settingId={} and pan={} is given, resolve epos params", payDTO.getSettingId(), payDTO.getParams().get(PAN_PARAM));
            Optional<PcSetting> pcSetting = resolve(tran, setting.get().getProps().get(Prop.EPOS_EX).getValue(), payDTO.getParams());

            if (pcSetting.isPresent()) {
                return pcSetting;
            }
        }

        log.debug("Epos params not resolved for expression, try Uzcard and Humo epos resolvers");

        return Optional.ofNullable(cachedSettingService.getEposSetting(payDTO.getSettingId(), payDTO.getType()));
    }

    public Optional<PcSetting> resolve(SettingResolverDto settingResolverDto) {
        return resolve(settingResolverDto.getTran(), settingResolverDto.getExpression(), settingResolverDto.getParams());
    }

    @Data
    public class SettingResolverDto {
        private Transaction tran;
        private String expression = "#params['pan'] matches '^98606001.*' ? '1111,1111' : (#params['pan'] matches '^98606002.*' ? '2222,2222' : null)";
        private HashMap<String, Object> params;
    }

}
