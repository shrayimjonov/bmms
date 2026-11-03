package uz.paykap.web.rest;

import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import springfox.documentation.annotations.ApiIgnore;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;
import uz.javlon.commons.base.Result;
import uz.paykap.domain.Agent;
import uz.paykap.domain.Gateway;
import uz.paykap.domain.enumeration.DepositState;
import uz.paykap.repository.AgentRepository;
import uz.paykap.repository.DepositRepository;
import uz.paykap.repository.GatewayRepository;
import uz.paykap.service.DepositQueryService;
import uz.paykap.service.DepositService;
import uz.paykap.service.criteria.DepositCriteria;
import uz.paykap.service.dto.DepositConfigDTO;
import uz.paykap.service.dto.DepositDTO;
import uz.paykap.service.dto.ReplenishDepositDTO;
import uz.paykap.web.rest.errors.BadRequestAlertException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static uz.paykap.security.AuthoritiesConstants.ADMIN;
import static uz.paykap.security.AuthoritiesConstants.REPLENISH;

/**
 * REST controller for managing {@link uz.paykap.domain.Deposit}.
 */
@ApiIgnore
@RestController
@RequestMapping("/api")
public class DepositResource {

    private final Logger log = LoggerFactory.getLogger(DepositResource.class);

    private static final String ENTITY_NAME = "pkbillmsDeposit";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final DepositService depositService;

    private final DepositRepository depositRepository;

    private final DepositQueryService depositQueryService;
    private final GatewayRepository gatewayRepository;
    private final AgentRepository agentRepository;

    public DepositResource(
        DepositService depositService,
        DepositQueryService depositQueryService,
        GatewayRepository gatewayRepository,
        AgentRepository agentRepository,
        DepositRepository depositRepository
    ) {
        this.depositService = depositService;
        this.depositRepository = depositRepository;
        this.depositQueryService = depositQueryService;
        this.gatewayRepository = gatewayRepository;
        this.agentRepository = agentRepository;
    }

