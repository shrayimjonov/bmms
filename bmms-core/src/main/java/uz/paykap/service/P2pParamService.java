package uz.paykap.service;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uz.paykap.domain.P2pParam;
import uz.paykap.service.dto.p2p.P2pParamInfoRequest;
import uz.paykap.service.p2p.dto.P2pParamResult;

/**
 * Service Interface for managing {@link P2pParam}.
 */
public interface P2pParamService {
    /**
     * Save a p2pParam.
     *
     * @param p2pParam the entity to save.
     * @return the persisted entity.
     */
    P2pParam save(P2pParam p2pParam);

    /**
     * Partially updates a p2pParam.
     *
     * @param p2pParam the entity to update partially.
     * @return the persisted entity.
     */
    Optional<P2pParam> partialUpdate(P2pParam p2pParam);

    /**
     * Get all the p2pParams.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<P2pParam> findAll(Pageable pageable);

    /**
     * Get the "id" p2pParam.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<P2pParam> findOne(Long id);

    /**
     * Delete the "id" p2pParam.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    P2pParamResult getP2pParamInfo(P2pParamInfoRequest p2pParamInfoRequest);
}
