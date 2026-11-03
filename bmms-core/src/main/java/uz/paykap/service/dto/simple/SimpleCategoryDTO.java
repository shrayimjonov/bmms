package uz.paykap.service.dto.simple;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import uz.paykap.service.serializer.I18nListSerializer;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * A DTO for the Category entity.
 */
@Data
public class SimpleCategoryDTO implements Serializable {

    @ApiModelProperty(value = "Ид. категории", example = "1")
    private Long id;

    @ApiModelProperty(value = "Short name", example = "1")
    private String shortName;

    @ApiModelProperty(value = "URL лого файла", example = "https://www.google.ru/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png")
    @Size(max = 64)
    private String logo;

    @ApiModelProperty(value = "Порядок в списке, A-Z. (display order)", example = "1")
    private Integer ord;

    @ApiModelProperty(value = "Ид. родителя (категории)", example = "2")
    private Long parentId;

    @ApiModelProperty(value = "Краткое наим.", example = "Mobile Services")
    private String parentShortName;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @ApiModelProperty(value = "Названии элемента для UI на разных языках")
    @JsonSerialize(using = I18nListSerializer.class)
    private Set<SimpleI18nDTO> names = new HashSet<>();

}
