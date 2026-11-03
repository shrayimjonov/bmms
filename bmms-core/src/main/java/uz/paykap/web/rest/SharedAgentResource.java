package uz.paykap.web.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.devops.bmcore.dto.request.CheckRequest;
import uz.devops.bmcore.dto.request.PayRequest;
import uz.devops.bmcore.dto.request.StatusRequest;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.util.FileStorageService;
import uz.javlon.commons.base.ResultData;
import uz.javlon.commons.exception.ProblemEnum;
import uz.javlon.svgate.repository.BankRepository;
import uz.paykap.agent.tran.CommonTranServiceV3;
import uz.paykap.aop.globalSettings.EnableCheck;
import uz.paykap.aop.globalSettings.EnablePay;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.gateway.uzcard.UzcardDebitGatewayService;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.security.SecurityUtils;
import uz.paykap.service.AllowedAgentService;
import uz.paykap.service.DepositService;
import uz.paykap.service.dto.SimpleBankDTO;
import uz.paykap.service.dto.SimpleDepositDTO;
import uz.paykap.service.dto.simple.SimpleRecipientDTO;
import uz.paykap.service.mapper.BankMapper;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static uz.paykap.security.AuthoritiesConstants.ADMIN;
import static uz.paykap.security.AuthoritiesConstants.AGENT;

/**
 * REST controller for managing Agent.
 */
//@ApiIgnore
@Slf4j
@Api(
    value = "Ресурс для агента",
    tags = { "Ресурс для агента" },
    description = "1. Первым получаете список доступных поставщиков /recipients \n2. Вызывается /check/v2 с подходящими полями\n3. Вызывается /pay/v2 с billId из ответа в пред. запрос"
)
@SwaggerDefinition(
    tags = {
        @Tag(
            name = "Ресурс для агента",
            description = "1. Первым получаете список доступных поставщиков /recipients \n2. Вызывается /check/v2 с подходящими полями\n3. Вызывается /pay/v2 с billId из ответа в пред. запрос"
        ),
    }
)
@RestController
@AllArgsConstructor
@RequestMapping("/api/agent")
public class SharedAgentResource {

    @PreAuthorize("hasAnyRole('" + AGENT + "')")
    @ApiOperation("Получение всех доступных категорий, получателей и их полей")
    @GetMapping("/recipients")
    public ResponseEntity<String> getAllRecipients() {
        Optional<String> login = SecurityUtils.getCurrentUserLogin();

        if (login.isPresent()) {
            log.debug("REST request to get all recipients by agent : {}", login.get());
            return ResponseEntity.ok(allowedAgentService.getAllByLoginAsString(login.get()));
        }

        return ResponseEntity.ok("{}");
    }

    @PreAuthorize("hasAnyRole('" + AGENT + "')")
    @ApiOperation("Получение всех доступных категорий, получателей и их полей")
    @GetMapping("/recipients/{shortname}")
    public ResponseEntity<String> getAllRecipients(@PathVariable String shortname) {

        if (!shortname.isEmpty()) {
            log.info("REST request to get all recipients by agent : {}", shortname);
            return ResponseEntity.ok(allowedAgentService.getAllByLoginAsString(shortname));
        }

        return ResponseEntity.ok("{}");
    }

    @PreAuthorize("hasAnyRole('" + AGENT + "')")
    @ApiOperation("Получение услуги и полей")
    @GetMapping("/recipients/get-by-id/{id}")
    public ResponseEntity<String> getAllRecipientById(@PathVariable Long id) {
        log.debug("REST request to get recipient and fields by agent");
        return ResponseEntity.ok(allowedAgentService.getRecipientInfoByLoginAndIdAsString(id));
    }

    @PreAuthorize("hasAnyRole('" + AGENT + "','ROLE_RECIPIENT_READ')")
    @ApiOperation("Получение услуги по ИНН")
    @GetMapping("/recipient/inn/{inn}")
    public ResponseEntity<ResultData<List<SimpleRecipientDTO>>> getRecipientByInn(@PathVariable("inn") String inn) {
        log.debug("REST request to getRecipientsByInn : {}", inn);
        return ResponseEntity.ok(allowedAgentService.getRecipientsByInn(inn));
    }

