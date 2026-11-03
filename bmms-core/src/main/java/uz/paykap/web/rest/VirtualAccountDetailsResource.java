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
import uz.paykap.repository.VirtualAccountDetailsRepository;
import uz.paykap.service.VirtualAccountDetailsQueryService;
import uz.paykap.service.VirtualAccountDetailsService;
import uz.paykap.service.criteria.VirtualAccountDetailsCriteria;
import uz.paykap.service.dto.VirtualAccountDetailsDTO;
import uz.paykap.web.rest.errors.BadRequestAlertException;

/**
 * REST controller for managing {@link uz.paykap.domain.VirtualAccountDetails}.
 */
@RestController
@RequestMapping("/api")
public class VirtualAccountDetailsResource {

    private final Logger log = LoggerFactory.getLogger(VirtualAccountDetailsResource.class);

    private static final String ENTITY_NAME = "pkbillmsVirtualAccountDetails";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final VirtualAccountDetailsService virtualAccountDetailsService;

    private final VirtualAccountDetailsRepository virtualAccountDetailsRepository;

    private final VirtualAccountDetailsQueryService virtualAccountDetailsQueryService;

    public VirtualAccountDetailsResource(
        VirtualAccountDetailsService virtualAccountDetailsService,
        VirtualAccountDetailsRepository virtualAccountDetailsRepository,
        VirtualAccountDetailsQueryService virtualAccountDetailsQueryService
    ) {
        this.virtualAccountDetailsService = virtualAccountDetailsService;
        this.virtualAccountDetailsRepository = virtualAccountDetailsRepository;
        this.virtualAccountDetailsQueryService = virtualAccountDetailsQueryService;
    }

    /**
     * {@code POST  /virtual-account-details} : Create a new virtualAccountDetails.
     *
     * @param virtualAccountDetailsDTO the virtualAccountDetailsDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new virtualAccountDetailsDTO, or with status {@code 400 (Bad Request)} if the virtualAccountDetails has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/virtual-account-details")
    public ResponseEntity<VirtualAccountDetailsDTO> createVirtualAccountDetails(
        @Valid @RequestBody VirtualAccountDetailsDTO virtualAccountDetailsDTO
    ) throws URISyntaxException {
        log.debug("REST request to save VirtualAccountDetails : {}", virtualAccountDetailsDTO);
        if (virtualAccountDetailsDTO.getId() != null) {
            throw new BadRequestAlertException("A new virtualAccountDetails cannot already have an ID", ENTITY_NAME, "idexists");
        }
        VirtualAccountDetailsDTO result = virtualAccountDetailsService.save(virtualAccountDetailsDTO);
        return ResponseEntity
            .created(new URI("/api/virtual-account-details/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /virtual-account-details/:id} : Updates an existing virtualAccountDetails.
     *
     * @param id the id of the virtualAccountDetailsDTO to save.
     * @param virtualAccountDetailsDTO the virtualAccountDetailsDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated virtualAccountDetailsDTO,
     * or with status {@code 400 (Bad Request)} if the virtualAccountDetailsDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the virtualAccountDetailsDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/virtual-account-details/{id}")
    public ResponseEntity<VirtualAccountDetailsDTO> updateVirtualAccountDetails(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody VirtualAccountDetailsDTO virtualAccountDetailsDTO
    ) throws URISyntaxException {
        log.debug("REST request to update VirtualAccountDetails : {}, {}", id, virtualAccountDetailsDTO);
        if (virtualAccountDetailsDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, virtualAccountDetailsDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!virtualAccountDetailsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        VirtualAccountDetailsDTO result = virtualAccountDetailsService.save(virtualAccountDetailsDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, virtualAccountDetailsDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /virtual-account-details/:id} : Partial updates given fields of an existing virtualAccountDetails, field will ignore if it is null
     *
     * @param id the id of the virtualAccountDetailsDTO to save.
     * @param virtualAccountDetailsDTO the virtualAccountDetailsDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated virtualAccountDetailsDTO,
     * or with status {@code 400 (Bad Request)} if the virtualAccountDetailsDTO is not valid,
     * or with status {@code 404 (Not Found)} if the virtualAccountDetailsDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the virtualAccountDetailsDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/virtual-account-details/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<VirtualAccountDetailsDTO> partialUpdateVirtualAccountDetails(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody VirtualAccountDetailsDTO virtualAccountDetailsDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update VirtualAccountDetails partially : {}, {}", id, virtualAccountDetailsDTO);
        if (virtualAccountDetailsDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, virtualAccountDetailsDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!virtualAccountDetailsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<VirtualAccountDetailsDTO> result = virtualAccountDetailsService.partialUpdate(virtualAccountDetailsDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, virtualAccountDetailsDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /virtual-account-details} : get all the virtualAccountDetails.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of virtualAccountDetails in body.
     */
    @GetMapping("/virtual-account-details")
    public ResponseEntity<List<VirtualAccountDetailsDTO>> getAllVirtualAccountDetails(
        VirtualAccountDetailsCriteria criteria,
        Pageable pageable
    ) {
        log.debug("REST request to get VirtualAccountDetails by criteria: {}", criteria);
        Page<VirtualAccountDetailsDTO> page = virtualAccountDetailsQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /virtual-account-details/count} : count all the virtualAccountDetails.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/virtual-account-details/count")
    public ResponseEntity<Long> countVirtualAccountDetails(VirtualAccountDetailsCriteria criteria) {
        log.debug("REST request to count VirtualAccountDetails by criteria: {}", criteria);
        return ResponseEntity.ok().body(virtualAccountDetailsQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /virtual-account-details/:id} : get the "id" virtualAccountDetails.
     *
     * @param id the id of the virtualAccountDetailsDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the virtualAccountDetailsDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/virtual-account-details/{id}")
    public ResponseEntity<VirtualAccountDetailsDTO> getVirtualAccountDetails(@PathVariable Long id) {
        log.debug("REST request to get VirtualAccountDetails : {}", id);
        Optional<VirtualAccountDetailsDTO> virtualAccountDetailsDTO = virtualAccountDetailsService.findOne(id);
        return ResponseUtil.wrapOrNotFound(virtualAccountDetailsDTO);
    }

    /**
     * {@code DELETE  /virtual-account-details/:id} : delete the "id" virtualAccountDetails.
     *
     * @param id the id of the virtualAccountDetailsDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/virtual-account-details/{id}")
    public ResponseEntity<Void> deleteVirtualAccountDetails(@PathVariable Long id) {
        log.debug("REST request to delete VirtualAccountDetails : {}", id);
        virtualAccountDetailsService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
