package uz.paykap.adapter.common.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import uz.devops.bmcore.adapter.dto.SharedCategory;
import uz.devops.bmcore.adapter.dto.SharedField;
import uz.devops.bmcore.adapter.dto.SharedRecipient;
import uz.paykap.service.dto.GatewayDTO;
import uz.paykap.service.dto.GatewayParamDTO;

import java.util.List;

@Getter
@Setter
@ToString
public class CommonDataDTOV3 {

    private SharedRecipient recipient;
    private SharedCategory category;
    private GatewayDTO gatewayDTO;
    private SharedField[] fields;
    private List<GatewayParamDTO> gatewayParams;

}
