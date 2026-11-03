package uz.paykap.service.action;

import org.springframework.http.HttpMethod;
import uz.devops.bmcore.enumeration.Lang;
import uz.paykap.service.dto.I18nDTO;

import java.util.Collection;
import java.util.Map;

public interface IAction {

    Collection<I18nDTO> getTitle();

    String getName();

    Collection<I18nDTO> getDescription();

    Boolean isActive();

    Boolean isConfirmable();

    Collection<I18nDTO> getConfirmationText();

    /**
     *
     * @return uri
     */
    String getUri();

    Map<String, Object> getUriParams();

    HttpMethod getMethod();

    /**
     * color, hover text, hover able and etc.
     *
     * @return btn styles and parameters
     */
    Map<String, ?> getBtnParams();

    default I18nDTO i18n(Lang lang, String s) {
        return new I18nDTO(lang, s);
    }
}
