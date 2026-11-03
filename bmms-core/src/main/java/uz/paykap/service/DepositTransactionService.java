package uz.paykap.service;

import java.time.Instant;
import java.util.Optional;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.cbu.config.Constants;
import uz.paykap.domain.DepositTransaction;
import uz.paykap.repository.DepositTransactionRepository;
import uz.paykap.security.SecurityUtils;
import uz.paykap.service.dto.DepositTransactionDTO;
import uz.paykap.service.dto.ReplenishDepositDTO;
import uz.paykap.service.mapper.DepositTransactionMapper;

/**
 * Service Implementation for managing {@link DepositTransaction}.
 */
@Service
@Transactional
public class DepositTransactionService {

    private final Logger log = LoggerFactory.getLogger(DepositTransactionService.class);

    private final DepositTransactionRepository depositTransactionRepository;

    private final DepositTransactionMapper depositTransactionMapper;

    public DepositTransactionService(
        DepositTransactionRepository depositTransactionRepository,
        DepositTransactionMapper depositTransactionMapper
    ) {
        this.depositTransactionRepository = depositTransactionRepository;
        this.depositTransactionMapper = depositTransactionMapper;
    }

    /**
     * Save a depositTransaction.
     *
     * @param depositTransactionDTO the entity to save.
     * @return the persisted entity.
     */
    public DepositTransactionDTO save(DepositTransactionDTO depositTransactionDTO) {
        log.debug("Request to save DepositTransaction : {}", depositTransactionDTO);
        DepositTransaction depositTransaction = depositTransactionMapper.toEntity(depositTransactionDTO);
        depositTransaction = depositTransactionRepository.save(depositTransaction);
        return depositTransactionMapper.toDto(depositTransaction);
    }

    public DepositTransactionDTO register(DepositTransactionDTO depositTransactionDTO) {
        log.debug("Request to save DepositTransaction : {}", depositTransactionDTO);
        DepositTransaction depositTransaction = depositTransactionMapper.toEntity(depositTransactionDTO);
        depositTransaction = depositTransactionRepository.save(depositTransaction);
        return depositTransactionMapper.toDto(depositTransaction);
    }

    /**
     * Partially update a depositTransaction.
     *
     * @param depositTransactionDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<DepositTransactionDTO> partialUpdate(DepositTransactionDTO depositTransactionDTO) {
        log.debug("Request to partially update DepositTransaction : {}", depositTransactionDTO);

        return depositTransactionRepository
            .findById(depositTransactionDTO.getId())
            .map(
                existingDepositTransaction -> {
                    depositTransactionMapper.partialUpdate(existingDepositTransaction, depositTransactionDTO);

                    return existingDepositTransaction;
                }
            )
            .map(depositTransactionRepository::save)
            .map(depositTransactionMapper::toDto);
    }

    /**
     * Get all the depositTransactions.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<DepositTransactionDTO> findAll(Pageable pageable) {
        log.debug("Request to get all DepositTransactions");
        return depositTransactionRepository.findAll(pageable).map(depositTransactionMapper::toDto);
    }

    /**
     * Get one depositTransaction by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<DepositTransactionDTO> findOne(Long id) {
        log.debug("Request to get DepositTransaction : {}", id);
        return depositTransactionRepository.findById(id).map(depositTransactionMapper::toDto);
    }

    /**
     * Delete the depositTransaction by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete DepositTransaction : {}", id);
        depositTransactionRepository.deleteById(id);
    }

    public void save(ReplenishDepositDTO replenish, Long beforeBalance, Long afterBalance, Integer currency) {
        DepositTransactionDTO depositTransactionDTO = new DepositTransactionDTO();
        depositTransactionDTO.setAmount(replenish.getBalanceAmount());
        depositTransactionDTO.setComment(
            SecurityUtils.getCurrentUserLogin().orElse("system") +
            " emitted " +
            replenish.getBalanceAmount() +
            " " +
            Constants.getAlfaCode(currency) +
            " (" +
            currency +
            ") for " +
            replenish.getAgentLogin() +
            ". " +
            beforeBalance +
            " -> " +
            replenish.getBalanceAmount() +
            " -> " +
            afterBalance
        );
        depositTransactionDTO.setDoc(replenish.getDoc());
        depositTransactionDTO.setPayerTaxnr(replenish.getPayerTaxnr());
        depositTransactionDTO.setRecipientTaxnr(replenish.getRecipientTaxnr());
        depositTransactionDTO.setUpdateDate(Instant.now());
        depositTransactionDTO.setAgentLogin(replenish.getAgentLogin());
        save(depositTransactionDTO);
    }
}
