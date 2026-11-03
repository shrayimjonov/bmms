package uz.paykap.event;

import java.util.Collections;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import uz.devops.pusher.event.PusherEvent;
import uz.paykap.config.PusherConstants;
import uz.paykap.event.dto.AgentDepositModifiedEvent;

@Slf4j
@Component
@AllArgsConstructor
public class AgentDepositEventListener implements ApplicationListener<AgentDepositModifiedEvent> {

    //    private final PusherService pusherService;
    //    private final ApplicationProperties properties;
    private final ApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(AgentDepositModifiedEvent event) {
        //        if (pusherService != null && properties.getPusher().getEnabled()) {
        String message = "Deposit modified : " + event.getAgent() + " from " + event.getDepositBefore() + " to " + event.getDepositAfter();
        //            pusherService.sendAsync(message, PushType.GROUP, "deposits", "deposits." + event.getAgent());
        applicationContext.publishEvent(new PusherEvent(this, Collections.singletonMap("message", message), PusherConstants.TAG_BM_PUSH));
        //        }
    }
}
