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
public class SimpleFieldDTO implements Serializable {

    @ApiModelProperty(value = "Ид. поля", required = true, example = "123")
    private Long id;
    @ApiModelProperty(value = "Название поля (параметра)", required = true, example = "clientId")
    private String name;
    @ApiModelProperty("Тип поля, TEXT - текстовое поле, SELECT - список (dropdown)")
    private FieldType type;
    @ApiModelProperty(value = "Тип вводимых данных для UI", example = "phone, email")
    private String contentType;
    @ApiModelProperty(value = "Java Regexp для валидации значения поля", example = "\\\\d{4,6}")
    private String validation;
    @ApiModelProperty(value = "Заполнитель для UI", example = "+998** *** ** **")
    private String placeholder;
    @ApiModelProperty(value = "Маска значений для UI", example = "+998xx-xxx-xx-xx")
    private String mask;
    @ApiModelProperty(value = "Символ маски значений для UI", example = "x")
    private String maskSymbol;
    @ApiModelProperty(value = "Минимальная длина значений", example = "5")
    private Integer lengthMin;
    @ApiModelProperty(value = "Максимальная длина значений", example = "10")
    private Integer lengthMax;
    @ApiModelProperty(value = "Требуемое поле", notes = "Поле требуемая для сортировки показа этого элемента или дочерных значений (field.values)")
    private String requireField;
    @ApiModelProperty(value = "Является ли поле необязательным", notes = "true - необязательное, false - обязательное")
    private Boolean optional;
    @ApiModelProperty(value = "Нужно ли отправлять значение в запросе", notes = "true - отправлять, false - не отправлять")
    private Boolean output;
    @ApiModelProperty(value = "Видимость", notes = "HIDDEN - не отображать в UI, PRINTER - отображать в чеке , MONITOR - отображать в экране (в деталях), ALL - отображать везде")
    private Visibility visibility;
    @ApiModelProperty(value = "Порядок в списке", notes = "A-Z. (display order)")
    private Integer ord;
    @ApiModelProperty("Ид. услуги (получателя)")
    private Long recipientId;
    @ApiModelProperty(value = "Тип запроса к которому относится поле: CHECK - проверка счета, PAY - оплата, STATUS - проверка статуса, CANCEL - отмена")
    private RequestType requestType;
    @ApiModelProperty(value = "Филтровать по значению", notes = "Если указано значение, то для показа этого элемента должно быть истиной выражение : вибранное значение поля 'requiredField' к этому значение (filterBy)")
    private String filterBy;
    @ApiModelProperty("Префиксы элемента для UI на разных языках")
    private Set<SimplePrefixDTO> prefixes = new HashSet<>();
    @ApiModelProperty("Значения элемента елси type = 'SELECT'")
    private Set<SimpleFieldValueDTO> values = new HashSet<>();

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @ApiModelProperty("Названии элемента для UI на разных языках")
    @JsonSerialize(using = I18nListSerializer.class)
    private Set<SimpleI18nDTO> titles = new HashSet<>();

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @ApiModelProperty("Описании элемента для UI на разных языках")
    @JsonSerialize(using = I18nListSerializer.class)
    private Set<SimpleI18nDTO> descriptions = new HashSet<>();

}
