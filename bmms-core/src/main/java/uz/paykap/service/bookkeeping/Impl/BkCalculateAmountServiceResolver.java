package uz.paykap.service.bookkeeping.Impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.domain.BookkeepingTemplate;
import uz.paykap.gateway.dto.BktCost;
import uz.paykap.service.bookkeeping.BkCalculateAmountService;

import java.time.Instant;

@Service
public class BkCalculateAmountServiceResolver extends BkCalculateAmountService {
    private final static Logger log = LoggerFactory.getLogger(BkCalculateAmountServiceResolver.class);

    @Override
    public BktCost getTranList(BookkeepingTemplate bookkeepingTemplate, Instant instantFrom, Instant instantTo) {
        log.debug("Request to get transactions !\n, bkTemplate: {} \n, from: {}\n, to: {}\n", bookkeepingTemplate, instantFrom, instantTo);

        switch (bookkeepingTemplate.getAmountCalculateType()) {
            case GATEWAY:
                return bkGatewayCalculateAmountService.getTranList(bookkeepingTemplate, instantFrom, instantTo);
            case PAY_TYPE:
                return bkPayTypeCalculateAmountService.getTranList(bookkeepingTemplate, instantFrom, instantTo);
            case RECIPIENT:
                return bkRecipientCalculateAmountService.getTranList(bookkeepingTemplate, instantFrom, instantTo);
            case SUB_RECIPIENT:
                return bkSubRecipientCalculateAmountService.getTranList(bookkeepingTemplate, instantFrom, instantTo);
            default: {
                throw new CodedException(ProblemEnum.NOT_IMPLEMENTED);
            }
        }

    }

    private final BkGatewayCalculateAmountService bkGatewayCalculateAmountService;
    private final BkPayTypeCalculateAmountService bkPayTypeCalculateAmountService;
    private final BkRecipientCalculateAmountService bkRecipientCalculateAmountService;
    private final BkSubRecipientCalculateAmountService bkSubRecipientCalculateAmountService;

    public BkCalculateAmountServiceResolver(BkGatewayCalculateAmountService bkGatewayCalculateAmountService, BkPayTypeCalculateAmountService bkPayTypeCalculateAmountService, BkRecipientCalculateAmountService bkRecipientCalculateAmountService, BkSubRecipientCalculateAmountService bkSubRecipientCalculateAmountService) {
        this.bkGatewayCalculateAmountService = bkGatewayCalculateAmountService;
        this.bkPayTypeCalculateAmountService = bkPayTypeCalculateAmountService;
        this.bkRecipientCalculateAmountService = bkRecipientCalculateAmountService;
        this.bkSubRecipientCalculateAmountService = bkSubRecipientCalculateAmountService;
    }
}
