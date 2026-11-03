package uz.paykap.event;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import uz.devops.pusher.event.PusherEvent;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.config.PusherConstants;
import uz.paykap.event.dto.PartnerBillingErrorEvent;

import java.util.Collections;

@Slf4j
@Component
@AllArgsConstructor
public class PartnerBillingErrorEventListener implements ApplicationListener<PartnerBillingErrorEvent> {

    private final ApplicationContext applicationContext;
    private final ApplicationProperties properties;

    @Override
    public void onApplicationEvent(PartnerBillingErrorEvent event) {

        String message = "WARNING : Error on " + event.getPartnerName() + "'s billing :" + (event.getRefNum() != null ? " refNum=" + event.getRefNum() : " :") + " " + event.getMessage();

        applicationContext.publishEvent(new PusherEvent(this, Collections.singletonMap("message", message), PusherConstants.TAG_BM_PUSH_WARN));
    }
}
