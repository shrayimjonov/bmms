package uz.paykap.service;

import org.springframework.validation.annotation.Validated;
import uz.paykap.service.dto.reconcile.AgentReconcileStatusRequest;
import uz.paykap.service.dto.reconcile.AgentReconcileStatusResponse;
import uz.paykap.service.dto.reconcile.CreateReconcileResult;
import uz.paykap.service.dto.reconcile.enumeration.AgentReconcileStatus;

import javax.validation.constraints.NotNull;

@Validated
public interface AgentReconciliationService<TRecoRequest, TUniqueRecoKey> {
    AgentReconcileStatus validateCreationRequest(TRecoRequest request);

    CreateReconcileResult createReconciliation(@NotNull TUniqueRecoKey key);

    void runReconcileAsync(CreateReconcileResult result, TRecoRequest request, String login);

    AgentReconcileStatusResponse getStatus(@NotNull AgentReconcileStatusRequest request);
}
