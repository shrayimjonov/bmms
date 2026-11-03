package uz.paykap.web.rest;

import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;
import uz.devops.munisagetdto.*;
import uz.paykap.service.munisWSDL.MunisAgentCommonService;

import javax.validation.Valid;

/**
 * REST controller for managing Agent.
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/munis")
//@Api(value = "Ресурс для агента Munis")
@ApiIgnore
public class MunisAgentResource {

    private final MunisAgentCommonService munisAgentCommonService;

    @ApiOperation(value = "Проверка счета перед оплатой", hidden = true)
    @PostMapping("/getPayee")
    public ResponseEntity<GetPayeeResponse> getPayee(@Valid @RequestBody GetPayee payeeRequest) {
        log.debug("REST request to getPayee : {}", payeeRequest);
        return ResponseEntity.ok(munisAgentCommonService.getPayee(payeeRequest));
    }

    @ApiOperation(value = "Проведение оплаты", hidden = true)
    @PostMapping("/transfer")
    public ResponseEntity<ReceiveTransferResponse> receiveTransfer(@Valid @RequestBody ReceiveTransfer transferRequest) {
        log.debug("REST request to receiveTransfer : {}", transferRequest);
        return ResponseEntity.ok(munisAgentCommonService.receiveTransfer(transferRequest));
    }

    @ApiOperation(value = "Отмена оплаты", hidden = true)
    @PostMapping("/cancel")
    public ResponseEntity<CancelTransferResponse> cancelTransfer(@Valid @RequestBody CancelTransfer cancelTransferRequest) {
        log.debug("REST request to cancelTransferRequest : {}", cancelTransferRequest);
        return ResponseEntity.ok(munisAgentCommonService.cancelTransfer(cancelTransferRequest));
    }
}
