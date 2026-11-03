package uz.paykap.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.javlon.svgate.config.SvgateProperties;
import uz.paykap.domain.Agent;
import uz.paykap.domain.AgentRecipient;
import uz.paykap.domain.Recipient;
import uz.paykap.domain.RecipientGroup;
import uz.paykap.repository.AgentRecipientRepository;
import uz.paykap.repository.RecipientGroupRepository;
import uz.paykap.service.dto.*;
import uz.paykap.service.mapper.AgentRecipientMapper;

/**
 * Service Implementation for managing {@link AgentRecipient}.
 */
@Slf4j
@AllArgsConstructor
@Service
@Transactional
public class AgentRecipientService {

    private final AgentRecipientRepository agentRecipientRepository;
    private final RecipientGroupRepository recipientGroupRepository;
    private final AgentRecipientMapper agentRecipientMapper;
    private final SvgateProperties svgateProperties;
    private final AgentService agentService;

    @Deprecated
    public AgentRecipient createOrGet(Long recipientId, String agentUsername, long agentDepositBalance) {
        AgentDTO agentDTO = agentService.getAgentOrCreate(agentUsername, agentDepositBalance);

        return createOrGet(recipientId, agentDTO, null);
    }

    public AgentRecipient createOrGet(Long recipientId, AgentDTO agent) {
        return createOrGet(recipientId, agent, null);
    }

    public AgentRecipient createOrGet(Long recipientId, AgentDTO agent, Long settingId) {
        return createOrGet(recipientId, agent, settingId, 0d, 0d);
    }

    public AgentRecipient createOrGet(Long recipientId, AgentDTO agent, double systemCommissionUp, double systemCommissionDown) {
        return createOrGet(recipientId, agent, null, systemCommissionUp, systemCommissionDown);
    }

    public AgentRecipient createOrGet(
        Long recipientId,
        AgentDTO agent,
        Long settingId,
        double systemCommissionUp,
        double systemCommissionDown
    ) {
        return createOrGet(recipientId, agent, settingId, systemCommissionUp, systemCommissionDown, 0d);
    }

    public AgentRecipient createOrGet(
        Long recipientId,
        AgentDTO agent,
        Long settingId,
        double systemCommissionUp,
        double systemCommissionDown,
        double agentFee
    ) {
        return createOrGet(recipientId, agent, settingId, systemCommissionUp, systemCommissionDown, agentFee, 0d);
    }

    public AgentRecipient createOrGet(
        Long recipientId,
        AgentDTO agent,
        Long settingId,
        double systemCommissionUp,
        double systemCommissionDown,
        double agentFee,
        double agentCommission
    ) {
        return createOrGet(recipientId, agent, settingId, systemCommissionUp, systemCommissionDown, agentFee, agentCommission, 0d);
    }

    public AgentRecipient createOrGet(
        Long recipientId,
        AgentDTO agent,
        Long settingId,
        double systemCommissionUp,
        double systemCommissionDown,
        double agentFee,
        double agentCommission,
        double systemFee
    ) {
        return createOrGet(
            recipientId,
            agent,
            settingId,
            systemCommissionUp,
            systemCommissionDown,
            agentFee,
            agentCommission,
            systemFee,
            null
        );
    }

    public AgentRecipient save(AgentRecipient agentRecipient){
        return agentRecipientRepository.save(agentRecipient);
    }

    public AgentRecipient createOrGet(
        Long recipientId,
        AgentDTO agent,
        Long settingId,
        double systemCommissionUp,
        double systemCommissionDown,
        double agentFee,
        double agentCommission,
        double systemFee,
        Long settingTypeId
    ) {
        Optional<AgentRecipient> optional = agentRecipientRepository.findTop1ByAgent_IdAndRecipient_Id(agent.getId(), recipientId);

        if (optional.isPresent()) {
            return optional.get();
        }

        RecipientDTO recipient = new RecipientDTO();
        recipient.setId(recipientId);

        AgentRecipientDTO agentRecipient = new AgentRecipientDTO()
            .agentCommission(agentCommission)
            .agentFee(agentFee)
            .systemCommissionUp(systemCommissionUp)
            .systemCommissionDown(systemCommissionDown)
            .systemFee(systemFee)
            .active(true)
            .agent(agent)
            .recipient(recipient)
            .requestGroup(agent.getShortName())
            .eposMerchant("123")
            .eposTerminal("123")
            .eposPort(1234);
        //

        if (settingId != null) {
            SettingDTO setting = new SettingDTO();
            setting.setId(settingId);

            agentRecipient.setting(setting);
        }

        if (settingTypeId != null) {
            SettingTypeDTO settingType = new SettingTypeDTO();
            settingType.setId(settingTypeId);

            agentRecipient.settingType(settingType);
        }

        log.debug("Saved AgentRecipient : recipient[{}]-agent[{}]", recipientId, agent);

        return agentRecipientRepository.save(agentRecipientMapper.toEntity(agentRecipient));
    }

