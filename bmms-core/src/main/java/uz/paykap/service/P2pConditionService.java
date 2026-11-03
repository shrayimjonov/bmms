package uz.paykap.service;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uz.paykap.domain.P2pCondition;

/**
 * Service Interface for managing {@link P2pCondition}.
 */
public interface P2pConditionService {
    /**
     * Save a p2pCondition.
     *
     * @param p2pCondition the entity to save.
     * @return the persisted entity.
     */
    P2pCondition save(P2pCondition p2pCondition);

    /**
     * Partially updates a p2pCondition.
     *
     * @param p2pCondition the entity to update partially.
     * @return the persisted entity.
     */
    Optional<P2pCondition> partialUpdate(P2pCondition p2pCondition);

    /**
     * Get all the p2pConditions.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<P2pCondition> findAll(Pageable pageable);

    /**
     * Get the "id" p2pCondition.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<P2pCondition> findOne(Long id);

    /**
     * Delete the "id" p2pCondition.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
