package uz.paykap.service.mapper;

import org.mapstruct.*;
import uz.paykap.domain.*;
import uz.paykap.service.dto.GroupSettingDTO;

/**
 * Mapper for the entity {@link GroupSetting} and its DTO {@link GroupSettingDTO}.
 */
@Mapper(componentModel = "spring", uses = { SettingPropertyMapper.class })
public interface GroupSettingMapper extends EntityMapper<GroupSettingDTO, GroupSetting> {
//    @Mapping(target = "settingType", source = "settingType", qualifiedByName = "name")
    //    @Mapping(target = "props", source = "props", qualifiedByName = "idSet")
    GroupSettingDTO toDto(GroupSetting s);

    @Mapping(target = "removeProp", ignore = true)
    GroupSetting toEntity(GroupSettingDTO groupSettingDTO);
}
