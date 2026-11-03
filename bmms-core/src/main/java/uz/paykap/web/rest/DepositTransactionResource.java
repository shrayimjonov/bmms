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
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;
import uz.paykap.repository.DepositTransactionRepository;
import uz.paykap.service.DepositTransactionQueryService;
import uz.paykap.service.DepositTransactionService;
import uz.paykap.service.criteria.DepositTransactionCriteria;
import uz.paykap.service.dto.DepositTransactionDTO;
import uz.paykap.web.rest.errors.BadRequestAlertException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * REST controller for managing {@link uz.paykap.domain.DepositTransaction}.
 */
@RestController
@RequestMapping("/api")
public class DepositTransactionResource {

    private final Logger log = LoggerFactory.getLogger(DepositTransactionResource.class);

    private static final String ENTITY_NAME = "pkbillmsDepositTransaction";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final DepositTransactionService depositTransactionService;

    private final DepositTransactionRepository depositTransactionRepository;

    private final DepositTransactionQueryService depositTransactionQueryService;

    public DepositTransactionResource(
        DepositTransactionService depositTransactionService,
        DepositTransactionRepository depositTransactionRepository,
        DepositTransactionQueryService depositTransactionQueryService
    ) {
        this.depositTransactionService = depositTransactionService;
        this.depositTransactionRepository = depositTransactionRepository;
        this.depositTransactionQueryService = depositTransactionQueryService;
    }

    /**
     * {@code POST  /deposit-transactions} : Create a new depositTransaction.
     *
     * @param depositTransactionDTO the depositTransactionDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new depositTransactionDTO, or with status {@code 400 (Bad Request)} if the depositTransaction has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/deposit-transactions")
    public ResponseEntity<DepositTransactionDTO> createDepositTransaction(@Valid @RequestBody DepositTransactionDTO depositTransactionDTO)
        throws URISyntaxException {
        log.debug("REST request to save DepositTransaction : {}", depositTransactionDTO);
        if (depositTransactionDTO.getId() != null) {
            throw new BadRequestAlertException("A new depositTransaction cannot already have an ID", ENTITY_NAME, "idexists");
        }
        DepositTransactionDTO result = depositTransactionService.save(depositTransactionDTO);
        return ResponseEntity
            .created(new URI("/api/deposit-transactions/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /deposit-transactions/:id} : Updates an existing depositTransaction.
     *
     * @param id the id of the depositTransactionDTO to save.
     * @param depositTransactionDTO the depositTransactionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated depositTransactionDTO,
     * or with status {@code 400 (Bad Request)} if the depositTransactionDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the depositTransactionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/deposit-transactions/{id}")
    public ResponseEntity<DepositTransactionDTO> updateDepositTransaction(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody DepositTransactionDTO depositTransactionDTO
    ) throws URISyntaxException {
        log.debug("REST request to update DepositTransaction : {}, {}", id, depositTransactionDTO);
        if (depositTransactionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, depositTransactionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!depositTransactionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        DepositTransactionDTO result = depositTransactionService.save(depositTransactionDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, depositTransactionDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /deposit-transactions/:id} : Partial updates given fields of an existing depositTransaction, field will ignore if it is null
     *
     * @param id the id of the depositTransactionDTO to save.
     * @param depositTransactionDTO the depositTransactionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated depositTransactionDTO,
     * or with status {@code 400 (Bad Request)} if the depositTransactionDTO is not valid,
     * or with status {@code 404 (Not Found)} if the depositTransactionDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the depositTransactionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/deposit-transactions/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<DepositTransactionDTO> partialUpdateDepositTransaction(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody DepositTransactionDTO depositTransactionDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update DepositTransaction partially : {}, {}", id, depositTransactionDTO);
        if (depositTransactionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, depositTransactionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!depositTransactionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<DepositTransactionDTO> result = depositTransactionService.partialUpdate(depositTransactionDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, depositTransactionDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /deposit-transactions} : get all the depositTransactions.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of depositTransactions in body.
     */
    @GetMapping("/deposit-transactions")
    public ResponseEntity<List<DepositTransactionDTO>> getAllDepositTransactions(DepositTransactionCriteria criteria, Pageable pageable, Boolean unPaged) {
        log.debug("REST request to get DepositTransactions by criteria: {}", criteria);
        Page<DepositTransactionDTO> page = depositTransactionQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /deposit-transactions/count} : count all the depositTransactions.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/deposit-transactions/count")
    public ResponseEntity<Long> countDepositTransactions(DepositTransactionCriteria criteria) {
        log.debug("REST request to count DepositTransactions by criteria: {}", criteria);
        return ResponseEntity.ok().body(depositTransactionQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /deposit-transactions/:id} : get the "id" depositTransaction.
     *
     * @param id the id of the depositTransactionDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the depositTransactionDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/deposit-transactions/{id}")
    public ResponseEntity<DepositTransactionDTO> getDepositTransaction(@PathVariable Long id) {
        log.debug("REST request to get DepositTransaction : {}", id);
        Optional<DepositTransactionDTO> depositTransactionDTO = depositTransactionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(depositTransactionDTO);
    }

    /**
     * {@code DELETE  /deposit-transactions/:id} : delete the "id" depositTransaction.
     *
     * @param id the id of the depositTransactionDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/deposit-transactions/{id}")
    public ResponseEntity<Void> deleteDepositTransaction(@PathVariable Long id) {
        log.debug("REST request to delete DepositTransaction : {}", id);
        depositTransactionService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
