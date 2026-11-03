package uz.paykap.service.dto.reconcile.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AgentReconcileStatus {
    SUCCESS(0, "Success"),
    PROCESSING(10, "Reconciliation is in processing"),
    FAILED(-100, "Reconciliation failed"),
    EMPTY_TRANSACTIONS(-101, "Transactions collection in request are empty"),
    PROCESS_BUSY(-102, "There is another reconciliation task in processing"),
    RECIPIENT_NOT_FOUND(-110, "Requested recipient not found"),
    GATEWAY_NOT_FOUND(-120, "Requested gateway not found"),
    RECONCILE_NOT_FOUND(-130, "Requested reconciliation not found"),
    AGENT_CREDENTIALS_ARE_EMPTY(-140, "Agent credentials are empty or null"),
    AGENT_GET_ERROR(-150, "Agent get error"),
    ;

    private final Integer code;
    private final String message;

    public Boolean isSuccess() {
        return this == SUCCESS;
    }
}
