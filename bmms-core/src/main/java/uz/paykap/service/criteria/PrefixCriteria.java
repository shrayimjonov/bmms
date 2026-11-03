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
import uz.paykap.domain.enumeration.PrefixType;

/**
 * Criteria class for the {@link uz.paykap.domain.Prefix} entity. This class is used
 * in {@link uz.paykap.web.rest.PrefixResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /prefixes?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class PrefixCriteria implements Serializable, Criteria {

    /**
     * Class for filtering PrefixType
     */
    public static class PrefixTypeFilter extends Filter<PrefixType> {

        public PrefixTypeFilter() {}

        public PrefixTypeFilter(PrefixTypeFilter filter) {
            super(filter);
        }

        @Override
        public PrefixTypeFilter copy() {
            return new PrefixTypeFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private PrefixTypeFilter type;

    private StringFilter value;

    private BooleanFilter output;

    private StringFilter requireField;

    private LongFilter fieldId;

    public PrefixCriteria() {}

    public PrefixCriteria(PrefixCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.type = other.type == null ? null : other.type.copy();
        this.value = other.value == null ? null : other.value.copy();
        this.output = other.output == null ? null : other.output.copy();
        this.requireField = other.requireField == null ? null : other.requireField.copy();
        this.fieldId = other.fieldId == null ? null : other.fieldId.copy();
    }

    @Override
    public PrefixCriteria copy() {
        return new PrefixCriteria(this);
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

    public PrefixTypeFilter getType() {
        return type;
    }

    public PrefixTypeFilter type() {
        if (type == null) {
            type = new PrefixTypeFilter();
        }
        return type;
    }

    public void setType(PrefixTypeFilter type) {
        this.type = type;
    }

    public StringFilter getValue() {
        return value;
    }

    public StringFilter value() {
        if (value == null) {
            value = new StringFilter();
        }
        return value;
    }

    public void setValue(StringFilter value) {
        this.value = value;
    }

    public BooleanFilter getOutput() {
        return output;
    }

    public BooleanFilter output() {
        if (output == null) {
            output = new BooleanFilter();
        }
        return output;
    }

    public void setOutput(BooleanFilter output) {
        this.output = output;
    }

    public StringFilter getRequireField() {
        return requireField;
    }

    public StringFilter requireField() {
        if (requireField == null) {
            requireField = new StringFilter();
        }
        return requireField;
    }

    public void setRequireField(StringFilter requireField) {
        this.requireField = requireField;
    }

    public LongFilter getFieldId() {
        return fieldId;
    }

    public LongFilter fieldId() {
        if (fieldId == null) {
            fieldId = new LongFilter();
        }
        return fieldId;
    }

    public void setFieldId(LongFilter fieldId) {
        this.fieldId = fieldId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PrefixCriteria that = (PrefixCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(type, that.type) &&
            Objects.equals(value, that.value) &&
            Objects.equals(output, that.output) &&
            Objects.equals(requireField, that.requireField) &&
            Objects.equals(fieldId, that.fieldId)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, value, output, requireField, fieldId);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PrefixCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (type != null ? "type=" + type + ", " : "") +
            (value != null ? "value=" + value + ", " : "") +
            (output != null ? "output=" + output + ", " : "") +
            (requireField != null ? "requireField=" + requireField + ", " : "") +
            (fieldId != null ? "fieldId=" + fieldId + ", " : "") +
            "}";
    }
}
