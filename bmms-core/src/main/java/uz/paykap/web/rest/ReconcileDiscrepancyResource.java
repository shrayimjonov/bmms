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
import uz.paykap.repository.ReconcileDiscrepancyRepository;
import uz.paykap.service.ReconcileDiscrepancyQueryService;
import uz.paykap.service.ReconcileDiscrepancyService;
import uz.paykap.service.criteria.ReconcileDiscrepancyCriteria;
import uz.paykap.service.dto.ReconcileDiscrepancyDTO;
import uz.paykap.web.rest.errors.BadRequestAlertException;

/**
 * REST controller for managing {@link uz.paykap.domain.ReconcileDiscrepancy}.
 */
@ApiIgnore
@RestController
@RequestMapping("/api")
public class ReconcileDiscrepancyResource {

    private final Logger log = LoggerFactory.getLogger(ReconcileDiscrepancyResource.class);

    private static final String ENTITY_NAME = "pkbillmsReconcileDiscrepancy";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ReconcileDiscrepancyService reconcileDiscrepancyService;

    private final ReconcileDiscrepancyRepository reconcileDiscrepancyRepository;

    private final ReconcileDiscrepancyQueryService reconcileDiscrepancyQueryService;

    public ReconcileDiscrepancyResource(
        ReconcileDiscrepancyService reconcileDiscrepancyService,
        ReconcileDiscrepancyRepository reconcileDiscrepancyRepository,
        ReconcileDiscrepancyQueryService reconcileDiscrepancyQueryService
    ) {
        this.reconcileDiscrepancyService = reconcileDiscrepancyService;
        this.reconcileDiscrepancyRepository = reconcileDiscrepancyRepository;
        this.reconcileDiscrepancyQueryService = reconcileDiscrepancyQueryService;
    }

    /**
     * {@code POST  /reconcile-discrepancies} : Create a new reconcileDiscrepancy.
     *
     * @param reconcileDiscrepancyDTO the reconcileDiscrepancyDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new reconcileDiscrepancyDTO, or with status {@code 400 (Bad Request)} if the reconcileDiscrepancy has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/reconcile-discrepancies")
    public ResponseEntity<ReconcileDiscrepancyDTO> createReconcileDiscrepancy(
        @Valid @RequestBody ReconcileDiscrepancyDTO reconcileDiscrepancyDTO
    ) throws URISyntaxException {
        log.debug("REST request to save ReconcileDiscrepancy : {}", reconcileDiscrepancyDTO);
        if (reconcileDiscrepancyDTO.getId() != null) {
            throw new BadRequestAlertException("A new reconcileDiscrepancy cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ReconcileDiscrepancyDTO result = reconcileDiscrepancyService.save(reconcileDiscrepancyDTO);
        return ResponseEntity
            .created(new URI("/api/reconcile-discrepancies/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /reconcile-discrepancies/:id} : Updates an existing reconcileDiscrepancy.
     *
     * @param id the id of the reconcileDiscrepancyDTO to save.
     * @param reconcileDiscrepancyDTO the reconcileDiscrepancyDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated reconcileDiscrepancyDTO,
     * or with status {@code 400 (Bad Request)} if the reconcileDiscrepancyDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the reconcileDiscrepancyDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/reconcile-discrepancies/{id}")
    public ResponseEntity<ReconcileDiscrepancyDTO> updateReconcileDiscrepancy(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ReconcileDiscrepancyDTO reconcileDiscrepancyDTO
    ) throws URISyntaxException {
        log.debug("REST request to update ReconcileDiscrepancy : {}, {}", id, reconcileDiscrepancyDTO);
        if (reconcileDiscrepancyDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, reconcileDiscrepancyDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!reconcileDiscrepancyRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        ReconcileDiscrepancyDTO result = reconcileDiscrepancyService.save(reconcileDiscrepancyDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, reconcileDiscrepancyDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /reconcile-discrepancies/:id} : Partial updates given fields of an existing reconcileDiscrepancy, field will ignore if it is null
     *
     * @param id the id of the reconcileDiscrepancyDTO to save.
     * @param reconcileDiscrepancyDTO the reconcileDiscrepancyDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated reconcileDiscrepancyDTO,
     * or with status {@code 400 (Bad Request)} if the reconcileDiscrepancyDTO is not valid,
     * or with status {@code 404 (Not Found)} if the reconcileDiscrepancyDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the reconcileDiscrepancyDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/reconcile-discrepancies/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<ReconcileDiscrepancyDTO> partialUpdateReconcileDiscrepancy(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ReconcileDiscrepancyDTO reconcileDiscrepancyDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update ReconcileDiscrepancy partially : {}, {}", id, reconcileDiscrepancyDTO);
        if (reconcileDiscrepancyDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, reconcileDiscrepancyDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!reconcileDiscrepancyRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ReconcileDiscrepancyDTO> result = reconcileDiscrepancyService.partialUpdate(reconcileDiscrepancyDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, reconcileDiscrepancyDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /reconcile-discrepancies} : get all the reconcileDiscrepancies.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of reconcileDiscrepancies in body.
     */
    @GetMapping("/reconcile-discrepancies")
    public ResponseEntity<List<ReconcileDiscrepancyDTO>> getAllReconcileDiscrepancies(
        ReconcileDiscrepancyCriteria criteria,
        Pageable pageable
    ) {
        log.debug("REST request to get ReconcileDiscrepancies by criteria: {}", criteria);
        Page<ReconcileDiscrepancyDTO> page = reconcileDiscrepancyQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /reconcile-discrepancies/count} : count all the reconcileDiscrepancies.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/reconcile-discrepancies/count")
    public ResponseEntity<Long> countReconcileDiscrepancies(ReconcileDiscrepancyCriteria criteria) {
        log.debug("REST request to count ReconcileDiscrepancies by criteria: {}", criteria);
        return ResponseEntity.ok().body(reconcileDiscrepancyQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /reconcile-discrepancies/:id} : get the "id" reconcileDiscrepancy.
     *
     * @param id the id of the reconcileDiscrepancyDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the reconcileDiscrepancyDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/reconcile-discrepancies/{id}")
    public ResponseEntity<ReconcileDiscrepancyDTO> getReconcileDiscrepancy(@PathVariable Long id) {
        log.debug("REST request to get ReconcileDiscrepancy : {}", id);
        Optional<ReconcileDiscrepancyDTO> reconcileDiscrepancyDTO = reconcileDiscrepancyService.findOne(id);
        return ResponseUtil.wrapOrNotFound(reconcileDiscrepancyDTO);
    }

    /**
     * {@code DELETE  /reconcile-discrepancies/:id} : delete the "id" reconcileDiscrepancy.
     *
     * @param id the id of the reconcileDiscrepancyDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/reconcile-discrepancies/{id}")
    public ResponseEntity<Void> deleteReconcileDiscrepancy(@PathVariable Long id) {
        log.debug("REST request to delete ReconcileDiscrepancy : {}", id);
        reconcileDiscrepancyService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
