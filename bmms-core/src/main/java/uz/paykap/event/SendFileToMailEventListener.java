package uz.paykap.event;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.domain.Agent;
import uz.paykap.domain.Gateway;
import uz.paykap.event.dto.AttachmentReceiverType;
import uz.paykap.event.dto.ReconcileFileReceiverDTO;
import uz.paykap.event.dto.SendEmailWithAttachmentEvent;
import uz.paykap.repository.AgentRepository;
import uz.paykap.repository.GatewayRepository;
import uz.paykap.service.MailService;

import java.util.Optional;

@Slf4j
@Component
@AllArgsConstructor
public class SendFileToMailEventListener implements ApplicationListener<SendEmailWithAttachmentEvent> {

    private final MailService mailService;
    private final AgentRepository agentRepository;
    private final ApplicationProperties properties;
    private final GatewayRepository gatewayRepository;

    @Override
    public void onApplicationEvent(SendEmailWithAttachmentEvent sendEmailWithAttachmentEvent) {

        log.debug("Sending email with attachment : {}", sendEmailWithAttachmentEvent.getSubject());

        for (ReconcileFileReceiverDTO emailRecipient : sendEmailWithAttachmentEvent.getEmailRecipientList()) {
            try {
                if (emailRecipient.getAttachmentReceiverType().equals(AttachmentReceiverType.AGENT)) {

                    Optional<Agent> optAgent = agentRepository.findTop1ByShortName(emailRecipient.getReceiverShortName());
                    if (optAgent.isPresent()) {
                        String to = String.valueOf(optAgent.get().getPhoneNumber()).matches("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$") ? String.valueOf(optAgent.get().getPhoneNumber()) : properties.getDefaultEmailTo();
                        mailService.sendEmailWithFile(to, properties.getDefaultEmailFrom(), emailRecipient.getReceiverShortName() + " reconcile", emailRecipient.getFilePath(), emailRecipient.getReceiverShortName() + " reconcile", false);
                    } else {
                        mailService.sendEmailWithFile(properties.getDefaultEmailTo(), properties.getDefaultEmailFrom(), sendEmailWithAttachmentEvent.getSubject() != null && sendEmailWithAttachmentEvent.getSubject().length() > 0 ? sendEmailWithAttachmentEvent.getSubject() : emailRecipient.getReceiverShortName() + " reconcile", emailRecipient.getFilePath(), emailRecipient.getReceiverShortName() + " reconcile", false);
                    }
                }
                if (emailRecipient.getAttachmentReceiverType().equals(AttachmentReceiverType.RECIPIENT)) {

                    Optional<Gateway> optGateway = gatewayRepository.findTop1ByName(emailRecipient.getReceiverShortName());//gateway name bo'yicha gatewayni olamiz
                    if (optGateway.isPresent() && optGateway.get().getContract() != null && optGateway.get().getContract().length() > 0) {//agar shu nomli gateway bor bo'lsa
                        String to = optGateway.get().getContract().matches("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$") ? optGateway.get().getContract() : properties.getDefaultEmailTo();//va shu gatewayni contract polyasida email ko'rsatilgan bo'lsa, shuni aks xolda propertydan defaultEmilTo olinadi
                        mailService.sendEmailWithFile(to, properties.getDefaultEmailFrom(), sendEmailWithAttachmentEvent.getSubject(), emailRecipient.getFilePath(), sendEmailWithAttachmentEvent.getSubject(), false);
                    } else {
                        mailService.sendEmailWithFile(properties.getDefaultEmailTo(), properties.getDefaultEmailFrom(), sendEmailWithAttachmentEvent.getSubject(), emailRecipient.getFilePath(), sendEmailWithAttachmentEvent.getSubject(), false);
                    }

                }
                if (emailRecipient.getAttachmentReceiverType().equals(AttachmentReceiverType.OTHER)) {
                    mailService.sendEmailWithFile(emailRecipient.getReceiverShortName(), properties.getDefaultEmailFrom(), emailRecipient.getReceiverShortName() + "reconcile", emailRecipient.getFilePath(), emailRecipient.getReceiverShortName() + "reconcile", false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
