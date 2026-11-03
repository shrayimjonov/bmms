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
import uz.paykap.domain.VirtualAccount;
import uz.paykap.repository.VirtualAccountRepository;
import uz.paykap.service.criteria.VirtualAccountCriteria;
import uz.paykap.service.dto.VirtualAccountDTO;
import uz.paykap.service.mapper.VirtualAccountMapper;

/**
 * Service for executing complex queries for {@link VirtualAccount} entities in the database.
 * The main input is a {@link VirtualAccountCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link VirtualAccountDTO} or a {@link Page} of {@link VirtualAccountDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class VirtualAccountQueryService extends QueryService<VirtualAccount> {

    private final Logger log = LoggerFactory.getLogger(VirtualAccountQueryService.class);

    private final VirtualAccountRepository virtualAccountRepository;

    private final VirtualAccountMapper virtualAccountMapper;

    public VirtualAccountQueryService(VirtualAccountRepository virtualAccountRepository, VirtualAccountMapper virtualAccountMapper) {
        this.virtualAccountRepository = virtualAccountRepository;
        this.virtualAccountMapper = virtualAccountMapper;
    }

    /**
     * Return a {@link List} of {@link VirtualAccountDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<VirtualAccountDTO> findByCriteria(VirtualAccountCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<VirtualAccount> specification = createSpecification(criteria);
        return virtualAccountMapper.toDto(virtualAccountRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link VirtualAccountDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<VirtualAccountDTO> findByCriteria(VirtualAccountCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<VirtualAccount> specification = createSpecification(criteria);
        return virtualAccountRepository.findAll(specification, page).map(virtualAccountMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(VirtualAccountCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<VirtualAccount> specification = createSpecification(criteria);
        return virtualAccountRepository.count(specification);
    }

    /**
     * Function to convert {@link VirtualAccountCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<VirtualAccount> createSpecification(VirtualAccountCriteria criteria) {
        Specification<VirtualAccount> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), VirtualAccount_.id));
            }
            if (criteria.getName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getName(), VirtualAccount_.name));
            }
            if (criteria.getOrgName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getOrgName(), VirtualAccount_.orgName));
            }
            if (criteria.getActive() != null) {
                specification = specification.and(buildSpecification(criteria.getActive(), VirtualAccount_.active));
            }
            if (criteria.getInn() != null) {
                specification = specification.and(buildStringSpecification(criteria.getInn(), VirtualAccount_.inn));
            }
            if (criteria.getUid() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getUid(), VirtualAccount_.uid));
            }
            if (criteria.getMode() != null) {
                specification = specification.and(buildSpecification(criteria.getMode(), VirtualAccount_.mode));
            }
            if (criteria.getDepositId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getDepositId(),
                            root -> root.join(VirtualAccount_.deposit, JoinType.LEFT).get(Deposit_.id)
                        )
                    );
            }
        }
        return specification;
    }
}
