package uz.paykap.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;
import uz.paykap.domain.P2pCondition;
import uz.paykap.domain.P2pCondition_;
import uz.paykap.repository.P2pConditionRepository;
import uz.paykap.service.criteria.P2pConditionCriteria;

import java.util.List;

/**
 * Service for executing complex queries for {@link P2pCondition} entities in the database.
 * The main input is a {@link P2pConditionCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link P2pCondition} or a {@link Page} of {@link P2pCondition} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class P2pConditionQueryService extends QueryService<P2pCondition> {

    private final Logger log = LoggerFactory.getLogger(P2pConditionQueryService.class);

    private final P2pConditionRepository p2pConditionRepository;

    public P2pConditionQueryService(P2pConditionRepository p2pConditionRepository) {
        this.p2pConditionRepository = p2pConditionRepository;
    }

    /**
     * Return a {@link List} of {@link P2pCondition} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<P2pCondition> findByCriteria(P2pConditionCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<P2pCondition> specification = createSpecification(criteria);
        return p2pConditionRepository.findAll(specification);
    }

    /**
     * Return a {@link Page} of {@link P2pCondition} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<P2pCondition> findByCriteria(P2pConditionCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<P2pCondition> specification = createSpecification(criteria);
        return p2pConditionRepository.findAll(specification, page);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(P2pConditionCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<P2pCondition> specification = createSpecification(criteria);
        return p2pConditionRepository.count(specification);
    }

    /**
     * Function to convert {@link P2pConditionCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<P2pCondition> createSpecification(P2pConditionCriteria criteria) {
        Specification<P2pCondition> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), P2pCondition_.id));
            }
            if (criteria.getValue() != null) {
                specification = specification.and(buildStringSpecification(criteria.getValue(), P2pCondition_.value));
            }
            if (criteria.getDescription() != null) {
                specification = specification.and(buildStringSpecification(criteria.getDescription(), P2pCondition_.description));
            }
        }
        return specification;
    }
}
