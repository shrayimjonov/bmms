package uz.paykap.service.dto;

import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.validation.constraints.*;
import uz.paykap.domain.enumeration.PrefixType;

/**
 * A DTO for the {@link uz.paykap.domain.Prefix} entity.
 */
public class PrefixDTO implements Serializable {

    private Long id;

    private PrefixType type;

    @Size(max = 16)
    private String value;

    /**
     * Need to prepend to field value
     */
    @ApiModelProperty(value = "Need to prepend to field value")
    private Boolean output;

    /**
     * Filter prefix value by selected parent field
     */
    @Size(max = 64)
    @ApiModelProperty(value = "Filter prefix value by selected parent field")
    private String requireField;

    private FieldDTO field;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PrefixType getType() {
        return type;
    }

    public void setType(PrefixType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Boolean getOutput() {
        return output;
    }

    public void setOutput(Boolean output) {
        this.output = output;
    }

    public String getRequireField() {
        return requireField;
    }

    public void setRequireField(String requireField) {
        this.requireField = requireField;
    }

    public FieldDTO getField() {
        return field;
    }

    public void setField(FieldDTO field) {
        this.field = field;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PrefixDTO)) {
            return false;
        }

        PrefixDTO prefixDTO = (PrefixDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, prefixDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PrefixDTO{" +
            "id=" + getId() +
            ", type='" + getType() + "'" +
            ", value='" + getValue() + "'" +
            ", output='" + getOutput() + "'" +
            ", requireField='" + getRequireField() + "'" +
            ", field=" + getField() +
            "}";
    }
}
