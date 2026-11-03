package uz.paykap.service.mapper.simple;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.paykap.domain.Field;
import uz.paykap.service.dto.simple.SimpleFieldDTO;

import java.util.List;

/**
 * Mapper for the entity Field and its DTO FieldDTO.
 */
@Mapper(componentModel = "spring", uses = {SimpleRecipientMapper.class, SimpleI18nMapper.class, SimplePrefixMapper.class, SimpleFieldValueMapper.class})
public interface SimpleFieldMapper {

    @Mapping(source = "recipient.id", target = "recipientId")
    @Mapping(source = "content", target = "contentType")
//    @Mapping(source = "recipient.shortName", target = "recipientShortName")
    SimpleFieldDTO toDto(Field field);

    List<SimpleFieldDTO> toDto(List<Field> fields);
}
