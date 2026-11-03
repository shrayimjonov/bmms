package uz.paykap.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.*;
import uz.devops.bmcore.enumeration.Lang;

/**
 * A FieldValue.
 */
@Entity
@Table(name = "field_value")
public class FieldValue extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    /**
     * Param value to send to server
     */
    @NotNull
    @Size(max = 64)
    @Column(name = "value", length = 64, nullable = false)
    private String value;

    /**
     * Price of selected value
     */
    @Column(name = "amount")
    private Long amount;

    /**
     * Filter the values (field.type=select) by parent field
     */
    @Size(max = 16)
    @Column(name = "filter_by", length = 16)
    private String filterBy;

    /**
     * Display order
     */
    @Column(name = "ord")
    private Integer ord;

    @Column(name = "check_id")
    private String checkId;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "recipient", "titles", "descriptions", "values", "prefixes" }, allowSetters = true)
    private Field field;

    @ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
    @NotNull
    @JoinTable(
        name = "rel_field_value__title",
        joinColumns = @JoinColumn(name = "field_value_id"),
        inverseJoinColumns = @JoinColumn(name = "title_id")
    )
    private Set<I18n> titles = new HashSet<>();

    @ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
    @JoinTable(
        name = "rel_field_value__description",
        joinColumns = @JoinColumn(name = "field_value_id"),
        inverseJoinColumns = @JoinColumn(name = "description_id")
    )
    private Set<I18n> descriptions = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FieldValue id(Long id) {
        this.id = id;
        return this;
    }

    public String getValue() {
        return this.value;
    }

    public FieldValue value(String value) {
        this.value = value;
        return this;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getAmount() {
        return this.amount;
    }

    public FieldValue amount(Long amount) {
        this.amount = amount;
        return this;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getFilterBy() {
        return this.filterBy;
    }

    public FieldValue filterBy(String filterBy) {
        this.filterBy = filterBy;
        return this;
    }

    public void setFilterBy(String filterBy) {
        this.filterBy = filterBy;
    }

    public Integer getOrd() {
        return this.ord;
    }

    public FieldValue ord(Integer ord) {
        this.ord = ord;
        return this;
    }

    public void setOrd(Integer ord) {
        this.ord = ord;
    }

    public String getCheckId() {
        return checkId;
    }

    public FieldValue checkId(String checkId) {
        this.checkId = checkId;
        return this;
    }

    public void setCheckId(String checkId) {
        this.checkId = checkId;
    }

    public Field getField() {
        return this.field;
    }

    public FieldValue field(Field field) {
        this.setField(field);
        return this;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public Set<I18n> getTitles() {
        return this.titles;
    }

    public FieldValue titles(Set<I18n> i18ns) {
        this.setTitles(i18ns);
        return this;
    }

    public FieldValue addTitle(Lang lang, String label) {
        I18n e = new I18n();
        e.setLang(lang);
        e.setText(label);
        this.titles.add(e);
        return this;
    }

    public FieldValue addTitle(I18n i18n) {
        this.titles.add(i18n);
        return this;
    }

    public FieldValue removeTitle(I18n i18n) {
        this.titles.remove(i18n);
        return this;
    }

    public void setTitles(Set<I18n> i18ns) {
        this.titles = i18ns;
    }

    public Set<I18n> getDescriptions() {
        return this.descriptions;
    }

    public FieldValue descriptions(Set<I18n> i18ns) {
        this.setDescriptions(i18ns);
        return this;
    }

    public FieldValue addDescription(I18n i18n) {
        this.descriptions.add(i18n);
        return this;
    }

    public FieldValue removeDescription(I18n i18n) {
        this.descriptions.remove(i18n);
        return this;
    }

    public void setDescriptions(Set<I18n> i18ns) {
        this.descriptions = i18ns;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FieldValue)) {
            return false;
        }
        return id != null && id.equals(((FieldValue) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "FieldValue{" +
            "id=" + getId() +
            ", value='" + getValue() + "'" +
            ", amount=" + getAmount() +
            ", filterBy='" + getFilterBy() + "'" +
            ", ord=" + getOrd() +
            ", checkId='" + getCheckId() + "'" +
            "}";
    }
}
