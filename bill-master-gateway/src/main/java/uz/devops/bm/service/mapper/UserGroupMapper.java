package uz.devops.bm.service.mapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.control.MappingControl;
import uz.devops.bm.domain.Authority;
import uz.devops.bm.domain.UserGroup;
import uz.devops.bm.service.dto.AdminUserGroupDTO;
import uz.devops.bm.service.dto.UserGroupDTO;

/**
 * Mapper for the entity {@link UserGroup} and its DTO {@link UserGroupDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface UserGroupMapper extends EntityMapper<UserGroupDTO, UserGroup> {
    default AdminUserGroupDTO fromEntityToAUserGroup(UserGroup userGroup) {
        if (userGroup == null) {
            return null;
        }

        AdminUserGroupDTO adminUserGroupDTO = new AdminUserGroupDTO();
        adminUserGroupDTO.setId(userGroup.getId());
        adminUserGroupDTO.setActive(userGroup.getActive());
        adminUserGroupDTO.setName(userGroup.getName());
        adminUserGroupDTO.setDetails(userGroup.getDetails());
        adminUserGroupDTO.setType(userGroup.getType());

        adminUserGroupDTO.setAuthorities(new HashSet<>());
        for (Authority authority : userGroup.getAuthorities()) {
            adminUserGroupDTO.getAuthorities().add(authority.getName());
        }

        return adminUserGroupDTO;
    }

    default List<AdminUserGroupDTO> fromEntityToAUserGroup(List<UserGroup> userGroups) {
        if (userGroups == null) {
            return null;
        }

        List<AdminUserGroupDTO> adminUserGroupDTOS = new ArrayList<>();
        for (UserGroup userGroup : userGroups) {
            adminUserGroupDTOS.add(fromEntityToAUserGroup(userGroup));
        }
        return adminUserGroupDTOS;
    }

    default UserGroup fromAUserGroupToEntity(AdminUserGroupDTO adminUserGroupDTO) {
        if (adminUserGroupDTO == null) {
            return null;
        }
        UserGroup userGroup = new UserGroup();
        userGroup.setId(adminUserGroupDTO.getId());
        userGroup.setName(adminUserGroupDTO.getName());
        userGroup.setActive(adminUserGroupDTO.getActive());
        userGroup.setDetails(adminUserGroupDTO.getDetails());
        userGroup.setType(adminUserGroupDTO.getType());
        //        userGroup.setAuthorities(adminUserGroupDTO.getAuthorities());
        return userGroup;
    }

    default List<UserGroup> fromAUserGroupToEntity(List<AdminUserGroupDTO> adminUserGroupDTOS) {
        if (adminUserGroupDTOS == null) {
            return null;
        }

        List<UserGroup> userGroups = new ArrayList<>();
        for (AdminUserGroupDTO userGroup : adminUserGroupDTOS) {
            userGroups.add(fromAUserGroupToEntity(userGroup));
        }
        return userGroups;
    }
}
