package uz.paykap.web.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.paykap.service.AgentReconciliationService;
import uz.paykap.service.dto.reconcile.AgentReconcileRequest;
import uz.paykap.service.dto.reconcile.AgentReconcileResponse;
import uz.paykap.service.dto.reconcile.AgentReconcileStatusRequest;
import uz.paykap.service.dto.reconcile.AgentReconcileStatusResponse;
import uz.paykap.service.dto.reconcile.enumeration.AgentReconcileStatus;
import uz.paykap.service.reconciliation.AgentReconciliationServiceImpl;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * REST controller for managing agent reconcile.
 */
@Slf4j
@RestController
@RequestMapping("/api/reconcile/agent")
public class AgentReconcileResource {

    private final AgentReconciliationService<AgentReconcileRequest, Long> agentReconciliationService;

    public AgentReconcileResource(@Qualifier(AgentReconciliationServiceImpl.SERVICE_NAME) AgentReconciliationService<AgentReconcileRequest, Long> agentReconciliationService) {
        this.agentReconciliationService = agentReconciliationService;
    }

    @PostMapping("/run")
    public ResponseEntity<AgentReconcileResponse> runReconcile(@Valid @NotNull @RequestBody AgentReconcileRequest request) {
        log.info("Rest request to agent common reconciliation");

        var validationStatus = agentReconciliationService.validateCreationRequest(request);

        if (!validationStatus.isSuccess()) {
            return ResponseEntity.ok(getRunFailResponse(request, validationStatus));
        }

        var result = agentReconciliationService.createReconciliation(request.getRecipientId());

        if (!result.getStatus().isSuccess()) {
            return ResponseEntity.ok(getRunFailResponse(request, result.getStatus()));
        }

        agentReconciliationService.runReconcileAsync(result, request, "");

        return ResponseEntity.ok(new AgentReconcileResponse(result.getReconcileId(), request.getFromDate(), request.getToDate(),
                true, AgentReconcileStatus.PROCESSING.getCode(), AgentReconcileStatus.PROCESSING.getMessage()));
    }

    @PostMapping("/status")
    public ResponseEntity<AgentReconcileStatusResponse> reconcileStatus(@Valid @NotNull @RequestBody AgentReconcileStatusRequest request) {
        log.info("Rest request to agent reconciliation status");

        var result = agentReconciliationService.getStatus(request);
        return ResponseEntity.ok(result);
    }

    private AgentReconcileResponse getRunFailResponse(@RequestBody @Valid @NotNull AgentReconcileRequest request, AgentReconcileStatus status) {
        log.trace("getRunFailResponse started");
        return new AgentReconcileResponse(null, request.getFromDate(), request.getToDate(), status.isSuccess(), status.getCode(), status.getMessage());
    }
}
