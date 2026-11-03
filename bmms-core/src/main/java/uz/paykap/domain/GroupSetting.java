package uz.paykap.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import uz.paykap.domain.enumeration.Prop;

/**
 * Тип настройки
 */
@ApiModel(description = "Тип настройки")
@Entity
@Table(name = "group_setting")
public class GroupSetting extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    /**
     * Группа - clearingNumber of terminal
     */
    @NotNull
    @Size(max = 64)
    @ApiModelProperty(value = "Группа - clearingNumber of terminal", required = true)
    @Column(name = "request_group", length = 64, nullable = false)
    private String requestGroup;

//    @ManyToOne
//    @JsonIgnoreProperties("")
//    private SettingType settingType;

    /* See detailed info about mapping into Map :
     * https://thoughts-on-java.org/map-association-java-util-map/
     * */
    @ManyToMany(cascade = CascadeType.ALL)
    @NotNull
    @JoinTable(
        name = "rel_group_setting__prop",
        joinColumns = @JoinColumn(name = "group_setting_id"),
        inverseJoinColumns = @JoinColumn(name = "prop_id")
    )
    @MapKey(name = "key")
    @MapKeyEnumerated(value = EnumType.STRING)
    private Map<Prop, SettingProperty> props = new HashMap<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GroupSetting id(Long id) {
        this.id = id;
        return this;
    }

    public String getRequestGroup() {
        return this.requestGroup;
    }

    public GroupSetting requestGroup(String requestGroup) {
        this.requestGroup = requestGroup;
        return this;
    }

    public void setRequestGroup(String requestGroup) {
        this.requestGroup = requestGroup;
    }

    public Map<Prop, SettingProperty> getProps() {
        return props;
    }

    public GroupSetting props(Map<Prop, SettingProperty> settingProperties) {
        this.props = settingProperties;
        return this;
    }

    public GroupSetting addProp(SettingProperty settingProperty) {
        this.props.put(settingProperty.getKey(), settingProperty);
        return this;
    }

    public GroupSetting removeProp(SettingProperty settingProperty) {
        this.props.remove(settingProperty);
        return this;
    }

    public void setProps(Map<Prop, SettingProperty> settingProperties) {
        this.props = settingProperties;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GroupSetting)) {
            return false;
        }
        return id != null && id.equals(((GroupSetting) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "GroupSetting{" +
                "id=" + getId() +
                ", requestGroup='" + getRequestGroup() + "'" +
                "}";
    }
}
