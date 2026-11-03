package uz.paykap.service.impl;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.paykap.domain.AgentParams;
import uz.paykap.repository.AgentParamsRepository;
import uz.paykap.service.AgentParamsService;
import uz.paykap.service.dto.AgentParamsDTO;
import uz.paykap.service.mapper.AgentParamsMapper;

/**
 * Service Implementation for managing {@link AgentParams}.
 */
@Service
@Transactional
public class AgentParamsServiceImpl implements AgentParamsService {

    private final Logger log = LoggerFactory.getLogger(AgentParamsServiceImpl.class);

    private final AgentParamsRepository agentParamsRepository;

    private final AgentParamsMapper agentParamsMapper;

    public AgentParamsServiceImpl(AgentParamsRepository agentParamsRepository, AgentParamsMapper agentParamsMapper) {
        this.agentParamsRepository = agentParamsRepository;
        this.agentParamsMapper = agentParamsMapper;
    }

    @Override
    public AgentParamsDTO save(AgentParamsDTO agentParamsDTO) {
        log.debug("Request to save AgentParams : {}", agentParamsDTO);
        AgentParams agentParams = agentParamsMapper.toEntity(agentParamsDTO);
        agentParams = agentParamsRepository.save(agentParams);
        return agentParamsMapper.toDto(agentParams);
    }

    @Override
    public Optional<AgentParamsDTO> partialUpdate(AgentParamsDTO agentParamsDTO) {
        log.debug("Request to partially update AgentParams : {}", agentParamsDTO);

        return agentParamsRepository
            .findById(agentParamsDTO.getId())
            .map(
                existingAgentParams -> {
                    agentParamsMapper.partialUpdate(existingAgentParams, agentParamsDTO);

                    return existingAgentParams;
                }
            )
            .map(agentParamsRepository::save)
            .map(agentParamsMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AgentParamsDTO> findAll(Pageable pageable) {
        log.debug("Request to get all AgentParams");
        return agentParamsRepository.findAll(pageable).map(agentParamsMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AgentParamsDTO> findOne(Long id) {
        log.debug("Request to get AgentParams : {}", id);
        return agentParamsRepository.findById(id).map(agentParamsMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete AgentParams : {}", id);
        agentParamsRepository.deleteById(id);
    }
}
