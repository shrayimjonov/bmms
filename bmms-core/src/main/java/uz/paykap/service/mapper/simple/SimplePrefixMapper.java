package uz.paykap.service.mapper.simple;

import org.mapstruct.Mapper;
import uz.paykap.domain.Prefix;
import uz.paykap.service.dto.simple.SimplePrefixDTO;

import java.util.List;

/**
 * Mapper for the entity Prefix and its DTO PrefixDTO.
 */
@Mapper(componentModel = "spring", uses = {SimpleFieldMapper.class})
public interface SimplePrefixMapper {

//    @Mapping(source = "field.id", target = "fieldId")
//    @Mapping(source = "field.name", target = "fieldName")
    SimplePrefixDTO toDto(Prefix prefix);

    List<SimplePrefixDTO> toDto(List<Prefix> prefixes);
}