    /**
     * {@code POST  /deposits} : Create a new deposit.
     *
     * @param depositDTO the depositDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new depositDTO, or with status {@code 400 (Bad Request)} if the deposit has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/deposits")
    public ResponseEntity<DepositDTO> createDeposit(@Valid @RequestBody DepositDTO depositDTO) throws URISyntaxException {
        log.debug("REST request to save Deposit : {}", depositDTO);
        if (depositDTO.getId() != null) {
            throw new BadRequestAlertException("A new deposit cannot already have an ID", ENTITY_NAME, "idexists");
        }
        depositDTO.setState(DepositState.NEW);
        DepositDTO result = depositService.save(depositDTO);
        return ResponseEntity
            .created(new URI("/api/deposits/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /deposits/:id} : Updates an existing deposit.
     *
     * @param id         the id of the depositDTO to save.
     * @param depositDTO the depositDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated depositDTO,
     * or with status {@code 400 (Bad Request)} if the depositDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the depositDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/deposits/{id}")
    public ResponseEntity<DepositDTO> updateDeposit(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody DepositDTO depositDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Deposit : {}, {}", id, depositDTO);
        if (depositDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, depositDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!depositRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        DepositDTO result = depositService.save(depositDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, depositDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /deposits/:id} : Partial updates given fields of an existing deposit, field will ignore if it is null
     *
     * @param id         the id of the depositDTO to save.
     * @param depositDTO the depositDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated depositDTO,
     * or with status {@code 400 (Bad Request)} if the depositDTO is not valid,
     * or with status {@code 404 (Not Found)} if the depositDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the depositDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/deposits/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<DepositDTO> partialUpdateDeposit(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody DepositDTO depositDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Deposit partially : {}, {}", id, depositDTO);
        if (depositDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, depositDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!depositRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<DepositDTO> result = depositService.partialUpdate(depositDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, depositDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /deposits} : get all the deposits.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of deposits in body.
     */
    @GetMapping("/deposits")
    public ResponseEntity<List<DepositDTO>> getAllDeposits(DepositCriteria criteria, Pageable pageable, Boolean unPaged) {
        log.debug("REST request to get Deposits by criteria: {}", criteria);
        Page<DepositDTO> page = depositQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/deposits/criteria")
    public ResponseEntity<List<DepositDTO>> getAllDeposits(
        DepositCriteria criteria,
        Pageable pageable,
        String gatewayName,
        String agentShortName
    ) {
        log.debug("REST request to get Deposits by criteria: {}", criteria);
        log.debug("With gateway name :{}", gatewayName);
        log.debug("With agent name :{}", agentShortName);

        //TODO - Fix/Remove customization
        Gateway gateway = gatewayRepository.findTop1ByName(gatewayName).orElse(null);
        if (gateway != null) {
            LongFilter longFilter = new LongFilter();
            longFilter.setEquals(gateway.getId());
            criteria.setAgentId(longFilter);
        }

        //TODO - Fix/Remove customization
        Agent agent = agentRepository.findTop1ByShortName(agentShortName).orElse(null);
        if (agent != null) {
            LongFilter longFilter = new LongFilter();
            longFilter.setEquals(agent.getId());
            criteria.setAgentId(longFilter);
        }

        Page<DepositDTO> page = depositQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = uz.paykap.web.rest.util.PaginationUtil.generatePaginationHttpHeaders(page, "/api/deposits");
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /deposits/count} : count all the deposits.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/deposits/count")
    public ResponseEntity<Long> countDeposits(DepositCriteria criteria) {
        log.debug("REST request to count Deposits by criteria: {}", criteria);
        return ResponseEntity.ok().body(depositQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /deposits/:id} : get the "id" deposit.
     *
     * @param id the id of the depositDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the depositDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/deposits/{id}")
    public ResponseEntity<DepositDTO> getDeposit(@PathVariable Long id) {
        log.debug("REST request to get Deposit : {}", id);
        Optional<DepositDTO> depositDTO = depositService.findOne(id);
        return ResponseUtil.wrapOrNotFound(depositDTO);
    }

    @PreAuthorize("hasAnyRole('" + ADMIN + "','" + REPLENISH + "')")
    @ApiOperation("Пополнение депосита агента")
    @PostMapping("/deposits/replenish")
    public ResponseEntity<Result> replenish(@Valid @RequestBody ReplenishDepositDTO replenishDepositDTO) {
        log.debug("REST request to replenish Deposit : {}", replenishDepositDTO);
        return ResponseEntity.ok(depositService.processEmission(replenishDepositDTO));
    }

    @PreAuthorize("hasAnyRole('" + ADMIN + "','" + REPLENISH + "')")
    @ApiOperation("Настроыка депосита агента")
    @PostMapping("/deposits/configure")
    public ResponseEntity<Result> configureDepositSettings(@Valid @RequestBody DepositConfigDTO depositConfigDTO) {
        log.debug("REST request to configure Deposit : {}", depositConfigDTO);
        return ResponseEntity.ok(depositService.configureDepositSettings(depositConfigDTO));
    }

    /**
     * {@code DELETE  /deposits/:id} : delete the "id" deposit.
     *
     * @param id the id of the depositDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/deposits/{id}")
    public ResponseEntity<Void> deleteDeposit(@PathVariable Long id) {
        log.debug("REST request to delete Deposit : {}", id);
        depositService.softDelete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code DELETE  /deposits/:id} : delete the "id" deposit.
     *
     * @param id the id of the depositDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/deposits/hard/{id}")
    public ResponseEntity<Void> hardDeleteDeposit(@PathVariable Long id) {
        log.debug("REST request to hard delete Deposit : {}", id);
        depositService.delete(id);
        return ResponseEntity
                .noContent()
                .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                .build();
    }
}
