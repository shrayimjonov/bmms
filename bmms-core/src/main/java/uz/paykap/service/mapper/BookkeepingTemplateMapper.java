package uz.paykap.service.mapper;

import org.mapstruct.*;
import uz.paykap.domain.*;
import uz.paykap.service.dto.BookkeepingTemplateDTO;

/**
 * Mapper for the entity {@link BookkeepingTemplate} and its DTO {@link BookkeepingTemplateDTO}.
 */
@Mapper(componentModel = "spring", uses = { GatewayMapper.class, RecipientMapper.class })
public interface BookkeepingTemplateMapper extends EntityMapper<BookkeepingTemplateDTO, BookkeepingTemplate> {
    @Mapping(target = "gateway", source = "gateway", qualifiedByName = "name")
    @Mapping(target = "recipient", source = "recipient", qualifiedByName = "shortName")
    BookkeepingTemplateDTO toDto(BookkeepingTemplate s);
}
