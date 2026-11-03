package uz.paykap.service.dto;

import io.github.jhipster.service.filter.*;

import java.io.Serializable;


/**
 * Criteria class for the Deposit entity. This class is used in DepositResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /deposits?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class DepositCriteria implements Serializable {
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

    private LongFilter gatewayId;

    private LongFilter agentId;

    private LongFilter virtualAccountId;

    public DepositCriteria() {
    }

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public LongFilter getOverdraftLimit() {
        return overdraftLimit;
    }

    public void setOverdraftLimit(LongFilter overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }

    public LongFilter getBalance() {
        return balance;
    }

    public void setBalance(LongFilter balance) {
        this.balance = balance;
    }

    public IntegerFilter getBalanceCcy() {
        return balanceCcy;
    }

    public void setBalanceCcy(IntegerFilter balanceCcy) {
        this.balanceCcy = balanceCcy;
    }

    public LongFilter getBookedBalance() {
        return bookedBalance;
    }

    public void setBookedBalance(LongFilter bookedBalance) {
        this.bookedBalance = bookedBalance;
    }

    public LongFilter getLimitInform() {
        return limitInform;
    }

    public void setLimitInform(LongFilter limitInform) {
        this.limitInform = limitInform;
    }

    public LongFilter getLimitDisable() {
        return limitDisable;
    }

    public void setLimitDisable(LongFilter limitDisable) {
        this.limitDisable = limitDisable;
    }

    public StringFilter getContactGroup() {
        return contactGroup;
    }

    public void setContactGroup(StringFilter contactGroup) {
        this.contactGroup = contactGroup;
    }

    public BooleanFilter getBlocked() {
        return blocked;
    }

    public void setBlocked(BooleanFilter blocked) {
        this.blocked = blocked;
    }

    public BooleanFilter getUnlimit() {
        return unlimit;
    }

    public void setUnlimit(BooleanFilter unlimit) {
        this.unlimit = unlimit;
    }

    public LongFilter getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(LongFilter gatewayId) {
        this.gatewayId = gatewayId;
    }

    public LongFilter getAgentId() {
        return agentId;
    }

    public void setAgentId(LongFilter agentId) {
        this.agentId = agentId;
    }

    public LongFilter getVirtualAccountId() {
        return virtualAccountId;
    }

    public void setVirtualAccountId(LongFilter virtualAccountId) {
        this.virtualAccountId = virtualAccountId;
    }

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
                (gatewayId != null ? "gatewayId=" + gatewayId + ", " : "") +
                (agentId != null ? "agentId=" + agentId + ", " : "") +
            (virtualAccountId != null ? "virtualAccountId=" + virtualAccountId + ", " : "") +
            "}";
    }

}
