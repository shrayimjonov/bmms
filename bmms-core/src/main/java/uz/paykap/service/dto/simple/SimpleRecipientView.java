package uz.paykap.service.dto.simple;

import java.util.Set;

public interface SimpleRecipientView {

    Long getId();

    String getShortName();

    String getLogo();

    Integer getAmountRatio();

    Long getMinAmount();

    Long getMaxAmount();

    Integer getOrd();

    Long getCategoryId();

    Set<SimpleI18nView> getTitles();

    Set<SimpleI18nView> getFullNames();

    String getParams();
}
