package uz.paykap.event;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import uz.devops.pusher.event.PusherEvent;
import uz.paykap.config.PusherConstants;
import uz.paykap.event.dto.ErrorModifyDepositEvent;

import java.util.Collections;

@Slf4j
@Component
@AllArgsConstructor
public class ErrorModifyDepositListener implements ApplicationListener<ErrorModifyDepositEvent> {

//    private final PusherService pusherService;
//    private final ApplicationProperties properties;
    private final ApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(ErrorModifyDepositEvent event) {
//        if (pusherService != null && properties.getPusher().getEnabled()) {
            String message = "Error occurred while modifying agent deposit : " + event.getAgent() + " deposit : " + event.getDeposit();
//            pusherService.sendAsync(message, PushType.GROUP, "deposits", "deposits." + event.getAgent());
            applicationContext.publishEvent(new PusherEvent(this, Collections.singletonMap("message", message), PusherConstants.TAG_BM_PUSH_WARN));
//        }
    }
}
