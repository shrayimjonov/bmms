package uz.paykap.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import uz.paykap.IntegrationTest;
import uz.paykap.service.dto.CategoryDTO;
import uz.paykap.service.impl.FileStorageServiceImpl;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@IntegrationTest
public class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private FileStorageServiceImpl fileStorageService;

    private static final String LOGO_NAME = "logo.png";
    private static final byte[] LOGO_FILE_BYTES = new byte[]{1, 2, 3, 6, 5, 4};

    @Test
    public void testCategorySaveWithExistParameters() {
        String returningLogoName = saveFileToFileStorage(LOGO_NAME);
        CategoryDTO savedCategory = saveNewCategory(returningLogoName, "name1");
        assertThat(savedCategory.getLogo()).isEqualTo(returningLogoName);
        assertThat(savedCategory.getLogoFile()).isNull();
    }

    @Test
    public void testCategorySaveWithNullLogoName() {
        String returningLogoName = saveFileToFileStorage( "name2");
        CategoryDTO savedCategory = saveNewCategory( null, returningLogoName);
        assertThat(savedCategory.getLogo()).isEqualTo(returningLogoName);
        assertThat(savedCategory.getLogoFile()).isNull();
    }

    @Test
    public void testCategoryUpdateWithExistParameters() {
        String returningLogoName = saveFileToFileStorage( LOGO_NAME);
        CategoryDTO categoryDTO = saveNewCategory( LOGO_NAME, "name3");
        categoryDTO.setLogoFile(LOGO_FILE_BYTES);
        Optional<CategoryDTO> savedCategoryDTO = categoryService.partialUpdate(categoryDTO);
        assertThat(savedCategoryDTO.get().getLogo()).isEqualTo(returningLogoName);
        assertThat(savedCategoryDTO.get().getLogoFile()).isNull();
    }

    @Test
    public void testCategoryUpdateWithNullLogoName() {
        String returningLogoName = saveFileToFileStorage( "name4");
        CategoryDTO categoryDTO = saveNewCategory( null, returningLogoName);
        categoryDTO.setLogoFile(LOGO_FILE_BYTES);
        Optional<CategoryDTO> savedCategoryDTO = categoryService.partialUpdate(categoryDTO);
        assertThat(savedCategoryDTO.get().getLogo()).isEqualTo(returningLogoName);
        assertThat(savedCategoryDTO.get().getLogoFile()).isNull();
    }

    private CategoryDTO saveNewCategory( String logoName, String shortName) {
        return categoryService.save(createCategoryDTO(LOGO_FILE_BYTES, logoName, shortName));
    }

    private String saveFileToFileStorage( String logoName) {
        return fileStorageService.storeFile(LOGO_FILE_BYTES, logoName);
    }

    public CategoryDTO createCategoryDTO(byte[] logoFile, String logo, String shortName) {
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setLogoFile(logoFile);
        categoryDTO.setLogo(logo);
        categoryDTO.setShortName(shortName);
        categoryDTO.setActive(true);
        return categoryDTO;
    }
}
