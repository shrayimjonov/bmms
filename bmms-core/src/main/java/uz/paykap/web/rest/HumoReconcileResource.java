package uz.paykap.web.rest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;
import uz.javlon.commons.result.CommonResult;
import uz.paykap.adapter.verification.HumoReconcileService;
import uz.paykap.service.dto.HumoReconcileTerminalRequest;

import javax.validation.Valid;

@Slf4j
@ApiIgnore
@RestController
@RequestMapping("/api/humo/reconcile")
@AllArgsConstructor
public class HumoReconcileResource {

    @PostMapping("/run/terminal")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<CommonResult> runByTerminalId(@Valid @RequestBody HumoReconcileTerminalRequest humoReconcileTerminalRequest) {
        log.info("Reconcile humo terminal: {}", humoReconcileTerminalRequest);

        return ResponseEntity.ok(humoReconcileService.reconcile(humoReconcileTerminalRequest.getTerminalId()));
    }

    private final HumoReconcileService humoReconcileService;

}
