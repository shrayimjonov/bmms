package uz.paykap.service.dto;

import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.IntegerFilter;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;

/**
 * Criteria class for the FieldValue entity. This class is used in FieldValueResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /field-values?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@Getter
@Setter
public class FieldValueCriteria implements Serializable {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter value;

    private LongFilter amount;

    private StringFilter filterBy;

    private IntegerFilter ord;

    private StringFilter checkId;

    private LongFilter fieldId;

    private LongFilter titleId;

    private LongFilter descriptionId;

    public FieldValueCriteria() {}

    @Override
    public String toString() {
        return (
            "FieldValueCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (value != null ? "value=" + value + ", " : "") +
            (amount != null ? "amount=" + amount + ", " : "") +
            (filterBy != null ? "filterBy=" + filterBy + ", " : "") +
            (ord != null ? "ord=" + ord + ", " : "") +
            (checkId != null ? "checkId=" + checkId + ", " : "") +
            (fieldId != null ? "fieldId=" + fieldId + ", " : "") +
            (titleId != null ? "titleId=" + titleId + ", " : "") +
            (descriptionId != null ? "descriptionId=" + descriptionId + ", " : "") +
            "}"
        );
    }
}
