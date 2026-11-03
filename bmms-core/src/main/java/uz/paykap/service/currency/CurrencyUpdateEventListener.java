package uz.paykap.service.currency;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class CurrencyUpdateEventListener implements ApplicationListener<CurrencyUpdateEvent> {

    private final CurrencyEventSender sender;

    @Override
    public void onApplicationEvent(@NonNull CurrencyUpdateEvent event) {
        log.debug("update currency, handle event: {}", event);
        sender.send(event);
    }
}
