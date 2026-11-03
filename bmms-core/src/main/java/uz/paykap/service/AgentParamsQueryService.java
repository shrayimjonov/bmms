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
import uz.paykap.domain.AgentParams;
import uz.paykap.repository.AgentParamsRepository;
import uz.paykap.service.criteria.AgentParamsCriteria;
import uz.paykap.service.dto.AgentParamsDTO;
import uz.paykap.service.mapper.AgentParamsMapper;

/**
 * Service for executing complex queries for {@link AgentParams} entities in the database.
 * The main input is a {@link AgentParamsCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link AgentParamsDTO} or a {@link Page} of {@link AgentParamsDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class AgentParamsQueryService extends QueryService<AgentParams> {

    private final Logger log = LoggerFactory.getLogger(AgentParamsQueryService.class);

    private final AgentParamsRepository agentParamsRepository;

    private final AgentParamsMapper agentParamsMapper;

    public AgentParamsQueryService(AgentParamsRepository agentParamsRepository, AgentParamsMapper agentParamsMapper) {
        this.agentParamsRepository = agentParamsRepository;
        this.agentParamsMapper = agentParamsMapper;
    }

    /**
     * Return a {@link List} of {@link AgentParamsDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<AgentParamsDTO> findByCriteria(AgentParamsCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<AgentParams> specification = createSpecification(criteria);
        return agentParamsMapper.toDto(agentParamsRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link AgentParamsDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<AgentParamsDTO> findByCriteria(AgentParamsCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<AgentParams> specification = createSpecification(criteria);
        return agentParamsRepository.findAll(specification, page).map(agentParamsMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(AgentParamsCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<AgentParams> specification = createSpecification(criteria);
        return agentParamsRepository.count(specification);
    }

    /**
     * Function to convert {@link AgentParamsCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<AgentParams> createSpecification(AgentParamsCriteria criteria) {
        Specification<AgentParams> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), AgentParams_.id));
            }
            if (criteria.getCallBackUrl() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCallBackUrl(), AgentParams_.callBackUrl));
            }
            if (criteria.getUsername() != null) {
                specification = specification.and(buildStringSpecification(criteria.getUsername(), AgentParams_.username));
            }
            if (criteria.getPassword() != null) {
                specification = specification.and(buildStringSpecification(criteria.getPassword(), AgentParams_.password));
            }
            if (criteria.getBankClientId() != null) {
                specification = specification.and(buildStringSpecification(criteria.getBankClientId(), AgentParams_.bankClientId));
            }
            if (criteria.getBankAccountPrefix() != null) {
                specification =
                    specification.and(buildStringSpecification(criteria.getBankAccountPrefix(), AgentParams_.bankAccountPrefix));
            }
            if (criteria.getAgentId() != null) {
                specification =
                    specification.and(
                        buildSpecification(criteria.getAgentId(), root -> root.join(AgentParams_.agent, JoinType.LEFT).get(Agent_.id))
                    );
            }
        }
        return specification;
    }
}
