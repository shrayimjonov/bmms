package uz.paykap.service;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.paykap.domain.RecipientGroup;
import uz.paykap.repository.RecipientGroupRepository;
import uz.paykap.service.dto.RecipientGroupDTO;
import uz.paykap.service.mapper.RecipientGroupMapper;

/**
 * Service Implementation for managing {@link RecipientGroup}.
 */
@Service
@Transactional
public class RecipientGroupService {

    private final Logger log = LoggerFactory.getLogger(RecipientGroupService.class);

    private final RecipientGroupRepository recipientGroupRepository;

    private final RecipientGroupMapper recipientGroupMapper;

    public RecipientGroupService(RecipientGroupRepository recipientGroupRepository, RecipientGroupMapper recipientGroupMapper) {
        this.recipientGroupRepository = recipientGroupRepository;
        this.recipientGroupMapper = recipientGroupMapper;
    }

    /**
     * Save a recipientGroup.
     *
     * @param recipientGroupDTO the entity to save.
     * @return the persisted entity.
     */
    public RecipientGroupDTO save(RecipientGroupDTO recipientGroupDTO) {
        log.debug("Request to save RecipientGroup : {}", recipientGroupDTO);
        RecipientGroup recipientGroup = recipientGroupMapper.toEntity(recipientGroupDTO);
        recipientGroup = recipientGroupRepository.save(recipientGroup);
        return recipientGroupMapper.toDto(recipientGroup);
    }

    /**
     * Partially update a recipientGroup.
     *
     * @param recipientGroupDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<RecipientGroupDTO> partialUpdate(RecipientGroupDTO recipientGroupDTO) {
        log.debug("Request to partially update RecipientGroup : {}", recipientGroupDTO);

        return recipientGroupRepository
            .findById(recipientGroupDTO.getId())
            .map(
                existingRecipientGroup -> {
                    recipientGroupMapper.partialUpdate(existingRecipientGroup, recipientGroupDTO);

                    return existingRecipientGroup;
                }
            )
            .map(recipientGroupRepository::save)
            .map(recipientGroupMapper::toDto);
    }

    /**
     * Get all the recipientGroups.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<RecipientGroupDTO> findAll(Pageable pageable) {
        log.debug("Request to get all RecipientGroups");
        return recipientGroupRepository.findAll(pageable).map(recipientGroupMapper::toDto);
    }

    /**
     * Get all the recipientGroups with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<RecipientGroupDTO> findAllWithEagerRelationships(Pageable pageable) {
        return recipientGroupRepository.findAllWithEagerRelationships(pageable).map(recipientGroupMapper::toDto);
    }

    /**
     * Get one recipientGroup by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<RecipientGroupDTO> findOne(Long id) {
        log.debug("Request to get RecipientGroup : {}", id);
        return recipientGroupRepository.findOneWithEagerRelationships(id).map(recipientGroupMapper::toDto);
    }

    /**
     * Delete the recipientGroup by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete RecipientGroup : {}", id);
        recipientGroupRepository.deleteById(id);
    }
}
