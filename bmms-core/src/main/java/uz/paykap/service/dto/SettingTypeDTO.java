package uz.paykap.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link uz.paykap.domain.SettingType} entity.
 */
@ApiModel(description = "Наименование настройки")
public class SettingTypeDTO extends AbstractAuditingDTO implements Serializable {

    private Long id;

    /**
     * Setting unique name
     */
    @NotNull
    @Size(max = 128)
    @ApiModelProperty(value = "Setting unique name", required = true)
    private String name;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SettingTypeDTO)) {
            return false;
        }

        SettingTypeDTO settingTypeDTO = (SettingTypeDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, settingTypeDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SettingTypeDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            "}";
    }
}
