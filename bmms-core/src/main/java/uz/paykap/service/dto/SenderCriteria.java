package uz.paykap.service.dto;

import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.Filter;
import java.io.Serializable;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;
import uz.devops.bmcore.enumeration.PayType;

/**
 * Criteria class for the Sender entity. This class is used in SenderResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /senders?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class SenderCriteria implements Serializable {

    /**
     * Class for filtering PayType
     */
    public static class PayTypeFilter extends Filter<PayType> {}

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter name;

    private LongFilter ord;

    private BooleanFilter active;

    private StringFilter springBeanName;

    private PayTypeFilter payType;

    private LongFilter agentId;

    public SenderCriteria() {}

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getName() {
        return name;
    }

    public void setName(StringFilter name) {
        this.name = name;
    }

    public LongFilter getOrd() {
        return ord;
    }

    public void setOrd(LongFilter ord) {
        this.ord = ord;
    }

    public BooleanFilter getActive() {
        return active;
    }

    public void setActive(BooleanFilter active) {
        this.active = active;
    }

    public StringFilter getSpringBeanName() {
        return springBeanName;
    }

    public void setSpringBeanName(StringFilter springBeanName) {
        this.springBeanName = springBeanName;
    }

    public PayTypeFilter getPayType() {
        return payType;
    }

    public void setPayType(PayTypeFilter payType) {
        this.payType = payType;
    }

    public LongFilter getAgentId() {
        return agentId;
    }

    public void setAgentId(LongFilter agentId) {
        this.agentId = agentId;
    }

    @Override
    public String toString() {
        return (
            "SenderCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (name != null ? "name=" + name + ", " : "") +
            (ord != null ? "ord=" + ord + ", " : "") +
            (active != null ? "active=" + active + ", " : "") +
            (springBeanName != null ? "springBeanName=" + springBeanName + ", " : "") +
            (payType != null ? "payType=" + payType + ", " : "") +
            (agentId != null ? "agentId=" + agentId + ", " : "") +
            "}"
        );
    }
}
