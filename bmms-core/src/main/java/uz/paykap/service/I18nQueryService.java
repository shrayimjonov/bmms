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
import uz.paykap.domain.I18n;
import uz.paykap.repository.I18nRepository;
import uz.paykap.service.criteria.I18nCriteria;
import uz.paykap.service.dto.I18nDTO;
import uz.paykap.service.mapper.I18nMapper;

/**
 * Service for executing complex queries for {@link I18n} entities in the database.
 * The main input is a {@link I18nCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link I18nDTO} or a {@link Page} of {@link I18nDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class I18nQueryService extends QueryService<I18n> {

    private final Logger log = LoggerFactory.getLogger(I18nQueryService.class);

    private final I18nRepository i18nRepository;

    private final I18nMapper i18nMapper;

    public I18nQueryService(I18nRepository i18nRepository, I18nMapper i18nMapper) {
        this.i18nRepository = i18nRepository;
        this.i18nMapper = i18nMapper;
    }

    /**
     * Return a {@link List} of {@link I18nDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<I18nDTO> findByCriteria(I18nCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<I18n> specification = createSpecification(criteria);
        return i18nMapper.toDto(i18nRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link I18nDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<I18nDTO> findByCriteria(I18nCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<I18n> specification = createSpecification(criteria);
        return i18nRepository.findAll(specification, page).map(i18nMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(I18nCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<I18n> specification = createSpecification(criteria);
        return i18nRepository.count(specification);
    }

    /**
     * Function to convert {@link I18nCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<I18n> createSpecification(I18nCriteria criteria) {
        Specification<I18n> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), I18n_.id));
            }
            if (criteria.getLang() != null) {
                specification = specification.and(buildSpecification(criteria.getLang(), I18n_.lang));
            }
            if (criteria.getText() != null) {
                specification = specification.and(buildStringSpecification(criteria.getText(), I18n_.text));
            }
        }
        return specification;
    }
}
