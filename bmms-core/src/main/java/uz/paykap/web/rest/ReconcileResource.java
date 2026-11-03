package uz.paykap.web.rest;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.javlon.commons.base.BasicResult;
import uz.paykap.adapter.verification.AutomaticReconcileService;
import uz.paykap.recipient.p2p.reconciliation.HumoP2pReconciliationService;
import uz.paykap.recipient.p2p.reconciliation.UzCardP2pReconciliationService;
import uz.paykap.service.dto.reconcile.p2p.P2pReconcileDTO;

/**
 * REST controller for managing reconcile.
 */

@RestController
@AllArgsConstructor
@RequestMapping("/api/reconcile")
@ConditionalOnProperty(prefix = "application.reconciliation", name = "enableAuto", havingValue = "true")
public class ReconcileResource {

    private final Logger log = LoggerFactory.getLogger(ReconcileResource.class);

    private final AutomaticReconcileService automaticReconcileService;

    private final HumoP2pReconciliationService humoP2pReconciliationService;
    private final UzCardP2pReconciliationService uzCardP2pReconciliationService;

    @GetMapping("/humo/run")
    public ResponseEntity<Boolean> runHumoReconcile() {
        log.info("Rest request to run humo reconcile");
        automaticReconcileService.startHumoReconciliation();

        return ResponseEntity.ok(true);
    }

    @PostMapping("/humo/p2p/run")
    public ResponseEntity<Boolean> runHumoP2pReconcile(@RequestBody P2pReconcileDTO p2pReconcileDTO) {
        log.info("REST request to humo-p2p-reconcile: {}", p2pReconcileDTO);
        BasicResult reconcile = humoP2pReconciliationService.reconcile(p2pReconcileDTO.getFromDate(), p2pReconcileDTO.getToDate());
        return ResponseEntity.ok(reconcile.isSuccess());
    }

    @PostMapping("/uzcard/p2p/run")
    public ResponseEntity<Boolean> runUzCardP2pReconcile(@RequestBody P2pReconcileDTO p2pReconcileDTO) {
        log.info("REST request to uzcard-p2p-reconcile: {}", p2pReconcileDTO);
        BasicResult reconcile = uzCardP2pReconciliationService.reconcile(p2pReconcileDTO.getFromDate(), p2pReconcileDTO.getToDate());
        return ResponseEntity.ok(reconcile.isSuccess());
    }
}
