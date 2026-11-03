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
import uz.paykap.domain.enumeration.DepositState;

/**
 * Criteria class for the {@link uz.paykap.domain.Deposit} entity. This class is used
 * in {@link uz.paykap.web.rest.DepositResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /deposits?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class DepositCriteria implements Serializable, Criteria {

    /**
     * Class for filtering DepositState
     */
    public static class DepositStateFilter extends Filter<DepositState> {

        public DepositStateFilter() {}

        public DepositStateFilter(DepositStateFilter filter) {
            super(filter);
        }

        @Override
        public DepositStateFilter copy() {
            return new DepositStateFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private LongFilter overdraftLimit;

    private LongFilter balance;

    private IntegerFilter balanceCcy;

    private LongFilter bookedBalance;

    private LongFilter limitInform;

    private LongFilter limitDisable;

    private StringFilter contactGroup;

    private BooleanFilter blocked;

    private BooleanFilter unlimit;

    private DepositStateFilter state;

    private LongFilter gatewayId;

    private LongFilter agentId;

    private StringFilter agentShortName;

    private LongFilter virtualAccountId;

    public DepositCriteria() {}

    public DepositCriteria(DepositCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.overdraftLimit = other.overdraftLimit == null ? null : other.overdraftLimit.copy();
        this.balance = other.balance == null ? null : other.balance.copy();
        this.balanceCcy = other.balanceCcy == null ? null : other.balanceCcy.copy();
        this.bookedBalance = other.bookedBalance == null ? null : other.bookedBalance.copy();
        this.limitInform = other.limitInform == null ? null : other.limitInform.copy();
        this.limitDisable = other.limitDisable == null ? null : other.limitDisable.copy();
        this.contactGroup = other.contactGroup == null ? null : other.contactGroup.copy();
        this.blocked = other.blocked == null ? null : other.blocked.copy();
        this.unlimit = other.unlimit == null ? null : other.unlimit.copy();
        this.state = other.state == null ? null : other.state.copy();
        this.gatewayId = other.gatewayId == null ? null : other.gatewayId.copy();
        this.agentId = other.agentId == null ? null : other.agentId.copy();
        this.agentShortName = other.agentShortName == null ? null : other.agentShortName.copy();
        this.virtualAccountId = other.virtualAccountId == null ? null : other.virtualAccountId.copy();
    }

    @Override
    public DepositCriteria copy() {
        return new DepositCriteria(this);
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

    public LongFilter getOverdraftLimit() {
        return overdraftLimit;
    }

    public LongFilter overdraftLimit() {
        if (overdraftLimit == null) {
            overdraftLimit = new LongFilter();
        }
        return overdraftLimit;
    }

    public void setOverdraftLimit(LongFilter overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }

    public LongFilter getBalance() {
        return balance;
    }

    public LongFilter balance() {
        if (balance == null) {
            balance = new LongFilter();
        }
        return balance;
    }

    public void setBalance(LongFilter balance) {
        this.balance = balance;
    }

    public IntegerFilter getBalanceCcy() {
        return balanceCcy;
    }

    public IntegerFilter balanceCcy() {
        if (balanceCcy == null) {
            balanceCcy = new IntegerFilter();
        }
        return balanceCcy;
    }

    public void setBalanceCcy(IntegerFilter balanceCcy) {
        this.balanceCcy = balanceCcy;
    }

    public LongFilter getBookedBalance() {
        return bookedBalance;
    }

    public LongFilter bookedBalance() {
        if (bookedBalance == null) {
            bookedBalance = new LongFilter();
        }
        return bookedBalance;
    }

    public void setBookedBalance(LongFilter bookedBalance) {
        this.bookedBalance = bookedBalance;
    }

    public LongFilter getLimitInform() {
        return limitInform;
    }

    public LongFilter limitInform() {
        if (limitInform == null) {
            limitInform = new LongFilter();
        }
        return limitInform;
    }

    public void setLimitInform(LongFilter limitInform) {
        this.limitInform = limitInform;
    }

    public LongFilter getLimitDisable() {
        return limitDisable;
    }

    public LongFilter limitDisable() {
        if (limitDisable == null) {
            limitDisable = new LongFilter();
        }
        return limitDisable;
    }

    public void setLimitDisable(LongFilter limitDisable) {
        this.limitDisable = limitDisable;
    }

    public StringFilter getContactGroup() {
        return contactGroup;
    }

    public StringFilter contactGroup() {
        if (contactGroup == null) {
            contactGroup = new StringFilter();
        }
        return contactGroup;
    }

    public void setContactGroup(StringFilter contactGroup) {
        this.contactGroup = contactGroup;
    }

    public BooleanFilter getBlocked() {
        return blocked;
    }

    public BooleanFilter blocked() {
        if (blocked == null) {
            blocked = new BooleanFilter();
        }
        return blocked;
    }

    public void setBlocked(BooleanFilter blocked) {
        this.blocked = blocked;
    }

    public BooleanFilter getUnlimit() {
        return unlimit;
    }

    public BooleanFilter unlimit() {
        if (unlimit == null) {
            unlimit = new BooleanFilter();
        }
        return unlimit;
    }

    public void setUnlimit(BooleanFilter unlimit) {
        this.unlimit = unlimit;
    }

    public DepositStateFilter getState() {
        return state;
    }

    public DepositStateFilter state() {
        if (state == null) {
            state = new DepositStateFilter();
        }
        return state;
    }

    public void setState(DepositStateFilter state) {
        this.state = state;
    }

    public LongFilter getGatewayId() {
        return gatewayId;
    }

    public LongFilter gatewayId() {
        if (gatewayId == null) {
            gatewayId = new LongFilter();
        }
        return gatewayId;
    }

    public void setGatewayId(LongFilter gatewayId) {
        this.gatewayId = gatewayId;
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

    public StringFilter getAgentShortName() {
        if (agentShortName == null) {
            agentShortName = new StringFilter();
        }
        return agentShortName;
    }

    public void setAgentShortName(StringFilter agentShortName) {
        this.agentShortName = agentShortName;
    }

    public LongFilter getVirtualAccountId() {
        return virtualAccountId;
    }

    public LongFilter virtualAccountId() {
        if (virtualAccountId == null) {
            virtualAccountId = new LongFilter();
        }
        return virtualAccountId;
    }

    public void setVirtualAccountId(LongFilter virtualAccountId) {
        this.virtualAccountId = virtualAccountId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DepositCriteria that = (DepositCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(overdraftLimit, that.overdraftLimit) &&
            Objects.equals(balance, that.balance) &&
            Objects.equals(balanceCcy, that.balanceCcy) &&
            Objects.equals(bookedBalance, that.bookedBalance) &&
            Objects.equals(limitInform, that.limitInform) &&
            Objects.equals(limitDisable, that.limitDisable) &&
            Objects.equals(contactGroup, that.contactGroup) &&
            Objects.equals(blocked, that.blocked) &&
            Objects.equals(unlimit, that.unlimit) &&
            Objects.equals(state, that.state) &&
            Objects.equals(gatewayId, that.gatewayId) &&
            Objects.equals(agentId, that.agentId) &&
            Objects.equals(agentShortName, that.agentShortName) &&
            Objects.equals(virtualAccountId, that.virtualAccountId)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            overdraftLimit,
            balance,
            balanceCcy,
            bookedBalance,
            limitInform,
            limitDisable,
            contactGroup,
            blocked,
            unlimit,
            state,
            gatewayId,
            agentId,
                agentShortName,
            virtualAccountId
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DepositCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (overdraftLimit != null ? "overdraftLimit=" + overdraftLimit + ", " : "") +
            (balance != null ? "balance=" + balance + ", " : "") +
            (balanceCcy != null ? "balanceCcy=" + balanceCcy + ", " : "") +
            (bookedBalance != null ? "bookedBalance=" + bookedBalance + ", " : "") +
            (limitInform != null ? "limitInform=" + limitInform + ", " : "") +
            (limitDisable != null ? "limitDisable=" + limitDisable + ", " : "") +
            (contactGroup != null ? "contactGroup=" + contactGroup + ", " : "") +
            (blocked != null ? "blocked=" + blocked + ", " : "") +
            (unlimit != null ? "unlimit=" + unlimit + ", " : "") +
            (state != null ? "state=" + state + ", " : "") +
            (gatewayId != null ? "gatewayId=" + gatewayId + ", " : "") +
            (agentId != null ? "agentId=" + agentId + ", " : "") +
            (agentShortName != null ? "agentShortName=" + agentShortName + ", " : "") +
            (virtualAccountId != null ? "virtualAccountId=" + virtualAccountId + ", " : "") +
            "}";
    }
}
