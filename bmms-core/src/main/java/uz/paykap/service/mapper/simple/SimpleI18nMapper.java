package uz.paykap.service.mapper.simple;

import org.mapstruct.Mapper;
import uz.paykap.domain.I18n;
import uz.paykap.service.dto.simple.SimpleI18nDTO;
import uz.paykap.service.mapper.EntityMapper;

/**
 * Mapper for the entity I18n and its DTO I18nDTO.
 */
@Mapper(componentModel = "spring")
public interface SimpleI18nMapper extends EntityMapper<SimpleI18nDTO, I18n> {

}
