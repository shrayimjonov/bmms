package uz.paykap.service.mapper;

import org.mapstruct.*;
import uz.paykap.domain.*;
import uz.paykap.service.dto.AgentRecipientDTO;

/**
 * Mapper for the entity {@link AgentRecipient} and its DTO {@link AgentRecipientDTO}.
 */
@Mapper(
    componentModel = "spring",
    uses = { AgentMapper.class, RecipientMapper.class, SettingMapper.class, RecipientGroupMapper.class }
)
public interface AgentRecipientMapper extends EntityMapper<AgentRecipientDTO, AgentRecipient> {
    @Mapping(target = "agent", source = "agent", qualifiedByName = "shortName")
    @Mapping(target = "recipient", source = "recipient", qualifiedByName = "shortName")
    @Mapping(target = "setting", source = "setting", qualifiedByName = "shortName")
//    @Mapping(target = "settingType", source = "settingType", qualifiedByName = "name")
    @Mapping(target = "recipientGroup", source = "recipientGroup", qualifiedByName = "name")
    AgentRecipientDTO toDto(AgentRecipient s);
}
