package uz.devops.bm.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import uz.devops.bm.domain.enumeration.JsonFormType;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * A JsonForm.
 */
@Table("json_form")
public class JsonForm extends AbstractAuditingEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
    private Long id;

    @NotNull(message = "must not be null")
    @Size(max = 128)
    @Column("name")
    private String name;

    @NotNull(message = "must not be null")
    @Column("type")
    private JsonFormType type;

    @Column("data")
    private String data;

    @Column("username")
    private String username;

    @Column("active")
    private Boolean active;

    @Size(max = 256)
    @Column("description")
    private String description;

    @Transient
    private Set<User> users = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public JsonForm id(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public JsonForm name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JsonFormType getType() {
        return this.type;
    }

    public JsonForm type(JsonFormType type) {
        this.type = type;
        return this;
    }

    public void setType(JsonFormType type) {
        this.type = type;
    }

    public String getData() {
        return this.data;
    }

    public JsonForm data(String data) {
        this.data = data;
        return this;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getUsername() {
        return this.username;
    }

    public JsonForm username(String username) {
        this.username = username;
        return this;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getActive() {
        return this.active;
    }

    public JsonForm active(Boolean active) {
        this.active = active;
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getDescription() {
        return this.description;
    }

    public JsonForm description(String description) {
        this.description = description;
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<User> getUsers() {
        return this.users;
    }

    public JsonForm users(Set<User> users) {
        this.setUsers(users);
        return this;
    }

    public JsonForm addUser(User user) {
        this.users.add(user);
        return this;
    }

    public JsonForm removeUser(User user) {
        this.users.remove(user);
        return this;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JsonForm)) {
            return false;
        }
        return id != null && id.equals(((JsonForm) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "JsonForm{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", type='" + getType() + "'" +
            ", data='" + getData() + "'" +
            ", username='" + getUsername() + "'" +
            ", active='" + getActive() + "'" +
            ", description='" + getDescription() + "'" +
            "}";
    }
}
