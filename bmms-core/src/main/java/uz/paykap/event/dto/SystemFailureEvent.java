package uz.paykap.event.dto;

import org.springframework.context.ApplicationEvent;
import uz.javlon.pusher.dto.PushType;

import java.util.HashMap;
import java.util.Map;

public class SystemFailureEvent extends ApplicationEvent {

    private final DangerType dangerType;
    private final String recipient;
    private final PushType pushType;
    private final String errorMessage;
    private final Map<String, String> params;

    public SystemFailureEvent(Object source, DangerType dangerType, String recipient, PushType pushType, String errorMessage) {
        super(source);
        this.dangerType = dangerType;
        this.recipient = recipient;
        this.pushType = pushType;
        this.errorMessage = errorMessage;
        params = new HashMap<>();
    }

    public DangerType getDangerType() {
        return dangerType;
    }

    public String getRecipient() {
        return recipient;
    }

    public PushType getPushType() {
        return pushType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Map<String, String> getParams() {
        return params;
    }
}
