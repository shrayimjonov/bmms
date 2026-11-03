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
import uz.paykap.domain.VirtualAccountDetails;
import uz.paykap.repository.VirtualAccountDetailsRepository;
import uz.paykap.service.criteria.VirtualAccountDetailsCriteria;
import uz.paykap.service.dto.VirtualAccountDetailsDTO;
import uz.paykap.service.mapper.VirtualAccountDetailsMapper;

/**
 * Service for executing complex queries for {@link VirtualAccountDetails} entities in the database.
 * The main input is a {@link VirtualAccountDetailsCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link VirtualAccountDetailsDTO} or a {@link Page} of {@link VirtualAccountDetailsDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class VirtualAccountDetailsQueryService extends QueryService<VirtualAccountDetails> {

    private final Logger log = LoggerFactory.getLogger(VirtualAccountDetailsQueryService.class);

    private final VirtualAccountDetailsRepository virtualAccountDetailsRepository;

    private final VirtualAccountDetailsMapper virtualAccountDetailsMapper;

    public VirtualAccountDetailsQueryService(
        VirtualAccountDetailsRepository virtualAccountDetailsRepository,
        VirtualAccountDetailsMapper virtualAccountDetailsMapper
    ) {
        this.virtualAccountDetailsRepository = virtualAccountDetailsRepository;
        this.virtualAccountDetailsMapper = virtualAccountDetailsMapper;
    }

    /**
     * Return a {@link List} of {@link VirtualAccountDetailsDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<VirtualAccountDetailsDTO> findByCriteria(VirtualAccountDetailsCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<VirtualAccountDetails> specification = createSpecification(criteria);
        return virtualAccountDetailsMapper.toDto(virtualAccountDetailsRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link VirtualAccountDetailsDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<VirtualAccountDetailsDTO> findByCriteria(VirtualAccountDetailsCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<VirtualAccountDetails> specification = createSpecification(criteria);
        return virtualAccountDetailsRepository.findAll(specification, page).map(virtualAccountDetailsMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(VirtualAccountDetailsCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<VirtualAccountDetails> specification = createSpecification(criteria);
        return virtualAccountDetailsRepository.count(specification);
    }

    /**
     * Function to convert {@link VirtualAccountDetailsCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<VirtualAccountDetails> createSpecification(VirtualAccountDetailsCriteria criteria) {
        Specification<VirtualAccountDetails> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), VirtualAccountDetails_.id));
            }
            if (criteria.getParams() != null) {
                specification = specification.and(buildStringSpecification(criteria.getParams(), VirtualAccountDetails_.params));
            }
            if (criteria.getAccount() != null) {
                specification = specification.and(buildStringSpecification(criteria.getAccount(), VirtualAccountDetails_.account));
            }
            if (criteria.getActive() != null) {
                specification = specification.and(buildSpecification(criteria.getActive(), VirtualAccountDetails_.active));
            }
            if (criteria.getMfo() != null) {
                specification = specification.and(buildStringSpecification(criteria.getMfo(), VirtualAccountDetails_.mfo));
            }
            if (criteria.getType() != null) {
                specification = specification.and(buildSpecification(criteria.getType(), VirtualAccountDetails_.type));
            }
            if (criteria.getVirtualAccountId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getVirtualAccountId(),
                            root -> root.join(VirtualAccountDetails_.virtualAccount, JoinType.LEFT).get(VirtualAccount_.id)
                        )
                    );
            }
        }
        return specification;
    }
}
