package uz.paykap.gateway.uzcard;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uz.javlon.commons.base.ResultData;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.config.ApplicationProperties;

@Service
public class UzcardSenderCardResolver {

    private final ApplicationProperties applicationProperties;

    public UzcardSenderCardResolver(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public ResultData<String> resolveSenderToken(String recipientCardPan) {

        if (recipientCardPan == null) {
            return new ResultData<>(new CodedException(ProblemEnum.INVALID_PARAMS, "pan is null"));
        }

        String cardBin; // substring containing first 4 characters

        if (recipientCardPan.length() > 4) {
            cardBin = StringUtils.substring(recipientCardPan, 0, 4);
        } else {
            cardBin = recipientCardPan;
        }

        if (!applicationProperties.getUzcardTokenMap().containsKey(cardBin)) {
            return new ResultData<>(new CodedException(ProblemEnum.BANK_NOT_SUPPORTED, "uzcard sender token not found"));
        }

        return new ResultData<>(applicationProperties.getUzcardTokenMap().get(cardBin));
    }
}
