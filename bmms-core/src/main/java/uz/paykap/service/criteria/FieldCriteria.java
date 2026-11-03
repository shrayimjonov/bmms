package uz.paykap.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.BooleanFilter;
import tech.jhipster.service.filter.Filter;
import tech.jhipster.service.filter.IntegerFilter;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;
import uz.devops.bmcore.enumeration.ControlType;
import uz.devops.bmcore.enumeration.FieldType;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.Visibility;

/**
 * Criteria class for the {@link uz.paykap.domain.Field} entity. This class is used
 * in {@link uz.paykap.web.rest.FieldResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /fields?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class FieldCriteria implements Serializable, Criteria {

    /**
     * Class for filtering FieldType
     */
    public static class FieldTypeFilter extends Filter<FieldType> {

        public FieldTypeFilter() {}

        public FieldTypeFilter(FieldTypeFilter filter) {
            super(filter);
        }

        @Override
        public FieldTypeFilter copy() {
            return new FieldTypeFilter(this);
        }
    }

    /**
     * Class for filtering ControlType
     */
    public static class ControlTypeFilter extends Filter<ControlType> {

        public ControlTypeFilter() {}

        public ControlTypeFilter(ControlTypeFilter filter) {
            super(filter);
        }

        @Override
        public ControlTypeFilter copy() {
            return new ControlTypeFilter(this);
        }
    }

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

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter name;

    private LongFilter extId;

    private FieldTypeFilter type;

    private ControlTypeFilter controlType;

    private StringFilter content;

    private StringFilter validation;

    private StringFilter placeholder;

    private StringFilter mask;

    private StringFilter maskSymbol;

    private IntegerFilter lengthMin;

    private IntegerFilter lengthMax;

    private StringFilter requireField;

    private StringFilter filterBy;

    private BooleanFilter optional;

    private BooleanFilter output;

    private VisibilityFilter visibility;

    private IntegerFilter ord;

    private RequestTypeFilter requestType;

    private LongFilter recipientId;

    private LongFilter titleId;

    private LongFilter descriptionId;

    private LongFilter valueId;

    private LongFilter prefixId;

    public FieldCriteria() {}

    public FieldCriteria(FieldCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.name = other.name == null ? null : other.name.copy();
        this.extId = other.extId == null ? null : other.extId.copy();
        this.type = other.type == null ? null : other.type.copy();
        this.controlType = other.controlType == null ? null : other.controlType.copy();
        this.content = other.content == null ? null : other.content.copy();
        this.validation = other.validation == null ? null : other.validation.copy();
        this.placeholder = other.placeholder == null ? null : other.placeholder.copy();
        this.mask = other.mask == null ? null : other.mask.copy();
        this.maskSymbol = other.maskSymbol == null ? null : other.maskSymbol.copy();
        this.lengthMin = other.lengthMin == null ? null : other.lengthMin.copy();
        this.lengthMax = other.lengthMax == null ? null : other.lengthMax.copy();
        this.requireField = other.requireField == null ? null : other.requireField.copy();
        this.filterBy = other.filterBy == null ? null : other.filterBy.copy();
        this.optional = other.optional == null ? null : other.optional.copy();
        this.output = other.output == null ? null : other.output.copy();
        this.visibility = other.visibility == null ? null : other.visibility.copy();
        this.ord = other.ord == null ? null : other.ord.copy();
        this.requestType = other.requestType == null ? null : other.requestType.copy();
        this.recipientId = other.recipientId == null ? null : other.recipientId.copy();
        this.titleId = other.titleId == null ? null : other.titleId.copy();
        this.descriptionId = other.descriptionId == null ? null : other.descriptionId.copy();
        this.valueId = other.valueId == null ? null : other.valueId.copy();
        this.prefixId = other.prefixId == null ? null : other.prefixId.copy();
    }

    @Override
    public FieldCriteria copy() {
        return new FieldCriteria(this);
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

    public LongFilter getExtId() {
        return extId;
    }

    public LongFilter extId() {
        if (extId == null) {
            extId = new LongFilter();
        }
        return extId;
    }

    public void setExtId(LongFilter extId) {
        this.extId = extId;
    }

    public FieldTypeFilter getType() {
        return type;
    }

    public FieldTypeFilter type() {
        if (type == null) {
            type = new FieldTypeFilter();
        }
        return type;
    }

    public void setType(FieldTypeFilter type) {
        this.type = type;
    }

    public ControlTypeFilter getControlType() {
        return controlType;
    }

    public ControlTypeFilter controlType() {
        if (controlType == null) {
            controlType = new ControlTypeFilter();
        }
        return controlType;
    }

    public void setControlType(ControlTypeFilter controlType) {
        this.controlType = controlType;
    }

    public StringFilter getContent() {
        return content;
    }

    public StringFilter content() {
        if (content == null) {
            content = new StringFilter();
        }
        return content;
    }

    public void setContent(StringFilter content) {
        this.content = content;
    }

    public StringFilter getValidation() {
        return validation;
    }

    public StringFilter validation() {
        if (validation == null) {
            validation = new StringFilter();
        }
        return validation;
    }

    public void setValidation(StringFilter validation) {
        this.validation = validation;
    }

    public StringFilter getPlaceholder() {
        return placeholder;
    }

    public StringFilter placeholder() {
        if (placeholder == null) {
            placeholder = new StringFilter();
        }
        return placeholder;
    }

    public void setPlaceholder(StringFilter placeholder) {
        this.placeholder = placeholder;
    }

    public StringFilter getMask() {
        return mask;
    }

    public StringFilter mask() {
        if (mask == null) {
            mask = new StringFilter();
        }
        return mask;
    }

    public void setMask(StringFilter mask) {
        this.mask = mask;
    }

    public StringFilter getMaskSymbol() {
        return maskSymbol;
    }

    public StringFilter maskSymbol() {
        if (maskSymbol == null) {
            maskSymbol = new StringFilter();
        }
        return maskSymbol;
    }

    public void setMaskSymbol(StringFilter maskSymbol) {
        this.maskSymbol = maskSymbol;
    }

    public IntegerFilter getLengthMin() {
        return lengthMin;
    }

    public IntegerFilter lengthMin() {
        if (lengthMin == null) {
            lengthMin = new IntegerFilter();
        }
        return lengthMin;
    }

    public void setLengthMin(IntegerFilter lengthMin) {
        this.lengthMin = lengthMin;
    }

    public IntegerFilter getLengthMax() {
        return lengthMax;
    }

    public IntegerFilter lengthMax() {
        if (lengthMax == null) {
            lengthMax = new IntegerFilter();
        }
        return lengthMax;
    }

    public void setLengthMax(IntegerFilter lengthMax) {
        this.lengthMax = lengthMax;
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

    public BooleanFilter getOptional() {
        return optional;
    }

    public BooleanFilter optional() {
        if (optional == null) {
            optional = new BooleanFilter();
        }
        return optional;
    }

    public void setOptional(BooleanFilter optional) {
        this.optional = optional;
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

    public LongFilter getValueId() {
        return valueId;
    }

    public LongFilter valueId() {
        if (valueId == null) {
            valueId = new LongFilter();
        }
        return valueId;
    }

    public void setValueId(LongFilter valueId) {
        this.valueId = valueId;
    }

    public LongFilter getPrefixId() {
        return prefixId;
    }

    public LongFilter prefixId() {
        if (prefixId == null) {
            prefixId = new LongFilter();
        }
        return prefixId;
    }

    public void setPrefixId(LongFilter prefixId) {
        this.prefixId = prefixId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FieldCriteria that = (FieldCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(name, that.name) &&
            Objects.equals(extId, that.extId) &&
            Objects.equals(type, that.type) &&
            Objects.equals(controlType, that.controlType) &&
            Objects.equals(content, that.content) &&
            Objects.equals(validation, that.validation) &&
            Objects.equals(placeholder, that.placeholder) &&
            Objects.equals(mask, that.mask) &&
            Objects.equals(maskSymbol, that.maskSymbol) &&
            Objects.equals(lengthMin, that.lengthMin) &&
            Objects.equals(lengthMax, that.lengthMax) &&
            Objects.equals(requireField, that.requireField) &&
            Objects.equals(filterBy, that.filterBy) &&
            Objects.equals(optional, that.optional) &&
            Objects.equals(output, that.output) &&
            Objects.equals(visibility, that.visibility) &&
            Objects.equals(ord, that.ord) &&
            Objects.equals(requestType, that.requestType) &&
            Objects.equals(recipientId, that.recipientId) &&
            Objects.equals(titleId, that.titleId) &&
            Objects.equals(descriptionId, that.descriptionId) &&
            Objects.equals(valueId, that.valueId) &&
            Objects.equals(prefixId, that.prefixId)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            name,
            extId,
            type,
            controlType,
            content,
            validation,
            placeholder,
            mask,
            maskSymbol,
            lengthMin,
            lengthMax,
            requireField,
            filterBy,
            optional,
            output,
            visibility,
            ord,
            requestType,
            recipientId,
            titleId,
            descriptionId,
            valueId,
            prefixId
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "FieldCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (name != null ? "name=" + name + ", " : "") +
            (extId != null ? "extId=" + extId + ", " : "") +
            (type != null ? "type=" + type + ", " : "") +
            (controlType != null ? "controlType=" + controlType + ", " : "") +
            (content != null ? "content=" + content + ", " : "") +
            (validation != null ? "validation=" + validation + ", " : "") +
            (placeholder != null ? "placeholder=" + placeholder + ", " : "") +
            (mask != null ? "mask=" + mask + ", " : "") +
            (maskSymbol != null ? "maskSymbol=" + maskSymbol + ", " : "") +
            (lengthMin != null ? "lengthMin=" + lengthMin + ", " : "") +
            (lengthMax != null ? "lengthMax=" + lengthMax + ", " : "") +
            (requireField != null ? "requireField=" + requireField + ", " : "") +
            (filterBy != null ? "filterBy=" + filterBy + ", " : "") +
            (optional != null ? "optional=" + optional + ", " : "") +
            (output != null ? "output=" + output + ", " : "") +
            (visibility != null ? "visibility=" + visibility + ", " : "") +
            (ord != null ? "ord=" + ord + ", " : "") +
            (requestType != null ? "requestType=" + requestType + ", " : "") +
            (recipientId != null ? "recipientId=" + recipientId + ", " : "") +
            (titleId != null ? "titleId=" + titleId + ", " : "") +
            (descriptionId != null ? "descriptionId=" + descriptionId + ", " : "") +
            (valueId != null ? "valueId=" + valueId + ", " : "") +
            (prefixId != null ? "prefixId=" + prefixId + ", " : "") +
            "}";
    }
}
