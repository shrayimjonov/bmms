package uz.paykap.aop.fraud;

import uz.devops.bmcore.dto.request.CheckRequest;
import uz.javlon.commons.base.ResultData;

public interface FraudService {

    ResultData<String> getFraudResponse(CheckRequest checkDTO);

}
