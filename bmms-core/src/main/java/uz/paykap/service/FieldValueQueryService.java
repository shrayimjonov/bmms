package uz.paykap.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;
import uz.paykap.domain.FieldValue;
import uz.paykap.domain.FieldValue_;
import uz.paykap.domain.Field_;
import uz.paykap.domain.I18n_;
import uz.paykap.repository.FieldValueRepository;
import uz.paykap.service.criteria.FieldValueCriteria;
import uz.paykap.service.dto.FieldValueDTO;
import uz.paykap.service.mapper.FieldValueMapper;

import javax.persistence.criteria.JoinType;
import java.util.List;

/**
 * Service for executing complex queries for {@link FieldValue} entities in the database.
 * The main input is a {@link FieldValueCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link FieldValueDTO} or a {@link Page} of {@link FieldValueDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class FieldValueQueryService extends QueryService<FieldValue> {

    private final Logger log = LoggerFactory.getLogger(FieldValueQueryService.class);

    private final FieldValueRepository fieldValueRepository;

    private final FieldValueMapper fieldValueMapper;

    public FieldValueQueryService(FieldValueRepository fieldValueRepository, FieldValueMapper fieldValueMapper) {
        this.fieldValueRepository = fieldValueRepository;
        this.fieldValueMapper = fieldValueMapper;
    }

    /**
     * Return a {@link List} of {@link FieldValueDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<FieldValueDTO> findByCriteria(FieldValueCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<FieldValue> specification = createSpecification(criteria);
        return fieldValueMapper.toDto(fieldValueRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link FieldValueDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<FieldValueDTO> findByCriteria(FieldValueCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<FieldValue> specification = createSpecification(criteria);
        return fieldValueRepository.findAll(specification, page).map(fieldValueMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(FieldValueCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<FieldValue> specification = createSpecification(criteria);
        return fieldValueRepository.count(specification);
    }

    /**
     * Function to convert {@link FieldValueCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<FieldValue> createSpecification(FieldValueCriteria criteria) {
        Specification<FieldValue> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), FieldValue_.id));
            }
            if (criteria.getValue() != null) {
                specification = specification.and(buildStringSpecification(criteria.getValue(), FieldValue_.value));
            }
            if (criteria.getAmount() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getAmount(), FieldValue_.amount));
            }
            if (criteria.getFilterBy() != null) {
                specification = specification.and(buildStringSpecification(criteria.getFilterBy(), FieldValue_.filterBy));
            }
            if (criteria.getOrd() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getOrd(), FieldValue_.ord));
            }
            if (criteria.getCheckId() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCheckId(), FieldValue_.checkId));
            }
            if (criteria.getFieldId() != null) {
                specification =
                    specification.and(
                        buildSpecification(criteria.getFieldId(), root -> root.join(FieldValue_.field, JoinType.LEFT).get(Field_.id))
                    );
            }
            if (criteria.getTitleId() != null) {
                specification =
                    specification.and(
                        buildSpecification(criteria.getTitleId(), root -> root.join(FieldValue_.titles, JoinType.LEFT).get(I18n_.id))
                    );
            }
            if (criteria.getDescriptionId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getDescriptionId(),
                            root -> root.join(FieldValue_.descriptions, JoinType.LEFT).get(I18n_.id)
                        )
                    );
            }
        }
        return specification;
    }
}
