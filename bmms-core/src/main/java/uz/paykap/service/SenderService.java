package uz.paykap.service;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bmcore.enumeration.PayType;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.domain.Sender;
import uz.paykap.repository.SenderRepository;
import uz.paykap.service.dto.SenderDTO;
import uz.paykap.service.mapper.SenderMapper;

/**
 * Service Implementation for managing {@link Sender}.
 */
@Service
@Transactional
public class SenderService {

    private final Logger log = LoggerFactory.getLogger(SenderService.class);

    private final SenderRepository senderRepository;

    private final SenderMapper senderMapper;

    private final ApplicationContext applicationContext;

    public SenderService(SenderRepository senderRepository, SenderMapper senderMapper, ApplicationContext applicationContext) {
        this.senderRepository = senderRepository;
        this.senderMapper = senderMapper;
        this.applicationContext = applicationContext;
    }

    /**
     * Save a sender.
     *
     * @param senderDTO the entity to save.
     * @return the persisted entity.
     */
    public SenderDTO save(SenderDTO senderDTO) {
        log.debug("Request to save Sender : {}", senderDTO);
        Sender sender = senderMapper.toEntity(senderDTO);
        sender = senderRepository.save(sender);
        return senderMapper.toDto(sender);
    }

    /**
     * Partially update a sender.
     *
     * @param senderDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<SenderDTO> partialUpdate(SenderDTO senderDTO) {
        log.debug("Request to partially update Sender : {}", senderDTO);

        return senderRepository
            .findById(senderDTO.getId())
            .map(
                existingSender -> {
                    senderMapper.partialUpdate(existingSender, senderDTO);

                    return existingSender;
                }
            )
            .map(senderRepository::save)
            .map(senderMapper::toDto);
    }

    /**
     * Get all the senders.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<SenderDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Senders");
        return senderRepository.findAll(pageable).map(senderMapper::toDto);
    }

    /**
     * Get one sender by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<SenderDTO> findOne(Long id) {
        log.debug("Request to get Sender : {}", id);
        return senderRepository.findById(id).map(senderMapper::toDto);
    }

    /**
     * Delete the sender by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Sender : {}", id);
        senderRepository.deleteById(id);
    }

    public String[] getBeans(PayType payType) {
        log.debug("Request to get beans with PayType:{}", payType);
        switch (payType) {
            case UZCARD:
            case CASH:
            case HUMO:
            case VISA:
                //TODO
                return applicationContext.getBeanNamesForType(SenderService.class);
            // TODO ADD OTHER PAY TYPES
            default:
                throw new CodedException(ProblemEnum.INVALID_PARAMS, "payType invalid");
        }
    }
}
