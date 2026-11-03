package uz.paykap.service;

import java.util.List;
import java.util.Optional;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.paykap.domain.DataAdapterHistory;
import uz.paykap.repository.DataAdapterHistoryRepository;

/**
 * Service Implementation for managing {@link DataAdapterHistory}.
 */
@Service
@Transactional
public class DataAdapterHistoryService {

    private final Logger log = LoggerFactory.getLogger(DataAdapterHistoryService.class);

    private final DataAdapterHistoryRepository dataAdapterHistoryRepository;

    public DataAdapterHistoryService(DataAdapterHistoryRepository dataAdapterHistoryRepository) {
        this.dataAdapterHistoryRepository = dataAdapterHistoryRepository;
    }

    /**
     * Save a dataAdapterHistory.
     *
     * @param dataAdapterHistory the entity to save.
     * @return the persisted entity.
     */
    public DataAdapterHistory save(DataAdapterHistory dataAdapterHistory) {
        log.debug("Request to save DataAdapterHistory : {}", dataAdapterHistory);
        return dataAdapterHistoryRepository.save(dataAdapterHistory);
    }

    /**
     * Partially update a dataAdapterHistory.
     *
     * @param dataAdapterHistory the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<DataAdapterHistory> partialUpdate(DataAdapterHistory dataAdapterHistory) {
        log.debug("Request to partially update DataAdapterHistory : {}", dataAdapterHistory);

        return dataAdapterHistoryRepository
            .findById(dataAdapterHistory.getId())
            .map(
                existingDataAdapterHistory -> {
                    if (dataAdapterHistory.getName() != null) {
                        existingDataAdapterHistory.setName(dataAdapterHistory.getName());
                    }
                    if (dataAdapterHistory.getDetails() != null) {
                        existingDataAdapterHistory.setDetails(dataAdapterHistory.getDetails());
                    }

                    return existingDataAdapterHistory;
                }
            )
            .map(dataAdapterHistoryRepository::save);
    }

    /**
     * Get all the dataAdapterHistories.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<DataAdapterHistory> findAll(Pageable pageable) {
        log.debug("Request to get all DataAdapterHistories");
        return dataAdapterHistoryRepository.findAll(pageable);
    }

    /**
     * Get one dataAdapterHistory by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<DataAdapterHistory> findOne(Long id) {
        log.debug("Request to get DataAdapterHistory : {}", id);
        return dataAdapterHistoryRepository.findById(id);
    }

    /**
     * Delete the dataAdapterHistory by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete DataAdapterHistory : {}", id);
        dataAdapterHistoryRepository.deleteById(id);
    }

    /**
     * Get one dataAdapterHistory by name.
     *
     * @param name the name of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public DataAdapterHistory findOneByName(String name) {
        log.debug("Request to get DataAdapterHistory : {}", name);
        List<DataAdapterHistory> adapterHistories = dataAdapterHistoryRepository.findByName(name);
        if (adapterHistories.isEmpty()) {
            return null;
        }
        return adapterHistories.get(0);
    }
}
