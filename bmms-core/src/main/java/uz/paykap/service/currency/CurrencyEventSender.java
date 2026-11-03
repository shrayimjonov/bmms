package uz.paykap.service.currency;

public interface CurrencyEventSender {
    void send(CurrencyUpdateEvent currencyUpdateEvent);
}
