package uz.paykap.event;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import uz.javlon.pusher.service.PusherService;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.event.dto.SystemFailureEvent;
import uz.paykap.service.MailService;

@Slf4j
@Component
@AllArgsConstructor
@ConditionalOnBean(value = {PusherService.class})
public class FailureTransactionEventListener implements ApplicationListener<SystemFailureEvent> {

    private final PusherService pusherService;
    private final ApplicationProperties properties;
    private final MailService mailService;

    @Override
    public void onApplicationEvent(SystemFailureEvent event) {
        if (pusherService != null && properties.getPusher().getEnabled()) {
            pusherService.sendAsync(event.getErrorMessage(), event.getPushType(), event.getRecipient());
        } else {
            mailService.sendEmailWithFile(properties.getDefaultEmailTo(), properties.getDefaultEmailFrom(), "ИНЦИДЕНТ! Превышен лимит: не успешный Транзакции", "nullFile", event.getErrorMessage(), false);
        }
    }
}
