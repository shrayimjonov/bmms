package uz.paykap.service.dto;

import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.DoubleFilter;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.FloatFilter;
import io.github.jhipster.service.filter.IntegerFilter;
import io.github.jhipster.service.filter.StringFilter;
import java.io.Serializable;
import java.util.Objects;
import tech.jhipster.service.filter.LongFilter;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.Visibility;

/**
 * Criteria class for the ResponseField entity. This class is used in ResponseFieldResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /response-fields?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class ResponseFieldCriteria implements Serializable {

    /**
     * Class for filtering Visibility
     */
    public static class VisibilityFilter extends Filter<Visibility> {}

    /**
     * Class for filtering RequestType
     */
    public static class RequestTypeFilter extends Filter<RequestType> {}

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter name;

    private VisibilityFilter visibility;

    private IntegerFilter ord;

    private RequestTypeFilter requestType;

    private LongFilter recipientId;

    private LongFilter labelId;

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getName() {
        return name;
    }

    public void setName(StringFilter name) {
        this.name = name;
    }

    public VisibilityFilter getVisibility() {
        return visibility;
    }

    public void setVisibility(VisibilityFilter visibility) {
        this.visibility = visibility;
    }

    public IntegerFilter getOrd() {
        return ord;
    }

    public void setOrd(IntegerFilter ord) {
        this.ord = ord;
    }

    public RequestTypeFilter getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestTypeFilter requestType) {
        this.requestType = requestType;
    }

    public LongFilter getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(LongFilter recipientId) {
        this.recipientId = recipientId;
    }

    public LongFilter getLabelId() {
        return labelId;
    }

    public void setLabelId(LongFilter labelId) {
        this.labelId = labelId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ResponseFieldCriteria that = (ResponseFieldCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(name, that.name) &&
            Objects.equals(visibility, that.visibility) &&
            Objects.equals(ord, that.ord) &&
            Objects.equals(requestType, that.requestType) &&
            Objects.equals(recipientId, that.recipientId) &&
            Objects.equals(labelId, that.labelId)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, visibility, ord, requestType, recipientId, labelId);
    }

    @Override
    public String toString() {
        return (
            "ResponseFieldCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (name != null ? "name=" + name + ", " : "") +
            (visibility != null ? "visibility=" + visibility + ", " : "") +
            (ord != null ? "ord=" + ord + ", " : "") +
            (requestType != null ? "requestType=" + requestType + ", " : "") +
            (recipientId != null ? "recipientId=" + recipientId + ", " : "") +
            (labelId != null ? "labelId=" + labelId + ", " : "") +
            "}"
        );
    }
}
