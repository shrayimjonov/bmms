package uz.paykap.service.criteria;

import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;
import uz.paykap.domain.enumeration.P2pProcessorType;

import java.io.Serializable;
import java.util.Objects;

/**
 * Criteria class for the {@link uz.paykap.domain.P2pParam} entity. This class is used
 * in {@link uz.paykap.web.rest.P2pParamResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /p-2-p-params?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class P2pParamCriteria implements Serializable, Criteria {

    /**
     * Class for filtering P2pProcessorType
     */
    public static class P2pProcessorTypeFilter extends Filter<P2pProcessorType> {

        public P2pProcessorTypeFilter() {}

        public P2pProcessorTypeFilter(P2pProcessorTypeFilter filter) {
            super(filter);
        }

        @Override
        public P2pProcessorTypeFilter copy() {
            return new P2pProcessorTypeFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private IntegerFilter priority;

    private BooleanFilter active;

    private P2pProcessorTypeFilter senderPc;

    private P2pProcessorTypeFilter recipientPc;

    private StringFilter name;

    private LongFilter senderConditionId;

    private LongFilter recipientConditionId;

    private LongFilter senderPercentId;

    private LongFilter recipientPercentId;

    private StringFilter promo;

    public P2pParamCriteria() {}

    public P2pParamCriteria(P2pParamCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.priority = other.priority == null ? null : other.priority.copy();
        this.active = other.active == null ? null : other.active.copy();
        this.senderPc = other.senderPc == null ? null : other.senderPc.copy();
        this.recipientPc = other.recipientPc == null ? null : other.recipientPc.copy();
        this.name = other.name == null ? null : other.name.copy();
        this.senderConditionId = other.senderConditionId == null ? null : other.senderConditionId.copy();
        this.recipientConditionId = other.recipientConditionId == null ? null : other.recipientConditionId.copy();
        this.senderPercentId = other.senderPercentId == null ? null : other.senderPercentId.copy();
        this.recipientPercentId = other.recipientPercentId == null ? null : other.recipientPercentId.copy();
        this.promo = other.promo == null ? null : other.promo.copy();
    }

    @Override
    public P2pParamCriteria copy() {
        return new P2pParamCriteria(this);
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

    public IntegerFilter getPriority() {
        return priority;
    }

    public IntegerFilter priority() {
        if (priority == null) {
            priority = new IntegerFilter();
        }
        return priority;
    }

    public void setPriority(IntegerFilter priority) {
        this.priority = priority;
    }

    public BooleanFilter getActive() {
        return active;
    }

    public BooleanFilter active() {
        if (active == null) {
            active = new BooleanFilter();
        }
        return active;
    }

    public void setActive(BooleanFilter active) {
        this.active = active;
    }

    public P2pProcessorTypeFilter getSenderPc() {
        return senderPc;
    }

    public P2pProcessorTypeFilter senderPc() {
        if (senderPc == null) {
            senderPc = new P2pProcessorTypeFilter();
        }
        return senderPc;
    }

    public void setSenderPc(P2pProcessorTypeFilter senderPc) {
        this.senderPc = senderPc;
    }

    public P2pProcessorTypeFilter getRecipientPc() {
        return recipientPc;
    }

    public P2pProcessorTypeFilter recipientPc() {
        if (recipientPc == null) {
            recipientPc = new P2pProcessorTypeFilter();
        }
        return recipientPc;
    }

    public void setRecipientPc(P2pProcessorTypeFilter recipientPc) {
        this.recipientPc = recipientPc;
    }

    public StringFilter getName() {
        return name;
    }

    public StringFilter name() {
        if (name == null) {
            name = new StringFilter();
        }
        return name;
    }

    public void setName(StringFilter name) {
        this.name = name;
    }

    public LongFilter getSenderConditionId() {
        return senderConditionId;
    }

    public LongFilter senderConditionId() {
        if (senderConditionId == null) {
            senderConditionId = new LongFilter();
        }
        return senderConditionId;
    }

    public void setSenderConditionId(LongFilter senderConditionId) {
        this.senderConditionId = senderConditionId;
    }

    public LongFilter getRecipientConditionId() {
        return recipientConditionId;
    }

    public LongFilter recipientConditionId() {
        if (recipientConditionId == null) {
            recipientConditionId = new LongFilter();
        }
        return recipientConditionId;
    }

    public void setRecipientConditionId(LongFilter recipientConditionId) {
        this.recipientConditionId = recipientConditionId;
    }

    public LongFilter getSenderPercentId() {
        return senderPercentId;
    }

    public LongFilter senderPercentId() {
        if (senderPercentId == null) {
            senderPercentId = new LongFilter();
        }
        return senderPercentId;
    }

    public void setSenderPercentId(LongFilter senderPercentId) {
        this.senderPercentId = senderPercentId;
    }

    public LongFilter getRecipientPercentId() {
        return recipientPercentId;
    }

    public LongFilter recipientPercentId() {
        if (recipientPercentId == null) {
            recipientPercentId = new LongFilter();
        }
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
            Objects.equals(name, that.name) &&
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
            name,
            senderConditionId,
            recipientConditionId,
            senderPercentId,
            recipientPercentId,
            promo
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "P2pParamCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (priority != null ? "priority=" + priority + ", " : "") +
            (active != null ? "active=" + active + ", " : "") +
            (senderPc != null ? "senderPc=" + senderPc + ", " : "") +
            (recipientPc != null ? "recipientPc=" + recipientPc + ", " : "") +
            (name != null ? "name=" + name + ", " : "") +
            (senderConditionId != null ? "senderConditionId=" + senderConditionId + ", " : "") +
            (recipientConditionId != null ? "recipientConditionId=" + recipientConditionId + ", " : "") +
            (senderPercentId != null ? "senderPercentId=" + senderPercentId + ", " : "") +
            (recipientPercentId != null ? "recipientPercentId=" + recipientPercentId + ", " : "") +
            (promo != null ? "promo=" + promo + ", " : "") +
            "}";
    }
}
