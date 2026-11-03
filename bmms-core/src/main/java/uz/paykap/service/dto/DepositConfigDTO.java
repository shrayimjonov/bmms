package uz.paykap.service.dto;

import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class DepositConfigDTO {

    @NotNull
    private String agentLogin;

    @ApiModelProperty(value = "Порог овердрафта", notes = "Сумма в тийинах минимального порога кредитного депосита, до которого баланс может понизится. По умолчанию 0, логично 0 или отрицательное число.)", example = "0")
    private Long overdraftLimit;

    @ApiModelProperty(value = "Порог остановки оплат", required = true)
    private Long limitDisable;

    @ApiModelProperty(value = "Documentary basis for configuration", example = "Order-N23")
    @Size(max = 64)
    private String doc;

    public Long getOverdraftLimit() {
        return overdraftLimit;
    }

    public void setOverdraftLimit(Long overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }

    public String getAgentLogin() {
        return agentLogin;
    }

    public void setAgentLogin(String agentLogin) {
        this.agentLogin = agentLogin;
    }

    public String getDoc() {
        return doc;
    }

    public void setDoc(String doc) {
        this.doc = doc;
    }

    public Long getLimitDisable() {
        return limitDisable;
    }

    public void setLimitDisable(Long limitDisable) {
        this.limitDisable = limitDisable;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("agentLogin", agentLogin)
            .append("overdraftLimit", overdraftLimit)
            .append("limitDisable", limitDisable)
            .append("doc", doc)
            .toString();
    }
}
