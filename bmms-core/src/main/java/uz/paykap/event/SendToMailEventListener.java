package uz.paykap.event;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import uz.devops.pusher.event.PusherEvent;
import uz.paykap.config.PusherConstants;
import uz.paykap.event.dto.SendToMailEvent;

import java.util.Collections;

@Slf4j
@Component
@AllArgsConstructor
public class SendToMailEventListener implements ApplicationListener<SendToMailEvent> {

    private final ApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(SendToMailEvent event) {

        String message = "Send to Email  : " + event.getSendTo() + " with subject " + event.getSubject();

        log.debug(message + " with content " + event.getContent());

        applicationContext.publishEvent(new PusherEvent(this, Collections.singletonMap("message", message), PusherConstants.TAG_BM_PUSH));
    }
}
