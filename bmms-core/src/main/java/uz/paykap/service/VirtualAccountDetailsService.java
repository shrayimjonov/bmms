package uz.paykap.service;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.paykap.domain.VirtualAccountDetails;
import uz.paykap.repository.VirtualAccountDetailsRepository;
import uz.paykap.service.dto.VirtualAccountDetailsDTO;
import uz.paykap.service.mapper.VirtualAccountDetailsMapper;

/**
 * Service Implementation for managing {@link VirtualAccountDetails}.
 */
@Service
@Transactional
public class VirtualAccountDetailsService {

    private final Logger log = LoggerFactory.getLogger(VirtualAccountDetailsService.class);

    private final VirtualAccountDetailsRepository virtualAccountDetailsRepository;

    private final VirtualAccountDetailsMapper virtualAccountDetailsMapper;

    public VirtualAccountDetailsService(
        VirtualAccountDetailsRepository virtualAccountDetailsRepository,
        VirtualAccountDetailsMapper virtualAccountDetailsMapper
    ) {
        this.virtualAccountDetailsRepository = virtualAccountDetailsRepository;
        this.virtualAccountDetailsMapper = virtualAccountDetailsMapper;
    }

    /**
     * Save a virtualAccountDetails.
     *
     * @param virtualAccountDetailsDTO the entity to save.
     * @return the persisted entity.
     */
    public VirtualAccountDetailsDTO save(VirtualAccountDetailsDTO virtualAccountDetailsDTO) {
        log.debug("Request to save VirtualAccountDetails : {}", virtualAccountDetailsDTO);
        VirtualAccountDetails virtualAccountDetails = virtualAccountDetailsMapper.toEntity(virtualAccountDetailsDTO);
        virtualAccountDetails = virtualAccountDetailsRepository.save(virtualAccountDetails);
        return virtualAccountDetailsMapper.toDto(virtualAccountDetails);
    }

    /**
     * Partially update a virtualAccountDetails.
     *
     * @param virtualAccountDetailsDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<VirtualAccountDetailsDTO> partialUpdate(VirtualAccountDetailsDTO virtualAccountDetailsDTO) {
        log.debug("Request to partially update VirtualAccountDetails : {}", virtualAccountDetailsDTO);

        return virtualAccountDetailsRepository
            .findById(virtualAccountDetailsDTO.getId())
            .map(
                existingVirtualAccountDetails -> {
                    virtualAccountDetailsMapper.partialUpdate(existingVirtualAccountDetails, virtualAccountDetailsDTO);

                    return existingVirtualAccountDetails;
                }
            )
            .map(virtualAccountDetailsRepository::save)
            .map(virtualAccountDetailsMapper::toDto);
    }

    /**
     * Get all the virtualAccountDetails.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<VirtualAccountDetailsDTO> findAll(Pageable pageable) {
        log.debug("Request to get all VirtualAccountDetails");
        return virtualAccountDetailsRepository.findAll(pageable).map(virtualAccountDetailsMapper::toDto);
    }

    /**
     * Get one virtualAccountDetails by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<VirtualAccountDetailsDTO> findOne(Long id) {
        log.debug("Request to get VirtualAccountDetails : {}", id);
        return virtualAccountDetailsRepository.findById(id).map(virtualAccountDetailsMapper::toDto);
    }

    /**
     * Delete the virtualAccountDetails by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete VirtualAccountDetails : {}", id);
        virtualAccountDetailsRepository.deleteById(id);
    }
}
