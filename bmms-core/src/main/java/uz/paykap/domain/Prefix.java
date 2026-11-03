package uz.paykap.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.*;
import uz.paykap.domain.enumeration.PrefixType;

/**
 * A Prefix.
 */
@Entity
@Table(name = "prefix")
public class Prefix extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private PrefixType type;

    @Size(max = 16)
    @Column(name = "value", length = 16)
    private String value;

    /**
     * Need to prepend to field value
     */
    @Column(name = "output")
    private Boolean output;

    /**
     * Filter prefix value by selected parent field
     */
    @Size(max = 64)
    @Column(name = "require_field", length = 64)
    private String requireField;

    @ManyToOne
    @JsonIgnoreProperties(value = { "recipient", "titles", "descriptions", "values", "prefixes" }, allowSetters = true)
    private Field field;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Prefix id(Long id) {
        this.id = id;
        return this;
    }

    public PrefixType getType() {
        return this.type;
    }

    public Prefix type(PrefixType type) {
        this.type = type;
        return this;
    }

    public void setType(PrefixType type) {
        this.type = type;
    }

    public String getValue() {
        return this.value;
    }

    public Prefix value(String value) {
        this.value = value;
        return this;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Boolean getOutput() {
        return this.output;
    }

    public Boolean isOutput() {
        return output;
    }

    public Prefix output(Boolean output) {
        this.output = output;
        return this;
    }

    public void setOutput(Boolean output) {
        this.output = output;
    }

    public String getRequireField() {
        return this.requireField;
    }

    public Prefix requireField(String requireField) {
        this.requireField = requireField;
        return this;
    }

    public void setRequireField(String requireField) {
        this.requireField = requireField;
    }

    public Field getField() {
        return this.field;
    }

    public Prefix field(Field field) {
        this.setField(field);
        return this;
    }

    public void setField(Field field) {
        this.field = field;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Prefix)) {
            return false;
        }
        return id != null && id.equals(((Prefix) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Prefix{" +
            "id=" + getId() +
            ", type='" + getType() + "'" +
            ", value='" + getValue() + "'" +
            ", output='" + getOutput() + "'" +
            ", requireField='" + getRequireField() + "'" +
            "}";
    }
}
