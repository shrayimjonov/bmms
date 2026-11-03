package uz.devops.bm.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import uz.devops.bm.domain.enumeration.UserGroupType;

/**
 * Группа привилегий - Роль
 */
@Table("user_group")
public class UserGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    /**
     * Наименование
     */
    @NotNull(message = "must not be null")
    @Size(max = 64)
    @Column("name")
    private String name;

    /**
     * Активность
     */
    @NotNull(message = "must not be null")
    @Column("active")
    private Boolean active;

    /**
     * Описание действия
     */
    @Column("details")
    private String details;

    /**
     * Тип пользователя
     */
    @Column("type")
    private UserGroupType type;

    @JsonIgnore
    @Transient
    private Set<Authority> authorities = new HashSet<>();

    public Set<Authority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<Authority> authorities) {
        this.authorities = authorities;
    }

    public UserGroup authorities(Set<Authority> authorities) {
        this.authorities = authorities;
        return this;
    }

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserGroup id(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public UserGroup name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getActive() {
        return this.active;
    }

    public UserGroup active(Boolean active) {
        this.active = active;
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getDetails() {
        return this.details;
    }

    public UserGroup details(String details) {
        this.details = details;
        return this;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public UserGroupType getType() {
        return this.type;
    }

    public UserGroup type(UserGroupType type) {
        this.type = type;
        return this;
    }

    public void setType(UserGroupType type) {
        this.type = type;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserGroup)) {
            return false;
        }
        return id != null && id.equals(((UserGroup) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return (
            "UserGroup{" +
            "id=" +
            id +
            ", name='" +
            name +
            '\'' +
            ", active=" +
            active +
            ", details='" +
            details +
            '\'' +
            ", type=" +
            type +
            ", authorities=" +
            authorities +
            '}'
        );
    }
}
