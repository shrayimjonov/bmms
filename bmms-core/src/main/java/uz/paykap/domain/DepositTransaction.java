package uz.paykap.domain;

import java.io.Serializable;
import java.time.Instant;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * Deposit History
 */
@Entity
@Table(name = "deposit_transaction")
public class DepositTransaction extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    /**
     * Tax number (INN, VAT) of payer. see : https://en.wikipedia.org/wiki/VAT_identification_number
     */
    @Size(max = 32)
    @Column(name = "payer_taxnr", length = 32)
    private String payerTaxnr;

    /**
     * Tax number (INN, VAT) of payer. see : https://en.wikipedia.org/wiki/VAT_identification_number
     */
    @Size(max = 32)
    @Column(name = "recipient_taxnr", length = 32)
    private String recipientTaxnr;

    @NotNull
    @Column(name = "amount", nullable = false)
    private Long amount;

    @NotNull
    @Column(name = "update_date", nullable = false)
    private Instant updateDate;

    @Column(name = "comment")
    private String comment;

    @Size(max = 64)
    @Column(name = "doc", length = 64)
    private String doc;

    @Column(name = "agent_login")
    private String agentLogin;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DepositTransaction id(Long id) {
        this.id = id;
        return this;
    }

    public String getPayerTaxnr() {
        return this.payerTaxnr;
    }

    public DepositTransaction payerTaxnr(String payerTaxnr) {
        this.payerTaxnr = payerTaxnr;
        return this;
    }

    public void setPayerTaxnr(String payerTaxnr) {
        this.payerTaxnr = payerTaxnr;
    }

    public String getRecipientTaxnr() {
        return this.recipientTaxnr;
    }

    public DepositTransaction recipientTaxnr(String recipientTaxnr) {
        this.recipientTaxnr = recipientTaxnr;
        return this;
    }

    public void setRecipientTaxnr(String recipientTaxnr) {
        this.recipientTaxnr = recipientTaxnr;
    }

    public Long getAmount() {
        return this.amount;
    }

    public DepositTransaction amount(Long amount) {
        this.amount = amount;
        return this;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Instant getUpdateDate() {
        return this.updateDate;
    }

    public DepositTransaction updateDate(Instant updateDate) {
        this.updateDate = updateDate;
        return this;
    }

    public void setUpdateDate(Instant updateDate) {
        this.updateDate = updateDate;
    }

    public String getComment() {
        return this.comment;
    }

    public DepositTransaction comment(String comment) {
        this.comment = comment;
        return this;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDoc() {
        return this.doc;
    }

    public DepositTransaction doc(String doc) {
        this.doc = doc;
        return this;
    }

    public void setDoc(String doc) {
        this.doc = doc;
    }

    public String getAgentLogin() {
        return agentLogin;
    }

    public DepositTransaction agentLogin(String agentLogin) {
        this.agentLogin = agentLogin;
        return this;
    }

    public void setAgentLogin(String agentLogin) {
        this.agentLogin = agentLogin;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DepositTransaction)) {
            return false;
        }
        return id != null && id.equals(((DepositTransaction) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DepositTransaction{" +
            "id=" + getId() +
            ", payerTaxnr='" + getPayerTaxnr() + "'" +
            ", recipientTaxnr='" + getRecipientTaxnr() + "'" +
            ", amount=" + getAmount() +
            ", updateDate='" + getUpdateDate() + "'" +
            ", comment='" + getComment() + "'" +
            ", doc='" + getDoc() + "'" +
                ", agentLogin='" + getAgentLogin() + "'" +
            "}";
    }
}
