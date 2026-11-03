package uz.devops.bm.service.dto;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Lob;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import uz.devops.bm.domain.enumeration.JsonFormType;

/**
 * A DTO for the {@link uz.devops.bm.domain.JsonForm} entity.
 */
public class JsonFormDTO implements Serializable {

    private Long id;

    @NotNull(message = "must not be null")
    @Size(max = 128)
    private String name;

    @NotNull(message = "must not be null")
    private JsonFormType type;

    @Lob
    private String data;

    private String username;

    private Boolean active;

    @Size(max = 256)
    private String description;

    private Set<UserDTO> users = new HashSet<>();

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

    public JsonFormType getType() {
        return type;
    }

    public void setType(JsonFormType type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<UserDTO> getUsers() {
        return users;
    }

    public void setUsers(Set<UserDTO> users) {
        this.users = users;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JsonFormDTO)) {
            return false;
        }

        JsonFormDTO jsonFormDTO = (JsonFormDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, jsonFormDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "JsonFormDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", type='" + getType() + "'" +
            ", data='" + getData() + "'" +
            ", username='" + getUsername() + "'" +
            ", active='" + getActive() + "'" +
            ", description='" + getDescription() + "'" +
            ", users=" + getUsers() +
            "}";
    }
}
