package uz.paykap.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uz.devops.bmcore.adapter.dto.SharedField;
import uz.paykap.domain.Field;
import uz.paykap.service.dto.FieldDTO;

import java.util.Set;

/**
 * Mapper for the entity {@link Field} and its DTO {@link FieldDTO}.
 */
@Mapper(componentModel = "spring", uses = { RecipientMapper.class, I18nMapper.class })
public interface FieldMapper extends EntityMapper<FieldDTO, Field> {
    @Mapping(target = "recipient", source = "recipient", qualifiedByName = "shortName")
    @Mapping(target = "titles", source = "titles", qualifiedByName = "idSet")
    @Mapping(target = "descriptions", source = "descriptions", qualifiedByName = "idSet")
    FieldDTO toDto(Field s);

    @Mapping(target = "removeTitle", ignore = true)
    @Mapping(target = "removeDescription", ignore = true)
    Field toEntity(FieldDTO fieldDTO);

    @Named("name")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    FieldDTO toDtoName(Field field);

    Field fromShared(SharedField field);

    Set<Field> fromShared(Set<SharedField> field);
}
