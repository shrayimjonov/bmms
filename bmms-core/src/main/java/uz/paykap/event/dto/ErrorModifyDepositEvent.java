package uz.paykap.event.dto;

import org.springframework.context.ApplicationEvent;

public class ErrorModifyDepositEvent extends ApplicationEvent {

    private final Long requestAmount;
    private final Long deposit;
    private final String agent;

    public ErrorModifyDepositEvent(Object source, Long requestAmount, Long deposit, String agent) {
        super(source);
        this.requestAmount = requestAmount;
        this.deposit = deposit;
        this.agent = agent;
    }

    public Long getRequestAmount() {
        return requestAmount;
    }

    public Long getDeposit() {
        return deposit;
    }

    public String getAgent() {
        return agent;
    }

}
