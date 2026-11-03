package uz.paykap.service.mapper.simple;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.paykap.domain.Category;
import uz.paykap.service.dto.simple.SimpleCategoryDTO;

import java.util.List;

/**
 * Mapper for the entity Category and its DTO CategoryDTO.
 */
@Mapper(componentModel = "spring", uses = {SimpleI18nMapper.class})
public interface SimpleCategoryMapper {

    @Mapping(source = "parent.id", target = "parentId")
    @Mapping(source = "parent.shortName", target = "parentShortName")
    SimpleCategoryDTO toDto(Category category);

    List<SimpleCategoryDTO> toDto(List<Category> categories);
}
