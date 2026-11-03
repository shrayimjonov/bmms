package uz.paykap.adapter.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.adapter.CommonDataAdapter;
import uz.devops.bmcore.adapter.dto.SharedDataAdapterDTO;
import uz.devops.bmcore.adapter.dto.SharedRecipient;
import uz.javlon.commons.base.ResultData;
import uz.paykap.adapter.DataAdapterDelegate;
import uz.paykap.adapter.common.dto.CommonDataDTOV3;
import uz.paykap.domain.Recipient;
import uz.paykap.gateway.common.CommonGatewayServiceV3;
import uz.paykap.service.GatewayParamService;
import uz.paykap.service.dto.GatewayParamDTO;
import uz.paykap.service.dto.RecipientDTO;

@Slf4j
@AllArgsConstructor
@Service(CommonDataAdapterV3.NAME)
public class CommonDataAdapterV3 extends CommonDataAdapter {

    public static final String NAME = "commonDataAdapterV3";
    public static final String COMMON_GATEWAY_SHORT_NAME = "common-gateway-v3";
    public static final String COMMON_RECIPIENTS_FOLDER = "classpath*:commons/*.json";

    @Override
    public ResultData<SharedDataAdapterDTO> returnData() {
        Resource[] resources = loadRecipientResources();

        SharedDataAdapterDTO sharedDataAdapterDTO = new SharedDataAdapterDTO();

        if (resources.length <= 0) {
            return new ResultData<>(sharedDataAdapterDTO);
        }

        for (Resource resource : resources) {
            log.debug("Reading resource : {}", resource.getFilename());

            try {
                CommonDataDTOV3 commonDataDTO = objectMapper.readValue(
                    IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8),
                    CommonDataDTOV3.class
                );

                if (
                    commonDataDTO == null ||
                    commonDataDTO.getRecipient() == null ||
                    commonDataDTO.getGatewayParams() == null ||
                    commonDataDTO.getGatewayParams().size() == 0 ||
                    commonDataDTO.getFields() == null ||
                    commonDataDTO.getFields().length == 0
                ) {
                    log.warn("CommonDataDTO invalid : {}", commonDataDTO);

                    continue;
                }

                //                commonDataDTO.getRecipient().setGatewayId(gateway.getId());

                setDefaultValues(commonDataDTO.getRecipient());

                Optional<Recipient> recipientOptional = dataAdapterDelegate
                    .getRecipientRepository()
                    .findTop1ByShortName(commonDataDTO.getRecipient().getShortName());

                Recipient recipient = recipientOptional.orElseGet(
                    () ->
                        dataAdapterDelegate
                            .getRecipientRepository()
                            .save(dataAdapterDelegate.getRecipientMapper().fromShared(commonDataDTO.getRecipient()))
                );

                log.debug("Saved Recipient : {}", recipient.getShortName());

                //                saveAgentRecipient(recipient, agent);

                sharedDataAdapterDTO.addRecipient(
                    commonDataDTO.getRecipient().categoryShortName(commonDataDTO.getCategory().getShortName())
                );
                sharedDataAdapterDTO.addCategory(commonDataDTO.getCategory());
                sharedDataAdapterDTO.addFields(commonDataDTO.getRecipient().getShortName(), commonDataDTO.getFields());

                for (GatewayParamDTO gatewayParam : commonDataDTO.getGatewayParams()) {
                    //todo
                    gatewayParam.setRecipient(new RecipientDTO(recipient.getId(), recipient.getShortName()));
                    gatewayParamService.getOrCreate(gatewayParam);
                    log.debug("Saved gatewayParam : {}.{}", recipient.getShortName(), gatewayParam.getType());
                }
            } catch (IOException e) {
                log.warn("Can not parse : {}", resource.getFilename());
            }
        }

        return new ResultData<>(sharedDataAdapterDTO);
    }

    private void setDefaultValues(SharedRecipient sharedRecipient) {
        if (sharedRecipient.getLimitRatio() == null) {
            sharedRecipient.limitRatio(100);
        }
    }

    private Resource[] loadRecipientResources(/*String pattern*/) {
        try {
            return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(COMMON_RECIPIENTS_FOLDER);
        } catch (IOException e) {
            return new Resource[] {};
        }
    }

    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    private final GatewayParamService gatewayParamService;
    private final DataAdapterDelegate dataAdapterDelegate;

    @Override
    public Class<?> getClazz() {
        return this.getClass();
    }

    @Override
    public String getGatewayShortName() {
        return COMMON_GATEWAY_SHORT_NAME;
    }

    @Override
    public String getGatewayServiceBeanName() {
        return CommonGatewayServiceV3.NAME;
    }
}
