package uz.paykap.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.Filter;
import tech.jhipster.service.filter.IntegerFilter;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.Visibility;
import uz.paykap.domain.enumeration.ResponseFieldType;

/**
 * Criteria class for the {@link uz.paykap.domain.ResponseField} entity. This class is used
 * in {@link uz.paykap.web.rest.ResponseFieldResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /response-fields?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class ResponseFieldCriteria implements Serializable, Criteria {

    /**
     * Class for filtering Visibility
     */
    public static class VisibilityFilter extends Filter<Visibility> {

        public VisibilityFilter() {}

        public VisibilityFilter(VisibilityFilter filter) {
            super(filter);
        }

        @Override
        public VisibilityFilter copy() {
            return new VisibilityFilter(this);
        }
    }

    /**
     * Class for filtering RequestType
     */
    public static class RequestTypeFilter extends Filter<RequestType> {

        public RequestTypeFilter() {}

        public RequestTypeFilter(RequestTypeFilter filter) {
            super(filter);
        }

        @Override
        public RequestTypeFilter copy() {
            return new RequestTypeFilter(this);
        }
    }

    /**
     * Class for filtering ResponseFieldType
     */
    public static class ResponseFieldTypeFilter extends Filter<ResponseFieldType> {

        public ResponseFieldTypeFilter() {}

        public ResponseFieldTypeFilter(ResponseFieldTypeFilter filter) {
            super(filter);
        }

        @Override
        public ResponseFieldTypeFilter copy() {
            return new ResponseFieldTypeFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter name;

    private VisibilityFilter visibility;

    private IntegerFilter ord;

    private RequestTypeFilter requestType;

    private ResponseFieldTypeFilter type;

    private IntegerFilter amountRatio;

    private LongFilter recipientId;

    private LongFilter labelId;

    public ResponseFieldCriteria() {}

    public ResponseFieldCriteria(ResponseFieldCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.name = other.name == null ? null : other.name.copy();
        this.visibility = other.visibility == null ? null : other.visibility.copy();
        this.ord = other.ord == null ? null : other.ord.copy();
        this.requestType = other.requestType == null ? null : other.requestType.copy();
        this.type = other.type == null ? null : other.type.copy();
        this.amountRatio = other.amountRatio == null ? null : other.amountRatio.copy();
        this.recipientId = other.recipientId == null ? null : other.recipientId.copy();
        this.labelId = other.labelId == null ? null : other.labelId.copy();
    }

    @Override
    public ResponseFieldCriteria copy() {
        return new ResponseFieldCriteria(this);
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

    public StringFilter getName() {
        return name;
    }

    public StringFilter name() {
        if (name == null) {
            name = new StringFilter();
        }
        return name;
    }

    public void setName(StringFilter name) {
        this.name = name;
    }

    public VisibilityFilter getVisibility() {
        return visibility;
    }

    public VisibilityFilter visibility() {
        if (visibility == null) {
            visibility = new VisibilityFilter();
        }
        return visibility;
    }

    public void setVisibility(VisibilityFilter visibility) {
        this.visibility = visibility;
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

    public RequestTypeFilter getRequestType() {
        return requestType;
    }

    public RequestTypeFilter requestType() {
        if (requestType == null) {
            requestType = new RequestTypeFilter();
        }
        return requestType;
    }

    public void setRequestType(RequestTypeFilter requestType) {
        this.requestType = requestType;
    }

    public ResponseFieldTypeFilter getType() {
        return type;
    }

    public ResponseFieldTypeFilter type() {
        if (type == null) {
            type = new ResponseFieldTypeFilter();
        }
        return type;
    }

    public void setType(ResponseFieldTypeFilter type) {
        this.type = type;
    }

    public IntegerFilter getAmountRatio() {
        return amountRatio;
    }

    public IntegerFilter amountRatio() {
        if (amountRatio == null) {
            amountRatio = new IntegerFilter();
        }
        return amountRatio;
    }

    public void setAmountRatio(IntegerFilter amountRatio) {
        this.amountRatio = amountRatio;
    }

    public LongFilter getRecipientId() {
        return recipientId;
    }

    public LongFilter recipientId() {
        if (recipientId == null) {
            recipientId = new LongFilter();
        }
        return recipientId;
    }

    public void setRecipientId(LongFilter recipientId) {
        this.recipientId = recipientId;
    }

    public LongFilter getLabelId() {
        return labelId;
    }

    public LongFilter labelId() {
        if (labelId == null) {
            labelId = new LongFilter();
        }
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
            Objects.equals(type, that.type) &&
            Objects.equals(amountRatio, that.amountRatio) &&
            Objects.equals(recipientId, that.recipientId) &&
            Objects.equals(labelId, that.labelId)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, visibility, ord, requestType, type, amountRatio, recipientId, labelId);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ResponseFieldCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (name != null ? "name=" + name + ", " : "") +
            (visibility != null ? "visibility=" + visibility + ", " : "") +
            (ord != null ? "ord=" + ord + ", " : "") +
            (requestType != null ? "requestType=" + requestType + ", " : "") +
            (type != null ? "type=" + type + ", " : "") +
            (amountRatio != null ? "amountRatio=" + amountRatio + ", " : "") +
            (recipientId != null ? "recipientId=" + recipientId + ", " : "") +
            (labelId != null ? "labelId=" + labelId + ", " : "") +
            "}";
    }
}
