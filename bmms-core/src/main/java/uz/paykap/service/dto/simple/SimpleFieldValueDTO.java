package uz.paykap.service.dto.simple;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import uz.paykap.service.serializer.I18nListSerializer;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * A DTO for the FieldValue entity.
 */
@Data
public class SimpleFieldValueDTO implements Serializable {

    @ApiModelProperty(value = "Значение", example = "12345abc")
    private String value;

    @ApiModelProperty(value = "Предопределенная сумма", notes = "если указано значение то подставляется в значение суммы родительского поля")
    private Long amount;

    @ApiModelProperty(value = "Филтровать по значению", notes = "Если указано значение, то для показа этого элемента должно быть истиной выражение : вибранное значение поля 'requiredField' к этому значение (filterBy)")
    private String filterBy;

    @ApiModelProperty("Порядок в списке, A-Z. (display order)")
    private Integer ord;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @ApiModelProperty(value = "Названии элемента для UI на разных языках", example = "Городское отделение Beeline")
    @JsonSerialize(using = I18nListSerializer.class)
    private Set<SimpleI18nDTO> titles = new HashSet<>();

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @ApiModelProperty("Описании элемента для UI на разных языках")
    @JsonSerialize(using = I18nListSerializer.class)
    private Set<SimpleI18nDTO> descriptions = new HashSet<>();
}
