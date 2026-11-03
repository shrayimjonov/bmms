package uz.paykap.service.bookkeeping;

import uz.paykap.domain.BookkeepingTemplate;
import uz.paykap.gateway.dto.BktCost;

import java.time.Instant;

public abstract class BkCalculateAmountService {
    public abstract BktCost getTranList(BookkeepingTemplate bookkeepingTemplate, Instant instantFrom, Instant instantTo);
}
