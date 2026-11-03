package uz.paykap.event.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class SendSmsEvent extends ApplicationEvent {

    private final String[] sendTo;
    private final String content;

    public SendSmsEvent(Object source, String content, String... sendTo) {
        super(source);
        this.sendTo = sendTo;
        this.content = content;
    }
}
