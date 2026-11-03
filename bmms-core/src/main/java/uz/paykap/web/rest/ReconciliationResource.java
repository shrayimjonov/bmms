package uz.paykap.web.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;
import uz.javlon.commons.base.Result;
import uz.paykap.adapter.verification.ReconciliationServiceV3;
import uz.paykap.adapter.verification.dto.ReconciliationRequestDTO;
import uz.paykap.service.dto.ReconcileManualDTO;
import uz.paykap.utils.DateUtil;

import javax.validation.Valid;

import static uz.paykap.security.AuthoritiesConstants.ADMIN;

@Slf4j
@ApiIgnore
@Api(value = "Ресурс для сверки")
@RestController
@RequestMapping("/api/reconcile")
@AllArgsConstructor
public class ReconciliationResource {

    private final ReconciliationServiceV3 reconciliationServiceV3;

    @PreAuthorize("hasAnyRole('" + ADMIN + "')")
    @ApiOperation(value = "Инициация сверки", notes = "для админки")
    @PostMapping({"/manual/v3", "/manual"})
    public ResponseEntity<Result> reconcileRecipientV3(@Valid @RequestBody ReconcileManualDTO reconcileManualDTO) {
        log.debug("  : {}", reconcileManualDTO);

        ReconciliationRequestDTO reconciliationDTO = new ReconciliationRequestDTO();

        reconciliationDTO.setFromTime(DateUtil.dayStart(reconcileManualDTO.getLocalDate()));
        reconciliationDTO.setToTime(DateUtil.dayEnd(reconcileManualDTO.getLocalDate()));

        reconciliationDTO.setGatewayName(reconcileManualDTO.getGatewayService());

        return ResponseEntity.ok(reconciliationServiceV3.startReconcileByGateway(reconciliationDTO.getFromTime(), reconciliationDTO.getToTime()));
    }

}
