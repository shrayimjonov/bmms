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
import uz.devops.bmcore.enumeration.PayType;
import uz.paykap.repository.SenderRepository;
import uz.paykap.service.SenderQueryService;
import uz.paykap.service.SenderService;
import uz.paykap.service.criteria.SenderCriteria;
import uz.paykap.service.dto.SenderDTO;
import uz.paykap.web.rest.errors.BadRequestAlertException;

/**
 * REST controller for managing {@link uz.paykap.domain.Sender}.
 */
@ApiIgnore
@RestController
@RequestMapping("/api")
public class SenderResource {

    private final Logger log = LoggerFactory.getLogger(SenderResource.class);

    private static final String ENTITY_NAME = "pkbillmsSender";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final SenderService senderService;

    private final SenderRepository senderRepository;

    private final SenderQueryService senderQueryService;

    public SenderResource(SenderService senderService, SenderRepository senderRepository, SenderQueryService senderQueryService) {
        this.senderService = senderService;
        this.senderRepository = senderRepository;
        this.senderQueryService = senderQueryService;
    }

    /**
     * {@code POST  /senders} : Create a new sender.
     *
     * @param senderDTO the senderDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new senderDTO, or with status {@code 400 (Bad Request)} if the sender has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/senders")
    public ResponseEntity<SenderDTO> createSender(@Valid @RequestBody SenderDTO senderDTO) throws URISyntaxException {
        log.debug("REST request to save Sender : {}", senderDTO);
        if (senderDTO.getId() != null) {
            throw new BadRequestAlertException("A new sender cannot already have an ID", ENTITY_NAME, "idexists");
        }
        SenderDTO result = senderService.save(senderDTO);
        return ResponseEntity
            .created(new URI("/api/senders/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /senders/:id} : Updates an existing sender.
     *
     * @param id the id of the senderDTO to save.
     * @param senderDTO the senderDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated senderDTO,
     * or with status {@code 400 (Bad Request)} if the senderDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the senderDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/senders/{id}")
    public ResponseEntity<SenderDTO> updateSender(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody SenderDTO senderDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Sender : {}, {}", id, senderDTO);
        if (senderDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, senderDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!senderRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        SenderDTO result = senderService.save(senderDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, senderDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /senders/:id} : Partial updates given fields of an existing sender, field will ignore if it is null
     *
     * @param id the id of the senderDTO to save.
     * @param senderDTO the senderDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated senderDTO,
     * or with status {@code 400 (Bad Request)} if the senderDTO is not valid,
     * or with status {@code 404 (Not Found)} if the senderDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the senderDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/senders/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<SenderDTO> partialUpdateSender(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody SenderDTO senderDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Sender partially : {}, {}", id, senderDTO);
        if (senderDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, senderDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!senderRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<SenderDTO> result = senderService.partialUpdate(senderDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, senderDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /senders} : get all the senders.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of senders in body.
     */
    @GetMapping("/senders")
    public ResponseEntity<List<SenderDTO>> getAllSenders(SenderCriteria criteria, Pageable pageable) {
        log.debug("REST request to get Senders by criteria: {}", criteria);
        Page<SenderDTO> page = senderQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /senders/count} : count all the senders.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/senders/count")
    public ResponseEntity<Long> countSenders(SenderCriteria criteria) {
        log.debug("REST request to count Senders by criteria: {}", criteria);
        return ResponseEntity.ok().body(senderQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /senders/:id} : get the "id" sender.
     *
     * @param id the id of the senderDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the senderDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/senders/{id}")
    public ResponseEntity<SenderDTO> getSender(@PathVariable Long id) {
        log.debug("REST request to get Sender : {}", id);
        Optional<SenderDTO> senderDTO = senderService.findOne(id);
        return ResponseUtil.wrapOrNotFound(senderDTO);
    }

    /**
     * {@code DELETE  /senders/:id} : delete the "id" sender.
     *
     * @param id the id of the senderDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/senders/{id}")
    public ResponseEntity<Void> deleteSender(@PathVariable Long id) {
        log.debug("REST request to delete Sender : {}", id);
        senderService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    @PostMapping("/senders/beans")
    public ResponseEntity<String[]> getBeans(@Valid @RequestBody PayType payType) {
        log.debug("REST request to get beans with PayType : {}", payType);
        if (payType == null) {
            throw new BadRequestAlertException("Cannot get beans, when payType null", ENTITY_NAME, "payTypeNull");
        }

        return ResponseEntity.ok(senderService.getBeans(payType));
    }
}
