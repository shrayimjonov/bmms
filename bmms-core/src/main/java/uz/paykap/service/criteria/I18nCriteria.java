package uz.paykap.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.BooleanFilter;
import tech.jhipster.service.filter.DoubleFilter;
import tech.jhipster.service.filter.Filter;
import tech.jhipster.service.filter.FloatFilter;
import tech.jhipster.service.filter.IntegerFilter;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;
import uz.devops.bmcore.enumeration.Lang;

/**
 * Criteria class for the {@link uz.paykap.domain.I18n} entity. This class is used
 * in {@link uz.paykap.web.rest.I18nResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /i-18-ns?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class I18nCriteria implements Serializable, Criteria {

    /**
     * Class for filtering Lang
     */
    public static class LangFilter extends Filter<Lang> {

        public LangFilter() {}

        public LangFilter(LangFilter filter) {
            super(filter);
        }

        @Override
        public LangFilter copy() {
            return new LangFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private LangFilter lang;

    private StringFilter text;

    public I18nCriteria() {}

    public I18nCriteria(I18nCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.lang = other.lang == null ? null : other.lang.copy();
        this.text = other.text == null ? null : other.text.copy();
    }

    @Override
    public I18nCriteria copy() {
        return new I18nCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public LongFilter id() {
        if (id == null) {
            id = new LongFilter();
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public LangFilter getLang() {
        return lang;
    }

    public LangFilter lang() {
        if (lang == null) {
            lang = new LangFilter();
        }
        return lang;
    }

    public void setLang(LangFilter lang) {
        this.lang = lang;
    }

    public StringFilter getText() {
        return text;
    }

    public StringFilter text() {
        if (text == null) {
            text = new StringFilter();
        }
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

    // prettier-ignore
    @Override
    public String toString() {
        return "I18nCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (lang != null ? "lang=" + lang + ", " : "") +
            (text != null ? "text=" + text + ", " : "") +
            "}";
    }
}
