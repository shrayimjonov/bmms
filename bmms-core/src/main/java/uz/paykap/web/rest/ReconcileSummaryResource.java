package uz.paykap.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;
import uz.paykap.repository.ReconcileSummaryRepository;
import uz.paykap.service.ReconcileSummaryQueryService;
import uz.paykap.service.ReconcileSummaryService;
import uz.paykap.service.criteria.ReconcileSummaryCriteria;
import uz.paykap.service.dto.ReconcileSummaryDTO;
import uz.paykap.web.rest.errors.BadRequestAlertException;

/**
 * REST controller for managing {@link uz.paykap.domain.ReconcileSummary}.
 */
@ApiIgnore
@RestController
@RequestMapping("/api")
public class ReconcileSummaryResource {

    private final Logger log = LoggerFactory.getLogger(ReconcileSummaryResource.class);

    private static final String ENTITY_NAME = "pkbillmsReconcileSummary";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ReconcileSummaryService reconcileSummaryService;

    private final ReconcileSummaryRepository reconcileSummaryRepository;

    private final ReconcileSummaryQueryService reconcileSummaryQueryService;

    public ReconcileSummaryResource(
        ReconcileSummaryService reconcileSummaryService,
        ReconcileSummaryRepository reconcileSummaryRepository,
        ReconcileSummaryQueryService reconcileSummaryQueryService
    ) {
        this.reconcileSummaryService = reconcileSummaryService;
        this.reconcileSummaryRepository = reconcileSummaryRepository;
        this.reconcileSummaryQueryService = reconcileSummaryQueryService;
    }

    /**
     * {@code POST  /reconcile-summaries} : Create a new reconcileSummary.
     *
     * @param reconcileSummaryDTO the reconcileSummaryDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new reconcileSummaryDTO, or with status {@code 400 (Bad Request)} if the reconcileSummary has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/reconcile-summaries")
    public ResponseEntity<ReconcileSummaryDTO> createReconcileSummary(@Valid @RequestBody ReconcileSummaryDTO reconcileSummaryDTO)
        throws URISyntaxException {
        log.debug("REST request to save ReconcileSummary : {}", reconcileSummaryDTO);
        if (reconcileSummaryDTO.getId() != null) {
            throw new BadRequestAlertException("A new reconcileSummary cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ReconcileSummaryDTO result = reconcileSummaryService.save(reconcileSummaryDTO);
        return ResponseEntity
            .created(new URI("/api/reconcile-summaries/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /reconcile-summaries/:id} : Updates an existing reconcileSummary.
     *
     * @param id the id of the reconcileSummaryDTO to save.
     * @param reconcileSummaryDTO the reconcileSummaryDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated reconcileSummaryDTO,
     * or with status {@code 400 (Bad Request)} if the reconcileSummaryDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the reconcileSummaryDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/reconcile-summaries/{id}")
    public ResponseEntity<ReconcileSummaryDTO> updateReconcileSummary(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ReconcileSummaryDTO reconcileSummaryDTO
    ) throws URISyntaxException {
        log.debug("REST request to update ReconcileSummary : {}, {}", id, reconcileSummaryDTO);
        if (reconcileSummaryDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, reconcileSummaryDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!reconcileSummaryRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        ReconcileSummaryDTO result = reconcileSummaryService.save(reconcileSummaryDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, reconcileSummaryDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /reconcile-summaries/:id} : Partial updates given fields of an existing reconcileSummary, field will ignore if it is null
     *
     * @param id the id of the reconcileSummaryDTO to save.
     * @param reconcileSummaryDTO the reconcileSummaryDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated reconcileSummaryDTO,
     * or with status {@code 400 (Bad Request)} if the reconcileSummaryDTO is not valid,
     * or with status {@code 404 (Not Found)} if the reconcileSummaryDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the reconcileSummaryDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/reconcile-summaries/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<ReconcileSummaryDTO> partialUpdateReconcileSummary(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ReconcileSummaryDTO reconcileSummaryDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update ReconcileSummary partially : {}, {}", id, reconcileSummaryDTO);
        if (reconcileSummaryDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, reconcileSummaryDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!reconcileSummaryRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ReconcileSummaryDTO> result = reconcileSummaryService.partialUpdate(reconcileSummaryDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, reconcileSummaryDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /reconcile-summaries} : get all the reconcileSummaries.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of reconcileSummaries in body.
     */
    @GetMapping("/reconcile-summaries")
    public ResponseEntity<List<ReconcileSummaryDTO>> getAllReconcileSummaries(ReconcileSummaryCriteria criteria, Pageable pageable) {
        log.debug("REST request to get ReconcileSummaries by criteria: {}", criteria);
        Page<ReconcileSummaryDTO> page = reconcileSummaryQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /reconcile-summaries/count} : count all the reconcileSummaries.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/reconcile-summaries/count")
    public ResponseEntity<Long> countReconcileSummaries(ReconcileSummaryCriteria criteria) {
        log.debug("REST request to count ReconcileSummaries by criteria: {}", criteria);
        return ResponseEntity.ok().body(reconcileSummaryQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /reconcile-summaries/:id} : get the "id" reconcileSummary.
     *
     * @param id the id of the reconcileSummaryDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the reconcileSummaryDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/reconcile-summaries/{id}")
    public ResponseEntity<ReconcileSummaryDTO> getReconcileSummary(@PathVariable Long id) {
        log.debug("REST request to get ReconcileSummary : {}", id);
        Optional<ReconcileSummaryDTO> reconcileSummaryDTO = reconcileSummaryService.findOne(id);
        return ResponseUtil.wrapOrNotFound(reconcileSummaryDTO);
    }

    /**
     * {@code DELETE  /reconcile-summaries/:id} : delete the "id" reconcileSummary.
     *
     * @param id the id of the reconcileSummaryDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/reconcile-summaries/{id}")
    public ResponseEntity<Void> deleteReconcileSummary(@PathVariable Long id) {
        log.debug("REST request to delete ReconcileSummary : {}", id);
        reconcileSummaryService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
