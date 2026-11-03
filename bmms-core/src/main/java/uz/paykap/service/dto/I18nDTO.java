package uz.paykap.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.validation.constraints.*;

import lombok.NoArgsConstructor;
import uz.devops.bmcore.enumeration.Lang;

/**
 * A DTO for the {@link uz.paykap.domain.I18n} entity.
 */
@ApiModel(description = "Языковые Переводы")
@NoArgsConstructor
public class I18nDTO implements Serializable {

    private Long id;

    /**
     * Language
     */
    @NotNull
    @ApiModelProperty(value = "Language", required = true)
    private Lang lang;

    /**
     * Text of translation
     */
    @NotNull
    @Size(max = 64)
    @ApiModelProperty(value = "Text of translation", required = true)
    private String text;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Lang getLang() {
        return lang;
    }

    public void setLang(Lang lang) {
        this.lang = lang;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof I18nDTO)) {
            return false;
        }

        I18nDTO i18nDTO = (I18nDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, i18nDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    public I18nDTO(@NotNull Lang lang, @NotNull @Size(max = 64) String text) {
        this.lang = lang;
        this.text = text;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "I18nDTO{" +
            "id=" + getId() +
            ", lang='" + getLang() + "'" +
            ", text='" + getText() + "'" +
            "}";
    }
}
