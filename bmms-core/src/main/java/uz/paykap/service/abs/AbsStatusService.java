package uz.paykap.service.abs;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import uz.paykap.service.abs.request.AbsTransactionInfoRequest;
import uz.paykap.service.abs.request.AbsTransactionStatusRequest;
import uz.paykap.service.abs.response.AbsTransactionInfo;
import uz.paykap.service.abs.response.AbsTransactionStatusResult;
import uz.paykap.service.dto.result.BmResultData;

public interface AbsStatusService {
    BmResultData<AbsTransactionInfo> getTransactionStatus(AbsTransactionInfoRequest request);

    BmResultData<Boolean> checkBankOperDay( );

    BmResultData<AbsTransactionStatusResult> getRequestStatusAndBody(AbsTransactionStatusRequest request);
}
