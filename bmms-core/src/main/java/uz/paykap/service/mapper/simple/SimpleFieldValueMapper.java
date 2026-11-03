package uz.paykap.service.mapper.simple;

import org.mapstruct.Mapper;
import uz.paykap.domain.FieldValue;
import uz.paykap.service.dto.simple.SimpleFieldValueDTO;

import java.util.List;

/**
 * Mapper for the entity FieldValue and its DTO FieldValueDTO.
 */
@Mapper(componentModel = "spring", uses = {SimpleFieldMapper.class, SimpleI18nMapper.class})
public interface SimpleFieldValueMapper {

//    @Mapping(source = "field.id", target = "fieldId")
//    @Mapping(source = "field.name", target = "fieldName")
    SimpleFieldValueDTO toDto(FieldValue fieldValue);

    List<SimpleFieldValueDTO> toDto(List<FieldValue> fieldValues);
}
