package uz.paykap.service;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uz.paykap.domain.P2pCommission;

/**
 * Service Interface for managing {@link P2pCommission}.
 */
public interface P2pCommissionService {
    /**
     * Save a p2pCommission.
     *
     * @param p2pCommission the entity to save.
     * @return the persisted entity.
     */
    P2pCommission save(P2pCommission p2pCommission);

    /**
     * Partially updates a p2pCommission.
     *
     * @param p2pCommission the entity to update partially.
     * @return the persisted entity.
     */
    Optional<P2pCommission> partialUpdate(P2pCommission p2pCommission);

    /**
     * Get all the p2pCommissions.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<P2pCommission> findAll(Pageable pageable);

    /**
     * Get the "id" p2pCommission.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<P2pCommission> findOne(Long id);

    /**
     * Delete the "id" p2pCommission.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
