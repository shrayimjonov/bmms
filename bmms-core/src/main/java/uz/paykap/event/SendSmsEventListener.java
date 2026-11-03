package uz.paykap.event;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import uz.javlon.play.sms.service.PlaySmsService;
import uz.paykap.event.dto.SendSmsEvent;


@Slf4j
@Component
@AllArgsConstructor
@ConditionalOnBean(value = {PlaySmsService.class})
public class SendSmsEventListener implements ApplicationListener<SendSmsEvent> {

    private final PlaySmsService playSmsService;

    @Override
    public void onApplicationEvent(SendSmsEvent event) {
        playSmsService.send(event.getContent(), event.getSendTo());
    }
}
