package uz.paykap.service.dto;

import io.swagger.annotations.ApiModel;
import java.io.Serializable;
import java.util.Objects;
import javax.validation.constraints.*;
import uz.paykap.domain.enumeration.Prop;

/**
 * A DTO for the {@link uz.paykap.domain.SettingProperty} entity.
 */
@ApiModel(description = "Значения настроек оплат")
public class SettingPropertyDTO extends AbstractAuditingDTO implements Serializable {

    private Long id;

    @NotNull
    private Prop key;

    @NotNull
    @Size(max = 64)
    private String value;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Prop getKey() {
        return key;
    }

    public void setKey(Prop key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SettingPropertyDTO)) {
            return false;
        }

        SettingPropertyDTO settingPropertyDTO = (SettingPropertyDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, settingPropertyDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SettingPropertyDTO{" +
            "id=" + getId() +
            ", key='" + getKey() + "'" +
            ", value='" + getValue() + "'" +
            "}";
    }
}
