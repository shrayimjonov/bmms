package uz.paykap.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uz.paykap.domain.SettingProperty;
import uz.paykap.service.dto.SettingPropertyDTO;

import java.util.Set;

/**
 * Mapper for the entity {@link SettingProperty} and its DTO {@link SettingPropertyDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface SettingPropertyMapper extends EntityMapper<SettingPropertyDTO, SettingProperty> {
    @Named("idSet")
    @Mapping(target = "id", source = "id")
    Set<SettingPropertyDTO> toDtoIdSet(Set<SettingProperty> settingProperty);

    default SettingProperty fromId(Long id) {
        if (id == null) {
            return null;
        }
        SettingProperty settingProperty = new SettingProperty();
        settingProperty.setId(id);
        return settingProperty;
    }
}
