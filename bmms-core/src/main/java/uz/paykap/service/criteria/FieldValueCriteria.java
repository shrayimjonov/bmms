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

/**
 * Criteria class for the {@link uz.paykap.domain.FieldValue} entity. This class is used
 * in {@link uz.paykap.web.rest.FieldValueResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /field-values?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class FieldValueCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter value;

    private LongFilter amount;

    private StringFilter filterBy;

    private StringFilter checkId;

    private IntegerFilter ord;

    private LongFilter fieldId;

    private LongFilter titleId;

    private LongFilter descriptionId;

    public FieldValueCriteria() {}

    public FieldValueCriteria(FieldValueCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.value = other.value == null ? null : other.value.copy();
        this.amount = other.amount == null ? null : other.amount.copy();
        this.filterBy = other.filterBy == null ? null : other.filterBy.copy();
        this.ord = other.ord == null ? null : other.ord.copy();
        this.fieldId = other.fieldId == null ? null : other.fieldId.copy();
        this.titleId = other.titleId == null ? null : other.titleId.copy();
        this.descriptionId = other.descriptionId == null ? null : other.descriptionId.copy();
    }

    @Override
    public FieldValueCriteria copy() {
        return new FieldValueCriteria(this);
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

    public LongFilter getAmount() {
        return amount;
    }

    public LongFilter amount() {
        if (amount == null) {
            amount = new LongFilter();
        }
        return amount;
    }

    public void setAmount(LongFilter amount) {
        this.amount = amount;
    }

    public StringFilter getFilterBy() {
        return filterBy;
    }

    public StringFilter filterBy() {
        if (filterBy == null) {
            filterBy = new StringFilter();
        }
        return filterBy;
    }

    public void setFilterBy(StringFilter filterBy) {
        this.filterBy = filterBy;
    }

    public IntegerFilter getOrd() {
        return ord;
    }

    public IntegerFilter ord() {
        if (ord == null) {
            ord = new IntegerFilter();
        }
        return ord;
    }

    public void setOrd(IntegerFilter ord) {
        this.ord = ord;
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

    public LongFilter getTitleId() {
        return titleId;
    }

    public LongFilter titleId() {
        if (titleId == null) {
            titleId = new LongFilter();
        }
        return titleId;
    }

    public void setTitleId(LongFilter titleId) {
        this.titleId = titleId;
    }

    public LongFilter getDescriptionId() {
        return descriptionId;
    }

    public LongFilter descriptionId() {
        if (descriptionId == null) {
            descriptionId = new LongFilter();
        }
        return descriptionId;
    }

    public void setDescriptionId(LongFilter descriptionId) {
        this.descriptionId = descriptionId;
    }

    public StringFilter getCheckId() {
        return checkId;
    }

    public void setCheckId(StringFilter checkId) {
        this.checkId = checkId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FieldValueCriteria that = (FieldValueCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(value, that.value) &&
            Objects.equals(amount, that.amount) &&
            Objects.equals(filterBy, that.filterBy) &&
            Objects.equals(ord, that.ord) &&
            Objects.equals(fieldId, that.fieldId) &&
            Objects.equals(titleId, that.titleId) &&
            Objects.equals(descriptionId, that.descriptionId)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value, amount, filterBy, ord, fieldId, titleId, descriptionId);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "FieldValueCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (value != null ? "value=" + value + ", " : "") +
            (amount != null ? "amount=" + amount + ", " : "") +
            (filterBy != null ? "filterBy=" + filterBy + ", " : "") +
            (ord != null ? "ord=" + ord + ", " : "") +
            (fieldId != null ? "fieldId=" + fieldId + ", " : "") +
            (titleId != null ? "titleId=" + titleId + ", " : "") +
            (descriptionId != null ? "descriptionId=" + descriptionId + ", " : "") +
            "}";
    }
}
