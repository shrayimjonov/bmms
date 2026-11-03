package uz.paykap.service;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.paykap.domain.ReconcileDiscrepancy;
import uz.paykap.repository.ReconcileDiscrepancyRepository;
import uz.paykap.service.dto.ReconcileDiscrepancyDTO;
import uz.paykap.service.mapper.ReconcileDiscrepancyMapper;

/**
 * Service Implementation for managing {@link ReconcileDiscrepancy}.
 */
@Service
@Transactional
public class ReconcileDiscrepancyService {

    private final Logger log = LoggerFactory.getLogger(ReconcileDiscrepancyService.class);

    private final ReconcileDiscrepancyRepository reconcileDiscrepancyRepository;

    private final ReconcileDiscrepancyMapper reconcileDiscrepancyMapper;

    public ReconcileDiscrepancyService(
        ReconcileDiscrepancyRepository reconcileDiscrepancyRepository,
        ReconcileDiscrepancyMapper reconcileDiscrepancyMapper
    ) {
        this.reconcileDiscrepancyRepository = reconcileDiscrepancyRepository;
        this.reconcileDiscrepancyMapper = reconcileDiscrepancyMapper;
    }

    /**
     * Save a reconcileDiscrepancy.
     *
     * @param reconcileDiscrepancyDTO the entity to save.
     * @return the persisted entity.
     */
    public ReconcileDiscrepancyDTO save(ReconcileDiscrepancyDTO reconcileDiscrepancyDTO) {
        log.debug("Request to save ReconcileDiscrepancy : {}", reconcileDiscrepancyDTO);
        ReconcileDiscrepancy reconcileDiscrepancy = reconcileDiscrepancyMapper.toEntity(reconcileDiscrepancyDTO);
        reconcileDiscrepancy = reconcileDiscrepancyRepository.save(reconcileDiscrepancy);
        return reconcileDiscrepancyMapper.toDto(reconcileDiscrepancy);
    }

    /**
     * Partially update a reconcileDiscrepancy.
     *
     * @param reconcileDiscrepancyDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ReconcileDiscrepancyDTO> partialUpdate(ReconcileDiscrepancyDTO reconcileDiscrepancyDTO) {
        log.debug("Request to partially update ReconcileDiscrepancy : {}", reconcileDiscrepancyDTO);

        return reconcileDiscrepancyRepository
            .findById(reconcileDiscrepancyDTO.getId())
            .map(
                existingReconcileDiscrepancy -> {
                    reconcileDiscrepancyMapper.partialUpdate(existingReconcileDiscrepancy, reconcileDiscrepancyDTO);

                    return existingReconcileDiscrepancy;
                }
            )
            .map(reconcileDiscrepancyRepository::save)
            .map(reconcileDiscrepancyMapper::toDto);
    }

    /**
     * Get all the reconcileDiscrepancies.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<ReconcileDiscrepancyDTO> findAll(Pageable pageable) {
        log.debug("Request to get all ReconcileDiscrepancies");
        return reconcileDiscrepancyRepository.findAll(pageable).map(reconcileDiscrepancyMapper::toDto);
    }

    /**
     * Get one reconcileDiscrepancy by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ReconcileDiscrepancyDTO> findOne(Long id) {
        log.debug("Request to get ReconcileDiscrepancy : {}", id);
        return reconcileDiscrepancyRepository.findById(id).map(reconcileDiscrepancyMapper::toDto);
    }

    /**
     * Delete the reconcileDiscrepancy by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete ReconcileDiscrepancy : {}", id);
        reconcileDiscrepancyRepository.deleteById(id);
    }

    public Optional<ReconcileDiscrepancy> findBySystemTranId(Long systemTranId) {
        log.debug("Request to get ReconcileDiscrepancy by systemTranId: {}", systemTranId);
        return reconcileDiscrepancyRepository.findTop1BySystemTransactionId(systemTranId);
    }

    public ReconcileDiscrepancy save(ReconcileDiscrepancy reconcileDiscrepancy) {
        log.debug("Request to save ReconcileDiscrepancy : {}", reconcileDiscrepancy);
        return reconcileDiscrepancyRepository.save(reconcileDiscrepancy);
    }
}
