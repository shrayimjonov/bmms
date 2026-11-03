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
import uz.paykap.domain.P2pCommission;
import uz.paykap.repository.P2pCommissionRepository;
import uz.paykap.service.P2pCommissionQueryService;
import uz.paykap.service.P2pCommissionService;
import uz.paykap.service.criteria.P2pCommissionCriteria;
import uz.paykap.web.rest.errors.BadRequestAlertException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * REST controller for managing {@link uz.paykap.domain.P2pCommission}.
 */
@ApiIgnore
@RestController
@RequestMapping("/api")
public class P2pCommissionResource {

    private final Logger log = LoggerFactory.getLogger(P2pCommissionResource.class);

    private static final String ENTITY_NAME = "pkbillmsP2PCommission";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final P2pCommissionService p2pCommissionService;

    private final P2pCommissionRepository p2pCommissionRepository;

    private final P2pCommissionQueryService p2pCommissionQueryService;

    public P2pCommissionResource(
        P2pCommissionService p2pCommissionService,
        P2pCommissionRepository p2pCommissionRepository,
        P2pCommissionQueryService p2pCommissionQueryService
    ) {
        this.p2pCommissionService = p2pCommissionService;
        this.p2pCommissionRepository = p2pCommissionRepository;
        this.p2pCommissionQueryService = p2pCommissionQueryService;
    }

    /**
     * {@code POST  /p-2-p-commissions} : Create a new p2pCommission.
     *
     * @param p2pCommission the p2pCommission to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new p2pCommission, or with status {@code 400 (Bad Request)} if the p2pCommission has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/p-2-p-commissions")
    public ResponseEntity<P2pCommission> createP2pCommission(@Valid @RequestBody P2pCommission p2pCommission) throws URISyntaxException {
        log.debug("REST request to save P2pCommission : {}", p2pCommission);
        if (p2pCommission.getId() != null) {
            throw new BadRequestAlertException("A new p2pCommission cannot already have an ID", ENTITY_NAME, "idexists");
        }
        P2pCommission result = p2pCommissionService.save(p2pCommission);
        return ResponseEntity
            .created(new URI("/api/p-2-p-commissions/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /p-2-p-commissions/:id} : Updates an existing p2pCommission.
     *
     * @param id the id of the p2pCommission to save.
     * @param p2pCommission the p2pCommission to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated p2pCommission,
     * or with status {@code 400 (Bad Request)} if the p2pCommission is not valid,
     * or with status {@code 500 (Internal Server Error)} if the p2pCommission couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/p-2-p-commissions/{id}")
    public ResponseEntity<P2pCommission> updateP2pCommission(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody P2pCommission p2pCommission
    ) throws URISyntaxException {
        log.debug("REST request to update P2pCommission : {}, {}", id, p2pCommission);
        if (p2pCommission.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, p2pCommission.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!p2pCommissionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        P2pCommission result = p2pCommissionService.save(p2pCommission);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, p2pCommission.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /p-2-p-commissions/:id} : Partial updates given fields of an existing p2pCommission, field will ignore if it is null
     *
     * @param id the id of the p2pCommission to save.
     * @param p2pCommission the p2pCommission to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated p2pCommission,
     * or with status {@code 400 (Bad Request)} if the p2pCommission is not valid,
     * or with status {@code 404 (Not Found)} if the p2pCommission is not found,
     * or with status {@code 500 (Internal Server Error)} if the p2pCommission couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/p-2-p-commissions/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<P2pCommission> partialUpdateP2pCommission(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody P2pCommission p2pCommission
    ) throws URISyntaxException {
        log.debug("REST request to partial update P2pCommission partially : {}, {}", id, p2pCommission);
        if (p2pCommission.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, p2pCommission.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!p2pCommissionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<P2pCommission> result = p2pCommissionService.partialUpdate(p2pCommission);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, p2pCommission.getId().toString())
        );
    }

    /**
     * {@code GET  /p-2-p-commissions} : get all the p2pCommissions.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of p2pCommissions in body.
     */
    @GetMapping("/p-2-p-commissions")
    public ResponseEntity<List<P2pCommission>> getAllP2pCommissions(P2pCommissionCriteria criteria, Pageable pageable, Boolean unPaged) {
        log.debug("REST request to get P2pCommissions by criteria: {}", criteria);
        Page<P2pCommission> page = p2pCommissionQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /p-2-p-commissions/count} : count all the p2pCommissions.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/p-2-p-commissions/count")
    public ResponseEntity<Long> countP2pCommissions(P2pCommissionCriteria criteria) {
        log.debug("REST request to count P2pCommissions by criteria: {}", criteria);
        return ResponseEntity.ok().body(p2pCommissionQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /p-2-p-commissions/:id} : get the "id" p2pCommission.
     *
     * @param id the id of the p2pCommission to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the p2pCommission, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/p-2-p-commissions/{id}")
    public ResponseEntity<P2pCommission> getP2pCommission(@PathVariable Long id) {
        log.debug("REST request to get P2pCommission : {}", id);
        Optional<P2pCommission> p2pCommission = p2pCommissionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(p2pCommission);
    }

    /**
     * {@code DELETE  /p-2-p-commissions/:id} : delete the "id" p2pCommission.
     *
     * @param id the id of the p2pCommission to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/p-2-p-commissions/{id}")
    public ResponseEntity<Void> deleteP2pCommission(@PathVariable Long id) {
        log.debug("REST request to delete P2pCommission : {}", id);
        p2pCommissionService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
