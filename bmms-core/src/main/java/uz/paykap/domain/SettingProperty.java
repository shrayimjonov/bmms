package uz.paykap.domain;

import io.swagger.annotations.ApiModel;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.*;
import javax.validation.constraints.*;
import uz.paykap.domain.enumeration.Prop;

/**
 * Значения настроек оплат
 */
@ApiModel(description = "Значения настроек оплат")
@Entity
@Table(name = "setting_property")
public class SettingProperty extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "key", nullable = false)
    private Prop key;

    @NotNull
    @Size(max = 512)
    @Column(name = "value", length = 512, nullable = false)
    private String value;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SettingProperty id(Long id) {
        this.id = id;
        return this;
    }

    public Prop getKey() {
        return this.key;
    }

    public SettingProperty key(Prop key) {
        this.key = key;
        return this;
    }

    public void setKey(Prop key) {
        this.key = key;
    }

    public String getValue() {
        return this.value;
    }

    public SettingProperty value(String value) {
        this.value = value;
        return this;
    }

    public void setValue(String value) {
        this.value = value;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SettingProperty)) {
            return false;
        }
        SettingProperty settingProperty = (SettingProperty) o;
        if (settingProperty.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), settingProperty.getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SettingProperty{" +
            "id=" + getId() +
            ", key='" + getKey() + "'" +
            ", value='" + getValue() + "'" +
            "}";
    }
}
