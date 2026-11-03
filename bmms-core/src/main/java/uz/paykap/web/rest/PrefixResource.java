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
import uz.paykap.repository.PrefixRepository;
import uz.paykap.service.PrefixQueryService;
import uz.paykap.service.PrefixService;
import uz.paykap.service.criteria.PrefixCriteria;
import uz.paykap.service.dto.PrefixDTO;
import uz.paykap.web.rest.errors.BadRequestAlertException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * REST controller for managing {@link uz.paykap.domain.Prefix}.
 */
@ApiIgnore
@RestController
@RequestMapping("/api")
public class PrefixResource {

    private final Logger log = LoggerFactory.getLogger(PrefixResource.class);

    private static final String ENTITY_NAME = "pkbillmsPrefix";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PrefixService prefixService;

    private final PrefixRepository prefixRepository;

    private final PrefixQueryService prefixQueryService;

    public PrefixResource(PrefixService prefixService, PrefixRepository prefixRepository, PrefixQueryService prefixQueryService) {
        this.prefixService = prefixService;
        this.prefixRepository = prefixRepository;
        this.prefixQueryService = prefixQueryService;
    }

    /**
     * {@code POST  /prefixes} : Create a new prefix.
     *
     * @param prefixDTO the prefixDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new prefixDTO, or with status {@code 400 (Bad Request)} if the prefix has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/prefixes")
    public ResponseEntity<PrefixDTO> createPrefix(@Valid @RequestBody PrefixDTO prefixDTO) throws URISyntaxException {
        log.debug("REST request to save Prefix : {}", prefixDTO);
        if (prefixDTO.getId() != null) {
            throw new BadRequestAlertException("A new prefix cannot already have an ID", ENTITY_NAME, "idexists");
        }
        PrefixDTO result = prefixService.save(prefixDTO);
        return ResponseEntity
            .created(new URI("/api/prefixes/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /prefixes/:id} : Updates an existing prefix.
     *
     * @param id the id of the prefixDTO to save.
     * @param prefixDTO the prefixDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated prefixDTO,
     * or with status {@code 400 (Bad Request)} if the prefixDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the prefixDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/prefixes/{id}")
    public ResponseEntity<PrefixDTO> updatePrefix(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody PrefixDTO prefixDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Prefix : {}, {}", id, prefixDTO);
        if (prefixDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, prefixDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!prefixRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        PrefixDTO result = prefixService.save(prefixDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, prefixDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /prefixes/:id} : Partial updates given fields of an existing prefix, field will ignore if it is null
     *
     * @param id the id of the prefixDTO to save.
     * @param prefixDTO the prefixDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated prefixDTO,
     * or with status {@code 400 (Bad Request)} if the prefixDTO is not valid,
     * or with status {@code 404 (Not Found)} if the prefixDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the prefixDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/prefixes/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<PrefixDTO> partialUpdatePrefix(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody PrefixDTO prefixDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Prefix partially : {}, {}", id, prefixDTO);
        if (prefixDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, prefixDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!prefixRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<PrefixDTO> result = prefixService.partialUpdate(prefixDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, prefixDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /prefixes} : get all the prefixes.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of prefixes in body.
     */
    @GetMapping("/prefixes")
    public ResponseEntity<List<PrefixDTO>> getAllPrefixes(PrefixCriteria criteria, Pageable pageable, Boolean unPaged) {
        log.debug("REST request to get Prefixes by criteria: {}", criteria);
        Page<PrefixDTO> page = prefixQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /prefixes/count} : count all the prefixes.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/prefixes/count")
    public ResponseEntity<Long> countPrefixes(PrefixCriteria criteria) {
        log.debug("REST request to count Prefixes by criteria: {}", criteria);
        return ResponseEntity.ok().body(prefixQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /prefixes/:id} : get the "id" prefix.
     *
     * @param id the id of the prefixDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the prefixDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/prefixes/{id}")
    public ResponseEntity<PrefixDTO> getPrefix(@PathVariable Long id) {
        log.debug("REST request to get Prefix : {}", id);
        Optional<PrefixDTO> prefixDTO = prefixService.findOne(id);
        return ResponseUtil.wrapOrNotFound(prefixDTO);
    }

    /**
     * {@code DELETE  /prefixes/:id} : delete the "id" prefix.
     *
     * @param id the id of the prefixDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/prefixes/{id}")
    public ResponseEntity<Void> deletePrefix(@PathVariable Long id) {
        log.debug("REST request to delete Prefix : {}", id);
        prefixService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
