package uz.paykap.service.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.*;
import javax.validation.constraints.*;
import uz.paykap.domain.SettingProperty;
import uz.paykap.domain.enumeration.Prop;
import uz.paykap.service.serializer.SettingGroupPropsMapToListDeserializer;
import uz.paykap.service.serializer.SettingGroupPropsMapToListSerializer;

/**
 * A DTO for the {@link uz.paykap.domain.GroupSetting} entity.
 */
@ApiModel(description = "Тип настройки")
public class GroupSettingDTO extends AbstractAuditingDTO implements Serializable {

    private Long id;

    /**
     * Группа - clearingNumber of terminal
     */
    @NotNull
    @Size(max = 64)
    @ApiModelProperty(value = "Группа - clearingNumber of terminal", required = true)
    private String requestGroup;

    private SettingTypeDTO settingType;

    /* in json it is like : SettingProperty[] */
    @JsonSerialize(using = SettingGroupPropsMapToListSerializer.class)
    @JsonDeserialize(using = SettingGroupPropsMapToListDeserializer.class)
    private Map<Prop, SettingPropertyDTO> props = new HashMap<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequestGroup() {
        return requestGroup;
    }

    public void setRequestGroup(String requestGroup) {
        this.requestGroup = requestGroup;
    }

    public SettingTypeDTO getSettingType() {
        return settingType;
    }

    public void setSettingType(SettingTypeDTO settingType) {
        this.settingType = settingType;
    }

    public Map<Prop, SettingPropertyDTO> getProps() {
        return props;
    }

    public void setProps(Map<Prop, SettingPropertyDTO> settingProperties) {
        this.props = settingProperties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GroupSettingDTO)) {
            return false;
        }

        GroupSettingDTO groupSettingDTO = (GroupSettingDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, groupSettingDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "GroupSettingDTO{" +
            "id=" + getId() +
            ", requestGroup='" + getRequestGroup() + "'" +
            ", settingType=" + getSettingType() +
            ", props=" + getProps() +
            "}";
    }
}
