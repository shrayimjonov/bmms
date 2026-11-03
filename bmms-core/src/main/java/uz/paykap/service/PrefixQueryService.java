package uz.paykap.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;
import uz.paykap.domain.Field_;
import uz.paykap.domain.Prefix;
import uz.paykap.domain.Prefix_;
import uz.paykap.repository.PrefixRepository;
import uz.paykap.service.criteria.PrefixCriteria;
import uz.paykap.service.dto.PrefixDTO;
import uz.paykap.service.mapper.PrefixMapper;

import javax.persistence.criteria.JoinType;
import java.util.List;

/**
 * Service for executing complex queries for {@link Prefix} entities in the database.
 * The main input is a {@link PrefixCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link PrefixDTO} or a {@link Page} of {@link PrefixDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class PrefixQueryService extends QueryService<Prefix> {

    private final Logger log = LoggerFactory.getLogger(PrefixQueryService.class);

    private final PrefixRepository prefixRepository;

    private final PrefixMapper prefixMapper;

    public PrefixQueryService(PrefixRepository prefixRepository, PrefixMapper prefixMapper) {
        this.prefixRepository = prefixRepository;
        this.prefixMapper = prefixMapper;
    }

    /**
     * Return a {@link List} of {@link PrefixDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<PrefixDTO> findByCriteria(PrefixCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<Prefix> specification = createSpecification(criteria);
        return prefixMapper.toDto(prefixRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link PrefixDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<PrefixDTO> findByCriteria(PrefixCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Prefix> specification = createSpecification(criteria);
        return prefixRepository.findAll(specification, page).map(prefixMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(PrefixCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<Prefix> specification = createSpecification(criteria);
        return prefixRepository.count(specification);
    }

    /**
     * Function to convert {@link PrefixCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Prefix> createSpecification(PrefixCriteria criteria) {
        Specification<Prefix> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), Prefix_.id));
            }
            if (criteria.getType() != null) {
                specification = specification.and(buildSpecification(criteria.getType(), Prefix_.type));
            }
            if (criteria.getValue() != null) {
                specification = specification.and(buildStringSpecification(criteria.getValue(), Prefix_.value));
            }
            if (criteria.getOutput() != null) {
                specification = specification.and(buildSpecification(criteria.getOutput(), Prefix_.output));
            }
            if (criteria.getRequireField() != null) {
                specification = specification.and(buildStringSpecification(criteria.getRequireField(), Prefix_.requireField));
            }
            if (criteria.getFieldId() != null) {
                specification =
                    specification.and(
                        buildSpecification(criteria.getFieldId(), root -> root.join(Prefix_.field, JoinType.LEFT).get(Field_.id))
                    );
            }
        }
        return specification;
    }
}