    //    @PreAuthorize("hasAnyRole('" + ADMIN + "')")
    //    @ApiOperation("Проверка счета, без какого либо привязки к следуюшим действиям")
    //    @PostMapping("/get-info")
    //    @Timed
    //    public ResponseEntity<GetInfoResponse> getInfo(@Valid @RequestBody GetInfoRequest infoRequest) {
    //        log.debug("REST request to get info: {}", infoRequest);
    //        return ResponseEntity.ok(commonTranServiceV2.getInfo(infoRequest));
    //    }

    @PreAuthorize("hasAnyRole('" + AGENT + "')")
    @ApiOperation("Проверка счета перед оплатой, создаст чек (V2)")
    @PostMapping("/check/v2")
    @EnableCheck
    public ResponseEntity<SimpleBillResponse> checkV2(@Valid @RequestBody CheckRequest simple) {
        log.debug("REST request to check : {}", simple);
        return ResponseEntity.ok(commonTranServiceV3.check(simple));
    }

    @PreAuthorize("hasAnyRole('" + AGENT + "')")
    @ApiOperation("Оплата, подтверждение чека после проверки (V2)")
    @PostMapping("/pay/v2")
    @EnablePay
    public ResponseEntity<SimpleBillResponse> payV2(@Valid @RequestBody PayRequest simple) {
        log.debug("REST request to pay : {}", simple);
        return ResponseEntity.ok(commonTranServiceV3.pay(simple));
    }

    //TODO - impl
    //    @PreAuthorize("hasAnyRole('" + AGENT + "')")
    //    @ApiOperation(value = "Проведения платежа без чека", notes = "Оплата производится независимо от предварительного чека")
    //    @PostMapping("/pay/independent")
    //    @Timed
    //    public ResponseEntity<SimpleBillResponse> payIndependent(@Validated(SimpleCheckDTO.RequireAmount.class) @RequestBody IndependentPayDTO payDTO) {
    //        log.debug("REST request to pay : {}", payDTO);
    //        return ResponseEntity.ok(commonTranServiceV2.pay(payDTO));
    //    }

    @PreAuthorize("hasAnyRole('" + AGENT + "')")
    @ApiOperation("Баланс агента")
    @PostMapping("/deposit/agent")
    public ResponseEntity<SimpleDepositDTO> agentDeposit() {
        log.debug("REST request to get agent deposit");
        return ResponseEntity.ok(depositService.getAgentDeposit());
    }

    //    @PreAuthorize("hasAnyRole('" + ADMIN + "')")
    @ApiOperation("Баланс агента 2")
    @GetMapping("/deposit/agent/{shortName}")
    public ResponseEntity<SimpleDepositDTO> agentDeposit(@PathVariable("shortName") String shortname) {
        log.debug("REST request to get agent by shortname: {}", shortname);
        return ResponseEntity.ok(depositService.getAgentDeposit(shortname));
    }

    //    @ApiOperation(value = "Проверка номера карты для снятие средств", notes = "Создается чек и отправляется код подтверждение")
    //    @PostMapping("/ub/check")
    //    @Timed
    //    public ResponseEntity<SimpleBillDTO> checkUniversalBank(@Valid @RequestBody UbCheck check) {
    //        log.debug("REST request to check by name : {}", check);
    //
    //        SimpleCheckDTO simpleCheckDTO = new SimpleCheckDTO();
    //        simpleCheckDTO.setRecipientId(recipientRepository.findTop1ByShortName(UniversalBankGatewayService.SHORT_NAME).orElseThrow(() -> new CodedException(ProblemEnum.SERVICE_NOT_FOUND)).getId());
    //        simpleCheckDTO.setAccount(check.getPan());
    //        simpleCheckDTO.setCurrency(check.getCurrency());
    //
    //        Map<String,String> map = new HashMap<>(1);
    //        map.put(Constants.EXPIRY_PARAM, check.getExpDate());
    //
    //        simpleCheckDTO.setParams(map);
    //
    //        return ResponseEntity.ok(universalBankGatewayService.check(simpleCheckDTO));
    //    }

