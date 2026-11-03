package uz.paykap.service;

import java.util.List;
import javax.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;
import uz.paykap.domain.*; // for static metamodels
import uz.paykap.domain.ReconcileDiscrepancy;
import uz.paykap.repository.ReconcileDiscrepancyRepository;
import uz.paykap.service.criteria.ReconcileDiscrepancyCriteria;
import uz.paykap.service.dto.ReconcileDiscrepancyDTO;
import uz.paykap.service.mapper.ReconcileDiscrepancyMapper;

/**
 * Service for executing complex queries for {@link ReconcileDiscrepancy} entities in the database.
 * The main input is a {@link ReconcileDiscrepancyCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link ReconcileDiscrepancyDTO} or a {@link Page} of {@link ReconcileDiscrepancyDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class ReconcileDiscrepancyQueryService extends QueryService<ReconcileDiscrepancy> {

    private final Logger log = LoggerFactory.getLogger(ReconcileDiscrepancyQueryService.class);

    private final ReconcileDiscrepancyRepository reconcileDiscrepancyRepository;

    private final ReconcileDiscrepancyMapper reconcileDiscrepancyMapper;

    public ReconcileDiscrepancyQueryService(
        ReconcileDiscrepancyRepository reconcileDiscrepancyRepository,
        ReconcileDiscrepancyMapper reconcileDiscrepancyMapper
    ) {
        this.reconcileDiscrepancyRepository = reconcileDiscrepancyRepository;
        this.reconcileDiscrepancyMapper = reconcileDiscrepancyMapper;
    }

    /**
     * Return a {@link List} of {@link ReconcileDiscrepancyDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<ReconcileDiscrepancyDTO> findByCriteria(ReconcileDiscrepancyCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<ReconcileDiscrepancy> specification = createSpecification(criteria);
        return reconcileDiscrepancyMapper.toDto(reconcileDiscrepancyRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link ReconcileDiscrepancyDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<ReconcileDiscrepancyDTO> findByCriteria(ReconcileDiscrepancyCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<ReconcileDiscrepancy> specification = createSpecification(criteria);
        return reconcileDiscrepancyRepository.findAll(specification, page).map(reconcileDiscrepancyMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(ReconcileDiscrepancyCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<ReconcileDiscrepancy> specification = createSpecification(criteria);
        return reconcileDiscrepancyRepository.count(specification);
    }

    /**
     * Function to convert {@link ReconcileDiscrepancyCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<ReconcileDiscrepancy> createSpecification(ReconcileDiscrepancyCriteria criteria) {
        Specification<ReconcileDiscrepancy> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), ReconcileDiscrepancy_.id));
            }
            if (criteria.getGatewayName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getGatewayName(), ReconcileDiscrepancy_.gatewayName));
            }
            if (criteria.getSystemTransactionId() != null) {
                specification =
                    specification.and(
                        buildRangeSpecification(criteria.getSystemTransactionId(), ReconcileDiscrepancy_.systemTransactionId)
                    );
            }
            if (criteria.getGatewayTransactionId() != null) {
                specification =
                    specification.and(
                        buildStringSpecification(criteria.getGatewayTransactionId(), ReconcileDiscrepancy_.gatewayTransactionId)
                    );
            }
            if (criteria.getSystemAmount() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getSystemAmount(), ReconcileDiscrepancy_.systemAmount));
            }
            if (criteria.getGatewayAmount() != null) {
                specification =
                    specification.and(buildRangeSpecification(criteria.getGatewayAmount(), ReconcileDiscrepancy_.gatewayAmount));
            }
            if (criteria.getSystemStatus() != null) {
                specification = specification.and(buildSpecification(criteria.getSystemStatus(), ReconcileDiscrepancy_.systemStatus));
            }
            if (criteria.getGatewayStatus() != null) {
                specification = specification.and(buildSpecification(criteria.getGatewayStatus(), ReconcileDiscrepancy_.gatewayStatus));
            }
            if (criteria.getSystemDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getSystemDate(), ReconcileDiscrepancy_.systemDate));
            }
            if (criteria.getGatewayDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getGatewayDate(), ReconcileDiscrepancy_.gatewayDate));
            }
            if (criteria.getClosed() != null) {
                specification = specification.and(buildSpecification(criteria.getClosed(), ReconcileDiscrepancy_.closed));
            }
            if (criteria.getComment() != null) {
                specification = specification.and(buildStringSpecification(criteria.getComment(), ReconcileDiscrepancy_.comment));
            }
            if (criteria.getReconcileSummaryId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getReconcileSummaryId(),
                            root -> root.join(ReconcileDiscrepancy_.reconcileSummary, JoinType.LEFT).get(ReconcileSummary_.id)
                        )
                    );
            }
        }
        return specification;
    }
}
