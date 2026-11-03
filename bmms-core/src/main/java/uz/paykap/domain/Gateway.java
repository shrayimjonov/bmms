package uz.paykap.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A Gateway.
 */
@Entity
@Table(name = "gateway")
public class Gateway extends AbstractAuditingEntity implements Serializable {

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
    @Column(name = "org", length = 32, nullable = false)
    private String org;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active;

    /**
     * Bank MFO (BIN)
     */
    @NotNull
    @Size(max = 8)
    @Column(name = "mfo", length = 8, nullable = false)
    private String mfo;

    /**
     * Tax number (INN)
     */
    @Column(name = "taxnr")
    private Long taxnr;

    @NotNull
    @Size(max = 32)
    @Column(name = "ban", length = 32, nullable = false)
    private String ban;

    @NotNull
    @Size(max = 64)
    @Column(name = "contract", length = 64, nullable = false)
    private String contract;

    @Size(max = 128)
    @Column(name = "spring_bean_name", length = 128)
    private String springBeanName;

    @JsonIgnoreProperties(value = { "gateway", "agent", "virtualAccount" }, allowSetters = true)
    @OneToOne(mappedBy = "gateway")
    private Deposit deposit;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Gateway id(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public Gateway name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrg() {
        return this.org;
    }

    public Gateway org(String org) {
        this.org = org;
        return this;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public Boolean getActive() {
        return this.active;
    }

    public Boolean isActive() {
        return active;
    }

    public Gateway active(Boolean active) {
        this.active = active;
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getMfo() {
        return this.mfo;
    }

    public Gateway mfo(String mfo) {
        this.mfo = mfo;
        return this;
    }

    public void setMfo(String mfo) {
        this.mfo = mfo;
    }

    public Long getTaxnr() {
        return this.taxnr;
    }

    public Gateway taxnr(Long taxnr) {
        this.taxnr = taxnr;
        return this;
    }

    public void setTaxnr(Long taxnr) {
        this.taxnr = taxnr;
    }

    public String getBan() {
        return this.ban;
    }

    public Gateway ban(String ban) {
        this.ban = ban;
        return this;
    }

    public void setBan(String ban) {
        this.ban = ban;
    }

    public String getContract() {
        return this.contract;
    }

    public Gateway contract(String contract) {
        this.contract = contract;
        return this;
    }

    public void setContract(String contract) {
        this.contract = contract;
    }

    public String getSpringBeanName() {
        return this.springBeanName;
    }

    public Gateway springBeanName(String springBeanName) {
        this.springBeanName = springBeanName;
        return this;
    }

    public void setSpringBeanName(String springBeanName) {
        this.springBeanName = springBeanName;
    }

    public Deposit getDeposit() {
        return this.deposit;
    }

    public Gateway deposit(Deposit deposit) {
        this.setDeposit(deposit);
        return this;
    }

    public void setDeposit(Deposit deposit) {
        if (this.deposit != null) {
            this.deposit.setGateway(null);
        }
        if (deposit != null) {
            deposit.setGateway(this);
        }
        this.deposit = deposit;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Gateway)) {
            return false;
        }
        return id != null && id.equals(((Gateway) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Gateway{" +
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
