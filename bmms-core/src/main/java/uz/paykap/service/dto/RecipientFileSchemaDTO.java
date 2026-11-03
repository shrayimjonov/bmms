package uz.paykap.service.dto;

import java.io.Serializable;
import java.util.Objects;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link uz.paykap.domain.RecipientFileSchema} entity.
 */
public class RecipientFileSchemaDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 32)
    private String name;

    @NotNull
    @Size(max = 2048)
    private String idList;

    @Size(max = 512)
    private String root;

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

    public String getIdList() {
        return idList;
    }

    public void setIdList(String idList) {
        this.idList = idList;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RecipientFileSchemaDTO)) {
            return false;
        }

        RecipientFileSchemaDTO recipientFileSchemaDTO = (RecipientFileSchemaDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, recipientFileSchemaDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "RecipientFileSchemaDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", idList='" + getIdList() + "'" +
            ", root='" + getRoot() + "'" +
            "}";
    }
}
