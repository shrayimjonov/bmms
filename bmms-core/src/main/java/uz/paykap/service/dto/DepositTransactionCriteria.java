package uz.paykap.service.dto;

import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.InstantFilter;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;

/**
 * Criteria class for the DepositTransaction entity. This class is used in DepositTransactionResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /deposit-transactions?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@Getter
@Setter
public class DepositTransactionCriteria implements Serializable {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter payerTaxnr;

    private StringFilter recipientTaxnr;

    private LongFilter amount;

    private InstantFilter updateDate;

    private StringFilter comment;

    private StringFilter doc;

    private StringFilter agentLogin;

    public DepositTransactionCriteria() {}

    @Override
    public String toString() {
        return (
            "DepositTransactionCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (payerTaxnr != null ? "payerTaxnr=" + payerTaxnr + ", " : "") +
            (recipientTaxnr != null ? "recipientTaxnr=" + recipientTaxnr + ", " : "") +
            (amount != null ? "amount=" + amount + ", " : "") +
            (updateDate != null ? "updateDate=" + updateDate + ", " : "") +
            (comment != null ? "comment=" + comment + ", " : "") +
            (doc != null ? "doc=" + doc + ", " : "") +
            (agentLogin != null ? "agentLogin=" + agentLogin + ", " : "") +
            "}"
        );
    }
}
