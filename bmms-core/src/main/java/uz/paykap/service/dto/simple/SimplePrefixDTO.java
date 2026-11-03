package uz.paykap.service.dto.simple;

import io.swagger.annotations.ApiModelProperty;
import uz.paykap.domain.enumeration.PrefixType;

import java.io.Serializable;

/**
 * A DTO for the Prefix entity.
 */
public class SimplePrefixDTO implements Serializable {

    @ApiModelProperty(value = "Тип префикса", notes = "STATIC - статическое значение, SELECT - выбарочное значение (один из опции выборки), REQUIRE - статическое значение по отфилтрованному полю (см. requiredField)")
    private PrefixType type;
    @ApiModelProperty(value = "Значение префикса", example = "+998")
    private String value;
    @ApiModelProperty(value = "Добавить в начало значение поля", notes = "Нужно ли добавить значение в значение поля", example = "field.phoneNumber = '+998' + field.phoneNumber")
    private Boolean output;
    @ApiModelProperty(value = "Требуемое поле", notes = "Поле требуемая для сортировки показа этого элемента, (см. type='REQUIRE')")
    private String requireField;

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

    public Boolean isOutput() {
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

    @Override
    public String toString() {
        return "SimplePrefixDTO{" +
            "type='" + getType() + "'" +
            ", value='" + getValue() + "'" +
            ", output='" + isOutput() + "'" +
            ", requireField='" + getRequireField() + "'" +
//            ", field=" + getFieldId() +
//            ", field='" + getFieldName() + "'" +
            "}";
    }
}
