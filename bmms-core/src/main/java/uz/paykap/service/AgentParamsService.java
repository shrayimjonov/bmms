package uz.paykap.service;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uz.paykap.service.dto.AgentParamsDTO;

/**
 * Service Interface for managing {@link uz.paykap.domain.AgentParams}.
 */
public interface AgentParamsService {
    /**
     * Save a agentParams.
     *
     * @param agentParamsDTO the entity to save.
     * @return the persisted entity.
     */
    AgentParamsDTO save(AgentParamsDTO agentParamsDTO);

    /**
     * Partially updates a agentParams.
     *
     * @param agentParamsDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<AgentParamsDTO> partialUpdate(AgentParamsDTO agentParamsDTO);

    /**
     * Get all the agentParams.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<AgentParamsDTO> findAll(Pageable pageable);

    /**
     * Get the "id" agentParams.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AgentParamsDTO> findOne(Long id);

    /**
     * Delete the "id" agentParams.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
