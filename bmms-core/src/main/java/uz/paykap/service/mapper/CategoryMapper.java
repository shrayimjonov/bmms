package uz.paykap.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uz.devops.bmcore.adapter.dto.SharedCategory;
import uz.paykap.domain.Category;
import uz.paykap.service.dto.CategoryDTO;

import java.util.Set;

/**
 * Mapper for the entity {@link Category} and its DTO {@link CategoryDTO}.
 */
@Mapper(componentModel = "spring", uses = { I18nMapper.class })
public interface CategoryMapper extends EntityMapper<CategoryDTO, Category> {
    @Mapping(target = "parent", source = "parent", qualifiedByName = "shortName")
    @Mapping(target = "names", source = "names", qualifiedByName = "idSet")
    CategoryDTO toDto(Category s);

    @Mapping(target = "removeName", ignore = true)
    Category toEntity(CategoryDTO categoryDTO);

    Category fromShared(SharedCategory recipient);

    Set<Category> fromShared(Set<SharedCategory> recipient);

    @Named("shortName")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "shortName", source = "shortName")
    CategoryDTO toDtoShortName(Category category);
}