    /**
     * Save a agentRecipient.
     *
     * @param agentRecipientDTO the entity to save.
     * @return the persisted entity.
     */
    public AgentRecipientDTO save(AgentRecipientDTO agentRecipientDTO) {
        log.debug("Request to save AgentRecipient : {}", agentRecipientDTO);
        AgentRecipient agentRecipient = agentRecipientMapper.toEntity(agentRecipientDTO);
        agentRecipient = agentRecipientRepository.save(agentRecipient);
        return agentRecipientMapper.toDto(agentRecipient);
    }

    /**
     * Partially update a agentRecipient.
     *
     * @param agentRecipientDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<AgentRecipientDTO> partialUpdate(AgentRecipientDTO agentRecipientDTO) {
        log.debug("Request to partially update AgentRecipient : {}", agentRecipientDTO);

        return agentRecipientRepository
            .findById(agentRecipientDTO.getId())
            .map(
                existingAgentRecipient -> {
                    agentRecipientMapper.partialUpdate(existingAgentRecipient, agentRecipientDTO);

                    return existingAgentRecipient;
                }
            )
            .map(agentRecipientRepository::save)
            .map(agentRecipientMapper::toDto);
    }

    /**
     * Get all the agentRecipients.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<AgentRecipientDTO> findAll(Pageable pageable) {
        log.debug("Request to get all AgentRecipients");
        return agentRecipientRepository.findAll(pageable).map(agentRecipientMapper::toDto);
    }

    /**
     * Get one agentRecipient by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<AgentRecipientDTO> findOne(Long id) {
        log.debug("Request to get AgentRecipient : {}", id);
        return agentRecipientRepository.findById(id).map(agentRecipientMapper::toDto);
    }

    /**
     * Delete the agentRecipient by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete AgentRecipient : {}", id);
        agentRecipientRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<AgentRecipientDTO> findAll() {
        log.debug("Request to get all AgentRecipients");

        return agentRecipientMapper.toDto(agentRecipientRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<AgentRecipientDTO> findAllByAgent(Agent agent) {
        log.debug("Request to get  AgentRecipients by Agent");
        return agentRecipientMapper.toDto(agentRecipientRepository.findAllByAgent(agent).orElse(Collections.emptyList()));
    }

    @Transactional(readOnly = true)
    public List<AgentRecipientDTO> findTop50ByAgent(Agent agent) {
        log.debug("Request to get  AgentRecipients by Agent");
        return agentRecipientMapper.toDto(agentRecipientRepository.findTop50ByAgent(agent).orElse(Collections.emptyList()));
    }

    public List<AgentRecipientDTO> createOneOrAllByGroup(AgentRecipientDTO agentRecipientDTO) {
        if (agentRecipientDTO.getRecipientGroup() == null) {
            return Collections.singletonList(save(agentRecipientDTO));
        }

        RecipientGroup recipientGroup = recipientGroupRepository
            .findOneWithEagerRelationships(agentRecipientDTO.getRecipientGroup().getId())
            .orElseThrow(() -> new CodedException(ProblemEnum.NOT_FOUND, "recipientGroup"));

        List<AgentRecipientDTO> agentRecipients = new ArrayList<>();
        for (Recipient recipientDTO : recipientGroup.getRecipients()) {
            agentRecipientDTO.setRecipientGroup(agentRecipientDTO.getRecipientGroup());
            RecipientDTO recipient = new RecipientDTO();
            recipient.setId(recipientDTO.getId());

            agentRecipientDTO.setRecipient(recipient);
            agentRecipients.add(save(agentRecipientDTO));
        }

        return agentRecipients;
    }
}
