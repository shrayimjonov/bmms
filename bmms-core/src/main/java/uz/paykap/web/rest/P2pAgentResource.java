package uz.paykap.web.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.devops.bmcore.util.FileStorageService;
import uz.javlon.commons.exception.CodedException;
import uz.paykap.agent.tran.CommonTranServiceV3;
import uz.paykap.aop.globalSettings.PreHandleRequest;
import uz.paykap.config.Constants;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.service.P2pCheckRequestDecoratorService;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.p2p.P2pCancelRequest;
import uz.paykap.service.dto.p2p.P2pPayRequest;
import uz.paykap.service.dto.p2p.P2pRequestScopedContext;
import uz.paykap.service.dto.p2p.calc.P2pCalcRequest;
import uz.paykap.service.dto.p2p.calc.P2pCalcResponse;
import uz.paykap.service.dto.result.BmResult;
import uz.paykap.service.p2p.dto.response.P2pParticipantDto;
import uz.paykap.service.p2p.dto.response.P2pResponse;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static uz.paykap.security.AuthoritiesConstants.ADMIN;
import static uz.paykap.security.AuthoritiesConstants.ROLE_CAN_CANCEL;

/**
 * REST controller for managing Agent.
 */
@Slf4j
@AllArgsConstructor
@Api(value = "Ресурс для операций P2P", tags = {"uzcard"})
@SwaggerDefinition(tags = {
        @Tag(name = "uzcard", description = "Uzcard test pans: <b>VALID_PAN</b> - 1111111111111111, NOT_FOUND_PAN - 1234567890123457, SMS_OFF_PAN - 1234567890123456, NOT_CARD_OWNER_PAN - 1212121212121212 and other prod cards"
                + "\nHumo test tokens: <b>VALID_TOKEN</b> - 986009ROAZEP6488"
                + "\nHumo test pans: <b>VALID_PAN</b> - 9860090000000000"
                + "\nNCI test accounts: <b>VALID</b> - 22616000990000582001"
        )
})
@RestController
@RequestMapping("/api/agent/p2p")
@PreHandleRequest
public class P2pAgentResource {

    @ApiOperation("Подсчет комиссии для P2P операции")
    @PostMapping("/calc")
    public ResponseEntity<P2pCalcResponse> getCalc(@Valid @RequestBody P2pCalcRequest calcRequest) {
        log.debug("REST request to get p2p calcRequest : {}", calcRequest);
        return ResponseEntity.ok(p2pCheckRequestDecoratorService.calcCommission(calcRequest));
    }

    @ApiOperation("Проверка возможности перевода P2P")
    @PostMapping("/info")
    public ResponseEntity<P2pResponse> getInfo(@Valid @RequestBody P2pPayRequest info) {
        log.debug("REST request to get p2p info : {}", info);
        return ResponseEntity.ok(p2pCheckRequestDecoratorService.p2pInfo(info));
    }

    @ApiOperation("Перевод P2P")
    @PostMapping("/pay")
    public ResponseEntity<P2pResponse> pay(@Valid @RequestBody P2pPayRequest check) {
        log.debug("REST request to pay p2p : {}", check);
        P2pResponse p2pResponse = p2pCheckRequestDecoratorService.p2pPay(check);
        log.debug("Returning p2p pay response : {}", p2pResponse);
        return ResponseEntity.ok(p2pResponse);
    }

    @ApiOperation("Отмена P2P операции")
    @PreAuthorize("hasAnyRole('" + ROLE_CAN_CANCEL + "','" + ADMIN + "')")
    @PostMapping("/cancel")
    public ResponseEntity<P2pResponse> cancel(@Valid @RequestBody P2pCancelRequest cancel) {
        log.debug("REST request to cancel p2p : {}", cancel);
        final P2pResponse p2pResponse = p2pCheckRequestDecoratorService.p2pCancel(cancel);
        log.debug("Returning p2p cancel response : {}", p2pResponse);
        return ResponseEntity.ok(p2pResponse);
    }

//    @ApiOperation("Оплата, подтверждение чека после проверки")
//    @PostMapping("/pay")
//    @Timed
//    public ResponseEntity<SimpleBillResponse> pay(@Valid @RequestBody P2pPayRequest simple) {
//        log.debug("REST request to pay : {}", simple);
//        return ResponseEntity.ok(p2pCheckRequestDecoratorService.payP2p(simple));
//    }

    @ApiOperation(value = "Получение лого поставщика, формат fileName : 'r'+recipient.id+'.png'", hidden = true)
    @GetMapping("/logo/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            log.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @ApiOperation("Список P2P ошибок")
    @PostMapping("/errors")
    public ResponseEntity<List<BmResultStatus>> errors() {
        log.debug("REST request to get list of errors");

        return ResponseEntity.ok(Arrays.asList(BmResultStatus.values()));
    }

    @Order(1)
    @ExceptionHandler({CodedException.class})
    public ResponseEntity<P2pResponse> handleException(HttpServletRequest request, CodedException ex) {
        log.warn("Handle coded exception: {}", ex.getLocalizedMessage());

        P2pResponse response = p2pRequestScopedContext.getResponse();

        if (response == null) {
            response = new P2pResponse()
                    .fromBase(p2pRequestScopedContext.getRequest())
                    .withSender(new P2pParticipantDto(p2pRequestScopedContext.getSenderDto().getAccount(), p2pRequestScopedContext.getRequest().getAmount(), 0L, Constants.DEFAULT_CURRENCY_CODE, new HashMap<>(0)))
                    .withRecipient(new P2pParticipantDto(p2pRequestScopedContext.getRecipientDto().getAccount(), p2pRequestScopedContext.getRequest().getAmount(), 0L, Constants.DEFAULT_CURRENCY_CODE, new HashMap<>(0)))
                    .withStatusCode(new BmResult(BmResultStatus.UNKNOWN_ERROR, "from global p2p error handler"))
//                .withSuccess(false)
                    .withStatusMessage(ex.getLocalizedMessage())
            //
            ;
        }

        return ResponseEntity.ok(response);
    }

    private final FileStorageService fileStorageService;
    private final RecipientRepository recipientRepository;
    private final CommonTranServiceV3 commonTranService;
    private final P2pRequestScopedContext p2pRequestScopedContext;
    private final P2pCheckRequestDecoratorService p2pCheckRequestDecoratorService;

}
