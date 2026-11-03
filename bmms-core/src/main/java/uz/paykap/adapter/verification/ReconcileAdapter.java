package uz.paykap.adapter.verification;

import uz.javlon.commons.base.ResultData;
import uz.paykap.adapter.verification.dto.ReconciliationRequestDTO;

public abstract class ReconcileAdapter {

    public abstract ResultData getReconcileList(ReconciliationRequestDTO reconciliationRequestDTO);


}
