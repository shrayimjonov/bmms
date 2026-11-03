package uz.paykap.event;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import uz.paykap.event.dto.UzcardTerminalEvent;
import uz.paykap.service.uzcard.UzcardTerminalRegistrar;


@Slf4j
@Component
@AllArgsConstructor
public class UzcardTerminalEventListener implements ApplicationListener<UzcardTerminalEvent> {

    private final UzcardTerminalRegistrar uzcardTerminalRegistrar;

    @Override
    public void onApplicationEvent(UzcardTerminalEvent event) {
        if (event.isAdded()) {
            uzcardTerminalRegistrar.registerTerminal(event.getMerchantId(), event.getTerminalId(), event.getName(), event.getTerminalType());
        } else {
            uzcardTerminalRegistrar.removeTerminal(event.getMerchantId(), event.getTerminalId());
        }
    }
}
