package uz.paykap.service;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.paykap.domain.RecipientFileSchema;
import uz.paykap.repository.RecipientFileSchemaRepository;
import uz.paykap.service.dto.RecipientFileSchemaDTO;
import uz.paykap.service.mapper.RecipientFileSchemaMapper;

/**
 * Service Implementation for managing {@link RecipientFileSchema}.
 */
@Service
@Transactional
public class RecipientFileSchemaService {

    private final Logger log = LoggerFactory.getLogger(RecipientFileSchemaService.class);

    private final RecipientFileSchemaRepository recipientFileSchemaRepository;

    private final RecipientFileSchemaMapper recipientFileSchemaMapper;

    public RecipientFileSchemaService(
        RecipientFileSchemaRepository recipientFileSchemaRepository,
        RecipientFileSchemaMapper recipientFileSchemaMapper
    ) {
        this.recipientFileSchemaRepository = recipientFileSchemaRepository;
        this.recipientFileSchemaMapper = recipientFileSchemaMapper;
    }

    /**
     * Save a recipientFileSchema.
     *
     * @param recipientFileSchemaDTO the entity to save.
     * @return the persisted entity.
     */
    public RecipientFileSchemaDTO save(RecipientFileSchemaDTO recipientFileSchemaDTO) {
        log.debug("Request to save RecipientFileSchema : {}", recipientFileSchemaDTO);
        RecipientFileSchema recipientFileSchema = recipientFileSchemaMapper.toEntity(recipientFileSchemaDTO);
        recipientFileSchema = recipientFileSchemaRepository.save(recipientFileSchema);
        return recipientFileSchemaMapper.toDto(recipientFileSchema);
    }

    /**
     * Partially update a recipientFileSchema.
     *
     * @param recipientFileSchemaDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<RecipientFileSchemaDTO> partialUpdate(RecipientFileSchemaDTO recipientFileSchemaDTO) {
        log.debug("Request to partially update RecipientFileSchema : {}", recipientFileSchemaDTO);

        return recipientFileSchemaRepository
            .findById(recipientFileSchemaDTO.getId())
            .map(
                existingRecipientFileSchema -> {
                    recipientFileSchemaMapper.partialUpdate(existingRecipientFileSchema, recipientFileSchemaDTO);

                    return existingRecipientFileSchema;
                }
            )
            .map(recipientFileSchemaRepository::save)
            .map(recipientFileSchemaMapper::toDto);
    }

    /**
     * Get all the recipientFileSchemas.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<RecipientFileSchemaDTO> findAll(Pageable pageable) {
        log.debug("Request to get all RecipientFileSchemas");
        return recipientFileSchemaRepository.findAll(pageable).map(recipientFileSchemaMapper::toDto);
    }

    /**
     * Get one recipientFileSchema by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<RecipientFileSchemaDTO> findOne(Long id) {
        log.debug("Request to get RecipientFileSchema : {}", id);
        return recipientFileSchemaRepository.findById(id).map(recipientFileSchemaMapper::toDto);
    }

    /**
     * Delete the recipientFileSchema by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete RecipientFileSchema : {}", id);
        recipientFileSchemaRepository.deleteById(id);
    }

    public List<RecipientFileSchemaDTO> findAll() {
        log.debug("Request to get all RecipientFileSchema");
        return recipientFileSchemaMapper.toDto(recipientFileSchemaRepository.findAll());
    }

    public Map<String, List<Long>> getSchemas() {
        List<RecipientFileSchemaDTO> schemaDTOList = findAll();
        if (schemaDTOList.size() == 0) {
            return null;
        }
        List<Long> idList = new ArrayList<>();
        Map<String, List<Long>> stringListMap = new HashMap<>();
        for (RecipientFileSchemaDTO recipientFileSchemaDTO : schemaDTOList) {
            StringBuilder id = new StringBuilder();
            for (int index = 0; index < recipientFileSchemaDTO.getIdList().length(); index++) {
                if (recipientFileSchemaDTO.getIdList().charAt(index) == ';' || index + 1 == recipientFileSchemaDTO.getIdList().length()) {
                    idList.add(Long.valueOf(id.toString()));
                    id = new StringBuilder();
                } else {
                    id.append(recipientFileSchemaDTO.getIdList().charAt(index));
                }
            }
            stringListMap.put(recipientFileSchemaDTO.getName(), idList);
        }
        return stringListMap;
    }
}
