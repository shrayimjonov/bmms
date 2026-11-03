package uz.paykap.gateway.standart;

import org.springframework.stereotype.Service;
import uz.devops.bmcore.enumeration.Lang;
import uz.devops.bmcore.enumeration.RequestType;
import uz.javlon.commons.base.Result;
import uz.paykap.domain.Gateway;
import uz.paykap.domain.GatewayParam;
import uz.paykap.domain.I18n;
import uz.paykap.domain.Recipient;
import uz.paykap.domain.enumeration.MethodType;
import uz.paykap.domain.enumeration.ProtocolType;
import uz.paykap.repository.GatewayParamRepository;
import uz.paykap.repository.GatewayRepository;
import uz.paykap.repository.RecipientRepository;

import java.util.HashSet;
import java.util.Set;

@Service
public class SimpleGatewayService {

    public static final String SERVICE_ID = "1";
    private static final int TIYIN = 100;
    public static final int UZS = 860;
    public static final String DEFAULT = "default";

    Result saveSimpleGateway(SimpleGatewayParam simpleGatewayParam) {

        Gateway gateway = saveGateway(simpleGatewayParam.getRecipientName(), simpleGatewayParam.getProtocolType());

        Recipient recipient = saveRecipient(simpleGatewayParam.getRecipientName(), simpleGatewayParam.getMinAmount(), simpleGatewayParam.getMaxAmount(), gateway);

        saveGatewayParam(recipient, simpleGatewayParam);

        return Result.SUCCESS;
    }

    private Recipient saveRecipient(String shortName, Long minAmount, Long maxAmount, Gateway gateway) {
        Recipient recipient = new Recipient()
            .shortName(shortName)
            .serviceId(SERVICE_ID)
            .amountRatio(TIYIN)
            .ccy(UZS)
            .minAmount(minAmount)
            .maxAmount(maxAmount)
            .limitRatio(1)
            .active(false)
            .gateway(gateway)
            .titles(getI18ns(shortName, shortName, shortName))
            .fullNames(getI18ns(shortName, shortName, shortName));

        return recipientRepository.save(recipient);

    }

    private Gateway saveGateway(String name, ProtocolType protocolType) {
        Gateway gateway = new Gateway()
            .name(name)
            .org(name)
            .active(false)
            .mfo("1")
            .ban("1")
            .contract(DEFAULT);
        if (protocolType == ProtocolType.REST) {
            gateway.setSpringBeanName(SimpleBmGatewayService.NAME);
        } else {
            gateway.setSpringBeanName(DEFAULT);
        }

        return gatewayRepository.save(gateway);

    }

    private void saveGatewayParam(Recipient recipient, SimpleGatewayParam simpleGatewayParam) {
        for (RequestType requestType : simpleGatewayParam.getUrlMap().keySet()) {
            GatewayParam gatewayParam = new GatewayParam()
                .url(simpleGatewayParam.getUrlMap().get(requestType))
                .type(requestType)
                .username(simpleGatewayParam.getUsername())
                .password(simpleGatewayParam.getPassword())
                .timeout(simpleGatewayParam.getTimeout())
                .httpMethodType(MethodType.POST)
                .protocol(simpleGatewayParam.getProtocolType())
                .active(false)
                .recipient(recipient);
            gatewayParamRepository.save(gatewayParam);
        }
    }

    private Set<I18n> getI18ns(String en, String ru, String uz) {
        HashSet<I18n> i18ns = new HashSet<>(3);
        i18ns.add(new I18n().lang(Lang.EN).text(en));
        i18ns.add(new I18n().lang(Lang.UZ).text(uz));
        i18ns.add(new I18n().lang(Lang.RU).text(ru));

        return i18ns;
    }


    private final RecipientRepository recipientRepository;
    private final GatewayRepository gatewayRepository;
    private final GatewayParamRepository gatewayParamRepository;

    public SimpleGatewayService(RecipientRepository recipientRepository, GatewayRepository gatewayRepository, GatewayParamRepository gatewayParamRepository) {
        this.recipientRepository = recipientRepository;
        this.gatewayRepository = gatewayRepository;
        this.gatewayParamRepository = gatewayParamRepository;
    }

}
