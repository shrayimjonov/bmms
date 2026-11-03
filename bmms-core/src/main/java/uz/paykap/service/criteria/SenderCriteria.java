package uz.paykap.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.BooleanFilter;
import tech.jhipster.service.filter.DoubleFilter;
import tech.jhipster.service.filter.Filter;
import tech.jhipster.service.filter.FloatFilter;
import tech.jhipster.service.filter.IntegerFilter;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;
import uz.devops.bmcore.enumeration.PayType;

/**
 * Criteria class for the {@link uz.paykap.domain.Sender} entity. This class is used
 * in {@link uz.paykap.web.rest.SenderResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /senders?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class SenderCriteria implements Serializable, Criteria {

    /**
     * Class for filtering PayType
     */
    public static class PayTypeFilter extends Filter<PayType> {

        public PayTypeFilter() {}

        public PayTypeFilter(PayTypeFilter filter) {
            super(filter);
        }

        @Override
        public PayTypeFilter copy() {
            return new PayTypeFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter name;

    private LongFilter ord;

    private BooleanFilter active;

    private StringFilter springBeanName;

    private PayTypeFilter payType;

    private LongFilter agentId;

    public SenderCriteria() {}

    public SenderCriteria(SenderCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.name = other.name == null ? null : other.name.copy();
        this.ord = other.ord == null ? null : other.ord.copy();
        this.active = other.active == null ? null : other.active.copy();
        this.springBeanName = other.springBeanName == null ? null : other.springBeanName.copy();
        this.payType = other.payType == null ? null : other.payType.copy();
        this.agentId = other.agentId == null ? null : other.agentId.copy();
    }

    @Override
    public SenderCriteria copy() {
        return new SenderCriteria(this);
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

    public LongFilter getOrd() {
        return ord;
    }

    public LongFilter ord() {
        if (ord == null) {
            ord = new LongFilter();
        }
        return ord;
    }

    public void setOrd(LongFilter ord) {
        this.ord = ord;
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

    public StringFilter getSpringBeanName() {
        return springBeanName;
    }

    public StringFilter springBeanName() {
        if (springBeanName == null) {
            springBeanName = new StringFilter();
        }
        return springBeanName;
    }

    public void setSpringBeanName(StringFilter springBeanName) {
        this.springBeanName = springBeanName;
    }

    public PayTypeFilter getPayType() {
        return payType;
    }

    public PayTypeFilter payType() {
        if (payType == null) {
            payType = new PayTypeFilter();
        }
        return payType;
    }

    public void setPayType(PayTypeFilter payType) {
        this.payType = payType;
    }

    public LongFilter getAgentId() {
        return agentId;
    }

    public LongFilter agentId() {
        if (agentId == null) {
            agentId = new LongFilter();
        }
        return agentId;
    }

    public void setAgentId(LongFilter agentId) {
        this.agentId = agentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SenderCriteria that = (SenderCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(name, that.name) &&
            Objects.equals(ord, that.ord) &&
            Objects.equals(active, that.active) &&
            Objects.equals(springBeanName, that.springBeanName) &&
            Objects.equals(payType, that.payType) &&
            Objects.equals(agentId, that.agentId)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, ord, active, springBeanName, payType, agentId);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SenderCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (name != null ? "name=" + name + ", " : "") +
            (ord != null ? "ord=" + ord + ", " : "") +
            (active != null ? "active=" + active + ", " : "") +
            (springBeanName != null ? "springBeanName=" + springBeanName + ", " : "") +
            (payType != null ? "payType=" + payType + ", " : "") +
            (agentId != null ? "agentId=" + agentId + ", " : "") +
            "}";
    }
}
