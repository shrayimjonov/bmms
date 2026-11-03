package uz.paykap.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.*;
import uz.paykap.domain.enumeration.VirtualAccountType;

/**
 * A VirtualAccountDetails.
 */
@Entity
@Table(name = "virtual_account_details")
public class VirtualAccountDetails extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @NotNull
    @Size(max = 512)
    @Column(name = "params", length = 512, nullable = false)
    private String params;

    @NotNull
    @Column(name = "account", nullable = false)
    private String account;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active;

    /**
     * Organisation mfo
     */
    @NotNull
    @Column(name = "mfo", nullable = false)
    private String mfo;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private VirtualAccountType type;

    @ManyToOne
    @JsonIgnoreProperties(value = { "deposit" }, allowSetters = true)
    private VirtualAccount virtualAccount;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public VirtualAccountDetails id(Long id) {
        this.id = id;
        return this;
    }

    public String getParams() {
        return this.params;
    }

    public VirtualAccountDetails params(String params) {
        this.params = params;
        return this;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getAccount() {
        return this.account;
    }

    public VirtualAccountDetails account(String account) {
        this.account = account;
        return this;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Boolean isActive() {
        return this.active;
    }

    public Boolean getActive() {
        return this.active;
    }

    public VirtualAccountDetails active(Boolean active) {
        this.active = active;
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getMfo() {
        return this.mfo;
    }

    public VirtualAccountDetails mfo(String mfo) {
        this.mfo = mfo;
        return this;
    }

    public void setMfo(String mfo) {
        this.mfo = mfo;
    }

    public VirtualAccountType getType() {
        return this.type;
    }

    public VirtualAccountDetails type(VirtualAccountType type) {
        this.type = type;
        return this;
    }

    public void setType(VirtualAccountType type) {
        this.type = type;
    }

    public VirtualAccount getVirtualAccount() {
        return this.virtualAccount;
    }

    public VirtualAccountDetails virtualAccount(VirtualAccount virtualAccount) {
        this.setVirtualAccount(virtualAccount);
        return this;
    }

    public void setVirtualAccount(VirtualAccount virtualAccount) {
        this.virtualAccount = virtualAccount;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VirtualAccountDetails)) {
            return false;
        }
        return id != null && id.equals(((VirtualAccountDetails) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "VirtualAccountDetails{" +
            "id=" + getId() +
            ", params='" + getParams() + "'" +
            ", account='" + getAccount() + "'" +
            ", active='" + getActive() + "'" +
            ", mfo='" + getMfo() + "'" +
            ", type='" + getType() + "'" +
            "}";
    }
}
