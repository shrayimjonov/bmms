package uz.paykap.event.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import uz.javlon.commons.base.ResultData;
import uz.paykap.service.dto.reconcile.ComplexReconcileTransaction;

import java.util.Set;

@Getter
@EqualsAndHashCode(callSuper = true)
public class AgentReconcileEventArg extends ApplicationEvent {
    String gateway;
    ResultData<Set<ComplexReconcileTransaction>> result;

    public AgentReconcileEventArg(Object source, String gateway, ResultData<Set<ComplexReconcileTransaction>> result) {
        super(source);
        this.gateway = gateway;
        this.result = result;
    }
}
