package uz.paykap.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uz.paykap.domain.I18n;
import uz.paykap.service.dto.I18nDTO;

import java.util.Set;

/**
 * Mapper for the entity {@link I18n} and its DTO {@link I18nDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface I18nMapper extends EntityMapper<I18nDTO, I18n> {
    @Named("idSet")
    @Mapping(target = "id", source = "id")
    Set<I18nDTO> toDtoIdSet(Set<I18n> i18n);
}
