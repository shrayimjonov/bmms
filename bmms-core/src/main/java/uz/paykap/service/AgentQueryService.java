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
import uz.devops.bmcore.dto.KeyValue;
import uz.paykap.domain.*; // Dont change
import uz.paykap.repository.AgentRepository;
import uz.paykap.service.criteria.AgentCriteria;
import uz.paykap.service.dto.AgentDTO;
import uz.paykap.service.mapper.AgentMapper;

/**
 * Service for executing complex queries for {@link Agent} entities in the database.
 * The main input is a {@link AgentCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link AgentDTO} or a {@link Page} of {@link AgentDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class AgentQueryService extends QueryService<Agent> {

    private final Logger log = LoggerFactory.getLogger(AgentQueryService.class);

    private final AgentRepository agentRepository;

    private final AgentMapper agentMapper;

    public AgentQueryService(AgentRepository agentRepository, AgentMapper agentMapper) {
        this.agentRepository = agentRepository;
        this.agentMapper = agentMapper;
    }

    /**
     * Return a {@link List} of {@link AgentDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<AgentDTO> findByCriteria(AgentCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<Agent> specification = createSpecification(criteria);
        return agentMapper.toDto(agentRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link AgentDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<AgentDTO> findByCriteria(AgentCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Agent> specification = createSpecification(criteria);
        return agentRepository.findAll(specification, page).map(agentMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<KeyValue> findSimpleByCriteria(AgentCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Agent> specification = createSpecification(criteria);
        Page<Agent> agents = agentRepository.findAll(specification, page);
        return agentMapper.toSimple(agents.getContent());
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(AgentCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<Agent> specification = createSpecification(criteria);
        return agentRepository.count(specification);
    }

    /**
     * Function to convert {@link AgentCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Agent> createSpecification(AgentCriteria criteria) {
        Specification<Agent> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), Agent_.id));
            }
            if (criteria.getShortName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getShortName(), Agent_.shortName));
            }
            if (criteria.getAmountRatio() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getAmountRatio(), Agent_.amountRatio));
            }
            if (criteria.getApiKey() != null) {
                specification = specification.and(buildStringSpecification(criteria.getApiKey(), Agent_.apiKey));
            }
            //            if (criteria.getCurrencies() != null) {
            //                specification = specification.and(buildStringSpecification(criteria.getCurrencies(), Agent_.currencies));
            //            }
            //            if (criteria.getIpAddresses() != null) {
            //                specification = specification.and(buildStringSpecification(criteria.getIpAddresses(), Agent_.ipAddresses));
            //            }
            if (criteria.getOrd() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getOrd(), Agent_.ord));
            }
            if (criteria.getActive() != null) {
                specification = specification.and(buildSpecification(criteria.getActive(), Agent_.active));
            }
            if (criteria.getPhoneNumber() != null) {
                specification = specification.and(buildStringSpecification(criteria.getPhoneNumber(), Agent_.phoneNumber));
            }
            if (criteria.getInn() != null) {
                specification = specification.and(buildStringSpecification(criteria.getInn(), Agent_.inn));
            }
            if (criteria.getOrgName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getOrgName(), Agent_.orgName));
            }
            if (criteria.getEmail() != null) {
                specification = specification.and(buildStringSpecification(criteria.getEmail(), Agent_.email));
            }
            if (criteria.getState() != null) {
                specification = specification.and(buildSpecification(criteria.getState(), Agent_.state));
            }
            if (criteria.getTitleId() != null) {
                specification =
                    specification.and(
                        buildSpecification(criteria.getTitleId(), root -> root.join(Agent_.titles, JoinType.LEFT).get(I18n_.id))
                    );
            }
            if (criteria.getDepositId() != null) {
                specification =
                    specification.and(
                        buildSpecification(criteria.getDepositId(), root -> root.join(Agent_.deposit, JoinType.LEFT).get(Deposit_.id))
                    );
            }
        }
        return specification;
    }
}
