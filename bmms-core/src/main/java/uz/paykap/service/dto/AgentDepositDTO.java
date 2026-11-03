package uz.paykap.service.dto;

import javax.validation.constraints.NotNull;

public class AgentDepositDTO {

    @NotNull
    private Long depositAmount;

    @NotNull
    private Long agentId;

    public Long getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(Long depositAmount) {
        this.depositAmount = depositAmount;
    }

    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    @Override
    public String toString() {
        return "AgentDepositDTO{" +
            "depositAmount=" + depositAmount +
            ", agentId=" + agentId +
            '}';
    }

    public AgentDepositDTO depositAmount(Long depositAmount) {
        this.depositAmount = depositAmount;
        return this;
    }

    public AgentDepositDTO agentId(Long agentId) {
        this.agentId = agentId;
        return this;
    }
}
