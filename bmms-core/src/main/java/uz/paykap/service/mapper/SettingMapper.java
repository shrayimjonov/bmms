package uz.paykap.service.mapper;

import org.mapstruct.*;
import uz.paykap.domain.*;
import uz.paykap.service.dto.SettingDTO;

/**
 * Mapper for the entity {@link Setting} and its DTO {@link SettingDTO}.
 */
@Mapper(componentModel = "spring", uses = { SettingPropertyMapper.class })
public interface SettingMapper extends EntityMapper<SettingDTO, Setting> {
    @Mapping(target = "props", source = "props")
    SettingDTO toDto(Setting s);

    @Mapping(target = "removeProp", ignore = true)
    Setting toEntity(SettingDTO settingDTO);

    @Named("shortName")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "shortName", source = "shortName")
    SettingDTO toDtoShortName(Setting setting);

    default Setting fromId(Long id) {
        if (id == null) {
            return null;
        }
        Setting setting = new Setting();
        setting.setId(id);
        return setting;
    }
}
