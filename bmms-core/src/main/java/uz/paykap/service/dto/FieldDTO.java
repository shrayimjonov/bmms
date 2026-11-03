package uz.paykap.service.dto;

import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.validation.constraints.*;
import uz.devops.bmcore.enumeration.ControlType;
import uz.devops.bmcore.enumeration.FieldType;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.Visibility;
import uz.paykap.domain.FieldValue;
import uz.paykap.domain.Prefix;

/**
 * A DTO for the {@link uz.paykap.domain.Field} entity.
 */
public class FieldDTO implements Serializable {

    private Long id;

    /**
     * Field (merchant param) name; account_id, phone, login, ...
     */
    @NotNull
    @Size(max = 64)
    @ApiModelProperty(value = "Field (merchant param) name; account_id, phone, login, ...", required = true)
    private String name;

    /**
     * External id, optional
     */
    @NotNull
    @ApiModelProperty(value = "External id, optional", required = true)
    private Long extId;

    @NotNull
    private FieldType type;

    @NotNull
    private ControlType controlType;

    /**
     * Type of content for frontend usage; phone, email, text
     */
    @ApiModelProperty(value = "Type of content for frontend usage; phone, email, text")
    private String content;

    /**
     * Java regexp pattern to validate before sending to server
     */
    @Size(max = 128)
    @ApiModelProperty(value = "Java regexp pattern to validate before sending to server")
    private String validation;

    /**
     * Field placeholder for UI input box
     */
    @Size(max = 128)
    @ApiModelProperty(value = "Field placeholder for UI input box")
    private String placeholder;

    /**
     * UI input mask
     */
    @Size(max = 256)
    @ApiModelProperty(value = "UI input mask")
    private String mask;

    /**
     * UI input mask symbol
     */
    @Size(max = 2)
    @ApiModelProperty(value = "UI input mask symbol")
    private String maskSymbol;

    /**
     * Min length for text fields
     */
    @Min(value = 0)
    @Max(value = 256)
    @ApiModelProperty(value = "Min length for text fields")
    private Integer lengthMin;

    /**
     * Max length for text fields
     */
    @Min(value = 0)
    @Max(value = 256)
    @ApiModelProperty(value = "Max length for text fields")
    private Integer lengthMax;

    /**
     * Require parent field to draw this one
     */
    @Size(max = 64)
    @ApiModelProperty(value = "Require parent field to draw this one")
    private String requireField;

    /**
     * Filter showing by previous selected field
     */
    @Size(max = 16)
    @ApiModelProperty(value = "Filter showing by previous selected field")
    private String filterBy;

    /**
     * Is field optional to fill
     */
    @ApiModelProperty(value = "Is field optional to fill")
    private Boolean optional;

    /**
     * Need to send the value to server
     */
    @ApiModelProperty(value = "Need to send the value to server")
    private Boolean output;

    /**
     * Visible in UI, can be used for hidden static fields
     */
    @ApiModelProperty(value = "Visible in UI, can be used for hidden static fields")
    private Visibility visibility;

    /**
     * Display order
     */
    @ApiModelProperty(value = "Display order")
    private Integer ord;

    /**
     * Define to which of CHECK,PAY,STATUS,CANCEL,DEPOSIT belongs that field
     */
    @ApiModelProperty(value = "Define to which of CHECK,PAY,STATUS,CANCEL,DEPOSIT belongs that field")
    private RequestType requestType;

    private RecipientDTO recipient;

    private Set<I18nDTO> titles = new HashSet<>();

    private Set<I18nDTO> descriptions = new HashSet<>();

    private Set<FieldValue> values = new HashSet<>();

    private Set<Prefix> prefixes = new HashSet<>();

    public Set<FieldValue> getValues() {
        return values;
    }

    public void setValues(Set<FieldValue> values) {
        this.values = values;
    }

    public Set<Prefix> getPrefixes() {
        return prefixes;
    }

    public void setPrefixes(Set<Prefix> prefixes) {
        this.prefixes = prefixes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getExtId() {
        return extId;
    }

    public void setExtId(Long extId) {
        this.extId = extId;
    }

    public FieldType getType() {
        return type;
    }

    public void setType(FieldType type) {
        this.type = type;
    }

    public ControlType getControlType() {
        return controlType;
    }

    public void setControlType(ControlType controlType) {
        this.controlType = controlType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getValidation() {
        return validation;
    }

    public void setValidation(String validation) {
        this.validation = validation;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getMask() {
        return mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    public String getMaskSymbol() {
        return maskSymbol;
    }

    public void setMaskSymbol(String maskSymbol) {
        this.maskSymbol = maskSymbol;
    }

    public Integer getLengthMin() {
        return lengthMin;
    }

    public void setLengthMin(Integer lengthMin) {
        this.lengthMin = lengthMin;
    }

    public Integer getLengthMax() {
        return lengthMax;
    }

    public void setLengthMax(Integer lengthMax) {
        this.lengthMax = lengthMax;
    }

    public String getRequireField() {
        return requireField;
    }

    public void setRequireField(String requireField) {
        this.requireField = requireField;
    }

    public String getFilterBy() {
        return filterBy;
    }

    public void setFilterBy(String filterBy) {
        this.filterBy = filterBy;
    }

    public Boolean getOptional() {
        return optional;
    }

    public void setOptional(Boolean optional) {
        this.optional = optional;
    }

    public Boolean getOutput() {
        return output;
    }

    public void setOutput(Boolean output) {
        this.output = output;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public Integer getOrd() {
        return ord;
    }

    public void setOrd(Integer ord) {
        this.ord = ord;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public RecipientDTO getRecipient() {
        return recipient;
    }

    public void setRecipient(RecipientDTO recipient) {
        this.recipient = recipient;
    }

    public Set<I18nDTO> getTitles() {
        return titles;
    }

    public void setTitles(Set<I18nDTO> titles) {
        this.titles = titles;
    }

    public Set<I18nDTO> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(Set<I18nDTO> descriptions) {
        this.descriptions = descriptions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FieldDTO)) {
            return false;
        }

        FieldDTO fieldDTO = (FieldDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, fieldDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "FieldDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", extId=" + getExtId() +
            ", type='" + getType() + "'" +
            ", controlType='" + getControlType() + "'" +
            ", content='" + getContent() + "'" +
            ", validation='" + getValidation() + "'" +
            ", placeholder='" + getPlaceholder() + "'" +
            ", mask='" + getMask() + "'" +
            ", maskSymbol='" + getMaskSymbol() + "'" +
            ", lengthMin=" + getLengthMin() +
            ", lengthMax=" + getLengthMax() +
            ", requireField='" + getRequireField() + "'" +
            ", filterBy='" + getFilterBy() + "'" +
            ", optional='" + getOptional() + "'" +
            ", output='" + getOutput() + "'" +
            ", visibility='" + getVisibility() + "'" +
            ", ord=" + getOrd() +
            ", requestType='" + getRequestType() + "'" +
            ", recipient=" + getRecipient() +
            ", titles=" + getTitles() +
            ", descriptions=" + getDescriptions() +
            "}";
    }
}
