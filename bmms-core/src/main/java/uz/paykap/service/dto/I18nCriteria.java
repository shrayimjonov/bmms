package uz.paykap.service.dto;

import io.github.jhipster.service.filter.Filter;
import java.io.Serializable;
import java.util.Objects;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;
import uz.devops.bmcore.enumeration.Lang;

/**
 * Criteria class for the I18n entity. This class is used in I18nResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /i-18-ns?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class I18nCriteria implements Serializable {

    /**
     * Class for filtering Lang
     */
    public static class LangFilter extends Filter<Lang> {}

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private LangFilter lang;

    private StringFilter text;

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public LangFilter getLang() {
        return lang;
    }

    public void setLang(LangFilter lang) {
        this.lang = lang;
    }

    public StringFilter getText() {
        return text;
    }

    public void setText(StringFilter text) {
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final I18nCriteria that = (I18nCriteria) o;
        return Objects.equals(id, that.id) && Objects.equals(lang, that.lang) && Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, lang, text);
    }

    @Override
    public String toString() {
        return (
            "I18nCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (lang != null ? "lang=" + lang + ", " : "") +
            (text != null ? "text=" + text + ", " : "") +
            "}"
        );
    }
}
