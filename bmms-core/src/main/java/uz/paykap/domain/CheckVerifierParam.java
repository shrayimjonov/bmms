package uz.paykap.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A CheckVerifierParam.
 */
@Entity
@Table(name = "check_verifier_param")
public class CheckVerifierParam extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @Column(name = "account")
    private String account;

    @Column(name = "amount_in_tiyin")
    private Long amountInTiyin;

    @JsonIgnoreProperties(value = { "gateway", "category", "titles", "fullNames" }, allowSetters = true)
    @OneToOne(optional = false)
    @NotNull
    @JoinColumn(unique = true)
    private Recipient recipient;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CheckVerifierParam id(Long id) {
        this.id = id;
        return this;
    }

    public String getAccount() {
        return this.account;
    }

    public CheckVerifierParam account(String account) {
        this.account = account;
        return this;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Long getAmountInTiyin() {
        return this.amountInTiyin;
    }

    public CheckVerifierParam amountInTiyin(Long amountInTiyin) {
        this.amountInTiyin = amountInTiyin;
        return this;
    }

    public void setAmountInTiyin(Long amountInTiyin) {
        this.amountInTiyin = amountInTiyin;
    }

    public Recipient getRecipient() {
        return this.recipient;
    }

    public CheckVerifierParam recipient(Recipient recipient) {
        this.setRecipient(recipient);
        return this;
    }

    public void setRecipient(Recipient recipient) {
        this.recipient = recipient;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CheckVerifierParam)) {
            return false;
        }
        return id != null && id.equals(((CheckVerifierParam) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CheckVerifierParam{" +
            "id=" + getId() +
            ", account='" + getAccount() + "'" +
            ", amountInTiyin=" + getAmountInTiyin() +
            "}";
    }
}
