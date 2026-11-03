package uz.paykap.service.mapper;

import java.util.Set;
import org.mapstruct.*;
import uz.devops.bmcore.adapter.dto.SharedFieldValue;
import uz.paykap.domain.*;
import uz.paykap.service.dto.FieldValueDTO;

/**
 * Mapper for the entity {@link FieldValue} and its DTO {@link FieldValueDTO}.
 */
@Mapper(componentModel = "spring", uses = { FieldMapper.class, I18nMapper.class })
public interface FieldValueMapper extends EntityMapper<FieldValueDTO, FieldValue> {
    @Mapping(target = "field", source = "field", qualifiedByName = "name")
    @Mapping(target = "titles", source = "titles", qualifiedByName = "idSet")
    @Mapping(target = "descriptions", source = "descriptions", qualifiedByName = "idSet")
    FieldValueDTO toDto(FieldValue s);

    @Mapping(target = "removeTitle", ignore = true)
    @Mapping(target = "removeDescription", ignore = true)
    FieldValue toEntity(FieldValueDTO fieldValueDTO);

    FieldValue fromShared(SharedFieldValue sharedFieldValue);

    Set<FieldValue> fromShared(Set<SharedFieldValue> sharedFieldValues);

    FieldValue clone(FieldValue fieldValue);

    default FieldValue fromId(Long id) {
        if (id == null) {
            return null;
        }
        FieldValue fieldValue = new FieldValue();
        fieldValue.setId(id);
        return fieldValue;
    }
}
