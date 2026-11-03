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
import uz.paykap.domain.RecipientFileSchema;
import uz.paykap.repository.RecipientFileSchemaRepository;
import uz.paykap.service.criteria.RecipientFileSchemaCriteria;
import uz.paykap.service.dto.RecipientFileSchemaDTO;
import uz.paykap.service.mapper.RecipientFileSchemaMapper;

/**
 * Service for executing complex queries for {@link RecipientFileSchema} entities in the database.
 * The main input is a {@link RecipientFileSchemaCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link RecipientFileSchemaDTO} or a {@link Page} of {@link RecipientFileSchemaDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class RecipientFileSchemaQueryService extends QueryService<RecipientFileSchema> {

    private final Logger log = LoggerFactory.getLogger(RecipientFileSchemaQueryService.class);

    private final RecipientFileSchemaRepository recipientFileSchemaRepository;

    private final RecipientFileSchemaMapper recipientFileSchemaMapper;

    public RecipientFileSchemaQueryService(
        RecipientFileSchemaRepository recipientFileSchemaRepository,
        RecipientFileSchemaMapper recipientFileSchemaMapper
    ) {
        this.recipientFileSchemaRepository = recipientFileSchemaRepository;
        this.recipientFileSchemaMapper = recipientFileSchemaMapper;
    }

    /**
     * Return a {@link List} of {@link RecipientFileSchemaDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<RecipientFileSchemaDTO> findByCriteria(RecipientFileSchemaCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<RecipientFileSchema> specification = createSpecification(criteria);
        return recipientFileSchemaMapper.toDto(recipientFileSchemaRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link RecipientFileSchemaDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<RecipientFileSchemaDTO> findByCriteria(RecipientFileSchemaCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<RecipientFileSchema> specification = createSpecification(criteria);
        return recipientFileSchemaRepository.findAll(specification, page).map(recipientFileSchemaMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(RecipientFileSchemaCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<RecipientFileSchema> specification = createSpecification(criteria);
        return recipientFileSchemaRepository.count(specification);
    }

    /**
     * Function to convert {@link RecipientFileSchemaCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<RecipientFileSchema> createSpecification(RecipientFileSchemaCriteria criteria) {
        Specification<RecipientFileSchema> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), RecipientFileSchema_.id));
            }
            if (criteria.getName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getName(), RecipientFileSchema_.name));
            }
            if (criteria.getIdList() != null) {
                specification = specification.and(buildStringSpecification(criteria.getIdList(), RecipientFileSchema_.idList));
            }
            if (criteria.getRoot() != null) {
                specification = specification.and(buildStringSpecification(criteria.getRoot(), RecipientFileSchema_.root));
            }
        }
        return specification;
    }
}
