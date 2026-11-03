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
import uz.paykap.domain.BookkeepingTemplate;
import uz.paykap.repository.BookkeepingTemplateRepository;
import uz.paykap.service.criteria.BookkeepingTemplateCriteria;
import uz.paykap.service.dto.BookkeepingTemplateDTO;
import uz.paykap.service.mapper.BookkeepingTemplateMapper;

/**
 * Service for executing complex queries for {@link BookkeepingTemplate} entities in the database.
 * The main input is a {@link BookkeepingTemplateCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link BookkeepingTemplateDTO} or a {@link Page} of {@link BookkeepingTemplateDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class BookkeepingTemplateQueryService extends QueryService<BookkeepingTemplate> {

    private final Logger log = LoggerFactory.getLogger(BookkeepingTemplateQueryService.class);

    private final BookkeepingTemplateRepository bookkeepingTemplateRepository;

    private final BookkeepingTemplateMapper bookkeepingTemplateMapper;

    public BookkeepingTemplateQueryService(
        BookkeepingTemplateRepository bookkeepingTemplateRepository,
        BookkeepingTemplateMapper bookkeepingTemplateMapper
    ) {
        this.bookkeepingTemplateRepository = bookkeepingTemplateRepository;
        this.bookkeepingTemplateMapper = bookkeepingTemplateMapper;
    }

    /**
     * Return a {@link List} of {@link BookkeepingTemplateDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<BookkeepingTemplateDTO> findByCriteria(BookkeepingTemplateCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<BookkeepingTemplate> specification = createSpecification(criteria);
        return bookkeepingTemplateMapper.toDto(bookkeepingTemplateRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link BookkeepingTemplateDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<BookkeepingTemplateDTO> findByCriteria(BookkeepingTemplateCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<BookkeepingTemplate> specification = createSpecification(criteria);
        return bookkeepingTemplateRepository.findAll(specification, page).map(bookkeepingTemplateMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(BookkeepingTemplateCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<BookkeepingTemplate> specification = createSpecification(criteria);
        return bookkeepingTemplateRepository.count(specification);
    }

    /**
     * Function to convert {@link BookkeepingTemplateCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<BookkeepingTemplate> createSpecification(BookkeepingTemplateCriteria criteria) {
        Specification<BookkeepingTemplate> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), BookkeepingTemplate_.id));
            }
            if (criteria.getGroupId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getGroupId(), BookkeepingTemplate_.groupId));
            }
            if (criteria.getsMfo() != null) {
                specification = specification.and(buildStringSpecification(criteria.getsMfo(), BookkeepingTemplate_.sMfo));
            }
            if (criteria.getsInn() != null) {
                specification = specification.and(buildStringSpecification(criteria.getsInn(), BookkeepingTemplate_.sInn));
            }
            if (criteria.getsAccount() != null) {
                specification = specification.and(buildStringSpecification(criteria.getsAccount(), BookkeepingTemplate_.sAccount));
            }
            if (criteria.getrMfo() != null) {
                specification = specification.and(buildStringSpecification(criteria.getrMfo(), BookkeepingTemplate_.rMfo));
            }
            if (criteria.getrInn() != null) {
                specification = specification.and(buildStringSpecification(criteria.getrInn(), BookkeepingTemplate_.rInn));
            }
            if (criteria.getrAccount() != null) {
                specification = specification.and(buildStringSpecification(criteria.getrAccount(), BookkeepingTemplate_.rAccount));
            }
            if (criteria.getCalculateAmountRule() != null) {
                specification =
                    specification.and(
                        buildStringSpecification(criteria.getCalculateAmountRule(), BookkeepingTemplate_.calculateAmountRule)
                    );
            }
            if (criteria.getPeriod() != null) {
                specification = specification.and(buildSpecification(criteria.getPeriod(), BookkeepingTemplate_.period));
            }
            if (criteria.getPayTypes() != null) {
                //                specification = specification.and(buildSpecification(criteria.getPayTypes(), BookkeepingTemplate_.payTypes));
            }
            if (criteria.getDesc() != null) {
                specification = specification.and(buildStringSpecification(criteria.getDesc(), BookkeepingTemplate_.desc));
            }
            if (criteria.getsType() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getsType(), BookkeepingTemplate_.sType));
            }
            if (criteria.getsTypeIds() != null) {
                specification = specification.and(buildStringSpecification(criteria.getsTypeIds(), BookkeepingTemplate_.sTypeIds));
            }
            if (criteria.getDocType() != null) {
                specification = specification.and(buildStringSpecification(criteria.getDocType(), BookkeepingTemplate_.docType));
            }
            if (criteria.getDocCode() != null) {
                specification = specification.and(buildStringSpecification(criteria.getDocCode(), BookkeepingTemplate_.docCode));
            }
            if (criteria.getDocTaskCode() != null) {
                specification = specification.and(buildStringSpecification(criteria.getDocTaskCode(), BookkeepingTemplate_.docTaskCode));
            }
            if (criteria.getrAccountName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getrAccountName(), BookkeepingTemplate_.rAccountName));
            }
            if (criteria.getBookkeepingPayType() != null) {
                specification =
                    specification.and(buildSpecification(criteria.getBookkeepingPayType(), BookkeepingTemplate_.bookkeepingPayType));
            }
            if (criteria.getControl() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getControl(), BookkeepingTemplate_.control));
            }
            if (criteria.getBookkeepingType() != null) {
                specification = specification.and(buildSpecification(criteria.getBookkeepingType(), BookkeepingTemplate_.bookkeepingType));
            }
            if (criteria.getName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getName(), BookkeepingTemplate_.name));
            }
            if (criteria.getGroupIds() != null) {
                //TODO, Fix - when using @Converter it is conflicts with metamodel attributes
                specification = specification.and(buildStringSpecification(criteria.getGroupIds(), BookkeepingTemplate_.groupIds));
            }
            if (criteria.getSubRecipientId() != null) {
                specification =
                    specification.and(buildRangeSpecification(criteria.getSubRecipientId(), BookkeepingTemplate_.subRecipientId));
            }
            if (criteria.getAmountCalculateType() != null) {
                specification =
                    specification.and(buildSpecification(criteria.getAmountCalculateType(), BookkeepingTemplate_.amountCalculateType));
            }
            if (criteria.getGatewayId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getGatewayId(),
                            root -> root.join(BookkeepingTemplate_.gateway, JoinType.LEFT).get(Gateway_.id)
                        )
                    );
            }
            if (criteria.getRecipientId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getRecipientId(),
                            root -> root.join(BookkeepingTemplate_.recipient, JoinType.LEFT).get(Recipient_.id)
                        )
                    );
            }
        }
        return specification;
    }
}
