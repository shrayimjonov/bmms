package uz.paykap.service.abs.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uz.paykap.service.abs.AbsSimulationService;
import uz.paykap.service.abs.AbsStatusService;
import uz.paykap.service.abs.request.AbsTransactionInfoRequest;
import uz.paykap.service.abs.request.AbsTransactionStatusRequest;
import uz.paykap.service.abs.response.AbsTransactionInfo;
import uz.paykap.service.abs.response.AbsTransactionStatusResult;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.result.BmResultData;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "application.abs", name = "serviceBeanName", havingValue = AbsSimulationService.BEAN_NAME)
public class AbsSimStatusServiceImpl implements AbsStatusService {
    @Override
    public BmResultData<AbsTransactionInfo> getTransactionStatus(AbsTransactionInfoRequest request) {
        return new BmResultData<>(new AbsTransactionInfo());
    }

    @Override
    public BmResultData<Boolean> checkBankOperDay() {
        return new BmResultData<>(true);
    }

    @Override
    public BmResultData<AbsTransactionStatusResult> getRequestStatusAndBody(AbsTransactionStatusRequest request) {
        AbsTransactionStatusResult result = new AbsTransactionStatusResult();
        result.setErrorCode("0");
        result.setErrorNote("SUCCESS");
        result.setOldErrorCode("0");
        result.setOldErrorNote("SUCCESS");
        result.setTransId("149540623");
        result.setTypeQuery(1);
        result.setDateTime("2022-04-15 12:20:54.0");
        return new BmResultData<>(BmResultStatus.SUCCESS, result);
    }
}