    /* UZCARD SERVICE */

    @PreAuthorize("hasAnyRole('" + AGENT + "')")
    @ApiOperation("Проверка счета перед оплатой, создаст чек")
    @PostMapping("/check")
    @EnableCheck
    public ResponseEntity<SimpleBillResponse> check(@Valid @RequestBody CheckRequest simple) {
        log.debug("REST request to check : {}", simple);
        return ResponseEntity.ok(commonTranServiceV3.check(simple));
    }

    @PreAuthorize("hasAnyRole('" + AGENT + "')")
    @ApiOperation("Оплата, подтверждение чека после проверки")
    @PostMapping("/pay")
    @EnablePay
    public ResponseEntity<SimpleBillResponse> pay(@Valid @RequestBody PayRequest simple) {
        log.debug("REST request to pay : {}", simple);
        return ResponseEntity.ok(commonTranServiceV3.pay(simple));
    }

    @PreAuthorize("hasAnyRole('" + AGENT + "')")
    @ApiOperation("Статус платежа")
    @PostMapping("/status")
    public ResponseEntity<SimpleBillResponse> status(@Valid @RequestBody StatusRequest statusDTO) {
        log.debug("REST request to get status : {}", statusDTO);
        return ResponseEntity.ok(commonTranServiceV3.status(statusDTO));
    }

    @PreAuthorize("hasAnyRole('" + AGENT + "')")
    @ApiOperation("Поддерживаемые банки")
    @GetMapping("/supported-banks")
    public ResponseEntity<List<SimpleBankDTO>> supportedBanks() {
        log.debug("REST request to get supported banks ");
        //todo use login  ? ? ?
        return ResponseEntity.ok(bankMapper.toDto(bankRepository.findAll()));
    }

    //TODO - impl in V3
    //    @PreAuthorize("hasAnyRole('" + AGENT + "')")
    //    @ApiOperation("Краткий отчет за переод")
    //    @PostMapping("/reconcile")
    //    @Timed
    //    public ResponseEntity<SummaryReconcileResultDTO> reconcile(@Valid @RequestBody ReconcileDTO reconcileDTO) {
    //        log.debug("REST request to get reconcile data : {}", reconcileDTO);
    //        return ResponseEntity.ok(commonTranServiceV2.reconcile(reconcileDTO));
    //    }

    @PreAuthorize("hasAnyRole('" + AGENT + "')")
    @ApiOperation("Статусы платежей")
    @PostMapping("/statuses")
    public ResponseEntity<List<SimpleBillResponse>> statuses(@Valid @RequestBody List<StatusRequest> statusDTOs) {
        log.debug("REST request to get statuses : {}", statusDTOs);
        if (properties.getMultiStatusListSize() != null && properties.getMultiStatusListSize() > 0) {
            statusDTOs = statusDTOs.subList(0, properties.getMultiStatusListSize());
        }
        return ResponseEntity.ok(commonTranServiceV3.getStatusList(statusDTOs));
    }

    @PreAuthorize("hasAnyRole('" + AGENT + "','" + ADMIN + "')")
    @ApiOperation(value = "Получение лого поставщика")
    @GetMapping("/logo/{recipientId}")
    public ResponseEntity<Resource> download(@PathVariable Long recipientId, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource("r" + recipientId + ".png");

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

        return ResponseEntity
            .ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
            .body(resource);
    }

    @PreAuthorize("hasAnyRole('" + AGENT + "')")
    @ApiOperation("Список ошибок")
    @PostMapping({ "/errors", "/errors/v3" })
    public ResponseEntity<List<ProblemEnum>> errors() {
        log.debug("REST request to get list of errors");

        return ResponseEntity.ok(Arrays.asList(ProblemEnum.values()));
    }

    private final AllowedAgentService allowedAgentService;
    private final FileStorageService fileStorageService;
    private final CommonTranServiceV3 commonTranServiceV3;
    private final DepositService depositService;
    private final RecipientRepository recipientRepository;
    private final UzcardDebitGatewayService uzcardDebitGatewayService;
    private final ApplicationProperties properties;
    private final BankRepository bankRepository;
    private final BankMapper bankMapper;
}
