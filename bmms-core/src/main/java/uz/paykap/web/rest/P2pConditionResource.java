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
import springfox.documentation.annotations.ApiIgnore;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;
import uz.paykap.domain.P2pCondition;
import uz.paykap.repository.P2pConditionRepository;
import uz.paykap.service.P2pConditionQueryService;
import uz.paykap.service.P2pConditionService;
import uz.paykap.service.criteria.P2pConditionCriteria;
import uz.paykap.web.rest.errors.BadRequestAlertException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * REST controller for managing {@link uz.paykap.domain.P2pCondition}.
 */
@ApiIgnore
@RestController
@RequestMapping("/api")
public class P2pConditionResource {

    private final Logger log = LoggerFactory.getLogger(P2pConditionResource.class);

    private static final String ENTITY_NAME = "pkbillmsP2PCondition";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final P2pConditionService p2pConditionService;

    private final P2pConditionRepository p2pConditionRepository;

    private final P2pConditionQueryService p2pConditionQueryService;

    public P2pConditionResource(
        P2pConditionService p2pConditionService,
        P2pConditionRepository p2pConditionRepository,
        P2pConditionQueryService p2pConditionQueryService
    ) {
        this.p2pConditionService = p2pConditionService;
        this.p2pConditionRepository = p2pConditionRepository;
        this.p2pConditionQueryService = p2pConditionQueryService;
    }

    /**
     * {@code POST  /p-2-p-conditions} : Create a new p2pCondition.
     *
     * @param p2pCondition the p2pCondition to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new p2pCondition, or with status {@code 400 (Bad Request)} if the p2pCondition has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/p-2-p-conditions")
    public ResponseEntity<P2pCondition> createP2pCondition(@Valid @RequestBody P2pCondition p2pCondition) throws URISyntaxException {
        log.debug("REST request to save P2pCondition : {}", p2pCondition);
        if (p2pCondition.getId() != null) {
            throw new BadRequestAlertException("A new p2pCondition cannot already have an ID", ENTITY_NAME, "idexists");
        }
        P2pCondition result = p2pConditionService.save(p2pCondition);
        return ResponseEntity
            .created(new URI("/api/p-2-p-conditions/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /p-2-p-conditions/:id} : Updates an existing p2pCondition.
     *
     * @param id the id of the p2pCondition to save.
     * @param p2pCondition the p2pCondition to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated p2pCondition,
     * or with status {@code 400 (Bad Request)} if the p2pCondition is not valid,
     * or with status {@code 500 (Internal Server Error)} if the p2pCondition couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/p-2-p-conditions/{id}")
    public ResponseEntity<P2pCondition> updateP2pCondition(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody P2pCondition p2pCondition
    ) throws URISyntaxException {
        log.debug("REST request to update P2pCondition : {}, {}", id, p2pCondition);
        if (p2pCondition.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, p2pCondition.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!p2pConditionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        P2pCondition result = p2pConditionService.save(p2pCondition);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, p2pCondition.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /p-2-p-conditions/:id} : Partial updates given fields of an existing p2pCondition, field will ignore if it is null
     *
     * @param id the id of the p2pCondition to save.
     * @param p2pCondition the p2pCondition to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated p2pCondition,
     * or with status {@code 400 (Bad Request)} if the p2pCondition is not valid,
     * or with status {@code 404 (Not Found)} if the p2pCondition is not found,
     * or with status {@code 500 (Internal Server Error)} if the p2pCondition couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/p-2-p-conditions/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<P2pCondition> partialUpdateP2pCondition(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody P2pCondition p2pCondition
    ) throws URISyntaxException {
        log.debug("REST request to partial update P2pCondition partially : {}, {}", id, p2pCondition);
        if (p2pCondition.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, p2pCondition.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!p2pConditionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<P2pCondition> result = p2pConditionService.partialUpdate(p2pCondition);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, p2pCondition.getId().toString())
        );
    }

    /**
     * {@code GET  /p-2-p-conditions} : get all the p2pConditions.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of p2pConditions in body.
     */
    @GetMapping("/p-2-p-conditions")
    public ResponseEntity<List<P2pCondition>> getAllP2pConditions(P2pConditionCriteria criteria, Pageable pageable, Boolean unPaged) {
        log.debug("REST request to get P2pConditions by criteria: {}", criteria);
        Page<P2pCondition> page = p2pConditionQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /p-2-p-conditions/count} : count all the p2pConditions.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/p-2-p-conditions/count")
    public ResponseEntity<Long> countP2pConditions(P2pConditionCriteria criteria) {
        log.debug("REST request to count P2pConditions by criteria: {}", criteria);
        return ResponseEntity.ok().body(p2pConditionQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /p-2-p-conditions/:id} : get the "id" p2pCondition.
     *
     * @param id the id of the p2pCondition to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the p2pCondition, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/p-2-p-conditions/{id}")
    public ResponseEntity<P2pCondition> getP2pCondition(@PathVariable Long id) {
        log.debug("REST request to get P2pCondition : {}", id);
        Optional<P2pCondition> p2pCondition = p2pConditionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(p2pCondition);
    }

    /**
     * {@code DELETE  /p-2-p-conditions/:id} : delete the "id" p2pCondition.
     *
     * @param id the id of the p2pCondition to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/p-2-p-conditions/{id}")
    public ResponseEntity<Void> deleteP2pCondition(@PathVariable Long id) {
        log.debug("REST request to delete P2pCondition : {}", id);
        p2pConditionService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
