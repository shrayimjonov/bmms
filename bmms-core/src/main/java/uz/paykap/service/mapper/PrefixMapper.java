package uz.paykap.service.mapper;

import java.util.Set;
import org.mapstruct.*;
import uz.devops.bmcore.adapter.dto.SharedPrefix;
import uz.paykap.domain.*;
import uz.paykap.service.dto.PrefixDTO;

/**
 * Mapper for the entity {@link Prefix} and its DTO {@link PrefixDTO}.
 */
@Mapper(componentModel = "spring", uses = { FieldMapper.class })
public interface PrefixMapper extends EntityMapper<PrefixDTO, Prefix> {
    @Mapping(target = "field", source = "field", qualifiedByName = "name")
    PrefixDTO toDto(Prefix s);

    Prefix fromShared(SharedPrefix sharedPrefix);

    Set<Prefix> fromShared(Set<SharedPrefix> sharedPrefixes);
}
