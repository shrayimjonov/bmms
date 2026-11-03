package uz.paykap.service.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class SimpleDepositDTO {

    @NotNull
    private Long overdraftLimit;

    @NotNull
    private Long balance;

    @NotNull
    private Long limitInform;

    @NotNull
    private Long limitDisable;

    @Size(max = 64)
    private String contactGroup;


    public Long getOverdraftLimit() {
        return overdraftLimit;
    }

    public void setOverdraftLimit(Long overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public Long getLimitInform() {
        return limitInform;
    }

    public void setLimitInform(Long limitInform) {
        this.limitInform = limitInform;
    }

    public Long getLimitDisable() {
        return limitDisable;
    }

    public void setLimitDisable(Long limitDisable) {
        this.limitDisable = limitDisable;
    }

    public String getContactGroup() {
        return contactGroup;
    }

    public void setContactGroup(String contactGroup) {
        this.contactGroup = contactGroup;
    }

    @Override
    public String toString() {
        return "SimpleDepositDTO{" +
            "overdraftLimit=" + overdraftLimit +
            ", balance=" + balance +
            ", limitInform=" + limitInform +
            ", limitDisable=" + limitDisable +
            ", contactGroup='" + contactGroup + '\'' +
            '}';
    }


    public SimpleDepositDTO overdraftLimit(Long overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
        return this;
    }

    public SimpleDepositDTO balance(Long balance) {
        this.balance = balance;
        return this;
    }

    public SimpleDepositDTO limitInform(Long limitInform) {
        this.limitInform = limitInform;
        return this;
    }

    public SimpleDepositDTO limitDisable(Long limitDisable) {
        this.limitDisable = limitDisable;
        return this;
    }

    public SimpleDepositDTO contactGroup(String contactGroup) {
        this.contactGroup = contactGroup;
        return this;
    }
}
