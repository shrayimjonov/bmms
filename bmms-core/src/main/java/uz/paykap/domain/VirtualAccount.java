package uz.paykap.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.*;
import uz.paykap.domain.enumeration.ModeType;

/**
 * A VirtualAccount.
 */
@Entity
@Table(name = "virtual_account")
public class VirtualAccount extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @NotNull
    @Size(max = 64)
    @Column(name = "name", length = 64, nullable = false)
    private String name;

    @NotNull
    @Size(max = 32)
    @Column(name = "org_name", length = 32, nullable = false)
    private String orgName;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active;

    /**
     * Organisation inn
     */
    @NotNull
    @Column(name = "inn", nullable = false)
    private String inn;

    /**
     * Unique id
     */
    @Column(name = "uid")
    private Long uid;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode")
    private ModeType mode;

    @JsonIgnoreProperties(value = { "gateway", "agent", "virtualAccount" }, allowSetters = true)
    @OneToOne(mappedBy = "virtualAccount")
    private Deposit deposit;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public VirtualAccount id(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public VirtualAccount name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrgName() {
        return this.orgName;
    }

    public VirtualAccount orgName(String orgName) {
        this.orgName = orgName;
        return this;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public Boolean isActive() {
        return this.active;
    }

    public Boolean getActive() {
        return this.active;
    }

    public VirtualAccount active(Boolean active) {
        this.active = active;
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getInn() {
        return this.inn;
    }

    public VirtualAccount inn(String inn) {
        this.inn = inn;
        return this;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public Long getUid() {
        return this.uid;
    }

    public VirtualAccount uid(Long uid) {
        this.uid = uid;
        return this;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public ModeType getMode() {
        return this.mode;
    }

    public VirtualAccount mode(ModeType mode) {
        this.mode = mode;
        return this;
    }

    public void setMode(ModeType mode) {
        this.mode = mode;
    }

    public Deposit getDeposit() {
        return this.deposit;
    }

    public VirtualAccount deposit(Deposit deposit) {
        this.setDeposit(deposit);
        return this;
    }

    public void setDeposit(Deposit deposit) {
        if (this.deposit != null) {
            this.deposit.setVirtualAccount(null);
        }
        if (deposit != null) {
            deposit.setVirtualAccount(this);
        }
        this.deposit = deposit;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VirtualAccount)) {
            return false;
        }
        return id != null && id.equals(((VirtualAccount) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "VirtualAccount{" +
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
