package uz.paykap.service.abs.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uz.paykap.service.abs.AbsIsApiService;
import uz.paykap.service.abs.AbsStatusService;
import uz.paykap.service.abs.request.AbsTransactionInfoRequest;
import uz.paykap.service.abs.request.AbsTransactionStatusRequest;
import uz.paykap.service.abs.response.AbsTransactionInfo;
import uz.paykap.service.abs.response.AbsTransactionStatusResult;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.result.BmResultData;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "application.abs", name = "serviceBeanName", havingValue = AbsIsApiService.BEAN_NAME)
public class DavrAbsStatusServiceImpl implements AbsStatusService {
    @Override
    public BmResultData<AbsTransactionInfo> getTransactionStatus(AbsTransactionInfoRequest request) {
        return new BmResultData<>(BmResultStatus.NOT_IMPLEMENTED);
    }

    @Override
    public BmResultData<Boolean> checkBankOperDay() {
        return new BmResultData<>(BmResultStatus.NOT_IMPLEMENTED);
    }

    @Override
    public BmResultData<AbsTransactionStatusResult> getRequestStatusAndBody(AbsTransactionStatusRequest request) {
        return new BmResultData<>(BmResultStatus.NOT_IMPLEMENTED);
    }
}
