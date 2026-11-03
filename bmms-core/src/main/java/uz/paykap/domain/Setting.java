package uz.paykap.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import uz.paykap.domain.enumeration.Prop;

/**
 * Настройки для проведения трансакций
 */
@ApiModel(description = "Настройки для проведения трансакций")
@Entity
@Table(name = "setting")
public class Setting extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    /**
     * Short name to show in UI
     */
    @NotNull
    @Size(max = 64)
    @ApiModelProperty(value = "Short name to show in UI", required = true)
    @Column(name = "short_name", length = 64, nullable = false)
    private String shortName;

    /**
     * Порядковый номер
     */
    @ApiModelProperty(value = "Порядковый номер")
    @Column(name = "ord")
    private Integer ord;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active;

    /* See detailed info about mapping into Map :
     * https://thoughts-on-java.org/map-association-java-util-map/
     * */
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @NotNull
    @JoinTable(
        name = "setting_prop",
        joinColumns = @JoinColumn(name = "settings_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "props_id", referencedColumnName = "id")
    )
    @MapKey(name = "key")
    @MapKeyEnumerated(value = EnumType.STRING)
    private Map<Prop, SettingProperty> props = new HashMap<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Setting id(Long id) {
        this.id = id;
        return this;
    }

    public String getShortName() {
        return shortName;
    }

    public Setting shortName(String shortName) {
        this.shortName = shortName;
        return this;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Integer getOrd() {
        return ord;
    }

    public Setting ord(Integer ord) {
        this.ord = ord;
        return this;
    }

    public void setOrd(Integer ord) {
        this.ord = ord;
    }

    public Boolean getActive() {
        return active;
    }

    public Boolean isActive() {
        return active;
    }

    public Setting active(Boolean active) {
        this.active = active;
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Map<Prop, SettingProperty> getProps() {
        return props;
    }

    public Setting props(Map<Prop, SettingProperty> settingProperties) {
        this.props = settingProperties;
        return this;
    }

    public Setting addProp(SettingProperty settingProperty) {
        this.props.put(settingProperty.getKey(), settingProperty);
        return this;
    }

    public Setting removeProp(SettingProperty settingProperty) {
        this.props.remove(settingProperty);
        return this;
    }

    public void setProps(Map<Prop, SettingProperty> settingProperties) {
        this.props = settingProperties;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Setting setting = (Setting) o;
        if (setting.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), setting.getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Setting{" +
            "id=" + getId() +
            ", shortName='" + getShortName() + "'" +
            ", ord=" + getOrd() +
            ", active='" + getActive() + "'" +
            "}";
    }
}
