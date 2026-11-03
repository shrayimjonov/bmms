package uz.paykap.service.dto;

import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * A DTO for the {@link uz.paykap.domain.Gateway} entity.
 */
public class GatewayDTO extends AbstractAuditingDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 64)
    private String name;

    @NotNull
    @Size(max = 32)
    private String org;

    @NotNull
    private Boolean active;

    /**
     * Bank MFO (BIN)
     */
    @NotNull
    @Size(max = 8)
    @ApiModelProperty(value = "Bank MFO (BIN)", required = true)
    private String mfo;

    /**
     * Tax number (INN)
     */
    @ApiModelProperty(value = "Tax number (INN)")
    private Long taxnr;

    @NotNull
    @Size(max = 32)
    private String ban;

    @NotNull
    @Size(max = 64)
    private String contract;

    @Size(max = 128)
    private String springBeanName;

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

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
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

    public Long getTaxnr() {
        return taxnr;
    }

    public void setTaxnr(Long taxnr) {
        this.taxnr = taxnr;
    }

    public String getBan() {
        return ban;
    }

    public void setBan(String ban) {
        this.ban = ban;
    }

    public String getContract() {
        return contract;
    }

    public void setContract(String contract) {
        this.contract = contract;
    }

    public String getSpringBeanName() {
        return springBeanName;
    }

    public void setSpringBeanName(String springBeanName) {
        this.springBeanName = springBeanName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GatewayDTO)) {
            return false;
        }

        GatewayDTO gatewayDTO = (GatewayDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, gatewayDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "GatewayDTO{" +
                "id=" + getId() +
                ", name='" + getName() + "'" +
                ", org='" + getOrg() + "'" +
                ", active='" + getActive() + "'" +
                ", mfo='" + getMfo() + "'" +
                ", taxnr=" + getTaxnr() +
                ", ban='" + getBan() + "'" +
                ", contract='" + getContract() + "'" +
                ", springBeanName='" + getSpringBeanName() + "'" +
                "}";
    }
}
