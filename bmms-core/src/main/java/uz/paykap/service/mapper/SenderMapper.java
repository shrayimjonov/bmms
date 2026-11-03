package uz.paykap.service.mapper;

import org.mapstruct.*;
import uz.paykap.domain.*;
import uz.paykap.service.dto.SenderDTO;

/**
 * Mapper for the entity {@link Sender} and its DTO {@link SenderDTO}.
 */
@Mapper(componentModel = "spring", uses = { AgentMapper.class })
public interface SenderMapper extends EntityMapper<SenderDTO, Sender> {
    @Mapping(source = "agent.id", target = "agentId")
    @Mapping(source = "agent.shortName", target = "agentShortName")
    SenderDTO toDto(Sender sender);

    @Mapping(source = "agentId", target = "agent")
    Sender toEntity(SenderDTO senderDTO);

    default Sender fromId(Long id) {
        if (id == null) {
            return null;
        }
        Sender sender = new Sender();
        sender.setId(id);
        return sender;
    }
}
