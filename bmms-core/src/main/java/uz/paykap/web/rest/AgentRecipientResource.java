package uz.paykap.web.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import springfox.documentation.annotations.ApiIgnore;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;
import uz.paykap.domain.Agent;
import uz.paykap.domain.Recipient;
import uz.paykap.repository.AgentRecipientRepository;
import uz.paykap.repository.AgentRepository;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.service.AgentRecipientQueryService;
import uz.paykap.service.AgentRecipientService;
import uz.paykap.service.criteria.AgentRecipientCriteria;
import uz.paykap.service.dto.AgentRecipientDTO;
import uz.paykap.web.rest.errors.BadRequestAlertException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * REST controller for managing {@link uz.paykap.domain.AgentRecipient}.
 */
@ApiIgnore
@RestController
@RequestMapping("/api")
public class AgentRecipientResource {

    private final Logger log = LoggerFactory.getLogger(AgentRecipientResource.class);

    private static final String ENTITY_NAME = "pkbillmsAgentRecipient";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AgentRecipientService agentRecipientService;

    private final AgentRecipientRepository agentRecipientRepository;

    private final AgentRecipientQueryService agentRecipientQueryService;

    private final AgentRepository agentRepository;
    private final RecipientRepository recipientRepository;

    public AgentRecipientResource(
        AgentRecipientService agentRecipientService,
        AgentRecipientQueryService agentRecipientQueryService,
        AgentRepository agentRepository,
        RecipientRepository recipientRepository,
        AgentRecipientRepository agentRecipientRepository
    ) {
        this.agentRecipientService = agentRecipientService;
        this.agentRecipientRepository = agentRecipientRepository;
        this.agentRecipientQueryService = agentRecipientQueryService;
        this.agentRepository = agentRepository;
        this.recipientRepository = recipientRepository;
    }

    /**
     * {@code POST  /agent-recipients} : Create a new agentRecipient.
     *
     * @param agentRecipientDTO the agentRecipientDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new agentRecipientDTO, or with status {@code 400 (Bad Request)} if the agentRecipient has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/agent-recipients")
    public ResponseEntity<AgentRecipientDTO> createAgentRecipient(@Valid @RequestBody AgentRecipientDTO agentRecipientDTO)
        throws URISyntaxException {
        log.debug("REST request to save AgentRecipient : {}", agentRecipientDTO);
        if (agentRecipientDTO.getId() != null) {
            throw new BadRequestAlertException("A new agentRecipient cannot already have an ID", ENTITY_NAME, "idexists");
        }
        AgentRecipientDTO result = agentRecipientService.save(agentRecipientDTO);
        return ResponseEntity
            .created(new URI("/api/agent-recipients/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /agent-recipients/:id} : Updates an existing agentRecipient.
     *
     * @param id the id of the agentRecipientDTO to save.
     * @param agentRecipientDTO the agentRecipientDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated agentRecipientDTO,
     * or with status {@code 400 (Bad Request)} if the agentRecipientDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the agentRecipientDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/agent-recipients/{id}")
    public ResponseEntity<AgentRecipientDTO> updateAgentRecipient(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody AgentRecipientDTO agentRecipientDTO
    ) throws URISyntaxException {
        log.debug("REST request to update AgentRecipient : {}, {}", id, agentRecipientDTO);
        if (agentRecipientDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, agentRecipientDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!agentRecipientRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        AgentRecipientDTO result = agentRecipientService.save(agentRecipientDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, agentRecipientDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /agent-recipients/:id} : Partial updates given fields of an existing agentRecipient, field will ignore if it is null
     *
     * @param id the id of the agentRecipientDTO to save.
     * @param agentRecipientDTO the agentRecipientDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated agentRecipientDTO,
     * or with status {@code 400 (Bad Request)} if the agentRecipientDTO is not valid,
     * or with status {@code 404 (Not Found)} if the agentRecipientDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the agentRecipientDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/agent-recipients/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<AgentRecipientDTO> partialUpdateAgentRecipient(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody AgentRecipientDTO agentRecipientDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update AgentRecipient partially : {}, {}", id, agentRecipientDTO);
        if (agentRecipientDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, agentRecipientDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!agentRecipientRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<AgentRecipientDTO> result = agentRecipientService.partialUpdate(agentRecipientDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, agentRecipientDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /agent-recipients} : get all the agentRecipients.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of agentRecipients in body.
     */
    @GetMapping("/agent-recipients")
    public ResponseEntity<List<AgentRecipientDTO>> getAllAgentRecipients(AgentRecipientCriteria criteria, Pageable pageable, Boolean unPaged) {
        log.debug("REST request to get AgentRecipients by criteria: {}", criteria);
        Page<AgentRecipientDTO> page = agentRecipientQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /agent-recipients/count} : count all the agentRecipients.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/agent-recipients/count")
    public ResponseEntity<Long> countAgentRecipients(AgentRecipientCriteria criteria) {
        log.debug("REST request to count AgentRecipients by criteria: {}", criteria);
        return ResponseEntity.ok().body(agentRecipientQueryService.countByCriteria(criteria));
    }

    @GetMapping("/agent-recipients/criteria")
    public ResponseEntity<List<AgentRecipientDTO>> getAllAgentRecipients(
        AgentRecipientCriteria criteria,
        Pageable pageable,
        String recipientName,
        String agentName,
        Boolean unPaged
    ) {
        log.debug("REST request to get AgentRecipients by criteria: {}", criteria);
        Agent agent = agentRepository.findTop1ByShortName(agentName).orElse(null);

        if (agent != null) {
            LongFilter longFilter = new LongFilter();
            longFilter.setEquals(agent.getId());
            criteria.setAgentId(longFilter);
        }

        Recipient recipient = recipientRepository.findTop1ByShortName(recipientName).orElse(null);

        if (recipient != null) {
            LongFilter longFilter = new LongFilter();
            longFilter.setEquals(recipient.getId());
            criteria.setRecipientId(longFilter);
        }

        Page<AgentRecipientDTO> page = agentRecipientQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * {@code GET  /agent-recipients/:id} : get the "id" agentRecipient.
     *
     * @param id the id of the agentRecipientDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the agentRecipientDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/agent-recipients/{id}")
    public ResponseEntity<AgentRecipientDTO> getAgentRecipient(@PathVariable Long id) {
        log.debug("REST request to get AgentRecipient : {}", id);
        Optional<AgentRecipientDTO> agentRecipientDTO = agentRecipientService.findOne(id);
        return ResponseUtil.wrapOrNotFound(agentRecipientDTO);
    }

    /**
     * {@code DELETE  /agent-recipients/:id} : delete the "id" agentRecipient.
     *
     * @param id the id of the agentRecipientDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/agent-recipients/{id}")
    public ResponseEntity<Void> deleteAgentRecipient(@PathVariable Long id) {
        log.debug("REST request to delete AgentRecipient : {}", id);
        agentRecipientService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    @PostMapping("/agent-recipients/group")
    public ResponseEntity<List<AgentRecipientDTO>> createByRecipientGroup(@RequestBody @Valid AgentRecipientDTO agentRecipientDTO) {
        log.debug("REST request to create AgentRecipients by recipient group : {}", agentRecipientDTO);
        return ResponseEntity.ok(agentRecipientService.createOneOrAllByGroup(agentRecipientDTO));
    }
}
