package uz.paykap.web.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
import uz.devops.bmcore.dto.request.PayRequest;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.javlon.commons.base.ResultData;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.domain.Transaction;
import uz.paykap.security.AuthoritiesConstants;
import uz.paykap.service.OverpayService;
import uz.paykap.service.dto.OverpayResult;
import uz.paykap.service.dto.PaymentEditDTO;
import uz.paykap.service.dto.TransactionDTO;
import uz.paykap.service.impl.OverpayServiceV2;

import javax.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.Map;


@ApiIgnore
@Api(value = "Ресурс для создания платежа со стороны Админки")
@RestController
@RequestMapping("/api/overpay")
public class OverpayResource {

    private final Logger log = LoggerFactory.getLogger(OverpayResource.class);

    private final OverpayService overpayService;
    private final OverpayServiceV2 overpayServiceV2;

    public OverpayResource(OverpayService overpayService, OverpayServiceV2 overpayServiceV2) {
        this.overpayService = overpayService;
        this.overpayServiceV2 = overpayServiceV2;
    }

    @ApiOperation("Проверка оплатой, и произведение оплаты на стороне поставщика")
    @RequestMapping(value = "/pay_gateway", method = RequestMethod.POST)
    public ResponseEntity<ResultData<List<TransactionDTO>>> payGateway(@Valid @RequestBody List<Long> tranIds, String login) {
        log.debug("REST request to pay from UI with transaction id : {}", tranIds);
        return ResponseEntity.ok(overpayService.payGateway(tranIds, login));
    }

    @ApiOperation("Редактирование платежа на стороне поставщика ")
    @PostMapping(value = "/edit_pay")
    public ResponseEntity<TransactionDTO> editPay(@Valid @RequestBody PaymentEditDTO paymentEditDTO) {
        log.debug("REST request to edit in gateway from UI with data: {}", paymentEditDTO);

        return ResponseEntity.ok(overpayServiceV2.edit(paymentEditDTO));
    }

    @ApiOperation("Установка суммы и типа оплаты на стороне поставщика ")
    @PostMapping(value = "/set_amount")
    public ResponseEntity<TransactionDTO> setAmount(@Valid @RequestBody PaymentEditDTO paymentEditDTO) {
        log.debug("REST request to set amount in gateway from UI with data: {}", paymentEditDTO);

        return ResponseEntity.ok(overpayService.enterAmount(paymentEditDTO));
    }

    @ApiOperation("Создание платежа , при успешнем чеке ")
    @PostMapping(value = "/create_pay")
    public ResponseEntity<TransactionDTO> createPay(@Valid @RequestBody PayRequest payDTO) {
        log.debug("REST request to create pay in gateway from UI with data: {}", payDTO);
        return ResponseEntity.ok(overpayService.createPay(payDTO));
    }

    @ApiOperation("Отмена платежа на стороне поставщика ")
    @GetMapping(value = "/cancel_gateway/{tranId}")
    public ResponseEntity<ResultData<OverpayResult>> cancelGateway(@PathVariable Long tranId) {
        log.debug("REST request to cancel in gateway from UI with transaction id: {}", tranId);
        return ResponseEntity.ok(overpayService.cancelGateway(tranId));
    }

    @ApiOperation("Снятие с карты суммы ")
    @GetMapping(value = "/pay_uzcard/{tranId}")
    public ResponseEntity<ResultData<OverpayResult>> payUzcard(@PathVariable Long tranId) {
        log.debug("REST request to pay : {}", tranId);

        return ResponseEntity.ok(overpayService.payUzcard(tranId));
    }

    @ApiOperation("Отмена платежа на стороне узкарда ")
    @GetMapping(value = "/cancel_agent/{tranId}")
    public ResponseEntity<ResultData<OverpayResult>> cancelUzcard(@PathVariable Long tranId) {
        log.debug("REST request to cancel in uzcard with transaction id: {}", tranId);
        return ResponseEntity.ok(overpayService.cancelSender(tranId));
    }

    @ApiOperation("Доставка данных на эмаил")
    @GetMapping(value = "/send/{tranId}")
    public ResponseEntity<Boolean> sendToEmail(@PathVariable Long tranId) {
        log.debug("REST request to send to email transaction id: {}", tranId);
        return ResponseEntity.ok(overpayService.sendCancelData(tranId));
    }

    @ApiOperation("Получение список транзакции")
    @PostMapping(value = "/getTransactions")
    public ResponseEntity<List<Transaction>> findAllTransactionWithBillStatus(@RequestBody Map<String, Instant> dateRange) {
        log.debug("REST request to get transactions by criteria, {}", dateRange);
        if (dateRange == null || dateRange.get("fromDate") == null || dateRange.get("toDate") == null) {
            log.error("Invalid params");
            throw new CodedException(ProblemEnum.INVALID_PARAMS);
        }
        Instant fromDate = dateRange.get("fromDate");
        Instant toDate = dateRange.get("toDate");
        return ResponseEntity.ok(overpayService.getTransactionListByDate(fromDate, toDate));
    }

    @ApiOperation("Проведение транзакции")
    @PreAuthorize("hasAnyRole('" + AuthoritiesConstants.ADMIN + "')")
    @PostMapping(value = "/doTransaction")
    public ResponseEntity<SimpleBillResponse> doNewTransactionBill(@RequestBody PayRequest payDTO) {
        log.debug("REST request for do new transaction bill");
        if (payDTO == null || payDTO.getBillId() == null) {
            log.error("Params is null");
            throw new CodedException(ProblemEnum.INVALID_PARAMS);
        }
        SimpleBillResponse result = overpayService.doNewPayBill(payDTO.getBillId());
        return ResponseEntity.ok(result);
    }

    @ApiOperation("Отмена платежа на стороне биллинга ")
    @GetMapping(value = "/cancel_inner/{tranId}")
    public ResponseEntity<TransactionDTO> cancelInner(@PathVariable Long tranId) {
        log.debug("REST request to cancel in gateway from UI with transaction id: {}", tranId);
        return ResponseEntity.ok(overpayService.innerCancel(tranId));
    }

    @ApiOperation("Только проверяет статус трансакции в системе поставщика")
    @GetMapping(value = "/get-status/{tranId}")
    public ResponseEntity<ResultData<OverpayResult>> getStatus(@PathVariable Long tranId) {
        log.debug("REST request to pay : {}", tranId);

        return ResponseEntity.ok(overpayService.checkStatusFromGateway(tranId));
    }

    @ApiOperation("Только проверяет статус трансакции в системе поставщика")
    @GetMapping(value = "/sync-status/{tranId}")
    public ResponseEntity<ResultData<OverpayResult>> syncStatus(@PathVariable Long tranId) {
        log.debug("REST request to pay : {}", tranId);

        return ResponseEntity.ok(overpayService.syncStatusFromGateway(tranId));
    }
}
