package uz.paykap.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;
import uz.paykap.domain.*;
import uz.paykap.repository.FieldRepository;
import uz.paykap.service.criteria.FieldCriteria;
import uz.paykap.service.dto.FieldDTO;
import uz.paykap.service.mapper.FieldMapper;

import javax.persistence.criteria.JoinType;
import java.util.List;

/**
 * Service for executing complex queries for {@link Field} entities in the database.
 * The main input is a {@link FieldCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link FieldDTO} or a {@link Page} of {@link FieldDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class FieldQueryService extends QueryService<Field> {

    private final Logger log = LoggerFactory.getLogger(FieldQueryService.class);

    private final FieldRepository fieldRepository;

    private final FieldMapper fieldMapper;

    public FieldQueryService(FieldRepository fieldRepository, FieldMapper fieldMapper) {
        this.fieldRepository = fieldRepository;
        this.fieldMapper = fieldMapper;
    }

    /**
     * Return a {@link List} of {@link FieldDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<FieldDTO> findByCriteria(FieldCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<Field> specification = createSpecification(criteria);
        return fieldMapper.toDto(fieldRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link FieldDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<FieldDTO> findByCriteria(FieldCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Field> specification = createSpecification(criteria);
        return fieldRepository.findAll(specification, page).map(fieldMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(FieldCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<Field> specification = createSpecification(criteria);
        return fieldRepository.count(specification);
    }

    /**
     * Function to convert {@link FieldCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Field> createSpecification(FieldCriteria criteria) {
        Specification<Field> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), Field_.id));
            }
            if (criteria.getName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getName(), Field_.name));
            }
            if (criteria.getExtId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getExtId(), Field_.extId));
            }
            if (criteria.getType() != null) {
                specification = specification.and(buildSpecification(criteria.getType(), Field_.type));
            }
            if (criteria.getControlType() != null) {
                specification = specification.and(buildSpecification(criteria.getControlType(), Field_.controlType));
            }
            if (criteria.getContent() != null) {
                specification = specification.and(buildStringSpecification(criteria.getContent(), Field_.content));
            }
            if (criteria.getValidation() != null) {
                specification = specification.and(buildStringSpecification(criteria.getValidation(), Field_.validation));
            }
            if (criteria.getPlaceholder() != null) {
                specification = specification.and(buildStringSpecification(criteria.getPlaceholder(), Field_.placeholder));
            }
            if (criteria.getMask() != null) {
                specification = specification.and(buildStringSpecification(criteria.getMask(), Field_.mask));
            }
            if (criteria.getMaskSymbol() != null) {
                specification = specification.and(buildStringSpecification(criteria.getMaskSymbol(), Field_.maskSymbol));
            }
            if (criteria.getLengthMin() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getLengthMin(), Field_.lengthMin));
            }
            if (criteria.getLengthMax() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getLengthMax(), Field_.lengthMax));
            }
            if (criteria.getRequireField() != null) {
                specification = specification.and(buildStringSpecification(criteria.getRequireField(), Field_.requireField));
            }
            if (criteria.getFilterBy() != null) {
                specification = specification.and(buildStringSpecification(criteria.getFilterBy(), Field_.filterBy));
            }
            if (criteria.getOptional() != null) {
                specification = specification.and(buildSpecification(criteria.getOptional(), Field_.optional));
            }
            if (criteria.getOutput() != null) {
                specification = specification.and(buildSpecification(criteria.getOutput(), Field_.output));
            }
            if (criteria.getVisibility() != null) {
                specification = specification.and(buildSpecification(criteria.getVisibility(), Field_.visibility));
            }
            if (criteria.getOrd() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getOrd(), Field_.ord));
            }
            if (criteria.getRequestType() != null) {
                specification = specification.and(buildSpecification(criteria.getRequestType(), Field_.requestType));
            }
            if (criteria.getRecipientId() != null) {
                specification =
                    specification.and(
                        buildSpecification(criteria.getRecipientId(), root -> root.join(Field_.recipient, JoinType.LEFT).get(Recipient_.id))
                    );
            }
            if (criteria.getTitleId() != null) {
                specification =
                    specification.and(
                        buildSpecification(criteria.getTitleId(), root -> root.join(Field_.titles, JoinType.LEFT).get(I18n_.id))
                    );
            }
            if (criteria.getDescriptionId() != null) {
                specification =
                    specification.and(
                        buildSpecification(criteria.getDescriptionId(), root -> root.join(Field_.descriptions, JoinType.LEFT).get(I18n_.id))
                    );
            }
            if (criteria.getValueId() != null) {
                specification =
                    specification.and(
                        buildSpecification(criteria.getValueId(), root -> root.join(Field_.values, JoinType.LEFT).get(FieldValue_.id))
                    );
            }
            if (criteria.getPrefixId() != null) {
                specification =
                    specification.and(
                        buildSpecification(criteria.getPrefixId(), root -> root.join(Field_.prefixes, JoinType.LEFT).get(Prefix_.id))
                    );
            }
        }
        return specification;
    }
}
