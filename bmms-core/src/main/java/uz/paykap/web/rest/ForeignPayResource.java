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
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;
import uz.paykap.repository.ForeignPayRepository;
import uz.paykap.service.ForeignPayQueryService;
import uz.paykap.service.ForeignPayService;
import uz.paykap.service.criteria.ForeignPayCriteria;
import uz.paykap.service.dto.ForeignPayDTO;
import uz.paykap.web.rest.errors.BadRequestAlertException;

/**
 * REST controller for managing {@link uz.paykap.domain.ForeignPay}.
 */
@RestController
@RequestMapping("/api")
public class ForeignPayResource {

    private final Logger log = LoggerFactory.getLogger(ForeignPayResource.class);

    private static final String ENTITY_NAME = "pkbillmsForeignPay";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ForeignPayService foreignPayService;

    private final ForeignPayRepository foreignPayRepository;

    private final ForeignPayQueryService foreignPayQueryService;

    public ForeignPayResource(
        ForeignPayService foreignPayService,
        ForeignPayRepository foreignPayRepository,
        ForeignPayQueryService foreignPayQueryService
    ) {
        this.foreignPayService = foreignPayService;
        this.foreignPayRepository = foreignPayRepository;
        this.foreignPayQueryService = foreignPayQueryService;
    }

    /**
     * {@code POST  /foreign-pays} : Create a new foreignPay.
     *
     * @param foreignPayDTO the foreignPayDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new foreignPayDTO, or with status {@code 400 (Bad Request)} if the foreignPay has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/foreign-pays")
    public ResponseEntity<ForeignPayDTO> createForeignPay(@Valid @RequestBody ForeignPayDTO foreignPayDTO) throws URISyntaxException {
        log.debug("REST request to save ForeignPay : {}", foreignPayDTO);
        if (foreignPayDTO.getId() != null) {
            throw new BadRequestAlertException("A new foreignPay cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ForeignPayDTO result = foreignPayService.save(foreignPayDTO);
        return ResponseEntity
            .created(new URI("/api/foreign-pays/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /foreign-pays/:id} : Updates an existing foreignPay.
     *
     * @param id the id of the foreignPayDTO to save.
     * @param foreignPayDTO the foreignPayDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated foreignPayDTO,
     * or with status {@code 400 (Bad Request)} if the foreignPayDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the foreignPayDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/foreign-pays/{id}")
    public ResponseEntity<ForeignPayDTO> updateForeignPay(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ForeignPayDTO foreignPayDTO
    ) throws URISyntaxException {
        log.debug("REST request to update ForeignPay : {}, {}", id, foreignPayDTO);
        if (foreignPayDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, foreignPayDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!foreignPayRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        ForeignPayDTO result = foreignPayService.save(foreignPayDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, foreignPayDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /foreign-pays/:id} : Partial updates given fields of an existing foreignPay, field will ignore if it is null
     *
     * @param id the id of the foreignPayDTO to save.
     * @param foreignPayDTO the foreignPayDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated foreignPayDTO,
     * or with status {@code 400 (Bad Request)} if the foreignPayDTO is not valid,
     * or with status {@code 404 (Not Found)} if the foreignPayDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the foreignPayDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/foreign-pays/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<ForeignPayDTO> partialUpdateForeignPay(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ForeignPayDTO foreignPayDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update ForeignPay partially : {}, {}", id, foreignPayDTO);
        if (foreignPayDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, foreignPayDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!foreignPayRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ForeignPayDTO> result = foreignPayService.partialUpdate(foreignPayDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, foreignPayDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /foreign-pays} : get all the foreignPays.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of foreignPays in body.
     */
    @GetMapping("/foreign-pays")
    public ResponseEntity<List<ForeignPayDTO>> getAllForeignPays(ForeignPayCriteria criteria, Pageable pageable) {
        log.debug("REST request to get ForeignPays by criteria: {}", criteria);
        Page<ForeignPayDTO> page = foreignPayQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /foreign-pays/count} : count all the foreignPays.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/foreign-pays/count")
    public ResponseEntity<Long> countForeignPays(ForeignPayCriteria criteria) {
        log.debug("REST request to count ForeignPays by criteria: {}", criteria);
        return ResponseEntity.ok().body(foreignPayQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /foreign-pays/:id} : get the "id" foreignPay.
     *
     * @param id the id of the foreignPayDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the foreignPayDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/foreign-pays/{id}")
    public ResponseEntity<ForeignPayDTO> getForeignPay(@PathVariable Long id) {
        log.debug("REST request to get ForeignPay : {}", id);
        Optional<ForeignPayDTO> foreignPayDTO = foreignPayService.findOne(id);
        return ResponseUtil.wrapOrNotFound(foreignPayDTO);
    }

    /**
     * {@code DELETE  /foreign-pays/:id} : delete the "id" foreignPay.
     *
     * @param id the id of the foreignPayDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/foreign-pays/{id}")
    public ResponseEntity<Void> deleteForeignPay(@PathVariable Long id) {
        log.debug("REST request to delete ForeignPay : {}", id);
        foreignPayService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
