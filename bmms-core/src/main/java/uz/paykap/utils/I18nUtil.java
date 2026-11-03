package uz.paykap.utils;

import lombok.experimental.UtilityClass;
import uz.devops.bmcore.enumeration.Lang;
import uz.paykap.domain.I18n;

import java.util.HashSet;
import java.util.Set;

@UtilityClass
public class I18nUtil {

    public static Set<I18n> getI18ns(String nameForAll) {
        return getI18ns(nameForAll, nameForAll, nameForAll);
    }

    public static Set<I18n> getI18ns(String en, String ru, String uz) {
        en = en.length() > 62 ? en.substring(0, 60) : en;
        ru = ru.length() > 62 ? ru.substring(0, 60) : ru;
        uz = uz.length() > 62 ? uz.substring(0, 60) : uz;
        HashSet<I18n> i18ns = new HashSet<>(3);
        i18ns.add(new I18n().lang(Lang.EN).text(en));
        i18ns.add(new I18n().lang(Lang.RU).text(ru));
        i18ns.add(new I18n().lang(Lang.UZ).text(uz));
        return i18ns;
    }

}
