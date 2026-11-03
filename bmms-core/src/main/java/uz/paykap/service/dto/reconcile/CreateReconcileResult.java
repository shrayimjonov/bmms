package uz.paykap.service.dto.reconcile;

import lombok.Value;
import uz.paykap.service.dto.reconcile.enumeration.AgentReconcileStatus;

@Value
public class CreateReconcileResult {
    Long reconcileId;
    Long gatewayId;
    String gateway;
    AgentReconcileStatus status;
}
