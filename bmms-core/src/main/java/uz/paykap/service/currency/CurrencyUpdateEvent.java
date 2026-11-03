package uz.paykap.service.currency;

import org.springframework.context.ApplicationEvent;
import uz.paykap.service.dto.CurrencyDTO;

public class CurrencyUpdateEvent extends ApplicationEvent {

    private final CurrencyDTO currencyDTO;

    public CurrencyUpdateEvent(Object source, CurrencyDTO currencyDTO) {
        super(source);
        this.currencyDTO = currencyDTO;
    }

    public CurrencyDTO getCurrencyDTO() {
        return currencyDTO;
    }
}
