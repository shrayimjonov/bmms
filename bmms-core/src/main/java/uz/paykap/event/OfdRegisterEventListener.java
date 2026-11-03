package uz.paykap.event;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uz.devops.ofd.service.dto.OfdRequestWrapper;
import uz.paykap.client.ofd.OfdClient;


@Slf4j
@Component
@AllArgsConstructor
@ConditionalOnProperty(prefix = "application", name = "ofd.enabled", havingValue = "true")
public class OfdRegisterEventListener {

    private final OfdClient ofdClient;

    @Async
    @EventListener
    public void onApplicationEvent(OfdRequestWrapper event) {
        try {
            log.debug("Ofd request : {}", event);
            var responseEntity = ofdClient.registerQr(new OfdRequestWrapper(event.getRequest(), event.getTemplate()));
            log.debug("Ofd response : {}", responseEntity.getBody());
        } catch (Exception e) {
            log.warn("Ofd template handle exception on message :{}", e.getMessage());
        }
    }
}
