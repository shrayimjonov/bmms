package uz.paykap.service.dto;

import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.validation.constraints.*;
import uz.paykap.domain.enumeration.ModeType;

/**
 * A DTO for the {@link uz.paykap.domain.VirtualAccount} entity.
 */
public class VirtualAccountDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 64)
    private String name;

    @NotNull
    @Size(max = 32)
    private String orgName;

    @NotNull
    private Boolean active;

    /**
     * Organisation inn
     */
    @NotNull
    @ApiModelProperty(value = "Organisation inn", required = true)
    private String inn;

    /**
     * Unique id
     */
    @ApiModelProperty(value = "Unique id")
    private Long uid;

    private ModeType mode;

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

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getInn() {
        return inn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public ModeType getMode() {
        return mode;
    }

    public void setMode(ModeType mode) {
        this.mode = mode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VirtualAccountDTO)) {
            return false;
        }

        VirtualAccountDTO virtualAccountDTO = (VirtualAccountDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, virtualAccountDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "VirtualAccountDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", orgName='" + getOrgName() + "'" +
            ", active='" + getActive() + "'" +
            ", inn='" + getInn() + "'" +
            ", uid=" + getUid() +
            ", mode='" + getMode() + "'" +
            "}";
    }
}
