package uz.paykap.service.dto;

import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.validation.constraints.*;
import uz.paykap.domain.enumeration.VirtualAccountType;

/**
 * A DTO for the {@link uz.paykap.domain.VirtualAccountDetails} entity.
 */
public class VirtualAccountDetailsDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 512)
    private String params;

    @NotNull
    private String account;

    @NotNull
    private Boolean active;

    /**
     * Organisation mfo
     */
    @NotNull
    @ApiModelProperty(value = "Organisation mfo", required = true)
    private String mfo;

    private VirtualAccountType type;

    private VirtualAccountDTO virtualAccount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getMfo() {
        return mfo;
    }

    public void setMfo(String mfo) {
        this.mfo = mfo;
    }

    public VirtualAccountType getType() {
        return type;
    }

    public void setType(VirtualAccountType type) {
        this.type = type;
    }

    public VirtualAccountDTO getVirtualAccount() {
        return virtualAccount;
    }

    public void setVirtualAccount(VirtualAccountDTO virtualAccount) {
        this.virtualAccount = virtualAccount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VirtualAccountDetailsDTO)) {
            return false;
        }

        VirtualAccountDetailsDTO virtualAccountDetailsDTO = (VirtualAccountDetailsDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, virtualAccountDetailsDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "VirtualAccountDetailsDTO{" +
            "id=" + getId() +
            ", params='" + getParams() + "'" +
            ", account='" + getAccount() + "'" +
            ", active='" + getActive() + "'" +
            ", mfo='" + getMfo() + "'" +
            ", type='" + getType() + "'" +
            ", virtualAccount=" + getVirtualAccount() +
            "}";
    }
}
