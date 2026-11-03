package uz.paykap.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uz.devops.bmcore.dto.KeyValue;
import uz.paykap.domain.Agent;
import uz.paykap.service.dto.AgentDTO;

import java.util.List;

/**
 * Mapper for the entity {@link Agent} and its DTO {@link AgentDTO}.
 */
@Mapper(componentModel = "spring", uses = { I18nMapper.class })
public interface AgentMapper extends EntityMapper<AgentDTO, Agent> {
    @Mapping(target = "titles", source = "titles", qualifiedByName = "idSet")
    AgentDTO toDto(Agent s);

    @Mapping(target = "deposit", ignore = true)
    Agent toEntity(AgentDTO agentDTO);

    @Named("shortName")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "shortName", source = "shortName")
    AgentDTO toDtoShortName(Agent agent);

    @Mapping(target = "key", source = "id")
    @Mapping(target = "value", source = "shortName")
    KeyValue toSimple(Agent agent);

    List<KeyValue> toSimple(List<Agent> agents);

    default Agent fromId(Long id) {
        if (id == null) {
            return null;
        }
        Agent agent = new Agent();
        agent.setId(id);
        return agent;
    }
}
