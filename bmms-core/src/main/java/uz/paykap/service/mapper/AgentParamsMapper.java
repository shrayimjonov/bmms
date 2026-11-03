package uz.paykap.service.mapper;

import org.mapstruct.*;
import uz.paykap.domain.*;
import uz.paykap.service.dto.AgentParamsDTO;

/**
 * Mapper for the entity {@link AgentParams} and its DTO {@link AgentParamsDTO}.
 */
@Mapper(componentModel = "spring", uses = { AgentMapper.class })
public interface AgentParamsMapper extends EntityMapper<AgentParamsDTO, AgentParams> {
    @Mapping(target = "agent", source = "agent", qualifiedByName = "shortName")
    AgentParamsDTO toDto(AgentParams s);
}
