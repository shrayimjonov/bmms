package uz.paykap.event.dto;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PartnerBillingErrorEvent extends ApplicationEvent {

    private String partnerName;
    private String message;
    private String refNum;

    public PartnerBillingErrorEvent(Object source, String partnerName, String message) {
        super(source);
        this.partnerName = partnerName;
        this.message = message;
    }

    public PartnerBillingErrorEvent(Object source, String partnerName, String message, String refNum) {
        super(source);
        this.partnerName = partnerName;
        this.message = message;
        this.refNum = refNum;
    }

    public PartnerBillingErrorEvent(Object source, String partnerName, String message, Long refNum) {
        this(source, partnerName, message, String.valueOf(refNum));
    }
}
