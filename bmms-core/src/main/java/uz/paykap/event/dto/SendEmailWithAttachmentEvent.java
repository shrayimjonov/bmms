package uz.paykap.event.dto;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class SendEmailWithAttachmentEvent extends ApplicationEvent {

    private List<ReconcileFileReceiverDTO> emailRecipientList;
    private String subject;

    public SendEmailWithAttachmentEvent(Object source, String subject, ReconcileFileReceiverDTO... emailRecipientList) {
        super(source);
        this.emailRecipientList = Arrays.asList(emailRecipientList);
        this.subject = subject;
    }

    public SendEmailWithAttachmentEvent(Object source, String subject, List<ReconcileFileReceiverDTO> emailRecipientList) {
        super(source);
        this.emailRecipientList = emailRecipientList;
        this.subject = subject;
    }

    public List<ReconcileFileReceiverDTO> getEmailRecipientList() {
        if (emailRecipientList == null) {
            return new ArrayList<>();
        }
        return emailRecipientList;
    }

}
