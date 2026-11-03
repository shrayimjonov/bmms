package uz.paykap.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.javers.core.metamodel.annotation.DiffIgnore;
import uz.devops.bmcore.enumeration.*;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * A Field.
 */
@Entity
@Table(name = "field")
public class Field extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    /**
     * Field (merchant param) name; account_id, phone, login, ...
     */
    @NotNull
    @Size(max = 64)
    @Column(name = "name", length = 64, nullable = false)
    private String name;

    /**
     * External id, optional
     */
    @NotNull
    @Column(name = "ext_id", nullable = false)
    private Long extId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private FieldType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "control_type", nullable = false)
    private ControlType controlType;

    /**
     * Type of content for frontend usage; phone, email, text
     */
    @Column(name = "content")
    private String content;

    /**
     * Java regexp pattern to validate before sending to server
     */
    @Size(max = 128)
    @Column(name = "validation", length = 128)
    private String validation;

    /**
     * Field placeholder for UI input box
     */
    @Size(max = 128)
    @Column(name = "placeholder", length = 128)
    private String placeholder;

    /**
     * UI input mask
     */
    @Size(max = 256)
    @Column(name = "mask", length = 256)
    private String mask;

    /**
     * UI input mask symbol
     */
    @Size(max = 2)
    @Column(name = "mask_symbol", length = 2)
    private String maskSymbol;

    /**
     * Min length for text fields
     */
    @Min(value = 0)
    @Max(value = 256)
    @Column(name = "length_min")
    private Integer lengthMin;

    /**
     * Max length for text fields
     */
    @Min(value = 0)
    @Max(value = 256)
    @Column(name = "length_max")
    private Integer lengthMax;

    /**
     * Require parent field to draw this one
     */
    @Size(max = 64)
    @Column(name = "require_field", length = 64)
    private String requireField;

    /**
     * Filter showing by previous selected field
     */
    @Size(max = 16)
    @Column(name = "filter_by", length = 16)
    private String filterBy;

    /**
     * Is field optional to fill
     */
    @Column(name = "optional")
    private Boolean optional;

    /**
     * Need to send the value to server
     */
    @Column(name = "output")
    private Boolean output;

    /**
     * Visible in UI, can be used for hidden static fields
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "visibility")
    private Visibility visibility;

    /**
     * Display order
     */
    @Column(name = "ord")
    private Integer ord;

    /**
     * Define to which of CHECK,PAY,STATUS,CANCEL,DEPOSIT belongs that field
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "request_type")
    private RequestType requestType;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "gateway", "category", "titles", "fullNames" }, allowSetters = true)
    private Recipient recipient;

    @DiffIgnore
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @NotNull
    @JoinTable(name = "rel_field__title", joinColumns = @JoinColumn(name = "field_id"), inverseJoinColumns = @JoinColumn(name = "title_id"))
    private Set<I18n> titles = new HashSet<>();

    @DiffIgnore
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
        name = "rel_field__description",
        joinColumns = @JoinColumn(name = "field_id"),
        inverseJoinColumns = @JoinColumn(name = "description_id")
    )
    private Set<I18n> descriptions = new HashSet<>();

    @DiffIgnore
    @OneToMany(mappedBy = "field", fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JsonIgnoreProperties(value = { "field", "titles", "descriptions" }, allowSetters = true)
    private Set<FieldValue> values = new HashSet<>();

    @DiffIgnore
    @OneToMany(mappedBy = "field", fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JsonIgnoreProperties(value = { "field" }, allowSetters = true)
    private Set<Prefix> prefixes = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Field id(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public Field name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getExtId() {
        return this.extId;
    }

    public Field extId(Long extId) {
        this.extId = extId;
        return this;
    }

    public void setExtId(Long extId) {
        this.extId = extId;
    }

    public FieldType getType() {
        return this.type;
    }

    public Field type(FieldType type) {
        this.type = type;
        return this;
    }

    public void setType(FieldType type) {
        this.type = type;
    }

    public ControlType getControlType() {
        return this.controlType;
    }

    public Field controlType(ControlType controlType) {
        this.controlType = controlType;
        return this;
    }

    public void setControlType(ControlType controlType) {
        this.controlType = controlType;
    }

    public String getContent() {
        return this.content;
    }

    public Field content(String content) {
        this.content = content;
        return this;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getValidation() {
        return this.validation;
    }

    public Field validation(String validation) {
        this.validation = validation;
        return this;
    }

    public void setValidation(String validation) {
        this.validation = validation;
    }

    public String getPlaceholder() {
        return this.placeholder;
    }

    public Field placeholder(String placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getMask() {
        return this.mask;
    }

    public Field mask(String mask) {
        this.mask = mask;
        return this;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    public String getMaskSymbol() {
        return this.maskSymbol;
    }

    public Field maskSymbol(String maskSymbol) {
        this.maskSymbol = maskSymbol;
        return this;
    }

    public void setMaskSymbol(String maskSymbol) {
        this.maskSymbol = maskSymbol;
    }

    public Integer getLengthMin() {
        return this.lengthMin;
    }

    public Field lengthMin(Integer lengthMin) {
        this.lengthMin = lengthMin;
        return this;
    }

    public void setLengthMin(Integer lengthMin) {
        this.lengthMin = lengthMin;
    }

    public Integer getLengthMax() {
        return this.lengthMax;
    }

    public Field lengthMax(Integer lengthMax) {
        this.lengthMax = lengthMax;
        return this;
    }

    public void setLengthMax(Integer lengthMax) {
        this.lengthMax = lengthMax;
    }

    public String getRequireField() {
        return this.requireField;
    }

    public Field requireField(String requireField) {
        this.requireField = requireField;
        return this;
    }

    public void setRequireField(String requireField) {
        this.requireField = requireField;
    }

    public String getFilterBy() {
        return this.filterBy;
    }

    public Field filterBy(String filterBy) {
        this.filterBy = filterBy;
        return this;
    }

    public void setFilterBy(String filterBy) {
        this.filterBy = filterBy;
    }

    public Boolean getOptional() {
        return this.optional;
    }

    public Boolean isOptional() {
        return this.optional;
    }

    public Field optional(Boolean optional) {
        this.optional = optional;
        return this;
    }

    public void setOptional(Boolean optional) {
        this.optional = optional;
    }

    public Boolean getOutput() {
        return this.output;
    }

    public Boolean isOutput() {
        return this.output;
    }

    public Field output(Boolean output) {
        this.output = output;
        return this;
    }

    public void setOutput(Boolean output) {
        this.output = output;
    }

    public Visibility getVisibility() {
        return this.visibility;
    }

    public Field visibility(Visibility visibility) {
        this.visibility = visibility;
        return this;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public Integer getOrd() {
        return this.ord;
    }

    public Field ord(Integer ord) {
        this.ord = ord;
        return this;
    }

    public void setOrd(Integer ord) {
        this.ord = ord;
    }

    public RequestType getRequestType() {
        return this.requestType;
    }

    public Field requestType(RequestType requestType) {
        this.requestType = requestType;
        return this;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public Recipient getRecipient() {
        return this.recipient;
    }

    public Field recipient(Recipient recipient) {
        this.setRecipient(recipient);
        return this;
    }

    public void setRecipient(Recipient recipient) {
        this.recipient = recipient;
    }

    public Set<I18n> getTitles() {
        return this.titles;
    }

    public Field titles(Set<I18n> i18ns) {
        this.setTitles(i18ns);
        return this;
    }

    public Field addTitle(I18n i18n) {
        this.titles.add(i18n);
        return this;
    }

    public Field removeTitle(I18n i18n) {
        this.titles.remove(i18n);
        return this;
    }

    public void setTitles(Set<I18n> i18ns) {
        this.titles = i18ns;
    }

    public Set<I18n> getDescriptions() {
        return this.descriptions;
    }

    public Field descriptions(Set<I18n> i18ns) {
        this.setDescriptions(i18ns);
        return this;
    }

    public Field addDescription(Lang lang, String label) {
        I18n e = new I18n();
        e.setLang(lang);
        e.setText(label);
        this.descriptions.add(e);
        return this;
    }

    public Field addDescription(I18n i18n) {
        this.descriptions.add(i18n);
        return this;
    }

    public Field removeDescription(I18n i18n) {
        this.descriptions.remove(i18n);
        return this;
    }

    public void setDescriptions(Set<I18n> i18ns) {
        this.descriptions = i18ns;
    }

    public Set<FieldValue> getValues() {
        return this.values;
    }

    public Field values(Set<FieldValue> fieldValues) {
        this.setValues(fieldValues);
        return this;
    }

    public Field addValue(FieldValue fieldValue) {
        this.values.add(fieldValue);
        fieldValue.setField(this);
        return this;
    }

    public Field removeValue(FieldValue fieldValue) {
        this.values.remove(fieldValue);
        fieldValue.setField(null);
        return this;
    }

    public void setValues(Set<FieldValue> fieldValues) {
        if (this.values != null) {
            this.values.forEach(i -> i.setField(null));
        }
        if (fieldValues != null) {
            fieldValues.forEach(i -> i.setField(this));
        }
        this.values = fieldValues;
    }

    public Set<Prefix> getPrefixes() {
        return this.prefixes;
    }

    public Field prefixes(Set<Prefix> prefixes) {
        this.setPrefixes(prefixes);
        return this;
    }

    public Field addPrefix(Prefix prefix) {
        this.prefixes.add(prefix);
        prefix.setField(this);
        return this;
    }

    public Field removePrefix(Prefix prefix) {
        this.prefixes.remove(prefix);
        prefix.setField(null);
        return this;
    }

    public void setPrefixes(Set<Prefix> prefixes) {
        if (this.prefixes != null) {
            this.prefixes.forEach(i -> i.setField(null));
        }
        if (prefixes != null) {
            prefixes.forEach(i -> i.setField(this));
        }
        this.prefixes = prefixes;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Field)) {
            return false;
        }
        return id != null && id.equals(((Field) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Field{" +
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
            "}";
    }
}
