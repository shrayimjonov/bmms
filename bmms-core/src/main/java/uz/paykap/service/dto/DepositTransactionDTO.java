package uz.paykap.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link uz.paykap.domain.DepositTransaction} entity.
 */
@ApiModel(description = "Deposit History")
public class DepositTransactionDTO implements Serializable {

    private Long id;

    /**
     * Tax number (INN, VAT) of payer. see : https://en.wikipedia.org/wiki/VAT_identification_number
     */
    @Size(max = 32)
    @ApiModelProperty(value = "Tax number (INN, VAT) of payer. see : https://en.wikipedia.org/wiki/VAT_identification_number")
    private String payerTaxnr;

    /**
     * Tax number (INN, VAT) of payer. see : https://en.wikipedia.org/wiki/VAT_identification_number
     */
    @Size(max = 32)
    @ApiModelProperty(value = "Tax number (INN, VAT) of payer. see : https://en.wikipedia.org/wiki/VAT_identification_number")
    private String recipientTaxnr;

    @NotNull
    private Long amount;

    @NotNull
    private Instant updateDate;

    private String comment;

    @Size(max = 64)
    private String doc;

    private String agentLogin;

    public String getAgentLogin() {
        return agentLogin;
    }

    public void setAgentLogin(String agentLogin) {
        this.agentLogin = agentLogin;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPayerTaxnr() {
        return payerTaxnr;
    }

    public void setPayerTaxnr(String payerTaxnr) {
        this.payerTaxnr = payerTaxnr;
    }

    public String getRecipientTaxnr() {
        return recipientTaxnr;
    }

    public void setRecipientTaxnr(String recipientTaxnr) {
        this.recipientTaxnr = recipientTaxnr;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Instant getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Instant updateDate) {
        this.updateDate = updateDate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDoc() {
        return doc;
    }

    public void setDoc(String doc) {
        this.doc = doc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DepositTransactionDTO)) {
            return false;
        }

        DepositTransactionDTO depositTransactionDTO = (DepositTransactionDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, depositTransactionDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DepositTransactionDTO{" +
            "id=" + getId() +
            ", payerTaxnr='" + getPayerTaxnr() + "'" +
            ", recipientTaxnr='" + getRecipientTaxnr() + "'" +
            ", amount=" + getAmount() +
            ", updateDate='" + getUpdateDate() + "'" +
            ", comment='" + getComment() + "'" +
            ", doc='" + getDoc() + "'" +
            "}";
    }
}
