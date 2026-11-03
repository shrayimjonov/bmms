package uz.paykap.service.dto;

import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.Filter;
import java.io.Serializable;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;
import uz.paykap.domain.enumeration.PrefixType;

/**
 * Criteria class for the Prefix entity. This class is used in PrefixResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /prefixes?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class PrefixCriteria implements Serializable {

    /**
     * Class for filtering PrefixType
     */
    public static class PrefixTypeFilter extends Filter<PrefixType> {}

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private PrefixTypeFilter type;

    private StringFilter value;

    private BooleanFilter output;

    private StringFilter requireField;

    private LongFilter fieldId;

    public PrefixCriteria() {}

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public PrefixTypeFilter getType() {
        return type;
    }

    public void setType(PrefixTypeFilter type) {
        this.type = type;
    }

    public StringFilter getValue() {
        return value;
    }

    public void setValue(StringFilter value) {
        this.value = value;
    }

    public BooleanFilter getOutput() {
        return output;
    }

    public void setOutput(BooleanFilter output) {
        this.output = output;
    }

    public StringFilter getRequireField() {
        return requireField;
    }

    public void setRequireField(StringFilter requireField) {
        this.requireField = requireField;
    }

    public LongFilter getFieldId() {
        return fieldId;
    }

    public void setFieldId(LongFilter fieldId) {
        this.fieldId = fieldId;
    }

    @Override
    public String toString() {
        return (
            "PrefixCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (type != null ? "type=" + type + ", " : "") +
            (value != null ? "value=" + value + ", " : "") +
            (output != null ? "output=" + output + ", " : "") +
            (requireField != null ? "requireField=" + requireField + ", " : "") +
            (fieldId != null ? "fieldId=" + fieldId + ", " : "") +
            "}"
        );
    }
}
