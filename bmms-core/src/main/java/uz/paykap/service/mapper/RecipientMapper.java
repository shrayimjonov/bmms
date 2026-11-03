package uz.paykap.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uz.devops.bmcore.adapter.dto.SharedRecipient;
import uz.paykap.domain.Recipient;
import uz.paykap.service.dto.RecipientDTO;

import java.util.Set;

/**
 * Mapper for the entity {@link Recipient} and its DTO {@link RecipientDTO}.
 */
@Mapper(componentModel = "spring", uses = { GatewayMapper.class, CategoryMapper.class, I18nMapper.class })
public interface RecipientMapper extends EntityMapper<RecipientDTO, Recipient> {
    @Mapping(target = "gateway", source = "gateway", qualifiedByName = "name")
    @Mapping(target = "category", source = "category", qualifiedByName = "shortName")
    @Mapping(target = "titles", source = "titles", qualifiedByName = "idSet")
    @Mapping(target = "fullNames", source = "fullNames", qualifiedByName = "idSet")
    RecipientDTO toDto(Recipient s);

    @Named("id")
    @Mapping(target = "id", source = "id")
    RecipientDTO toDtoId(Recipient recipient);

    @Mapping(target = "removeTitle", ignore = true)
    @Mapping(target = "removeFullName", ignore = true)
    Recipient toEntity(RecipientDTO recipientDTO);

    @Named("shortName")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "shortName", source = "shortName")
    RecipientDTO toDtoShortName(Recipient recipient);

    @Named("shortNameSet")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "shortName", source = "shortName")
    Set<RecipientDTO> toDtoShortNameSet(Set<Recipient> recipient);

    Recipient fromShared(SharedRecipient recipient);

    Set<Recipient> fromShared(Set<SharedRecipient> recipient);

    SharedRecipient toShared(RecipientDTO recipientDTO);
}
