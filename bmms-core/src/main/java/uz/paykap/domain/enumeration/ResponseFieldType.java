package uz.paykap.domain.enumeration;

import lombok.Getter;
import uz.devops.bmcore.enumeration.Lang;
import uz.paykap.service.dto.I18nDTO;

/**
 * The ResponseFieldType enumeration.
 */
public enum ResponseFieldType {
    TEXT(new I18nDTO(Lang.EN, "Text"), new I18nDTO(Lang.RU, "Текст"), new I18nDTO(Lang.UZ, "Text")),
    AMOUNT(new I18nDTO(Lang.EN, "Amount"), new I18nDTO(Lang.RU, "Сумма"), new I18nDTO(Lang.UZ, "Summa")),
    DATE(new I18nDTO(Lang.EN, "Date"), new I18nDTO(Lang.RU, "Дата"), new I18nDTO(Lang.UZ, "Sana"));

    @Getter
    private final I18nDTO[] i18n;
    ResponseFieldType(uz.paykap.service.dto.I18nDTO... i18n) {
        this.i18n = i18n;
    }
}
