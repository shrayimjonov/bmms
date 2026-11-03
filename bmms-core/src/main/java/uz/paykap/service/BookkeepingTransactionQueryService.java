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
import uz.paykap.domain.BookkeepingTransaction;
import uz.paykap.repository.BookkeepingTransactionRepository;
import uz.paykap.service.criteria.BookkeepingTransactionCriteria;
import uz.paykap.service.dto.BookkeepingTransactionDTO;
import uz.paykap.service.mapper.BookkeepingTransactionMapper;

/**
 * Service for executing complex queries for {@link BookkeepingTransaction} entities in the database.
 * The main input is a {@link BookkeepingTransactionCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link BookkeepingTransactionDTO} or a {@link Page} of {@link BookkeepingTransactionDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class BookkeepingTransactionQueryService extends QueryService<BookkeepingTransaction> {

    private final Logger log = LoggerFactory.getLogger(BookkeepingTransactionQueryService.class);

    private final BookkeepingTransactionRepository bookkeepingTransactionRepository;

    private final BookkeepingTransactionMapper bookkeepingTransactionMapper;

    public BookkeepingTransactionQueryService(
        BookkeepingTransactionRepository bookkeepingTransactionRepository,
        BookkeepingTransactionMapper bookkeepingTransactionMapper
    ) {
        this.bookkeepingTransactionRepository = bookkeepingTransactionRepository;
        this.bookkeepingTransactionMapper = bookkeepingTransactionMapper;
    }

    /**
     * Return a {@link List} of {@link BookkeepingTransactionDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<BookkeepingTransactionDTO> findByCriteria(BookkeepingTransactionCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<BookkeepingTransaction> specification = createSpecification(criteria);
        return bookkeepingTransactionMapper.toDto(bookkeepingTransactionRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link BookkeepingTransactionDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<BookkeepingTransactionDTO> findByCriteria(BookkeepingTransactionCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<BookkeepingTransaction> specification = createSpecification(criteria);
        return bookkeepingTransactionRepository.findAll(specification, page).map(bookkeepingTransactionMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(BookkeepingTransactionCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<BookkeepingTransaction> specification = createSpecification(criteria);
        return bookkeepingTransactionRepository.count(specification);
    }

    /**
     * Function to convert {@link BookkeepingTransactionCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<BookkeepingTransaction> createSpecification(BookkeepingTransactionCriteria criteria) {
        Specification<BookkeepingTransaction> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), BookkeepingTransaction_.id));
            }
            if (criteria.getGroupId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getGroupId(), BookkeepingTransaction_.groupId));
            }
            if (criteria.getsMfo() != null) {
                specification = specification.and(buildStringSpecification(criteria.getsMfo(), BookkeepingTransaction_.sMfo));
            }
            if (criteria.getsInn() != null) {
                specification = specification.and(buildStringSpecification(criteria.getsInn(), BookkeepingTransaction_.sInn));
            }
            if (criteria.getsAccount() != null) {
                specification = specification.and(buildStringSpecification(criteria.getsAccount(), BookkeepingTransaction_.sAccount));
            }
            if (criteria.getrMfo() != null) {
                specification = specification.and(buildStringSpecification(criteria.getrMfo(), BookkeepingTransaction_.rMfo));
            }
            if (criteria.getrInn() != null) {
                specification = specification.and(buildStringSpecification(criteria.getrInn(), BookkeepingTransaction_.rInn));
            }
            if (criteria.getrAccount() != null) {
                specification = specification.and(buildStringSpecification(criteria.getrAccount(), BookkeepingTransaction_.rAccount));
            }
            if (criteria.getAmount() != null) {
                specification = specification.and(buildStringSpecification(criteria.getAmount(), BookkeepingTransaction_.amount));
            }
            if (criteria.getPeriod() != null) {
                specification = specification.and(buildSpecification(criteria.getPeriod(), BookkeepingTransaction_.period));
            }
            if (criteria.getBookkeepingType() != null) {
                specification =
                    specification.and(buildSpecification(criteria.getBookkeepingType(), BookkeepingTransaction_.bookkeepingType));
            }
            if (criteria.getSuccess() != null) {
                specification = specification.and(buildSpecification(criteria.getSuccess(), BookkeepingTransaction_.success));
            }
            if (criteria.getDetails() != null) {
                specification = specification.and(buildStringSpecification(criteria.getDetails(), BookkeepingTransaction_.details));
            }
            if (criteria.getDocType() != null) {
                specification = specification.and(buildStringSpecification(criteria.getDocType(), BookkeepingTransaction_.docType));
            }
            if (criteria.getDocCode() != null) {
                specification = specification.and(buildStringSpecification(criteria.getDocCode(), BookkeepingTransaction_.docCode));
            }
            if (criteria.getDocTaskCode() != null) {
                specification = specification.and(buildStringSpecification(criteria.getDocTaskCode(), BookkeepingTransaction_.docTaskCode));
            }
            if (criteria.getrAccountName() != null) {
                specification =
                    specification.and(buildStringSpecification(criteria.getrAccountName(), BookkeepingTransaction_.rAccountName));
            }
            if (criteria.getConfirmUserID() != null) {
                specification =
                    specification.and(buildStringSpecification(criteria.getConfirmUserID(), BookkeepingTransaction_.confirmUserID));
            }
            if (criteria.getSendUserID() != null) {
                specification = specification.and(buildStringSpecification(criteria.getSendUserID(), BookkeepingTransaction_.sendUserID));
            }
            if (criteria.getCancelUserID() != null) {
                specification =
                    specification.and(buildStringSpecification(criteria.getCancelUserID(), BookkeepingTransaction_.cancelUserID));
            }
            if (criteria.getCreateTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreateTime(), BookkeepingTransaction_.createTime));
            }
            if (criteria.getConfirmTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getConfirmTime(), BookkeepingTransaction_.confirmTime));
            }
            if (criteria.getSendTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getSendTime(), BookkeepingTransaction_.sendTime));
            }
            if (criteria.getCancelTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCancelTime(), BookkeepingTransaction_.cancelTime));
            }
            if (criteria.getAbsId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getAbsId(), BookkeepingTransaction_.absId));
            }
            if (criteria.getDocId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getDocId(), BookkeepingTransaction_.docId));
            }
            if (criteria.getTemplateId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getTemplateId(), BookkeepingTransaction_.templateId));
            }
            if (criteria.getControl() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getControl(), BookkeepingTransaction_.control));
            }
            if (criteria.getResponseText() != null) {
                specification =
                    specification.and(buildStringSpecification(criteria.getResponseText(), BookkeepingTransaction_.responseText));
            }
            if (criteria.getType() != null) {
                specification = specification.and(buildSpecification(criteria.getType(), BookkeepingTransaction_.type));
            }
            if (criteria.getUnionId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getUnionId(), BookkeepingTransaction_.unionId));
            }
            if (criteria.getGroupIds() != null) {
                specification = specification.and(buildStringSpecification(criteria.getGroupIds(), BookkeepingTransaction_.groupIds));
            }
            if (criteria.getSubRecipient() != null) {
                specification =
                    specification.and(buildStringSpecification(criteria.getSubRecipient(), BookkeepingTransaction_.subRecipient));
            }
            if (criteria.getSubRecipientId() != null) {
                specification =
                    specification.and(buildRangeSpecification(criteria.getSubRecipientId(), BookkeepingTransaction_.subRecipientId));
            }
            if (criteria.getComment() != null) {
                specification = specification.and(buildStringSpecification(criteria.getComment(), BookkeepingTransaction_.comment));
            }
            if (criteria.getBookkeepingPayType() != null) {
                specification =
                    specification.and(buildSpecification(criteria.getBookkeepingPayType(), BookkeepingTransaction_.bookkeepingPayType));
            }
            if (criteria.getParams() != null) {
                specification = specification.and(buildStringSpecification(criteria.getParams(), BookkeepingTransaction_.params));
            }
            if (criteria.getGatewayId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getGatewayId(),
                            root -> root.join(BookkeepingTransaction_.gateway, JoinType.LEFT).get(Gateway_.id)
                        )
                    );
            }
            if (criteria.getRecipientId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getRecipientId(),
                            root -> root.join(BookkeepingTransaction_.recipient, JoinType.LEFT).get(Recipient_.id)
                        )
                    );
            }
        }
        return specification;
    }
}
