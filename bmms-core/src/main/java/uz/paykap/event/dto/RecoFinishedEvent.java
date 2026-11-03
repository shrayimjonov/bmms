package uz.paykap.event.dto;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import uz.devops.bmcore.reconciliation.dto.RecoTranDTO;
import uz.javlon.commons.base.ResultData;

import java.util.Set;

@Getter
public class RecoFinishedEvent extends ApplicationEvent {

    private ResultData<Set<RecoTranDTO>> resultData;
    private String shortName;

    public RecoFinishedEvent(Object source, String shortName, ResultData<Set<RecoTranDTO>> resultData) {
        super(source);
        this.shortName = shortName;
        this.resultData = resultData;
    }

}
