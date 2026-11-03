package uz.paykap.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link uz.paykap.domain.RecipientGroup} entity.
 */
@ApiModel(description = "Группа поставщиков")
public class RecipientGroupDTO implements Serializable {

    private Long id;

    /**
     * Название
     */
    @NotNull
    @Size(max = 64)
    @ApiModelProperty(value = "Название", required = true)
    private String name;

    /**
     * Активность
     */
    @NotNull
    @ApiModelProperty(value = "Активность", required = true)
    private Boolean active;

    /**
     * Дополнительная информация
     */
    @ApiModelProperty(value = "Дополнительная информация")
    private String more;

    private Set<RecipientDTO> recipients = new HashSet<>();

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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getMore() {
        return more;
    }

    public void setMore(String more) {
        this.more = more;
    }

    public Set<RecipientDTO> getRecipients() {
        return recipients;
    }

    public void setRecipients(Set<RecipientDTO> recipients) {
        this.recipients = recipients;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RecipientGroupDTO)) {
            return false;
        }

        RecipientGroupDTO recipientGroupDTO = (RecipientGroupDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, recipientGroupDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "RecipientGroupDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", active='" + getActive() + "'" +
            ", more='" + getMore() + "'" +
            ", recipients=" + getRecipients() +
            "}";
    }
}
