package uz.paykap.web.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;
import uz.javlon.commons.base.Result;
import uz.paykap.domain.Category;
import uz.paykap.domain.Gateway;
import uz.paykap.domain.SimpleRecipient;
import uz.paykap.repository.CategoryRepository;
import uz.paykap.repository.GatewayRepository;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.service.RecipientQueryService;
import uz.paykap.service.RecipientService;
import uz.paykap.service.criteria.RecipientCriteria;
import uz.paykap.service.dto.InnInfo;
import uz.paykap.service.dto.KeyWithNames;
import uz.paykap.service.dto.RecipientDTO;
import uz.paykap.web.rest.errors.BadRequestAlertException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.websocket.server.PathParam;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * REST controller for managing {@link uz.paykap.domain.Recipient}.
 */
@RestController
@RequestMapping("/api")
public class RecipientResource {

    private final Logger log = LoggerFactory.getLogger(RecipientResource.class);

    private static final String ENTITY_NAME = "pkbillmsRecipient";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final RecipientService recipientService;

    private final RecipientRepository recipientRepository;

    private final RecipientQueryService recipientQueryService;

    private final CategoryRepository categoryRepository;

    private final GatewayRepository gatewayRepository;

    public RecipientResource(
        RecipientService recipientService,
        RecipientRepository recipientRepository,
        RecipientQueryService recipientQueryService,
        CategoryRepository categoryRepository,
        GatewayRepository gatewayRepository
    ) {
        this.recipientService = recipientService;
        this.recipientRepository = recipientRepository;
        this.recipientQueryService = recipientQueryService;
        this.categoryRepository = categoryRepository;
        this.gatewayRepository = gatewayRepository;
    }

