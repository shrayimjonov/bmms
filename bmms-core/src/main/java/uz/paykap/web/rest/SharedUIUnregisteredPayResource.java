package uz.paykap.web.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
import tech.jhipster.web.util.ResponseUtil;
import uz.devops.bmcore.dto.request.CheckRequest;
import uz.devops.bmcore.dto.request.PayRequest;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.PayType;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.agent.tran.CommonTranServiceV3;
import uz.paykap.service.RecipientService;
import uz.paykap.service.dto.RecipientDTO;

import javax.validation.Valid;
import java.util.Optional;

//import uz.javlon.svgate.repository.BankRepository;

/**
 * REST controller for managing Agent.
 */
@Slf4j
@ApiIgnore
@Api(value = "Ресурс для UI", tags = {"Ресурс для UI"}, description = "1. Первым вызывается /unregistered-check с подходяшими полями\n2. Вызывается /unregistered-pay с billId (из ответа в пред. запроса), тип оплаты (PayType) и Номер карты (счета) плательщика")
@SwaggerDefinition(tags = {
    @Tag(name = "Ресурс для UI", description = "1. Первым вызывается /unregistered-check с подходяшими полями\n2. Вызывается /unregistered-pay с billId (из ответа в пред. запроса), тип оплаты (PayType) и Номер карты (счета) плательщика")
})
@RestController
@AllArgsConstructor
@RequestMapping("/api/ui")
public class SharedUIUnregisteredPayResource {

    public static final String UNREGISTERED_AGENT_LOGIN = "unregisteredAgentLogin";
    public static final String UNREGISTERED_AGENT_PASSWORD = "unregisteredAgentPassword";

    @ApiOperation("Проверка счета перед оплатой, создаст чек")
    @PostMapping("/unregistered-check")
    public ResponseEntity<SimpleBillResponse> unregisteredCheck(@Valid @RequestBody CheckRequest simple) {
        log.debug("REST request to unregisteredUiCheck : {}", simple);

        UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(UNREGISTERED_AGENT_LOGIN, UNREGISTERED_AGENT_PASSWORD);
        SecurityContextHolder.getContext().setAuthentication(authReq);
        return ResponseEntity.ok(commonTranServiceV3.unregisteredCheck(simple));
    }

    @ApiOperation("Оплата без авторизации, подтверждение чека после проверки")
    @PostMapping("/unregistered-pay")
    public ResponseEntity<SimpleBillResponse> unregisteredPay(@Valid @RequestBody PayRequest simple) {
        log.debug("REST request to unregisteredUiPay : {}", simple);

        if (simple.getType() != PayType.PC) {
            log.warn("Pay type should be PC");
            throw new CodedException(ProblemEnum.INVALID_PARAMS, "TYPE SHOULD BE PC");
        }
        UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(UNREGISTERED_AGENT_LOGIN, UNREGISTERED_AGENT_PASSWORD);
        SecurityContextHolder.getContext().setAuthentication(authReq);
        return ResponseEntity.ok(commonTranServiceV3.unregisteredPay(simple));
    }

    /**
     * {@code GET  /recipients/:id} : get the "id" recipient.
     *
     * @param id the id of the recipientDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the recipientDTO, or with status {@code 404 (Not Found)}.
     */
    @ApiOperation("Получение информация по поставщикам услуг")
    @GetMapping("/recipients/{id}")
    public ResponseEntity<RecipientDTO> getRecipient(@PathVariable Long id) {
        log.debug("REST request to get Recipient : {}", id);
        Optional<RecipientDTO> recipientDTO = recipientService.findOne(id);
        return ResponseUtil.wrapOrNotFound(recipientDTO);
    }

    private final CommonTranServiceV3 commonTranServiceV3;
    private final RecipientService recipientService;
}
