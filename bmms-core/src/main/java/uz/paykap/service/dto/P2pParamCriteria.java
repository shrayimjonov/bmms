package uz.paykap.service.dto;

import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.IntegerFilter;
import io.github.jhipster.service.filter.StringFilter;
import tech.jhipster.service.filter.LongFilter;
import uz.paykap.domain.enumeration.P2pProcessorType;

import java.io.Serializable;
import java.util.Objects;

/**
 * Criteria class for the P2pParam entity. This class is used in P2pParamResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /p-2-p-params?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class P2pParamCriteria implements Serializable {

    /**
     * Class for filtering P2pProcessorType
     */
    public static class P2pProcessorTypeFilter extends Filter<P2pProcessorType> {}

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private IntegerFilter priority;

    private BooleanFilter active;

    private P2pProcessorTypeFilter senderPc;

    private P2pProcessorTypeFilter recipientPc;

    private LongFilter senderConditionId;

    private LongFilter recipientConditionId;

    private LongFilter senderPercentId;

    private LongFilter recipientPercentId;

    private StringFilter promo;

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public IntegerFilter getPriority() {
        return priority;
    }

    public void setPriority(IntegerFilter priority) {
        this.priority = priority;
    }

    public BooleanFilter getActive() {
        return active;
    }

    public void setActive(BooleanFilter active) {
        this.active = active;
    }

    public P2pProcessorTypeFilter getSenderPc() {
        return senderPc;
    }

    public void setSenderPc(P2pProcessorTypeFilter senderPc) {
        this.senderPc = senderPc;
    }

    public P2pProcessorTypeFilter getRecipientPc() {
        return recipientPc;
    }

    public void setRecipientPc(P2pProcessorTypeFilter recipientPc) {
        this.recipientPc = recipientPc;
    }

    public LongFilter getSenderConditionId() {
        return senderConditionId;
    }

    public void setSenderConditionId(LongFilter senderConditionId) {
        this.senderConditionId = senderConditionId;
    }

    public LongFilter getRecipientConditionId() {
        return recipientConditionId;
    }

    public void setRecipientConditionId(LongFilter recipientConditionId) {
        this.recipientConditionId = recipientConditionId;
    }

    public LongFilter getSenderPercentId() {
        return senderPercentId;
    }

    public void setSenderPercentId(LongFilter senderPercentId) {
        this.senderPercentId = senderPercentId;
    }

    public LongFilter getRecipientPercentId() {
        return recipientPercentId;
    }

    public void setRecipientPercentId(LongFilter recipientPercentId) {
        this.recipientPercentId = recipientPercentId;
    }

    public StringFilter getPromo() {
        return promo;
    }

    public void setPromo(StringFilter promo) {
        this.promo = promo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final P2pParamCriteria that = (P2pParamCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(priority, that.priority) &&
            Objects.equals(active, that.active) &&
            Objects.equals(senderPc, that.senderPc) &&
            Objects.equals(recipientPc, that.recipientPc) &&
            Objects.equals(senderConditionId, that.senderConditionId) &&
            Objects.equals(recipientConditionId, that.recipientConditionId) &&
            Objects.equals(senderPercentId, that.senderPercentId) &&
            Objects.equals(recipientPercentId, that.recipientPercentId) &&
            Objects.equals(promo, that.promo)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            priority,
            active,
            senderPc,
            recipientPc,
            senderConditionId,
            recipientConditionId,
            senderPercentId,
            recipientPercentId,
            promo
        );
    }

    @Override
    public String toString() {
        return (
            "P2pParamCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (priority != null ? "priority=" + priority + ", " : "") +
            (active != null ? "active=" + active + ", " : "") +
            (senderPc != null ? "senderPc=" + senderPc + ", " : "") +
            (recipientPc != null ? "recipientPc=" + recipientPc + ", " : "") +
            (senderConditionId != null ? "senderConditionId=" + senderConditionId + ", " : "") +
            (recipientConditionId != null ? "recipientConditionId=" + recipientConditionId + ", " : "") +
            (senderPercentId != null ? "senderPercentId=" + senderPercentId + ", " : "") +
            (recipientPercentId != null ? "recipientPercentId=" + recipientPercentId + ", " : "") +
            (promo != null ? "promo=" + promo + ", " : "") +
            "}"
        );
    }
}
