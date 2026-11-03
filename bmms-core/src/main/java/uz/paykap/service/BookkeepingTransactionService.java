package uz.paykap.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.domain.BookkeepingTransaction;
import uz.paykap.domain.enumeration.BookkeepingTransactionType;
import uz.paykap.repository.BookkeepingTransactionRepository;
import uz.paykap.service.dto.BookkeepingTransactionDTO;
import uz.paykap.service.mapper.BookkeepingTransactionMapper;

/**
 * Service Implementation for managing {@link BookkeepingTransaction}.
 */
@Service
@Transactional
public class BookkeepingTransactionService {

    public static final String PREV_AMOUNT = "prev_amount";
    private final Logger log = LoggerFactory.getLogger(BookkeepingTransactionService.class);

    private final BookkeepingTransactionRepository bookkeepingTransactionRepository;

    private final BookkeepingTransactionMapper bookkeepingTransactionMapper;

    private final ObjectMapper objectMapper;

    public BookkeepingTransactionService(
        BookkeepingTransactionRepository bookkeepingTransactionRepository,
        BookkeepingTransactionMapper bookkeepingTransactionMapper,
        ObjectMapper objectMapper
    ) {
        this.bookkeepingTransactionRepository = bookkeepingTransactionRepository;
        this.bookkeepingTransactionMapper = bookkeepingTransactionMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * Save a bookkeepingTransaction.
     *
     * @param bookkeepingTransactionDTO the entity to save.
     * @return the persisted entity.
     */
    public BookkeepingTransactionDTO save(BookkeepingTransactionDTO bookkeepingTransactionDTO) {
        log.debug("Request to save BookkeepingTransaction : {}", bookkeepingTransactionDTO);
        BookkeepingTransaction bookkeepingTransaction = bookkeepingTransactionMapper.toEntity(bookkeepingTransactionDTO);
        bookkeepingTransaction = bookkeepingTransactionRepository.save(bookkeepingTransaction);
        return bookkeepingTransactionMapper.toDto(bookkeepingTransaction);
    }

    /**
     * Partially update a bookkeepingTransaction.
     *
     * @param bookkeepingTransactionDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<BookkeepingTransactionDTO> partialUpdate(BookkeepingTransactionDTO bookkeepingTransactionDTO) {
        log.debug("Request to partially update BookkeepingTransaction : {}", bookkeepingTransactionDTO);

        return bookkeepingTransactionRepository
            .findById(bookkeepingTransactionDTO.getId())
            .map(
                existingBookkeepingTransaction -> {
                    bookkeepingTransactionMapper.partialUpdate(existingBookkeepingTransaction, bookkeepingTransactionDTO);

                    return existingBookkeepingTransaction;
                }
            )
            .map(bookkeepingTransactionRepository::save)
            .map(bookkeepingTransactionMapper::toDto);
    }

    /**
     * Get all the bookkeepingTransactions.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<BookkeepingTransactionDTO> findAll(Pageable pageable) {
        log.debug("Request to get all BookkeepingTransactions");
        return bookkeepingTransactionRepository.findAll(pageable).map(bookkeepingTransactionMapper::toDto);
    }

    /**
     * Get one bookkeepingTransaction by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<BookkeepingTransactionDTO> findOne(Long id) {
        log.debug("Request to get BookkeepingTransaction : {}", id);
        return bookkeepingTransactionRepository.findById(id).map(bookkeepingTransactionMapper::toDto);
    }

    /**
     * Delete the bookkeepingTransaction by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete BookkeepingTransaction : {}", id);
        bookkeepingTransactionRepository.deleteById(id);
    }

    public List<BookkeepingTransactionDTO> getAllExternal() {
        log.debug("Request to get all external BookkeepingTransactions");
        return bookkeepingTransactionMapper.toDto(
            bookkeepingTransactionRepository.findAllByBookkeepingType(BookkeepingTransactionType.EXTERNAL).orElse(Collections.emptyList())
        );
    }

    public List<BookkeepingTransactionDTO> getAllInternal() {
        log.debug("Request to get all internal BookkeepingTransactions");
        return bookkeepingTransactionMapper.toDto(
            bookkeepingTransactionRepository.findAllByBookkeepingType(internal.example.com).orElse(Collections.emptyList())
        );
    }

    public BookkeepingTransactionDTO editSum(Long amount, Long bookkeepingTransactionId) {
        log.debug("Edit sum with id :{}", bookkeepingTransactionId);
        BookkeepingTransaction transaction = bookkeepingTransactionRepository
            .findById(bookkeepingTransactionId)
            .orElseThrow(() -> new CodedException(ProblemEnum.OBJECT_NOT_FOUND));

        try {
            Map<String, String> map = objectMapper.readValue(transaction.getParams(), new TypeReference<Map<String, String>>() {});
            if (!map.containsKey(PREV_AMOUNT)) {
                map.put(PREV_AMOUNT, transaction.getAmount());
            }
            transaction.setAmount(String.valueOf(amount));
        } catch (IOException e) {
            log.warn("Error when parsing :{}", e.getLocalizedMessage());
        }
        transaction = bookkeepingTransactionRepository.save(transaction);
        return bookkeepingTransactionMapper.toDto(transaction);
    }
}
