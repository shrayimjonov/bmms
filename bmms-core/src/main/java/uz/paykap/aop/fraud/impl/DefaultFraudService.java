package uz.paykap.aop.fraud.impl;

import org.springframework.stereotype.Service;
import uz.devops.bmcore.dto.request.CheckRequest;
import uz.javlon.commons.base.ResultData;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.aop.fraud.FraudService;

import java.util.Objects;

@Service
//@ConditionalOnProperty(prefix = "application", name = "fraudEnabled", havingValue = "true")
public class DefaultFraudService implements FraudService {

    public static final String ACCOUNT_FOR_FAIL = "223344115566";

    public DefaultFraudService() {
        System.out.println();
    }

    @Override
    public ResultData<String> getFraudResponse(CheckRequest checkDTO) {
        if (Objects.equals(checkDTO.getAccount(), ACCOUNT_FOR_FAIL)) {
            return new ResultData<>(false, "Error", new CodedException(ProblemEnum.UNKNOWN_ERROR));
        }
        return new ResultData<>("Success");
    }
}
