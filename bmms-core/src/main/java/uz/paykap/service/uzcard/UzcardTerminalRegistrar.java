package uz.paykap.service.uzcard;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uz.devops.pusher.event.PusherEvent;
import uz.javlon.commons.result.CommonResultData;
import uz.javlon.svgate.dto.sv.SimpleSvResponse;
import uz.javlon.svgate.dto.sv.SvTerminalDTO;
import uz.javlon.svgate.dto.sv.SvTerminalType;
import uz.javlon.svgate.service.SvGateTerminalServiceV2;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.config.PusherConstants;
import uz.paykap.domain.P2pParam;
import uz.paykap.domain.Setting;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.gateway.CachedSettingService;
import uz.paykap.gateway.dto.PcSetting;
import uz.paykap.repository.P2pParamRepository;
import uz.paykap.repository.SettingRepository;
import uz.paykap.service.dto.VirtualUzcardReplenishDto;
import uz.paykap.service.p2p.processor.UzcardParam;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@AllArgsConstructor
public class UzcardTerminalRegistrar {

    public static final String SENDER_FIELD_NAME = "sender";
    public static final String RECIPIENT_FIELD_NAME = "recipient";

    private final P2pParamRepository p2pParamRepository;
    private final SvGateTerminalServiceV2 svGateTerminalServiceV2;
    private final ObjectMapper objectMapper;
    private final SettingRepository settingRepository;
    private final CachedSettingService cachedSettingService;
    private final ApplicationContext applicationContext;
    private final ApplicationProperties properties;

    @Async
    @SneakyThrows
    public void registerTerminals() {

        log.info("Started registering Uzcard terminals from p2p params");

        List<P2pParam> p2pParams = p2pParamRepository.findAllByActiveTrue();

        for (P2pParam p2pParam : p2pParams) {

            parseAndRegisterFromP2pParam(p2pParam);

        }

        log.info("Started registering Uzcard terminals from settings");

        List<Setting> settings = settingRepository.findAllByActiveTrue();

        for (Setting setting : settings) {

            PcSetting uzcardSetting = cachedSettingService.getUzcardSetting(setting.getId());

            registerTerminal(uzcardSetting.getEposMerchant(), uzcardSetting.getEposTerminal(), setting.getShortName(), SvTerminalType.DEBIT);

        }

        log.info("Started registering Uzcard terminals from application.p2pUzcard.virtualCardMap");

        for (Map.Entry<P2pProcessorType, VirtualUzcardReplenishDto> entry : properties.getP2pUzcard().getVirtualCardMap().entrySet()) {

            if (!entry.getValue().getEnabled()) {
                log.info("! ! ! Auto replenish is off for {} ! ! !", entry.getKey());
                continue;
            }

            registerTerminal(entry.getValue().getMerchantId(), entry.getValue().getTerminalId(), "Davr Virtual Card replenish", SvTerminalType.CREDIT);
        }


    }

    private void parseAndRegisterFromP2pParam(P2pParam p2pParam) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(p2pParam.getValue());

        if (p2pParam.getSenderPc() == P2pProcessorType.UZCARD) {
            UzcardParam uzcardSetting = objectMapper.treeToValue(jsonNode.get(SENDER_FIELD_NAME), UzcardParam.class);

            registerTerminal(uzcardSetting.getMerchantId(), uzcardSetting.getTerminalId(), p2pParam.getSenderCondition().getValue(), SvTerminalType.DEBIT);
        }

        if (p2pParam.getRecipientPc() == P2pProcessorType.UZCARD) {
            UzcardParam uzcardSetting = objectMapper.treeToValue(jsonNode.get(RECIPIENT_FIELD_NAME), UzcardParam.class);

            registerTerminal(uzcardSetting.getMerchantId(), uzcardSetting.getTerminalId(), p2pParam.getRecipientCondition().getValue(), SvTerminalType.CREDIT);
        }
    }

    public void registerTerminal(@NotNull String merchantId, @NotNull String terminalId, String name, SvTerminalType terminalType) {

        var addedResponse = svGateTerminalServiceV2.add(new SvTerminalDTO(terminalId, merchantId, 7, name, terminalType));

        if (!addedResponse.isSuccess()) {
            log.warn("Uzcard terminal not added : {} : {}, reason {}", merchantId, terminalId, addedResponse.getMessage());

            if (Objects.equals(addedResponse.getMessage(), "Terminal Id and Merchant Id not found in SV!")) {
                applicationContext.publishEvent(new PusherEvent(this, Collections.singletonMap("message", "Terminal ID " + terminalId + " and Merchant ID " + merchantId + " not found in Uzcard/SmartVista!"), PusherConstants.TAG_BM_PUSH_WARN));
            }

        } else {
            log.info("Uzcard terminal added successfully : {} : {}", merchantId, terminalId);
        }
    }

    public void removeTerminal(@NotNull String merchantId, @NotNull String terminalId) {

        var addedResponse = svGateTerminalServiceV2.remove(new SvTerminalDTO(terminalId, merchantId, 0, "", SvTerminalType.P2P));

        if (!addedResponse.isSuccess()) {
            log.warn("Uzcard terminal not removed : {} : {}, reason {}", merchantId, terminalId, addedResponse.getMessage());
        } else {
            log.info("Uzcard terminal removed successfully : {} : {}", merchantId, terminalId);
        }
    }

}
