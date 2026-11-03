package uz.paykap.service.dto.simple;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import uz.devops.bmcore.enumeration.RecipientType;
import uz.paykap.service.serializer.I18nListSerializer;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * A DTO for the Recipient entity.
 */
@Data
public class SimpleRecipientDTO implements Serializable {

    @ApiModelProperty("Ид. получателя")
    private Long id;

    @ApiModelProperty(value = "Уникальное название поставщика")
    private String shortName;

    @ApiModelProperty("URL лого файла")
    private String logo;

    @ApiModelProperty(value = "Соотношение суммы", notes = "Например: 1 - рубли, сумы, доллара; 100 - копейки, тиины, центы")
    private Integer amountRatio;

    @ApiModelProperty(value = "Минимальная сумма")
    private Long minAmount;

    @ApiModelProperty(value = "Максимальная сумма")
    private Long maxAmount;

    @ApiModelProperty("Порядок в списке, A-Z. (display order)")
    private Integer ord;

    @ApiModelProperty("Ид. родителя (категории)")
    private Long categoryId;

    @ApiModelProperty("Тип")
    private RecipientType type;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @ApiModelProperty(value = "Названии элемента для UI на разных языках", example = "ru:'ООО Компания', uz:'MChJ Tashkilot', en:'LLC Company'")
    @JsonSerialize(using = I18nListSerializer.class)
    private Set<SimpleI18nDTO> titles = new HashSet<>();

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @ApiModelProperty(value = "Полные названия элемента для UI на разных языках", example = "ru:'ООО Компания', uz:'MChJ Tashkilot', en:'LLC Company'")
    @JsonSerialize(using = I18nListSerializer.class)
    private Set<SimpleI18nDTO> fullNames = new HashSet<>();

    @ApiModelProperty(value = "Дополнительные параметры в виде json")
    @JsonRawValue
    private String jsonParams;
}
