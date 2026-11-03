package uz.paykap.service.mapper;

import java.util.Set;
import org.mapstruct.*;
import uz.devops.bmcore.adapter.dto.SharedResponseField;
import uz.paykap.domain.*;
import uz.paykap.service.dto.ResponseFieldDTO;

/**
 * Mapper for the entity {@link ResponseField} and its DTO {@link ResponseFieldDTO}.
 */
@Mapper(componentModel = "spring", uses = { RecipientMapper.class, I18nMapper.class })
public interface ResponseFieldMapper extends EntityMapper<ResponseFieldDTO, ResponseField> {
    @Mapping(target = "recipient", source = "recipient", qualifiedByName = "shortName")
    @Mapping(target = "labels", source = "labels", qualifiedByName = "idSet")
    ResponseFieldDTO toDto(ResponseField s);

    @Mapping(target = "removeLabel", ignore = true)
    ResponseField toEntity(ResponseFieldDTO responseFieldDTO);

    @Mapping(source = "order", target = "ord")
    ResponseField fromShared(SharedResponseField sharedResponseField);

    Set<ResponseField> fromShared(Set<SharedResponseField> sharedResponseFields);
}
