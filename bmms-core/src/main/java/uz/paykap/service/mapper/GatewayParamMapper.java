package uz.paykap.service.mapper;

import org.mapstruct.*;
import uz.paykap.domain.*;
import uz.paykap.service.dto.GatewayParamDTO;

/**
 * Mapper for the entity {@link GatewayParam} and its DTO {@link GatewayParamDTO}.
 */
@Mapper(componentModel = "spring", uses = { RecipientMapper.class })
public interface GatewayParamMapper extends EntityMapper<GatewayParamDTO, GatewayParam> {
    @Mapping(target = "recipient", source = "recipient", qualifiedByName = "shortName")
    GatewayParamDTO toDto(GatewayParam s);
    //    @Mapping(source = "recipientId", target = "recipient")
    //    GatewayParam toEntity(GatewayParamDTO gatewayParamDTO);

    //    default GatewayParam fromId(Long id) {
    //        if (id == null) {
    //            return null;
    //        }
    //        GatewayParam gatewayParam = new GatewayParam();
    //        gatewayParam.setId(id);
    //        return gatewayParam;
    //    }
}
