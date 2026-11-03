package uz.paykap.event.dto;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SendToMailEvent extends ApplicationEvent {

    private final String sendTo;
    private final String subject;
    private final String content;
    private final boolean isMultipart;
    private final boolean isHtml;

    public SendToMailEvent(Object source, String sendTo, String subject, String content, boolean isMultipart, boolean isHtml) {
        super(source);
        this.sendTo = sendTo;
        this.subject = subject;
        this.content = content;
        this.isMultipart = isMultipart;
        this.isHtml = isHtml;
    }

}
