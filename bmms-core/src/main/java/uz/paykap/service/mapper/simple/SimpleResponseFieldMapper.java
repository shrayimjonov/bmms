package uz.paykap.service.mapper.simple;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.paykap.domain.ResponseField;
import uz.paykap.service.dto.simple.SimpleResponseFieldDTO;

import java.util.List;

/**
 * Mapper for the entity Field and its DTO FieldDTO.
 */
@Mapper(componentModel = "spring", uses = {SimpleRecipientMapper.class, SimpleI18nMapper.class})
public interface SimpleResponseFieldMapper {

    @Mapping(source = "recipient.id", target = "recipientId")
    SimpleResponseFieldDTO toDto(ResponseField responseField);

    List<SimpleResponseFieldDTO> toDto(List<ResponseField> responseFields);
}
