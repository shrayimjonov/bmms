package uz.paykap.service.dto.simple;

import uz.devops.bmcore.enumeration.Lang;

public interface SimpleI18nView {
    Lang getLang();

    String getText();
}
