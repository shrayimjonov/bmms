package uz.paykap.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ReplenishDepositDTO {

    @NotNull
    private String agentLogin;

    @Deprecated
    private Long overdraftLimit;

    @ApiModelProperty(value = "Replenish amount")
    @NotNull
    private Long balanceAmount;

    @ApiModelProperty(value = "Валюта суммы пополнения, трёхзначный цифровой ISO 4217, по умолчанию 860 (UZS)", example = "860 - Узбекский сум, 840 - Доллар США")
    @NotNull
    private Integer balanceCcy = 860;

    @ApiModelProperty(value = "Documentary basis for account replenishment", example = "Order-N23", name = "name of document")
    @Size(max = 64)
    private String doc;

    @JsonIgnore
    @ApiModelProperty(value = "Tax number (INN) of payer")
    private String payerTaxnr;

    @JsonIgnore
    @ApiModelProperty(value = "Tax number (INN) of recipient")
    private String recipientTaxnr;

    public String getAgentLogin() {
        return agentLogin;
    }

    public void setAgentLogin(String agentLogin) {
        this.agentLogin = agentLogin;
    }

    @Deprecated
    public Long getOverdraftLimit() {
        return overdraftLimit;
    }

    @Deprecated
    public void setOverdraftLimit(Long overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }

    public Long getBalanceAmount() {
        return balanceAmount;
    }

    public void setBalanceAmount(Long balanceAmount) {
        this.balanceAmount = balanceAmount;
    }

    public String getDoc() {
        return doc;
    }

    public void setDoc(String doc) {
        this.doc = doc;
    }

    public String getPayerTaxnr() {
        return payerTaxnr;
    }

    public void setPayerTaxnr(String payerTaxnr) {
        this.payerTaxnr = payerTaxnr;
    }

    public String getRecipientTaxnr() {
        return recipientTaxnr;
    }

    public void setRecipientTaxnr(String recipientTaxnr) {
        this.recipientTaxnr = recipientTaxnr;
    }

    public Integer getBalanceCcy() {
        return balanceCcy;
    }

    public void setBalanceCcy(Integer balanceCcy) {
        this.balanceCcy = balanceCcy;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("agentLogin", agentLogin)
            .append("overdraftLimit", overdraftLimit)
            .append("balanceAmount", balanceAmount)
            .append("balanceCcy", balanceCcy)
            .append("doc", doc)
            .append("payerTaxnr", payerTaxnr)
            .append("recipientTaxnr", recipientTaxnr)
            .toString();
    }
}