    /**
     * {@code POST  /recipients} : Create a new recipient.
     *
     * @param recipientDTO the recipientDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new recipientDTO, or with status {@code 400 (Bad Request)} if the recipient has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/recipients")
    public ResponseEntity<RecipientDTO> createRecipient(@Valid @RequestBody RecipientDTO recipientDTO) throws URISyntaxException {
        log.debug("REST request to save Recipient : {}", recipientDTO);
        if (recipientDTO.getId() != null) {
            throw new BadRequestAlertException("A new recipient cannot already have an ID", ENTITY_NAME, "idexists");
        }
        RecipientDTO result = recipientService.save(recipientDTO);
        return ResponseEntity
            .created(new URI("/api/recipients/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /recipients/:id} : Updates an existing recipient.
     *
     * @param id           the id of the recipientDTO to save.
     * @param recipientDTO the recipientDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated recipientDTO,
     * or with status {@code 400 (Bad Request)} if the recipientDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the recipientDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/recipients/{id}")
    public ResponseEntity<RecipientDTO> updateRecipient(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody RecipientDTO recipientDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Recipient : {}, {}", id, recipientDTO);
        if (recipientDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, recipientDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!recipientRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        RecipientDTO result = recipientService.save(recipientDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, recipientDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /recipients/:id} : Partial updates given fields of an existing recipient, field will ignore if it is null
     *
     * @param id           the id of the recipientDTO to save.
     * @param recipientDTO the recipientDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated recipientDTO,
     * or with status {@code 400 (Bad Request)} if the recipientDTO is not valid,
     * or with status {@code 404 (Not Found)} if the recipientDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the recipientDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/recipients/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<RecipientDTO> partialUpdateRecipient(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody RecipientDTO recipientDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Recipient partially : {}, {}", id, recipientDTO);
        if (recipientDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, recipientDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!recipientRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<RecipientDTO> result = recipientService.partialUpdate(recipientDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, recipientDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /recipients} : get all the recipients.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of recipients in body.
     */
    @GetMapping("/recipients")
    public ResponseEntity<List<RecipientDTO>> getAllRecipients(RecipientCriteria criteria, Pageable pageable, Boolean unPaged) {
        log.debug("REST request to get Recipients by criteria: {}", criteria);
        Page<RecipientDTO> page = recipientQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/recipients/allowed-simple")
    public ResponseEntity<List<SimpleRecipient>> getAllowedSimpleRecipients(
        Pageable pageable,
        @PathParam("recipientNamePart") String recipientNamePart
    ) {
        log.debug("REST request to get Recipients by criteria: {}", recipientNamePart);
        Page<SimpleRecipient> page = recipientService.getOnlyAllowedRecipientNamesForAgentOrAllForAdmin(recipientNamePart, pageable);
        HttpHeaders headers = uz.paykap.web.rest.util.PaginationUtil.generatePaginationHttpHeaders(page, "/api/recipients/allowed-names");
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/recipients/allowed-names")
    public ResponseEntity<List<KeyWithNames>> getAllowedRecipientNames(
        uz.paykap.service.dto.RecipientCriteria criteria,
        Pageable pageable
    ) {
        log.debug("REST request to get Recipients by criteria: {}", criteria);
        Page<KeyWithNames> page = recipientService.getAllowedRecipients(pageable);
        HttpHeaders headers = uz.paykap.web.rest.util.PaginationUtil.generatePaginationHttpHeaders(page, "/api/recipients/allowed-names");
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/recipients/criteria")
    public ResponseEntity<List<RecipientDTO>> getAllRecipients(
        RecipientCriteria criteria,
        Pageable pageable,
        String gatewayName,
        String categoryShortName
    ) {
        log.debug("REST request to get Recipients by criteria: {}", criteria);
        log.debug("With category name : {}", categoryShortName);
        log.debug("With gateway name : {}", gatewayName);
        Category category = categoryRepository.findTop1ByShortName(categoryShortName).orElse(null);

        if (category != null) {
            LongFilter longFilter = new LongFilter();
            longFilter.setEquals(category.getId());
            criteria.setCategoryId(longFilter);
        }

        Gateway gateway = gatewayRepository.findTop1ByName(gatewayName).orElse(null);

        if (gateway != null) {
            LongFilter longFilter = new LongFilter();
            longFilter.setEquals(gateway.getId());
            criteria.setGatewayId(longFilter);
        }
        Page<RecipientDTO> page = recipientQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = uz.paykap.web.rest.util.PaginationUtil.generatePaginationHttpHeaders(page, "/api/recipients/criteria");
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /recipients/count} : count all the recipients.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/recipients/count")
    public ResponseEntity<Long> countRecipients(RecipientCriteria criteria) {
        log.debug("REST request to count Recipients by criteria: {}", criteria);
        return ResponseEntity.ok().body(recipientQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /recipients/:id} : get the "id" recipient.
     *
     * @param id the id of the recipientDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the recipientDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/recipients/{id}")
    public ResponseEntity<RecipientDTO> getRecipient(@PathVariable Long id) {
        log.debug("REST request to get Recipient : {}", id);
        Optional<RecipientDTO> recipientDTO = recipientService.findOne(id);
        return ResponseUtil.wrapOrNotFound(recipientDTO);
    }

    /**
     * {@code DELETE  /recipients/:id} : delete the "id" recipient.
     *
     * @param id the id of the recipientDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/recipients/{id}")
    public ResponseEntity<Void> deleteRecipient(@PathVariable Long id) {
        log.debug("REST request to delete Recipient : {}", id);
        recipientService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    @DeleteMapping("/recipients/cascaded/{id}")
    public ResponseEntity<Void> deleteRecipientCascaded(@PathVariable Long id) {
        log.debug("REST request to delete Recipient : {}", id);
        recipientService.deleteCascaded(id);
        return ResponseEntity
            .ok()
            .headers(uz.paykap.web.rest.util.HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString()))
            .build();
    }

    @PostMapping("/update")
    public ResponseEntity<Result> updateRecipients(@Valid @RequestBody List<RecipientDTO> list) {
        log.debug("REST request to update Recipients : {}", list.toString());
        return ResponseEntity.ok(recipientService.updateRecipients(list));
    }

    @PostMapping("/recipients/get-by-inn")
    public ResponseEntity<List<RecipientDTO>> getRecipientsByInn(@Valid @RequestBody InnInfo dto) {
        log.debug("REST request to get Recipients by inn  : {}", dto);
        return ResponseEntity.ok(recipientService.getRecipientByInn(dto.getInn()));
    }
}
