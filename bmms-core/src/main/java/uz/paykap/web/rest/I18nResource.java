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
import uz.paykap.repository.I18nRepository;
import uz.paykap.service.I18nQueryService;
import uz.paykap.service.I18nService;
import uz.paykap.service.criteria.I18nCriteria;
import uz.paykap.service.dto.I18nDTO;
import uz.paykap.web.rest.errors.BadRequestAlertException;

/**
 * REST controller for managing {@link uz.paykap.domain.I18n}.
 */
@ApiIgnore
@RestController
@RequestMapping("/api")
public class I18nResource {

    private final Logger log = LoggerFactory.getLogger(I18nResource.class);

    private static final String ENTITY_NAME = "pkbillmsI18N";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final I18nService i18nService;

    private final I18nRepository i18nRepository;

    private final I18nQueryService i18nQueryService;

    public I18nResource(I18nService i18nService, I18nRepository i18nRepository, I18nQueryService i18nQueryService) {
        this.i18nService = i18nService;
        this.i18nRepository = i18nRepository;
        this.i18nQueryService = i18nQueryService;
    }

    /**
     * {@code POST  /i-18-ns} : Create a new i18n.
     *
     * @param i18nDTO the i18nDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new i18nDTO, or with status {@code 400 (Bad Request)} if the i18n has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/i-18-ns")
    public ResponseEntity<I18nDTO> createI18n(@Valid @RequestBody I18nDTO i18nDTO) throws URISyntaxException {
        log.debug("REST request to save I18n : {}", i18nDTO);
        if (i18nDTO.getId() != null) {
            throw new BadRequestAlertException("A new i18n cannot already have an ID", ENTITY_NAME, "idexists");
        }
        I18nDTO result = i18nService.save(i18nDTO);
        return ResponseEntity
            .created(new URI("/api/i-18-ns/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /i-18-ns/:id} : Updates an existing i18n.
     *
     * @param id the id of the i18nDTO to save.
     * @param i18nDTO the i18nDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated i18nDTO,
     * or with status {@code 400 (Bad Request)} if the i18nDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the i18nDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/i-18-ns/{id}")
    public ResponseEntity<I18nDTO> updateI18n(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody I18nDTO i18nDTO
    ) throws URISyntaxException {
        log.debug("REST request to update I18n : {}, {}", id, i18nDTO);
        if (i18nDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, i18nDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!i18nRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        I18nDTO result = i18nService.save(i18nDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, i18nDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /i-18-ns/:id} : Partial updates given fields of an existing i18n, field will ignore if it is null
     *
     * @param id the id of the i18nDTO to save.
     * @param i18nDTO the i18nDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated i18nDTO,
     * or with status {@code 400 (Bad Request)} if the i18nDTO is not valid,
     * or with status {@code 404 (Not Found)} if the i18nDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the i18nDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/i-18-ns/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<I18nDTO> partialUpdateI18n(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody I18nDTO i18nDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update I18n partially : {}, {}", id, i18nDTO);
        if (i18nDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, i18nDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!i18nRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<I18nDTO> result = i18nService.partialUpdate(i18nDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, i18nDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /i-18-ns} : get all the i18ns.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of i18ns in body.
     */
    @GetMapping("/i-18-ns")
    public ResponseEntity<List<I18nDTO>> getAllI18ns(I18nCriteria criteria, Pageable pageable) {
        log.debug("REST request to get I18ns by criteria: {}", criteria);
        Page<I18nDTO> page = i18nQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /i-18-ns/count} : count all the i18ns.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/i-18-ns/count")
    public ResponseEntity<Long> countI18ns(I18nCriteria criteria) {
        log.debug("REST request to count I18ns by criteria: {}", criteria);
        return ResponseEntity.ok().body(i18nQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /i-18-ns/:id} : get the "id" i18n.
     *
     * @param id the id of the i18nDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the i18nDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/i-18-ns/{id}")
    public ResponseEntity<I18nDTO> getI18n(@PathVariable Long id) {
        log.debug("REST request to get I18n : {}", id);
        Optional<I18nDTO> i18nDTO = i18nService.findOne(id);
        return ResponseUtil.wrapOrNotFound(i18nDTO);
    }

    /**
     * {@code DELETE  /i-18-ns/:id} : delete the "id" i18n.
     *
     * @param id the id of the i18nDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/i-18-ns/{id}")
    public ResponseEntity<Void> deleteI18n(@PathVariable Long id) {
        log.debug("REST request to delete I18n : {}", id);
        i18nService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
