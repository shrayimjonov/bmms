package uz.paykap.service.dto.simple;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import uz.devops.bmcore.enumeration.FieldType;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.Visibility;
import uz.paykap.service.serializer.I18nListSerializer;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * A DTO for the Field entity.
 */
@Getter
@Setter
@ToString
public class SimpleResponseFieldDTO implements Serializable {

    @ApiModelProperty(value = "Ид. поля ответа", required = true, example = "234")
    private Long id;
    @ApiModelProperty(value = "Название поля ответа (параметра)", required = true, example = "clientId")
    private String name;
    @ApiModelProperty(value = "Видимость", notes = "HIDDEN - не отображать в UI, PRINTER - отображать в чеке , MONITOR - отображать в экране (в деталях), ALL - отображать везде")
    private Visibility visibility;
    @ApiModelProperty(value = "Порядок в списке", notes = "A-Z. (display order)")
    private Integer ord;
    @ApiModelProperty("Ид. услуги (получателя)")
    private Long recipientId;
    @ApiModelProperty(value = "Тип запроса к которому относится поле: CHECK - проверка счета, PAY - оплата, STATUS - проверка статуса, CANCEL - отмена")
    private RequestType requestType;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @ApiModelProperty("Названии поля ответа для UI на разных языках")
    @JsonSerialize(using = I18nListSerializer.class)
    private Set<SimpleI18nDTO> labels = new HashSet<>();

}
