package uz.paykap.adapter.verification;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.javlon.commons.result.CommonResult;
import uz.paykap.gateway.humo.helperService.HumoDebitReconcileServiceV2;

@Slf4j
@Service(HumoReconcileService.NAME)
@AllArgsConstructor
public class HumoReconcileService {

    public static final String NAME = "humoReconcileService";

    private final HumoDebitReconcileServiceV2 debitReconcileServiceV2;

    public CommonResult reconcile(String terminalId) {
        return debitReconcileServiceV2.reconcile(terminalId);
    }
}
