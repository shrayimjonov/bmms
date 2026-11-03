package uz.paykap.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.*;
import javax.validation.constraints.*;
import uz.paykap.domain.SettingProperty;
import uz.paykap.domain.enumeration.Prop;

/**
 * A DTO for the {@link uz.paykap.domain.Setting} entity.
 */
@ApiModel(description = "Настройки для проведения трансакций")
public class SettingDTO implements Serializable {

    private Long id;

    /**
     * Short name to show in UI
     */
    @NotNull
    @Size(max = 64)
    @ApiModelProperty(value = "Short name to show in UI", required = true)
    private String shortName;

    /**
     * Порядковый номер
     */
    @ApiModelProperty(value = "Порядковый номер")
    private Integer ord;

    @NotNull
    private Boolean active;

    private Map<Prop, SettingProperty> props = new HashMap<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Integer getOrd() {
        return ord;
    }

    public void setOrd(Integer ord) {
        this.ord = ord;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Map<Prop, SettingProperty> getProps() {
        return props;
    }

    public void setProps(Map<Prop, SettingProperty> settingProperties) {
        this.props = settingProperties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SettingDTO)) {
            return false;
        }

        SettingDTO settingDTO = (SettingDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, settingDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SettingDTO{" +
            "id=" + getId() +
            ", shortName='" + getShortName() + "'" +
            ", ord=" + getOrd() +
            ", active='" + isActive() + "'" +
            "}";
    }
}
