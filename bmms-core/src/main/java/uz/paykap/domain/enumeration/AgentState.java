package uz.paykap.domain.enumeration;

import lombok.Getter;
import uz.devops.bmcore.enumeration.Lang;
//import uz.devops.libs.entity.state.ui.reflection.TranslateEnum;
import uz.paykap.service.dto.I18nDTO;

/**
 * The AgentState enumeration.
 */
public enum AgentState /*implements TranslateEnum<I18nDTO>*/ {
    NEW(new I18nDTO(Lang.EN, "New"), new I18nDTO(Lang.RU, "Новый"), new I18nDTO(Lang.UZ, "Yangi")),
    ACTIVE(new I18nDTO(Lang.EN, "Active"), new I18nDTO(Lang.RU, "Актив"), new I18nDTO(Lang.UZ, "Faol")),
    DELETED(new I18nDTO(Lang.EN, "Deleted"), new I18nDTO(Lang.RU, "Закритый"), new I18nDTO(Lang.UZ, "O'chirilgan"));

    @Getter
    private final I18nDTO[] i18n;

    AgentState(I18nDTO... i18n) {
        this.i18n = i18n;
    }
}
