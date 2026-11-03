package uz.paykap.web.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.devops.bmcore.dto.request.AgentPayRequest;
import uz.devops.bmcore.dto.request.CancelRequest;
import uz.devops.bmcore.dto.request.CheckRequest;
import uz.devops.bmcore.dto.request.PayRequest;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.util.FileDTO;
import uz.devops.bmcore.util.SharedContentStoreService;
import uz.javlon.commons.base.ResultData;
import uz.javlon.commons.exception.CodedException;
import uz.paykap.agent.tran.CommonTranServiceV3;
import uz.paykap.aop.globalSettings.PreHandleRequest;
import uz.paykap.gateway.dto.AgentCheckRequestDTO;
import uz.paykap.gateway.dto.AgentPayRequestDTO;
import uz.paykap.gateway.dto.ReconcileDTO;
import uz.paykap.gateway.dto.SummaryReconcileResultDTO;
import uz.paykap.web.rest.errors.BmTransactionException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static uz.paykap.security.AuthoritiesConstants.*;
import static uz.paykap.utils.SimpleBillUtil.codedExceptionResponse;

/**
 * REST controller for managing Agent.
 */
@Api(value = "Ресурс для агента V3")
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/agent")
@PreHandleRequest
public class SharedAgentResourceV3 {

    private final CommonTranServiceV3 commonTranServiceV3;
    private final SharedContentStoreService sharedContentStoreService;

    //    @PreAuthorize("hasAnyRole('" + AGENT + "')")
    @ApiOperation("Проверка счета перед оплатой, создаст чек (V3)")
    @PostMapping("/check/v3")
    public ResponseEntity<SimpleBillResponse> checkV3(@Valid @RequestBody CheckRequest simple) {
        log.debug("REST request to check : {}", simple);
        return ResponseEntity.ok(commonTranServiceV3.check(simple));
    }

    @PreAuthorize("hasAnyRole('" + SUB_AGENT + "')")
    @ApiOperation("Проверка счета перед оплатой, создаст чек (V3)")
    @PostMapping("/check/sub")
    public ResponseEntity<SimpleBillResponse> checkV3(@Valid @RequestBody AgentCheckRequestDTO simple) {
        log.debug("REST request to check : {}", simple);
        return ResponseEntity.ok(commonTranServiceV3.check(simple, simple.getAgentName()));
    }

    //    @PreAuthorize("hasAnyRole('" + AGENT + "')")
    @ApiOperation("Оплата, подтверждение чека после проверки (V3)")
    @PostMapping("/pay/v3")
    public ResponseEntity<SimpleBillResponse> payV3(@Valid @RequestBody PayRequest simple) {
        log.debug("REST request to pay : {}", simple);
        return ResponseEntity.ok(commonTranServiceV3.pay(simple));
    }

    @PreAuthorize("hasAnyRole('" + SUB_AGENT + "')")
    @ApiOperation("Оплата, подтверждение чека после проверки (V3)")
    @PostMapping("/pay/sub")
    public ResponseEntity<SimpleBillResponse> payV3(@Valid @RequestBody AgentPayRequestDTO simple) {
        log.debug("REST request to pay : {}", simple);
        return ResponseEntity.ok(commonTranServiceV3.pay(simple, simple.getAgentName(), false));
    }

    @ApiOperation("Сверка платежей")
    @PostMapping("/reconcile/v3")
    public ResponseEntity<SummaryReconcileResultDTO> reconcile(@Valid @RequestBody ReconcileDTO reconcileDTO) {
        log.debug("REST request to reconcile : {}", reconcileDTO);
        return ResponseEntity.ok(commonTranServiceV3.reconcile(reconcileDTO));
    }

    //    @PreAuthorize("hasAnyRole('" + AGENT + "')")
    @ApiOperation("Проверка счета перед оплатой, создаст чек (V3)")
    @PostMapping("/check/v4")
    public ResponseEntity<SimpleBillResponse> checkV4(@Valid @RequestBody CheckRequest simple) {
        log.debug("REST request to check : {}", simple);
        return ResponseEntity.ok(commonTranServiceV3.checkV4(simple));
    }

    @ApiOperation("Оплата, подтверждение чека после проверки (V4), с помощью agent tran id")
    @PostMapping("/pay/v4")
    public ResponseEntity<SimpleBillResponse> payV4(@Valid @RequestBody AgentPayRequest agentPayRequest) {
        log.debug("REST request to pay(v4) : {}", agentPayRequest);

        return ResponseEntity.ok(commonTranServiceV3.pay(agentPayRequest));
    }

    @PreAuthorize("hasAnyRole('" + ROLE_CAN_CANCEL + "','" + ADMIN + "')")
    @ApiOperation("Отмена транзакции (V3)")
    @PostMapping("/cancel/v3")
    public ResponseEntity<SimpleBillResponse> cancelV3(@Valid @RequestBody CancelRequest cancelRequest) {
        log.debug("REST request to cancel : {}", cancelRequest);
        return ResponseEntity.ok(commonTranServiceV3.cancel(cancelRequest));
    }

    @PreAuthorize("hasAnyRole('" + ROLE_CAN_CANCEL + "','" + ADMIN + "')")
    @ApiOperation("Частичная отмена транзакции (V3)")
    @PostMapping("/partial/cancel/v3")
    public ResponseEntity<SimpleBillResponse> partialCancelV3(@Valid @RequestBody CancelRequest cancelRequest) {
        log.debug("REST request to cancel : {}", cancelRequest);
        return ResponseEntity.ok(commonTranServiceV3.cancel(cancelRequest));
    }

    @PreAuthorize("hasAnyRole('" + AGENT + "','" + ADMIN + "')")
    @ApiOperation(value = "Получение лого поставщика")
    @GetMapping("/file/{fileUuid}")
    public ResponseEntity<Resource> download(@PathVariable String fileUuid) {
        // Load file as Resource
        ResultData<FileDTO> resource = sharedContentStoreService.getFile(fileUuid);

        if (!resource.isSuccess()) {
            throw resource.getException();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(resource.getData().getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getData().getName() + "\"")
                .body(new ByteArrayResource(resource.getData().getContent()));
    }

    @Order(1)
    @ExceptionHandler({BmTransactionException.class})
    public ResponseEntity<SimpleBillResponse> handleException(HttpServletRequest request, BmTransactionException ex) {
        log.warn("Handle transaction exception: {}", ex.getLocalizedMessage());

        SimpleBillResponse result = new SimpleBillResponse()
                .id(ex.getTransaction().getId())
                .account(ex.getTransaction().getRecipientAccount())
                .amount(ex.getTransaction().getRequestAmount())
                .currency(ex.getTransaction().getRequestCcy())
                .status(ex.getTransaction().getStatus())
                .success(false)
                .amountRatio(ex.getTransaction().getAmountRatio())
                .responseText(ex.getMessage() != null ? ex.getMessage() : ex.getTransaction().getSenderStatusText() + ", " + ex.getTransaction().getRecipientStatusText())
                .responseTime(ex.getTransaction().getDate())
                .requestTime(ex.getTransaction().getDate());

        return ResponseEntity.ok(result);
    }

    @Order(2)
    @ExceptionHandler({CodedException.class})
    public ResponseEntity<SimpleBillResponse> handleException(HttpServletRequest request, CodedException ex) {
        log.warn("Handle coded exception: {}", ex.getLocalizedMessage());

        return ResponseEntity.ok(codedExceptionResponse(ex));
    }

}
