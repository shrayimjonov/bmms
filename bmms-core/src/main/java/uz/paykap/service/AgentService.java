package uz.paykap.service;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.libs.keycloak.admin.service.KeycloakAdminService;
import uz.devops.libs.keycloak.admin.service.dto.AdminUserGroupDTO;
import uz.devops.libs.keycloak.admin.service.dto.KUserDTO;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.client.uaa.UaaClient;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.config.Constants;
import uz.paykap.domain.Agent;
import uz.paykap.domain.AgentParams;
import uz.paykap.domain.enumeration.AgentState;
import uz.paykap.event.dto.RegisterUserEvent;
import uz.paykap.repository.AgentParamsRepository;
import uz.paykap.repository.AgentRepository;
import uz.paykap.security.AuthoritiesConstants;
import uz.paykap.service.dto.AgentDTO;
import uz.paykap.service.dto.DetailedAgentDTO;
import uz.paykap.service.mapper.AgentMapper;
import uz.paykap.service.mapper.AgentParamsMapper;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Service Implementation for managing {@link Agent}.
 */
@Service
@Transactional
@AllArgsConstructor
public class AgentService {

    private final Logger log = LoggerFactory.getLogger(AgentService.class);

    private final AgentRepository agentRepository;

    private final AgentMapper agentMapper;
    private final DepositService depositService;

    private final ApplicationEventPublisher applicationEventPublisher;
    private final UaaClient uaaClient;
    private final ApplicationProperties applicationProperties;
    private final KeycloakAdminService keycloakAdminService;
    private final AgentParamsRepository agentParamsRepository;
    private final AgentParamsMapper agentParamsMapper;

    public AgentDTO getAgentOrCreate(String shortName) {
        return getAgentOrCreate(shortName, 0L);
    }

    public AgentDTO getAgentOrCreate(String shortName, long balance) {
        return getAgentOrCreate(shortName, balance, null);
    }

    public AgentDTO getAgentOrCreate(String shortName, long balance, String inn) {
        return getAgentOrCreate(shortName, balance, inn, Constants.DEFAULT_CURRENCIES);
    }

    public AgentDTO getAgentOrCreate(String shortName, String inn) {
        if (shortName == null) {
            shortName = inn;
        }
        return getAgentOrCreate(shortName, 0L, inn, Constants.DEFAULT_CURRENCIES);
    }

    public AgentDTO getAgentByInnAndShortName(String inn, String shortName) {
        if (shortName == null || Objects.equals(shortName, "")) {
            shortName = inn;
        }

        if (agentRepository.findTop1ByInnAndShortName(inn, shortName).isPresent()) {
            return agentMapper.toDto(agentRepository.findTop1ByInnAndShortName(inn, shortName).get());
        }

        throw new CodedException(ProblemEnum.OBJECT_NOT_FOUND, "agent not found");
    }

    public AgentDTO getAgentOrCreate(String shortName, long balance, String inn, Set<Integer> currencies) {
        Optional<Agent> agentOptional = agentRepository.findTop1ByShortName(shortName);

        if (agentOptional.isPresent()) {
            log.debug("Found Agent : {}", agentOptional.get().getShortName());
            return agentMapper.toDto(agentOptional.get());
        }

        Agent agent = new Agent()
                .shortName(shortName)
                .amountRatio(Constants.DEFAULT_AMOUNT_RATIO)
                .apiKey("100100100")
                .currencies(currencies)
                .ipAddresses(Collections.emptySet())
                .active(true)
                .inn(inn); //

        agent = agentRepository.save(agent);

        log.debug("Saved Agent : {}", agent.getShortName());

        for (Integer currency : currencies) {
            depositService.createAgentDeposit(agent.getId(), balance, 0L, currency);
            log.debug("Saved AgentDeposit for agent : {}", agent.getShortName());
        }

        return agentMapper.toDto(agent);
    }

    public AgentDTO getAgentOrCreate(Agent agent, long balance) {
        Optional<Agent> agentOptional = agentRepository.findTop1ByShortName(agent.getShortName());

        if (agentOptional.isPresent()) {
            log.debug("Found Agent : {}", agentOptional.get().getShortName());
            return agentMapper.toDto(agentOptional.get());
        }

        agent = agentRepository.save(agent);

        log.debug("Saved Agent : {}", agent.getShortName());

        depositService.createAgentDeposit(agent.getId(), balance);

        log.debug("Saved AgentDeposit for agent : {}", agent.getShortName());

        return agentMapper.toDto(agent);
    }

