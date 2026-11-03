package uz.paykap.service.mapper;

import org.mapstruct.*;
import uz.devops.bmcore.dto.GatewayQueryResult;
import uz.devops.bmcore.projection.GatewayResultParamProjection;
import uz.paykap.domain.*;
import uz.paykap.service.dto.GatewayDTO;

/**
 * Mapper for the entity {@link Gateway} and its DTO {@link GatewayDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface GatewayMapper extends EntityMapper<GatewayDTO, Gateway> {
    @Named("name")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    GatewayDTO toDtoName(Gateway gateway);

    @BeanMapping(
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
    )
    GatewayQueryResult fromProjection(GatewayResultParamProjection projection);
}
