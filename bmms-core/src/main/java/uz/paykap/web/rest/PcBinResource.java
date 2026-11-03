package uz.paykap.web.rest;

import com.codahale.metrics.annotation.Timed;
import uz.paykap.service.PcBinService;
import uz.paykap.web.rest.errors.BadRequestAlertException;
import uz.paykap.web.rest.util.HeaderUtil;
import uz.paykap.web.rest.util.PaginationUtil;
import uz.paykap.service.dto.PcBinDTO;
import uz.paykap.service.dto.PcBinCriteria;
import uz.paykap.service.PcBinQueryService;
import tech.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing PcBin.
 */
@RestController
@RequestMapping("/api")
public class PcBinResource {

    private final Logger log = LoggerFactory.getLogger(PcBinResource.class);

    private static final String ENTITY_NAME = "pkbillmsPcBin";

    private final PcBinService pcBinService;

    private final PcBinQueryService pcBinQueryService;

    public PcBinResource(PcBinService pcBinService, PcBinQueryService pcBinQueryService) {
        this.pcBinService = pcBinService;
        this.pcBinQueryService = pcBinQueryService;
    }

    /**
     * POST  /pc-bins : Create a new pcBin.
     *
     * @param pcBinDTO the pcBinDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new pcBinDTO, or with status 400 (Bad Request) if the pcBin has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/pc-bins")
    @Timed
    public ResponseEntity<PcBinDTO> createPcBin(@Valid @RequestBody PcBinDTO pcBinDTO) throws URISyntaxException {
        log.debug("REST request to save PcBin : {}", pcBinDTO);
        if (pcBinDTO.getId() != null) {
            throw new BadRequestAlertException("A new pcBin cannot already have an ID", ENTITY_NAME, "idexists");
        }
        PcBinDTO result = pcBinService.save(pcBinDTO);
        return ResponseEntity.created(new URI("/api/pc-bins/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /pc-bins : Updates an existing pcBin.
     *
     * @param pcBinDTO the pcBinDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated pcBinDTO,
     * or with status 400 (Bad Request) if the pcBinDTO is not valid,
     * or with status 500 (Internal Server Error) if the pcBinDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/pc-bins")
    @Timed
    public ResponseEntity<PcBinDTO> updatePcBin(@Valid @RequestBody PcBinDTO pcBinDTO) throws URISyntaxException {
        log.debug("REST request to update PcBin : {}", pcBinDTO);
        if (pcBinDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        PcBinDTO result = pcBinService.save(pcBinDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, pcBinDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /pc-bins : get all the pcBins.
     *
     * @param pageable the pagination information
     * @param criteria the criterias which the requested entities should match
     * @return the ResponseEntity with status 200 (OK) and the list of pcBins in body
     */
    @GetMapping("/pc-bins")
    @Timed
    public ResponseEntity<List<PcBinDTO>> getAllPcBins(PcBinCriteria criteria, Pageable pageable) {
        log.debug("REST request to get PcBins by criteria: {}", criteria);
        Page<PcBinDTO> page = pcBinQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/pc-bins");
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
    * GET  /pc-bins/count : count all the pcBins.
    *
    * @param criteria the criterias which the requested entities should match
    * @return the ResponseEntity with status 200 (OK) and the count in body
    */
    @GetMapping("/pc-bins/count")
    @Timed
    public ResponseEntity<Long> countPcBins(PcBinCriteria criteria) {
        log.debug("REST request to count PcBins by criteria: {}", criteria);
        return ResponseEntity.ok().body(pcBinQueryService.countByCriteria(criteria));
    }

    /**
     * GET  /pc-bins/:id : get the "id" pcBin.
     *
     * @param id the id of the pcBinDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the pcBinDTO, or with status 404 (Not Found)
     */
    @GetMapping("/pc-bins/{id}")
    @Timed
    public ResponseEntity<PcBinDTO> getPcBin(@PathVariable Long id) {
        log.debug("REST request to get PcBin : {}", id);
        Optional<PcBinDTO> pcBinDTO = pcBinService.findOne(id);
        return ResponseUtil.wrapOrNotFound(pcBinDTO);
    }

    /**
     * DELETE  /pc-bins/:id : delete the "id" pcBin.
     *
     * @param id the id of the pcBinDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/pc-bins/{id}")
    @Timed
    public ResponseEntity<Void> deletePcBin(@PathVariable Long id) {
        log.debug("REST request to delete PcBin : {}", id);
        pcBinService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
