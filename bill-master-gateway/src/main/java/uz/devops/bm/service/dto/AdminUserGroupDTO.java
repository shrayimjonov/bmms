package uz.devops.bm.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import uz.devops.bm.domain.enumeration.UserGroupType;

/**
 * A DTO for the {@link uz.devops.bm.domain.UserGroup} entity.
 */
@ApiModel(description = "Группа привилегий - Роль")
public class AdminUserGroupDTO implements Serializable {

    private Long id;

    /**
     * Наименование
     */
    @NotNull(message = "must not be null")
    @Size(max = 64)
    @ApiModelProperty(value = "Наименование", required = true)
    private String name;

    /**
     * Активность
     */
    @NotNull(message = "must not be null")
    @ApiModelProperty(value = "Активность", required = true)
    private Boolean active;

    /**
     * Описание действия
     */
    @ApiModelProperty(value = "Описание действия")
    private String details;

    /**
     * Тип пользователя
     */
    @ApiModelProperty(value = "Тип пользователя")
    private UserGroupType type;

    private Set<String> authorities;

    public Set<String> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<String> authorities) {
        this.authorities = authorities;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public UserGroupType getType() {
        return type;
    }

    public void setType(UserGroupType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AdminUserGroupDTO)) {
            return false;
        }

        AdminUserGroupDTO userGroupDTO = (AdminUserGroupDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, userGroupDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return (
            "UserGroupDTO{" +
            "id=" +
            id +
            ", name='" +
            name +
            '\'' +
            ", active=" +
            active +
            ", details='" +
            details +
            '\'' +
            ", type=" +
            type +
            ", authorities=" +
            authorities +
            '}'
        );
    }
}
