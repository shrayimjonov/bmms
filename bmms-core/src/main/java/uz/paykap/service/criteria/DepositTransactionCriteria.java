package uz.paykap.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.BooleanFilter;
import tech.jhipster.service.filter.DoubleFilter;
import tech.jhipster.service.filter.Filter;
import tech.jhipster.service.filter.FloatFilter;
import tech.jhipster.service.filter.InstantFilter;
import tech.jhipster.service.filter.IntegerFilter;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;

/**
 * Criteria class for the {@link uz.paykap.domain.DepositTransaction} entity. This class is used
 * in {@link uz.paykap.web.rest.DepositTransactionResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /deposit-transactions?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class DepositTransactionCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter payerTaxnr;

    private StringFilter recipientTaxnr;

    private LongFilter amount;

    private InstantFilter updateDate;

    private StringFilter comment;

    private StringFilter doc;

    public DepositTransactionCriteria() {}

    public DepositTransactionCriteria(DepositTransactionCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.payerTaxnr = other.payerTaxnr == null ? null : other.payerTaxnr.copy();
        this.recipientTaxnr = other.recipientTaxnr == null ? null : other.recipientTaxnr.copy();
        this.amount = other.amount == null ? null : other.amount.copy();
        this.updateDate = other.updateDate == null ? null : other.updateDate.copy();
        this.comment = other.comment == null ? null : other.comment.copy();
        this.doc = other.doc == null ? null : other.doc.copy();
    }

    @Override
    public DepositTransactionCriteria copy() {
        return new DepositTransactionCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public LongFilter id() {
        if (id == null) {
            id = new LongFilter();
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getPayerTaxnr() {
        return payerTaxnr;
    }

    public StringFilter payerTaxnr() {
        if (payerTaxnr == null) {
            payerTaxnr = new StringFilter();
        }
        return payerTaxnr;
    }

    public void setPayerTaxnr(StringFilter payerTaxnr) {
        this.payerTaxnr = payerTaxnr;
    }

    public StringFilter getRecipientTaxnr() {
        return recipientTaxnr;
    }

    public StringFilter recipientTaxnr() {
        if (recipientTaxnr == null) {
            recipientTaxnr = new StringFilter();
        }
        return recipientTaxnr;
    }

    public void setRecipientTaxnr(StringFilter recipientTaxnr) {
        this.recipientTaxnr = recipientTaxnr;
    }

    public LongFilter getAmount() {
        return amount;
    }

    public LongFilter amount() {
        if (amount == null) {
            amount = new LongFilter();
        }
        return amount;
    }

    public void setAmount(LongFilter amount) {
        this.amount = amount;
    }

    public InstantFilter getUpdateDate() {
        return updateDate;
    }

    public InstantFilter updateDate() {
        if (updateDate == null) {
            updateDate = new InstantFilter();
        }
        return updateDate;
    }

    public void setUpdateDate(InstantFilter updateDate) {
        this.updateDate = updateDate;
    }

    public StringFilter getComment() {
        return comment;
    }

    public StringFilter comment() {
        if (comment == null) {
            comment = new StringFilter();
        }
        return comment;
    }

    public void setComment(StringFilter comment) {
        this.comment = comment;
    }

    public StringFilter getDoc() {
        return doc;
    }

    public StringFilter doc() {
        if (doc == null) {
            doc = new StringFilter();
        }
        return doc;
    }

    public void setDoc(StringFilter doc) {
        this.doc = doc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DepositTransactionCriteria that = (DepositTransactionCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(payerTaxnr, that.payerTaxnr) &&
            Objects.equals(recipientTaxnr, that.recipientTaxnr) &&
            Objects.equals(amount, that.amount) &&
            Objects.equals(updateDate, that.updateDate) &&
            Objects.equals(comment, that.comment) &&
            Objects.equals(doc, that.doc)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, payerTaxnr, recipientTaxnr, amount, updateDate, comment, doc);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DepositTransactionCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (payerTaxnr != null ? "payerTaxnr=" + payerTaxnr + ", " : "") +
            (recipientTaxnr != null ? "recipientTaxnr=" + recipientTaxnr + ", " : "") +
            (amount != null ? "amount=" + amount + ", " : "") +
            (updateDate != null ? "updateDate=" + updateDate + ", " : "") +
            (comment != null ? "comment=" + comment + ", " : "") +
            (doc != null ? "doc=" + doc + ", " : "") +
            "}";
    }
}
