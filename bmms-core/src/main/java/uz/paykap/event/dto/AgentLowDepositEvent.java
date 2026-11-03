package uz.paykap.event.dto;

import org.springframework.context.ApplicationEvent;

public class AgentLowDepositEvent extends ApplicationEvent {

    private final Long agentId;
    private final Long deposit;
    private final String agent;

    public AgentLowDepositEvent(Object source, Long agentId, Long deposit, String agent) {
        super(source);
        this.agentId = agentId;
        this.deposit = deposit;
        this.agent = agent;
    }

    public Long getAgentId() {
        return agentId;
    }

    public Long getDeposit() {
        return deposit;
    }

    public String getAgent() {
        return agent;
    }

}
