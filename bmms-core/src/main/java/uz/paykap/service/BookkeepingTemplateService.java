package uz.paykap.service;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.paykap.domain.BookkeepingTemplate;
import uz.paykap.repository.BookkeepingTemplateRepository;
import uz.paykap.service.dto.BookkeepingTemplateDTO;
import uz.paykap.service.mapper.BookkeepingTemplateMapper;

/**
 * Service Implementation for managing {@link BookkeepingTemplate}.
 */
@Service
@Transactional
public class BookkeepingTemplateService {

    private final Logger log = LoggerFactory.getLogger(BookkeepingTemplateService.class);

    private final BookkeepingTemplateRepository bookkeepingTemplateRepository;

    private final BookkeepingTemplateMapper bookkeepingTemplateMapper;

    public BookkeepingTemplateService(
        BookkeepingTemplateRepository bookkeepingTemplateRepository,
        BookkeepingTemplateMapper bookkeepingTemplateMapper
    ) {
        this.bookkeepingTemplateRepository = bookkeepingTemplateRepository;
        this.bookkeepingTemplateMapper = bookkeepingTemplateMapper;
    }

    /**
     * Save a bookkeepingTemplate.
     *
     * @param bookkeepingTemplateDTO the entity to save.
     * @return the persisted entity.
     */
    public BookkeepingTemplateDTO save(BookkeepingTemplateDTO bookkeepingTemplateDTO) {
        log.debug("Request to save BookkeepingTemplate : {}", bookkeepingTemplateDTO);
        BookkeepingTemplate bookkeepingTemplate = bookkeepingTemplateMapper.toEntity(bookkeepingTemplateDTO);
        bookkeepingTemplate = bookkeepingTemplateRepository.save(bookkeepingTemplate);
        return bookkeepingTemplateMapper.toDto(bookkeepingTemplate);
    }

    /**
     * Partially update a bookkeepingTemplate.
     *
     * @param bookkeepingTemplateDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<BookkeepingTemplateDTO> partialUpdate(BookkeepingTemplateDTO bookkeepingTemplateDTO) {
        log.debug("Request to partially update BookkeepingTemplate : {}", bookkeepingTemplateDTO);

        return bookkeepingTemplateRepository
            .findById(bookkeepingTemplateDTO.getId())
            .map(
                existingBookkeepingTemplate -> {
                    bookkeepingTemplateMapper.partialUpdate(existingBookkeepingTemplate, bookkeepingTemplateDTO);

                    return existingBookkeepingTemplate;
                }
            )
            .map(bookkeepingTemplateRepository::save)
            .map(bookkeepingTemplateMapper::toDto);
    }

    /**
     * Get all the bookkeepingTemplates.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<BookkeepingTemplateDTO> findAll(Pageable pageable) {
        log.debug("Request to get all BookkeepingTemplates");
        return bookkeepingTemplateRepository.findAll(pageable).map(bookkeepingTemplateMapper::toDto);
    }

    /**
     * Get one bookkeepingTemplate by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<BookkeepingTemplateDTO> findOne(Long id) {
        log.debug("Request to get BookkeepingTemplate : {}", id);
        return bookkeepingTemplateRepository.findById(id).map(bookkeepingTemplateMapper::toDto);
    }

    /**
     * Delete the bookkeepingTemplate by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete BookkeepingTemplate : {}", id);
        bookkeepingTemplateRepository.deleteById(id);
    }
}
