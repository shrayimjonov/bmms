package uz.paykap.event.dto;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import uz.javlon.commons.base.ResultData;
import uz.paykap.gateway.dto.GatewayTranDTO;

import java.util.Set;

@Getter
public class ReconciliationFinishedEvent extends ApplicationEvent {

    private ResultData<Set<GatewayTranDTO>> resultData;
    private String recipientName;

    public ReconciliationFinishedEvent(Object source, String recipientName, ResultData<Set<GatewayTranDTO>> resultData) {
        super(source);
        this.recipientName = recipientName;
        this.resultData = resultData;
    }

}
