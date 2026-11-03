package uz.paykap.service.dto.simple;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import uz.devops.bmcore.enumeration.Lang;

import java.io.Serializable;

/**
 * A DTO for the I18n entity.
 */
@Getter
@Setter
@ToString
//@JsonSerialize(using = I18nValueSerializer.class)
public class SimpleI18nDTO implements Serializable {

    @ApiModelProperty(value = "Язык", example = "ru", required = true)
    private Lang lang;
    @ApiModelProperty(value = "Текст на данном языке", example = "Привет!", required = true)
    private String text;

}
