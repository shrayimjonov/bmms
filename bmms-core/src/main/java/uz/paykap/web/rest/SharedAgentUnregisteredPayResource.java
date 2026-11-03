package uz.paykap.web.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
import uz.devops.bmcore.dto.request.CheckRequest;
import uz.devops.bmcore.dto.request.PayRequest;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.javlon.commons.exception.CodedException;
import uz.paykap.agent.tran.CommonTranServiceV3;
import uz.paykap.service.dto.HoldPayRequest;
import uz.paykap.web.rest.errors.BmTransactionException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static uz.paykap.utils.SimpleBillUtil.codedExceptionResponse;

//import uz.javlon.svgate.repository.BankRepository;

/**
 * REST controller for managing Agent.
 */
@Slf4j
@ApiIgnore
@Api(value = "Ресурс для агента", tags = {"Ресурс для агента"}, description = "1. Первым вызывается /unregistered-check с подходяшими полями\n2. Вызывается /unregistered-pay с billId (из ответа в пред. запроса), тип оплаты (PayType) и Номер карты (счета) плательщика")
@SwaggerDefinition(tags = {
    @Tag(name = "Ресурс для агента", description = "1. Первым вызывается /unregistered-check с подходяшими полями\n2. Вызывается /unregistered-pay с billId (из ответа в пред. запроса), тип оплаты (PayType) и Номер карты (счета) плательщика")
})
@RestController
@AllArgsConstructor
@RequestMapping("/api/agent")
public class SharedAgentUnregisteredPayResource {

    private final CommonTranServiceV3 commonTranServiceV3;
    @ApiOperation("Проверка счета перед оплатой, создаст чек")
    @PostMapping("/unregistered-check")
    public ResponseEntity<SimpleBillResponse> unregisteredCheck(@Valid @RequestBody CheckRequest simple) {
        log.debug("REST request to unregisteredCheck : {}", simple);
        return ResponseEntity.ok(commonTranServiceV3.unregisteredCheck(simple));
    }

    @ApiOperation("Оплата без авторизации, подтверждение чека после проверки")
    @PostMapping("/unregistered-pay")
    public ResponseEntity<SimpleBillResponse> unregisteredPay(@Valid @RequestBody PayRequest simple) {
        log.debug("REST request to unregisteredPay : {}", simple);
        return ResponseEntity.ok(commonTranServiceV3.unregisteredPay(simple));
    }

    @ApiOperation("Холдирование, холдирование чека после проверки (V3)")
    @PostMapping("/hold")
    public ResponseEntity<SimpleBillResponse> holdPaymentWithOtp(@Valid @RequestBody HoldPayRequest simple) {
        log.debug("REST request to hold: {}", simple);
        return ResponseEntity.ok(commonTranServiceV3.holdPaymentWithOtp(simple));
    }

    @ApiOperation("Холдирование, холдирование чека после проверки (V3)")
    @PostMapping("/hold/without-otp")
    public ResponseEntity<SimpleBillResponse> holdPayment(@Valid @RequestBody HoldPayRequest simple) {
        log.debug("REST request to hold: {}", simple);
        return ResponseEntity.ok(commonTranServiceV3.holdPayment(simple));
    }

    @ApiOperation("Подтверждение оплаты после холдирование (V3)")
    @PostMapping("/confirm")
    public ResponseEntity<SimpleBillResponse> confirmPayment(@Valid @RequestBody PayRequest request) {
        log.debug("REST request to confirm : {}", request);
        return ResponseEntity.ok(commonTranServiceV3.confirmPayment(request));
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
