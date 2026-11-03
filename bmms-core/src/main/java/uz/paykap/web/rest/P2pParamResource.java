package uz.paykap.web.rest;

import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;
import uz.devops.bmcore.enumeration.PayType;
import uz.paykap.domain.P2pParam;
import uz.paykap.repository.P2pParamRepository;
import uz.paykap.service.P2pParamQueryService;
import uz.paykap.service.P2pParamService;
import uz.paykap.service.criteria.P2pParamCriteria;
import uz.paykap.service.dto.p2p.P2pParamInfoRequest;
import uz.paykap.service.p2p.dto.P2pParamResult;
import uz.paykap.web.rest.errors.BadRequestAlertException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * REST controller for managing {@link uz.paykap.domain.P2pParam}.
 */
@RestController
@RequestMapping("/api")
public class P2pParamResource {

    private final Logger log = LoggerFactory.getLogger(P2pParamResource.class);

    private static final String ENTITY_NAME = "pkbillmsP2PParam";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final P2pParamService p2pParamService;

    private final P2pParamRepository p2pParamRepository;

    private final P2pParamQueryService p2pParamQueryService;

    public P2pParamResource(
        P2pParamService p2pParamService,
        P2pParamRepository p2pParamRepository,
        P2pParamQueryService p2pParamQueryService
    ) {
        this.p2pParamService = p2pParamService;
        this.p2pParamRepository = p2pParamRepository;
        this.p2pParamQueryService = p2pParamQueryService;
    }

    /**
     * {@code POST  /p-2-p-params} : Create a new p2pParam.
     *
     * @param p2pParam the p2pParam to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new p2pParam, or with status {@code 400 (Bad Request)} if the p2pParam has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/p-2-p-params")
    public ResponseEntity<P2pParam> createP2pParam(@Valid @RequestBody P2pParam p2pParam) throws URISyntaxException {
        log.debug("REST request to save P2pParam : {}", p2pParam);
        if (p2pParam.getId() != null) {
            throw new BadRequestAlertException("A new p2pParam cannot already have an ID", ENTITY_NAME, "idexists");
        }
        P2pParam result = p2pParamService.save(p2pParam);
        return ResponseEntity
            .created(new URI("/api/p-2-p-params/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /p-2-p-params/:id} : Updates an existing p2pParam.
     *
     * @param id the id of the p2pParam to save.
     * @param p2pParam the p2pParam to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated p2pParam,
     * or with status {@code 400 (Bad Request)} if the p2pParam is not valid,
     * or with status {@code 500 (Internal Server Error)} if the p2pParam couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/p-2-p-params/{id}")
    public ResponseEntity<P2pParam> updateP2pParam(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody P2pParam p2pParam
    ) throws URISyntaxException {
        log.debug("REST request to update P2pParam : {}, {}", id, p2pParam);
        if (p2pParam.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, p2pParam.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!p2pParamRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        P2pParam result = p2pParamService.save(p2pParam);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, p2pParam.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /p-2-p-params/:id} : Partial updates given fields of an existing p2pParam, field will ignore if it is null
     *
     * @param id the id of the p2pParam to save.
     * @param p2pParam the p2pParam to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated p2pParam,
     * or with status {@code 400 (Bad Request)} if the p2pParam is not valid,
     * or with status {@code 404 (Not Found)} if the p2pParam is not found,
     * or with status {@code 500 (Internal Server Error)} if the p2pParam couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/p-2-p-params/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<P2pParam> partialUpdateP2pParam(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody P2pParam p2pParam
    ) throws URISyntaxException {
        log.debug("REST request to partial update P2pParam partially : {}, {}", id, p2pParam);
        if (p2pParam.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, p2pParam.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!p2pParamRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<P2pParam> result = p2pParamService.partialUpdate(p2pParam);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, p2pParam.getId().toString())
        );
    }

    /**
     * {@code GET  /p-2-p-params} : get all the p2pParams.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of p2pParams in body.
     */
    @GetMapping("/p-2-p-params")
    public ResponseEntity<List<P2pParam>> getAllP2pParams(P2pParamCriteria criteria, Pageable pageable, Boolean unPaged) {
        log.debug("REST request to get P2pParams by criteria: {}", criteria);
        Page<P2pParam> page = p2pParamQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /p-2-p-params/count} : count all the p2pParams.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/p-2-p-params/count")
    public ResponseEntity<Long> countP2pParams(P2pParamCriteria criteria) {
        log.debug("REST request to count P2pParams by criteria: {}", criteria);
        return ResponseEntity.ok().body(p2pParamQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /p-2-p-params/:id} : get the "id" p2pParam.
     *
     * @param id the id of the p2pParam to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the p2pParam, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/p-2-p-params/{id}")
    public ResponseEntity<P2pParam> getP2pParam(@PathVariable Long id) {
        log.debug("REST request to get P2pParam : {}", id);
        Optional<P2pParam> p2pParam = p2pParamService.findOne(id);
        return ResponseUtil.wrapOrNotFound(p2pParam);
    }

    /**
     * {@code DELETE  /p-2-p-params/:id} : delete the "id" p2pParam.
     *
     * @param id the id of the p2pParam to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/p-2-p-params/{id}")
    public ResponseEntity<Void> deleteP2pParam(@PathVariable Long id) {
        log.debug("REST request to delete P2pParam : {}", id);
        p2pParamService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    @ApiOperation("Получения информации p2p параметра")
    @PostMapping("/p-2-p-params/info")
    public ResponseEntity<P2pParamResult> getInfo(@Valid @RequestBody P2pParamInfoRequest p2pParamInfoRequest) {
        log.debug("REST request to get p2pParamInfo : {}", p2pParamInfoRequest);
        return ResponseEntity.ok(p2pParamService.getP2pParamInfo(p2pParamInfoRequest));
    }

    @ApiOperation("Получения список тип оплаты")
    @GetMapping("/p-2-p-params/types")
    public ResponseEntity<PayType[]> getPayTypes() {
        log.debug("REST request to get pay types");
        return ResponseEntity.ok(PayType.values());
    }
}
