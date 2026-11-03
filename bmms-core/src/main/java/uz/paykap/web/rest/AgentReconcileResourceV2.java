package uz.paykap.web.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.paykap.service.AgentReconciliationService;
import uz.paykap.service.dto.reconcile.AgentReconcileRequestV2;
import uz.paykap.service.dto.reconcile.AgentReconcileResponse;
import uz.paykap.service.dto.reconcile.AgentReconcileStatusRequest;
import uz.paykap.service.dto.reconcile.AgentReconcileStatusResponse;
import uz.paykap.service.dto.reconcile.enumeration.AgentReconcileStatus;
import uz.paykap.service.reconciliation.AgentReconciliationServiceImplV2;
import uz.paykap.utils.AgentReconcileV2Util;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * REST controller for managing agent reconcile.
 */
@Slf4j
@RestController
@RequestMapping("/api/reconcile/agent/v2")
public class AgentReconcileResourceV2 {

    private final AgentReconciliationService<AgentReconcileRequestV2, String> reconcileService;
    private final AgentReconcileV2Util util;

    public AgentReconcileResourceV2(@Qualifier(AgentReconciliationServiceImplV2.SERVICE_NAME) AgentReconciliationService<AgentReconcileRequestV2, String> reconcileService, AgentReconcileV2Util util) {
        this.reconcileService = reconcileService;
        this.util = util;
    }

    @PostMapping("/run")
    public ResponseEntity<AgentReconcileResponse> runReconcile(@Valid @NotNull @RequestBody AgentReconcileRequestV2 request) {
        log.info("Rest request to agent common reconciliation");

        var validationStatus = reconcileService.validateCreationRequest(request);

        if (!validationStatus.isSuccess()) {
            log.debug("reconcile request validation error occurred : {}", validationStatus);
            return ResponseEntity.ok(util.getRunFailResponse(request, validationStatus));
        }

        var login = util.getLogin();

        var result = reconcileService.createReconciliation(
                util.getGatewayName(login, request.getGroup(), request.getSubgroup()));

        if (!result.getStatus().isSuccess()) {
            log.debug("reconcile creation error occurred : {}", result);
            return ResponseEntity.ok(util.getRunFailResponse(request, result.getStatus()));
        }

        reconcileService.runReconcileAsync(result, request, login);

        return ResponseEntity.ok(new AgentReconcileResponse(result.getReconcileId(), request.getFromDate(), request.getToDate(),
                true, AgentReconcileStatus.PROCESSING.getCode(), AgentReconcileStatus.PROCESSING.getMessage()));
    }

    @PostMapping("/status")
    public ResponseEntity<AgentReconcileStatusResponse> reconcileStatus(@Valid @NotNull @RequestBody AgentReconcileStatusRequest request) {
        log.info("Rest request to agent reconciliation status");
        return ResponseEntity.ok(reconcileService.getStatus(request));
    }
}
