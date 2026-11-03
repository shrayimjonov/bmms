package uz.paykap.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.paykap.domain.Gateway;
import uz.paykap.repository.GatewayRepository;
import uz.paykap.service.dto.GatewayDTO;
import uz.paykap.service.mapper.GatewayMapper;

/**
 * Service Implementation for managing {@link Gateway}.
 */
@Service
@Transactional
public class GatewayService {

    private final Logger log = LoggerFactory.getLogger(GatewayService.class);

    private final GatewayRepository gatewayRepository;

    private final GatewayMapper gatewayMapper;

    public GatewayService(GatewayRepository gatewayRepository, GatewayMapper gatewayMapper) {
        this.gatewayRepository = gatewayRepository;
        this.gatewayMapper = gatewayMapper;
    }

    /**
     * Save a gateway.
     *
     * @param gatewayDTO the entity to save.
     * @return the persisted entity.
     */
    public GatewayDTO save(GatewayDTO gatewayDTO) {
        log.debug("Request to save Gateway : {}", gatewayDTO);
        Gateway gateway = gatewayMapper.toEntity(gatewayDTO);
        gateway = gatewayRepository.save(gateway);
        return gatewayMapper.toDto(gateway);
    }

    /**
     * Partially update a gateway.
     *
     * @param gatewayDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<GatewayDTO> partialUpdate(GatewayDTO gatewayDTO) {
        log.debug("Request to partially update Gateway : {}", gatewayDTO);

        return gatewayRepository
            .findById(gatewayDTO.getId())
            .map(
                existingGateway -> {
                    gatewayMapper.partialUpdate(existingGateway, gatewayDTO);

                    return existingGateway;
                }
            )
            .map(gatewayRepository::save)
            .map(gatewayMapper::toDto);
    }

    /**
     * Get all the gateways.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<GatewayDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Gateways");
        return gatewayRepository.findAll(pageable).map(gatewayMapper::toDto);
    }

    /**
     *  Get all the gateways where Deposit is {@code null}.
     *  @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<GatewayDTO> findAllWhereDepositIsNull() {
        log.debug("Request to get all gateways where Deposit is null");
        return StreamSupport
            .stream(gatewayRepository.findAll().spliterator(), false)
            .filter(gateway -> gateway.getDeposit() == null)
            .map(gatewayMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get one gateway by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<GatewayDTO> findOne(Long id) {
        log.debug("Request to get Gateway : {}", id);
        return gatewayRepository.findById(id).map(gatewayMapper::toDto);
    }

    /**
     * Delete the gateway by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Gateway : {}", id);
        gatewayRepository.deleteById(id);
    }

    public void deleteCascaded(Long id) {
        log.debug("Request to delete Gateway : {}", id);
        gatewayRepository.deleteCascaded(id);
    }
}
