package uz.paykap.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.paykap.domain.ReconcileSummary;
import uz.paykap.domain.ReconcileSummary_;
import uz.paykap.repository.ReconcileSummaryRepository;
import uz.paykap.service.criteria.ReconcileSummaryCriteria;
import uz.paykap.service.dto.ReconcileSummaryDTO;
import uz.paykap.service.mapper.ReconcileSummaryMapper;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link ReconcileSummary} entities in the database.
 * The main input is a {@link ReconcileSummaryCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link ReconcileSummaryDTO} or a {@link Page} of {@link ReconcileSummaryDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class ReconcileSummaryQueryService extends QueryService<ReconcileSummary> {

    private final Logger log = LoggerFactory.getLogger(ReconcileSummaryQueryService.class);

    private final ReconcileSummaryRepository reconcileSummaryRepository;

    private final ReconcileSummaryMapper reconcileSummaryMapper;

    public ReconcileSummaryQueryService(
        ReconcileSummaryRepository reconcileSummaryRepository,
        ReconcileSummaryMapper reconcileSummaryMapper
    ) {
        this.reconcileSummaryRepository = reconcileSummaryRepository;
        this.reconcileSummaryMapper = reconcileSummaryMapper;
    }

    /**
     * Return a {@link List} of {@link ReconcileSummaryDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<ReconcileSummaryDTO> findByCriteria(ReconcileSummaryCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<ReconcileSummary> specification = createSpecification(criteria);
        return reconcileSummaryMapper.toDto(reconcileSummaryRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link ReconcileSummaryDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<ReconcileSummaryDTO> findByCriteria(ReconcileSummaryCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<ReconcileSummary> specification = createSpecification(criteria);
        return reconcileSummaryRepository.findAll(specification, page).map(reconcileSummaryMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(ReconcileSummaryCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<ReconcileSummary> specification = createSpecification(criteria);
        return reconcileSummaryRepository.count(specification);
    }

    /**
     * Function to convert {@link ReconcileSummaryCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<ReconcileSummary> createSpecification(ReconcileSummaryCriteria criteria) {
        Specification<ReconcileSummary> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), ReconcileSummary_.id));
            }
            if (criteria.getGatewayId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getGatewayId(), ReconcileSummary_.gatewayId));
            }
            if (criteria.getSystemAmount() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getSystemAmount(), ReconcileSummary_.systemAmount));
            }
            if (criteria.getGatewayAmount() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getGatewayAmount(), ReconcileSummary_.gatewayAmount));
            }
            if (criteria.getSystemCount() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getSystemCount(), ReconcileSummary_.systemCount));
            }
            if (criteria.getGatewayCount() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getGatewayCount(), ReconcileSummary_.gatewayCount));
            }
            if (criteria.getDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getDate(), ReconcileSummary_.date));
            }
            if (criteria.getStatus() != null) {
                specification = specification.and(buildSpecification(criteria.getStatus(), ReconcileSummary_.status));
            }
            if (criteria.getSuccess() != null) {
                specification = specification.and(buildSpecification(criteria.getSuccess(), ReconcileSummary_.success));
            }
            if (criteria.getPeriod() != null) {
                specification = specification.and(buildSpecification(criteria.getPeriod(), ReconcileSummary_.period));
            }
            if (criteria.getGatewayName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getGatewayName(), ReconcileSummary_.gatewayName));
            }
            if (criteria.getFromDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getFromDate(), ReconcileSummary_.fromDate));
            }
            if (criteria.getToDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getToDate(), ReconcileSummary_.toDate));
            }
            if (criteria.getType() != null) {
                specification = specification.and(buildSpecification(criteria.getType(), ReconcileSummary_.type));
            }
        }
        return specification;
    }
}
