package uz.paykap.gateway.dto;

import java.util.HashMap;
import java.util.Map;

public class DepositBalanceDTO {

    private Long credit;
    private Long balance;
    private String name;
    private String currency;
    private Map<String, String> data = new HashMap<>();


    public Long getCredit() {
        return credit;
    }

    public void setCredit(Long credit) {
        this.credit = credit;
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "DepositBalanceDTO{" +
            "credit=" + credit +
            ", balance=" + balance +
            '}';
    }
}
