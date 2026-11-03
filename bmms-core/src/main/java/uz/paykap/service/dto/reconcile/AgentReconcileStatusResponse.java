package uz.paykap.service.dto.reconcile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentReconcileStatusResponse {
    private Set<SimpleReconcileTransaction> billTrans;
    private Set<SimpleReconcileTransaction> agentTrans;
    private Boolean success;
    private Integer statusCode;
    private String statusMessage;
}
