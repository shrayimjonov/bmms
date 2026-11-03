package uz.paykap.service.dto.reconcile;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.Instant;
import java.util.Objects;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ComplexReconcileTransaction {
    /**
     * Transaction id in billing's system
     */
    @Pattern(regexp = "^\\d*")
    private String billTranId;
    /**
     * Transaction id in agent's system
     */
    @NotNull
    private String agentTranId;
    /**
     * Account
     */
    @NotNull
    private String account;
    /**
     * Amount of transaction
     */
    @NotNull
    private Long amount;
    /**
     * Currency code (e.g. 860)
     */
    @NotNull
    private Integer currency;
    /**
     * Date of transaction
     */
    private Instant date;
    /**
     * Indicates whether transaction belongs to agent's request (Not need in agent request, true by default)
     */
    private Boolean agentTransaction = true;

    public ComplexReconcileTransaction(Long billTranId, String agentTranId, String account, Long amount, Integer currency, Instant date, Boolean agentTransaction) {
        this.billTranId = String.valueOf(billTranId);
        this.agentTranId = agentTranId;
        this.account = account;
        this.amount = amount;
        this.currency = currency;
        this.date = date;
        this.agentTransaction = agentTransaction;
    }

    public ComplexReconcileTransaction(String billTranId, String agentTranId, String account, Long amount, Integer currency, Instant date) {
        this.billTranId = billTranId;
        this.agentTranId = agentTranId;
        this.account = account;
        this.amount = amount;
        this.currency = currency;
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComplexReconcileTransaction that = (ComplexReconcileTransaction) o;
        return Objects.equals(billTranId, that.billTranId) && Objects.equals(agentTranId, that.agentTranId) && Objects.equals(account, that.account) && Objects.equals(amount, that.amount) && Objects.equals(currency, that.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(billTranId, agentTranId, account, amount, currency);
    }
}
