package uz.paykap.event.dto;

import org.springframework.context.ApplicationEvent;

public class AgentDepositModifiedEvent extends ApplicationEvent {

    private final Long depositBefore;
    private final Long depositAfter;
    private final String agent;

    public AgentDepositModifiedEvent(Object source, Long depositBefore, Long depositAfter, String agent) {
        super(source);
        this.depositBefore = depositBefore;
        this.depositAfter = depositAfter;
        this.agent = agent;
    }

    public Long getDepositBefore() {
        return depositBefore;
    }

    public Long getDepositAfter() {
        return depositAfter;
    }

    public String getAgent() {
        return agent;
    }
}
