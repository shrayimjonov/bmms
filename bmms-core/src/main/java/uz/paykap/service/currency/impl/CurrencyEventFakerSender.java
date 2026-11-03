package uz.paykap.service.currency.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uz.paykap.service.currency.CurrencyEventSender;
import uz.paykap.service.currency.CurrencyUpdateEvent;

@Slf4j
@Service("currencyEventSender")
@ConditionalOnProperty(
    prefix = "application.currencyEventSenderProperties",
    name = "currencySenderSimulate",
    havingValue = "true",
    matchIfMissing = true
)
public class CurrencyEventFakerSender implements CurrencyEventSender {

    @Override
    public void send(CurrencyUpdateEvent currencyUpdateEvent) {
        log.info("send simulated currency update event to rabbitMQ!!!");
    }
}
