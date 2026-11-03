package uz.paykap.service.dto;

import io.github.jhipster.service.filter.*;
import uz.devops.bmcore.enumeration.ControlType;
import uz.devops.bmcore.enumeration.FieldType;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.Visibility;

import java.io.Serializable;
import java.util.Objects;

/**
 * Criteria class for the Field entity. This class is used in FieldResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /fields?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class FieldCriteria implements Serializable {
    /**
     * Class for filtering FieldType
     */
    public static class FieldTypeFilter extends Filter<FieldType> {
    }
    /**
     * Class for filtering ControlType
     */
    public static class ControlTypeFilter extends Filter<ControlType> {
    }
    /**
     * Class for filtering Visibility
     */
    public static class VisibilityFilter extends Filter<Visibility> {
    }
    /**
     * Class for filtering RequestType
     */
    public static class RequestTypeFilter extends Filter<RequestType> {
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

    public LongFilter getExtId() {
        return extId;
    }

    public void setExtId(LongFilter extId) {
        this.extId = extId;
    }

    public FieldTypeFilter getType() {
        return type;
    }

    public void setType(FieldTypeFilter type) {
        this.type = type;
    }

    public ControlTypeFilter getControlType() {
        return controlType;
    }

    public void setControlType(ControlTypeFilter controlType) {
        this.controlType = controlType;
    }

    public StringFilter getContent() {
        return content;
    }

    public void setContent(StringFilter content) {
        this.content = content;
    }

    public StringFilter getValidation() {
        return validation;
    }

    public void setValidation(StringFilter validation) {
        this.validation = validation;
    }

    public StringFilter getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(StringFilter placeholder) {
        this.placeholder = placeholder;
    }

    public StringFilter getMask() {
        return mask;
    }

    public void setMask(StringFilter mask) {
        this.mask = mask;
    }

    public StringFilter getMaskSymbol() {
        return maskSymbol;
    }

    public void setMaskSymbol(StringFilter maskSymbol) {
        this.maskSymbol = maskSymbol;
    }

    public IntegerFilter getLengthMin() {
        return lengthMin;
    }

    public void setLengthMin(IntegerFilter lengthMin) {
        this.lengthMin = lengthMin;
    }

    public IntegerFilter getLengthMax() {
        return lengthMax;
    }

    public void setLengthMax(IntegerFilter lengthMax) {
        this.lengthMax = lengthMax;
    }

    public StringFilter getRequireField() {
        return requireField;
    }

    public void setRequireField(StringFilter requireField) {
        this.requireField = requireField;
    }

    public StringFilter getFilterBy() {
        return filterBy;
    }

    public void setFilterBy(StringFilter filterBy) {
        this.filterBy = filterBy;
    }

    public BooleanFilter getOptional() {
        return optional;
    }

    public void setOptional(BooleanFilter optional) {
        this.optional = optional;
    }

    public BooleanFilter getOutput() {
        return output;
    }

    public void setOutput(BooleanFilter output) {
        this.output = output;
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

    public LongFilter getTitleId() {
        return titleId;
    }

    public void setTitleId(LongFilter titleId) {
        this.titleId = titleId;
    }

    public LongFilter getDescriptionId() {
        return descriptionId;
    }

    public void setDescriptionId(LongFilter descriptionId) {
        this.descriptionId = descriptionId;
    }

    public LongFilter getValueId() {
        return valueId;
    }

    public void setValueId(LongFilter valueId) {
        this.valueId = valueId;
    }

    public LongFilter getPrefixId() {
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
        return
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
                Objects.equals(prefixId, that.prefixId);
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