    /**
     * Save a agent.
     *
     * @param agentDTO the entity to save.
     * @return the persisted entity.
     */
    public AgentDTO save(AgentDTO agentDTO) {
        log.debug("Request to save Agent : {}", agentDTO);

        Agent agent = agentMapper.toEntity(agentDTO);
        agent.setState(AgentState.NEW);
        agent = agentRepository.save(agent);

        log.debug("Saved Agent : {}", agent.getShortName());

        depositService.createAgentDeposit(agent.getId());

        log.debug("Saved AgentDeposit for agent : {}", agent.getShortName());

        RegisterUserEvent registerUserEvent = new RegisterUserEvent(
                this,
                agent.getShortName(),
                agent.getApiKey(),
                agent.getOrgName(),
                agent.getOrgName(),
                agent.getEmail(),
                Collections.singleton(AuthoritiesConstants.AGENT)
        );

        log.debug("Publish event to create User :{}", registerUserEvent);

        applicationEventPublisher.publishEvent(registerUserEvent);

        return agentMapper.toDto(agent);
    }

    public AgentDTO saveWithUser(DetailedAgentDTO detailedAgentDTO) {
        log.debug("Request to save Agent : {}", detailedAgentDTO);

        Agent agent = agentMapper.toEntity(detailedAgentDTO.getAgentDTO());
        agent.setState(AgentState.NEW);
        agent = agentRepository.save(agent);

        log.debug("Saved Agent : {}", agent.getShortName());

        depositService.createAgentDeposit(agent.getId());

        log.debug("Saved AgentDeposit for agent : {}", agent.getShortName());

        String password = applicationProperties.getUaaUserDefaultPassword();

        if (detailedAgentDTO.getPassword() != null && !detailedAgentDTO.getPassword().isBlank()) {
            password = detailedAgentDTO.getPassword();
        }

//        RegisterUserEvent registerUserEvent = new RegisterUserEvent(
//                this,
//                agent.getShortName(),
//                password,
//                agent.getOrgName(),
//                agent.getOrgName(),
//                detailedAgentDTO.getEMail(),
//                null
//        );
//
//        log.debug("Publish event to create User :{}", registerUserEvent);
//
//        applicationEventPublisher.publishEvent(registerUserEvent);

        KUserDTO user = new KUserDTO();
        AgentDTO agentDTO = detailedAgentDTO.getAgentDTO();
        user.setFirstName(agentDTO.getShortName());
        user.setLastName(agentDTO.getShortName());
        user.setPassword(password);
        user.setEmail(agentDTO.getEmail());
        user.setEnabled(true);
        user.setTemporary(false);
        var agentGroup = new AdminUserGroupDTO();
        agentGroup.setName("Agents");
        user.setUserGroups(Collections.singleton(agentGroup));
        user.setUsername(agentDTO.getShortName());
        keycloakAdminService.createUser(user);
        if (detailedAgentDTO.getAgentParamsDTO() != null) {
            detailedAgentDTO.getAgentParamsDTO().setAgent(agentMapper.toDto(agent));
            AgentParams agentParams = agentParamsMapper.toEntity(detailedAgentDTO.getAgentParamsDTO());
            agentParams.setBankAccountPrefix(applicationProperties.getAgentPayment().getDefaultAccountPattern());
            agentParamsRepository.save(agentParams);
        }
        return agentMapper.toDto(agent);
    }

    /**
     * Partially update a agent.
     *
     * @param agentDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<AgentDTO> partialUpdate(AgentDTO agentDTO) {
        log.debug("Request to partially update Agent : {}", agentDTO);

        return agentRepository
            .findById(agentDTO.getId())
            .map(
                existingAgent -> {
                    existingAgent.setState(AgentState.NEW);
                    agentMapper.partialUpdate(existingAgent, agentDTO);
                    return existingAgent;
                }
            )
            .map(agentRepository::save)
            .map(agentMapper::toDto);
    }

    /**
     * Get all the agents.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<AgentDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Agents");
        return agentRepository.findAll(pageable).map(agentMapper::toDto);
    }

    /**
     * Get all the agents with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<AgentDTO> findAllWithEagerRelationships(Pageable pageable) {
        return agentRepository.findAllWithEagerRelationships(pageable).map(agentMapper::toDto);
    }

    /**
     * Get all the agents where Deposit is {@code null}.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<AgentDTO> findAllWhereDepositIsNull() {
        log.debug("Request to get all agents where Deposit is null");
        return StreamSupport
                .stream(agentRepository.findAll().spliterator(), false)
                .filter(agent -> agent.getDeposit() == null)
                .map(agentMapper::toDto)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get one agent by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<AgentDTO> findOne(Long id) {
        log.debug("Request to get Agent : {}", id);
        return agentRepository.findOneWithEagerRelationships(id).map(agentMapper::toDto);
    }

    /**
     * Delete the agent by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Agent : {}", id);
        agentRepository.deleteById(id);
    }

    public void softDelete(Long id) {
        log.debug("Request to delete Agent : {}", id);
        depositService.softDeleteByAgentId(id);
        agentRepository.updateStatus(id, AgentState.DELETED);
    }
}
