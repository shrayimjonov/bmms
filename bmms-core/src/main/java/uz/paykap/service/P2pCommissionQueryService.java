package uz.paykap.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;
import uz.paykap.domain.P2pCommission;
import uz.paykap.domain.P2pCommission_;
import uz.paykap.repository.P2pCommissionRepository;
import uz.paykap.service.criteria.P2pCommissionCriteria;

import java.util.List;

/**
 * Service for executing complex queries for {@link P2pCommission} entities in the database.
 * The main input is a {@link P2pCommissionCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link P2pCommission} or a {@link Page} of {@link P2pCommission} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class P2pCommissionQueryService extends QueryService<P2pCommission> {

    private final Logger log = LoggerFactory.getLogger(P2pCommissionQueryService.class);

    private final P2pCommissionRepository p2pCommissionRepository;

    public P2pCommissionQueryService(P2pCommissionRepository p2pCommissionRepository) {
        this.p2pCommissionRepository = p2pCommissionRepository;
    }

    /**
     * Return a {@link List} of {@link P2pCommission} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<P2pCommission> findByCriteria(P2pCommissionCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<P2pCommission> specification = createSpecification(criteria);
        return p2pCommissionRepository.findAll(specification);
    }

    /**
     * Return a {@link Page} of {@link P2pCommission} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<P2pCommission> findByCriteria(P2pCommissionCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<P2pCommission> specification = createSpecification(criteria);
        return p2pCommissionRepository.findAll(specification, page);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(P2pCommissionCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<P2pCommission> specification = createSpecification(criteria);
        return p2pCommissionRepository.count(specification);
    }

    /**
     * Function to convert {@link P2pCommissionCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<P2pCommission> createSpecification(P2pCommissionCriteria criteria) {
        Specification<P2pCommission> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), P2pCommission_.id));
            }
            if (criteria.getName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getName(), P2pCommission_.name));
            }
            if (criteria.getUp() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getUp(), P2pCommission_.up));
            }
            if (criteria.getDown() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getDown(), P2pCommission_.down));
            }
            if (criteria.getFee() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getFee(), P2pCommission_.fee));
            }
            if (criteria.getDescription() != null) {
                specification = specification.and(buildStringSpecification(criteria.getDescription(), P2pCommission_.description));
            }
        }
        return specification;
    }
}
