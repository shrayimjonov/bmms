package uz.paykap.service.mapper;

import org.mapstruct.*;
import uz.paykap.domain.*;
import uz.paykap.service.dto.RecipientGroupDTO;

/**
 * Mapper for the entity {@link RecipientGroup} and its DTO {@link RecipientGroupDTO}.
 */
@Mapper(componentModel = "spring", uses = { RecipientMapper.class })
public interface RecipientGroupMapper extends EntityMapper<RecipientGroupDTO, RecipientGroup> {
    @Mapping(target = "recipients", source = "recipients", qualifiedByName = "shortNameSet")
    RecipientGroupDTO toDto(RecipientGroup s);

    @Mapping(target = "removeRecipient", ignore = true)
    RecipientGroup toEntity(RecipientGroupDTO recipientGroupDTO);

    @Named("name")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    RecipientGroupDTO toDtoName(RecipientGroup recipientGroup);
}
