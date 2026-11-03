package uz.paykap.web.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.devops.bmcore.dto.request.PayRequest;
import uz.devops.bmcore.dto.request.SharedCancelDTO;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.javlon.commons.base.SimpleResult;
import uz.javlon.commons.exception.CodedException;
import uz.paykap.agent.tran.CommonHoldTranService;
import uz.paykap.aop.globalSettings.PreHandleRequest;
import uz.paykap.security.AuthoritiesConstants;
import uz.paykap.service.hold.PartialReverseRequest;
import uz.paykap.web.rest.errors.BmTransactionException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static uz.paykap.utils.SimpleBillUtil.codedExceptionResponse;

@Api(value = "Ресурс холдирование")
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/agent/hold/v2")
@PreHandleRequest
public class SharedAgentHoldResource {

    private final CommonHoldTranService commonHoldTranService;

    @ApiOperation("Холдирование")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ROLE_BM_HOLD + "\")")
    @PostMapping("/")
    public ResponseEntity<SimpleBillResponse> holdForAgent(@Valid @RequestBody PayRequest request) {
        log.debug("REST request to hold : {}", request);
        return ResponseEntity.ok(commonHoldTranService.doHold(request));
    }

    @ApiOperation("Холдирование сверху")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ROLE_BM_OTP_HOLD + "\")")
    @PostMapping("/more")
    public ResponseEntity<SimpleBillResponse> holdMore(@Valid @RequestBody PayRequest request) {
        log.debug("REST request to hold more : {}", request);
        return ResponseEntity.ok(commonHoldTranService.reHold(request));
    }

    @ApiOperation("Холдирование с OTP")
    @PostMapping("/otp")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ROLE_BM_OTP_HOLD + "\")")
    public ResponseEntity<SimpleBillResponse> holdWithOtp(@Valid @RequestBody PayRequest request) {
        log.debug("REST request to hold with otp : {}", request);
        return ResponseEntity.ok(commonHoldTranService.holdWithOtp(request));
    }

    @ApiOperation("Подтверждений холдирование")
    @PostMapping("/confirm")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ROLE_BM_HOLD + "\")")
    public ResponseEntity<SimpleBillResponse> confirm(@Valid @RequestBody PayRequest request) {
        log.debug("REST request to hold-confirm : {}", request);
        return ResponseEntity.ok(commonHoldTranService.confirmHold(request));
    }

    @ApiOperation("Отмена холдирование")
    @PostMapping("/reverse")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ROLE_BM_HOLD + "\")")
    public ResponseEntity<SimpleResult> reverse(@Valid @RequestBody SharedCancelDTO request) {
        log.debug("REST request to hold-reverse : {}", request);
        return ResponseEntity.ok(commonHoldTranService.reverseHold(request));
    }

    @ApiOperation("Частичный Отмена холдирование")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ROLE_BM_HOLD + "\")")
    @PostMapping("/reverse/partial")
    public ResponseEntity<SimpleBillResponse> partialReverse(@Valid @RequestBody PartialReverseRequest request) {
        log.debug("REST request to partial-hold-reverse : {}", request);
        return ResponseEntity.ok(commonHoldTranService.partialReverse(request));
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
