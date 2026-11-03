package uz.paykap.web.rest;

import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
import uz.devops.bmcore.dto.response.DepositBalanceResponse;
import uz.devops.bmcore.enumeration.Lang;
import uz.devops.bmcore.gateway.GatewayBillingService;
import uz.javlon.commons.result.CommonResultData;
import uz.paykap.event.dto.ReindexCachesEvent;
import uz.paykap.gateway.GatewayServiceResolver;
import uz.paykap.security.AuthoritiesConstants;
import uz.paykap.service.dto.I18nDTO;
import uz.paykap.web.rest.errors.BadRequestAlertException;
import uz.paykap.web.rest.util.HeaderUtil;

/**
 * REST controller for managing Bill.
 */
@ApiIgnore
@RestController
@RequestMapping("/api")
public class AdminResource {

    private final Logger log = LoggerFactory.getLogger(AdminResource.class);

    private final ApplicationContext applicationContext;
    private final ApplicationEventPublisher publisher;
    private final GatewayServiceResolver gatewayServiceResolver;

    public AdminResource(
        ApplicationContext applicationContext,
        ApplicationEventPublisher publisher,
        GatewayServiceResolver gatewayServiceResolver
    ) {
        this.applicationContext = applicationContext;
        this.publisher = publisher;
        this.gatewayServiceResolver = gatewayServiceResolver;
    }

    @PreAuthorize("hasAnyRole('" + AuthoritiesConstants.ADMIN + "')")
    @GetMapping("/spring-bean-names/list")
    public ResponseEntity<String[]> getListOfAdapters() {
        log.debug("REST request to get list of Adapters");
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createAlert("adapter.getListOfAdapters", ""))
            .body(applicationContext.getBeanNamesForType(GatewayBillingService.class));
    }

    @GetMapping("/reindex/caches")
    public ResponseEntity<CommonResultData<List<I18nDTO>>> reindexCaches() {
        log.debug("REST request to reindex caches by admin");
        publisher.publishEvent(new ReindexCachesEvent(this, "REST request to reindex caches by admin"));
        return ResponseEntity.ok(
            new CommonResultData<>(
                Arrays.asList(
                    new I18nDTO(Lang.EN, "Cache successfully cleared"),
                    new I18nDTO(Lang.RU, "Кеш успешно очищен"),
                    new I18nDTO(Lang.UZ, "Kesh muvaffaqiyatli tozalandi")
                )
            )
        );
    }

    @PreAuthorize("hasAnyRole('" + AuthoritiesConstants.ADMIN + "," + AuthoritiesConstants.VIEW_RECIPIENT_DEPOSIT + "')")
    @PostMapping("/admin/deposit")
    public ResponseEntity<DepositBalanceResponse> getDeposit(@RequestBody String gatewayName) {
        log.debug("REST request to get recipient deposit by admin");
        if (gatewayName == null || gatewayName.isEmpty()) {
            throw new BadRequestAlertException("RequestParam gatewayName is null", "GATEWAY_NAME", "idexists");
        }

        DepositBalanceResponse balanceDTO = gatewayServiceResolver.deposit(gatewayName);

        return ResponseEntity.ok().headers(HeaderUtil.createAlert("Recipient Deposit is success", "")).body(balanceDTO);
    }
}
