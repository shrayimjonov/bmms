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
import uz.paykap.domain.GatewayParam;
import uz.paykap.repository.GatewayParamRepository;
import uz.paykap.service.dto.GatewayParamDTO;
import uz.paykap.service.mapper.GatewayParamMapper;

/**
 * Service Implementation for managing {@link GatewayParam}.
 */
@Service
@Transactional
public class GatewayParamService {

    private final Logger log = LoggerFactory.getLogger(GatewayParamService.class);

    private final GatewayParamRepository gatewayParamRepository;

    private final GatewayParamMapper gatewayParamMapper;

    public GatewayParamService(GatewayParamRepository gatewayParamRepository, GatewayParamMapper gatewayParamMapper) {
        this.gatewayParamRepository = gatewayParamRepository;
        this.gatewayParamMapper = gatewayParamMapper;
    }

    /**
     * Save a gatewayParam.
     *
     * @param gatewayParamDTO the entity to save.
     * @return the persisted entity.
     */
    public GatewayParamDTO save(GatewayParamDTO gatewayParamDTO) {
        log.debug("Request to save GatewayParam : {}", gatewayParamDTO);
        GatewayParam gatewayParam = gatewayParamMapper.toEntity(gatewayParamDTO);
        gatewayParam = gatewayParamRepository.save(gatewayParam);
        return gatewayParamMapper.toDto(gatewayParam);
    }

    /**
     * Partially update a gatewayParam.
     *
     * @param gatewayParamDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<GatewayParamDTO> partialUpdate(GatewayParamDTO gatewayParamDTO) {
        log.debug("Request to partially update GatewayParam : {}", gatewayParamDTO);

        return gatewayParamRepository
            .findById(gatewayParamDTO.getId())
            .map(
                existingGatewayParam -> {
                    gatewayParamMapper.partialUpdate(existingGatewayParam, gatewayParamDTO);

                    return existingGatewayParam;
                }
            )
            .map(gatewayParamRepository::save)
            .map(gatewayParamMapper::toDto);
    }

    /**
     * Get all the gatewayParams.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<GatewayParamDTO> findAll(Pageable pageable) {
        log.debug("Request to get all GatewayParams");
        return gatewayParamRepository.findAll(pageable).map(gatewayParamMapper::toDto);
    }

    /**
     * Get one gatewayParam by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<GatewayParamDTO> findOne(Long id) {
        log.debug("Request to get GatewayParam : {}", id);
        return gatewayParamRepository.findById(id).map(gatewayParamMapper::toDto);
    }

    /**
     * Delete the gatewayParam by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete GatewayParam : {}", id);
        gatewayParamRepository.deleteById(id);
    }

    /**
     * Get list gatewayParams by recipient id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public List<GatewayParamDTO> findAllByRecipientId(Long id) {
        log.debug("Request to get GatewayParam list by recipient ID: {}", id);
        return gatewayParamMapper.toDto(gatewayParamRepository.findAllByRecipient_Id(id));
    }

    public GatewayParamDTO getOrCreate(GatewayParamDTO gatewayParamDTO) {
        log.debug("Request to save GatewayParam : {}", gatewayParamDTO);
        GatewayParam gatewayParam = gatewayParamMapper.toEntity(gatewayParamDTO);
        //        gatewayParam = gatewayParamRepository.findTop1ByRecipientAndType(gatewayParam.getRecipient(),gatewayParam.getType()).orElse(gatewayParamRepository.save(gatewayParam));
        //        gatewayParam = gatewayParamRepository.save(gatewayParam);
        return gatewayParamMapper.toDto(
            gatewayParamRepository
                .findTop1ByRecipientAndType(gatewayParam.getRecipient(), gatewayParam.getType())
                .orElse(gatewayParamRepository.save(gatewayParam))
        );
    }
}
