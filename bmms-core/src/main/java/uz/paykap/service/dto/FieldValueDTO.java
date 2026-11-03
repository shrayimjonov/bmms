package uz.paykap.service.dto;

import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link uz.paykap.domain.FieldValue} entity.
 */
public class FieldValueDTO implements Serializable {

    private Long id;

    /**
     * Param value to send to server
     */
    @NotNull
    @Size(max = 64)
    @ApiModelProperty(value = "Param value to send to server", required = true)
    private String value;

    /**
     * Price of selected value
     */
    @ApiModelProperty(value = "Price of selected value")
    private Long amount;

    /**
     * Filter the values (field.type=select) by parent field
     */
    @Size(max = 16)
    @ApiModelProperty(value = "Filter the values (field.type=select) by parent field")
    private String filterBy;

    /**
     * Display order
     */
    @ApiModelProperty(value = "Display order")
    private Integer ord;

    private FieldDTO field;

    private Set<I18nDTO> titles = new HashSet<>();

    private Set<I18nDTO> descriptions = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getFilterBy() {
        return filterBy;
    }

    public void setFilterBy(String filterBy) {
        this.filterBy = filterBy;
    }

    public Integer getOrd() {
        return ord;
    }

    public void setOrd(Integer ord) {
        this.ord = ord;
    }

    public FieldDTO getField() {
        return field;
    }

    public void setField(FieldDTO field) {
        this.field = field;
    }

    public Set<I18nDTO> getTitles() {
        return titles;
    }

    public void setTitles(Set<I18nDTO> titles) {
        this.titles = titles;
    }

    public Set<I18nDTO> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(Set<I18nDTO> descriptions) {
        this.descriptions = descriptions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FieldValueDTO)) {
            return false;
        }

        FieldValueDTO fieldValueDTO = (FieldValueDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, fieldValueDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "FieldValueDTO{" +
            "id=" + getId() +
            ", value='" + getValue() + "'" +
            ", amount=" + getAmount() +
            ", filterBy='" + getFilterBy() + "'" +
            ", ord=" + getOrd() +
            ", field=" + getField() +
            ", titles=" + getTitles() +
            ", descriptions=" + getDescriptions() +
            "}";
    }
}
