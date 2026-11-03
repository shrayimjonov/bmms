package uz.paykap.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.paykap.domain.Category;
import uz.paykap.repository.CategoryRepository;
import uz.paykap.service.dto.CategoryDTO;
import uz.paykap.service.impl.FileStorageServiceImpl;
import uz.paykap.service.mapper.CategoryMapper;

import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link Category}.
 */
@Service
@Transactional
public class CategoryService {

    private final Logger log = LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;

    private final CategoryMapper categoryMapper;

    private final FileStorageServiceImpl fileStorageService;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper, FileStorageServiceImpl fileStorageService) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Save a category.
     *
     * @param categoryDTO the entity to save.
     * @return the persisted entity.
     */
    public CategoryDTO save(CategoryDTO categoryDTO) {
        log.debug("Request to save Category : {}", categoryDTO);
        String logoName = fileStorageService.storeFile(categoryDTO.getLogoFile(), categoryDTO.getLogo() == null ? categoryDTO.getShortName() : categoryDTO.getLogo());
        categoryDTO.setLogo(logoName);
        categoryDTO.setLogoFile(null);
        Category category = categoryMapper.toEntity(categoryDTO);
        category = categoryRepository.save(category);
        return categoryMapper.toDto(category);
    }

    /**
     * Partially update a category.
     *
     * @param categoryDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<CategoryDTO> partialUpdate(CategoryDTO categoryDTO) {
        log.debug("Request to partially update Category : {}", categoryDTO);

        return categoryRepository
            .findById(categoryDTO.getId())
            .map(
                existingCategory -> {
                    categoryMapper.partialUpdate(existingCategory, categoryDTO);
                    String logoName = fileStorageService.storeFile(categoryDTO.getLogoFile(), categoryDTO.getLogo() == null ? categoryDTO.getShortName() : categoryDTO.getLogo());
                    existingCategory.setLogo(logoName);
                    existingCategory.setLogoFile(null);
                    return existingCategory;
                }
            )
            .map(categoryRepository::save)
            .map(categoryMapper::toDto);
    }

    /**
     * Get all the categories.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<CategoryDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Categories");
        return categoryRepository.findAll(pageable).map(categoryMapper::toDto);
    }

    /**
     * Get all the categories with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<CategoryDTO> findAllWithEagerRelationships(Pageable pageable) {
        return categoryRepository.findAllWithEagerRelationships(pageable).map(categoryMapper::toDto);
    }

    /**
     * Get one category by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<CategoryDTO> findOne(Long id) {
        log.debug("Request to get Category : {}", id);
        return categoryRepository.findOneWithEagerRelationships(id).map(categoryMapper::toDto);
    }

    /**
     * Delete the category by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Category : {}", id);
        categoryRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> findAll() {
        log.debug("Request to get all Categories");
        return categoryMapper.toDto(categoryRepository.findAll());
    }

    public Category getCategoryOrCreate(Category category) {
        log.debug("Getting Category : {}", category.getShortName());
        return categoryRepository.findTop1ByShortName(category.getShortName()).orElseGet(() -> categoryRepository.save(category));
    }
}
