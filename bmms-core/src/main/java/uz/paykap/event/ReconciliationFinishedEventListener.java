package uz.paykap.event;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import uz.devops.pusher.event.PusherEvent;
import uz.paykap.config.PusherConstants;
import uz.paykap.event.dto.ReconciliationFinishedEvent;
import uz.paykap.service.MailService;

import java.util.Collections;

@Slf4j
@Component
@AllArgsConstructor
public class ReconciliationFinishedEventListener implements ApplicationListener<ReconciliationFinishedEvent> {

    private final MailService mailService;
    private final ApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(ReconciliationFinishedEvent event) {

        if (event.getResultData().isSuccess()) {
            String message = "Сверка прошла УСПЕШНО с " + event.getRecipientName();
            mailService.sendReconciliationSuccessEmail(event.getRecipientName());
            applicationContext.publishEvent(new PusherEvent(this, Collections.singletonMap("message", message), PusherConstants.TAG_BM_PUSH));
        } else {
            mailService.sendReconciliationHasDifferEmail(event.getRecipientName(), event.getResultData().getData());
            String message = "Сверка НЕ прошла с " + event.getRecipientName();
            applicationContext.publishEvent(new PusherEvent(this, Collections.singletonMap("message", message), PusherConstants.TAG_BM_PUSH_WARN));
        }
    }
}
