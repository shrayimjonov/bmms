package uz.paykap.service.mapper.simple;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.paykap.domain.Recipient;
import uz.paykap.service.dto.simple.SimpleRecipientDTO;

import java.util.List;

/**
 * Mapper for the entity Recipient and its DTO RecipientDTO.
 */
@Mapper(componentModel = "spring", uses = {SimpleCategoryMapper.class, SimpleI18nMapper.class})
public interface SimpleRecipientMapper {

    @Mapping(source = "category.id", target = "categoryId")
//    @Mapping(source = "category.shortName", target = "categoryShortName")
    SimpleRecipientDTO toDto(Recipient recipient);

    List<SimpleRecipientDTO> toDto(List<Recipient> recipient);
}
