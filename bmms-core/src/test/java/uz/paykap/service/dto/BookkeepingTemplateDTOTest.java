package uz.paykap.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class BookkeepingTemplateDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(BookkeepingTemplateDTO.class);
        BookkeepingTemplateDTO bookkeepingTemplateDTO1 = new BookkeepingTemplateDTO();
        bookkeepingTemplateDTO1.setId(1L);
        BookkeepingTemplateDTO bookkeepingTemplateDTO2 = new BookkeepingTemplateDTO();
        assertThat(bookkeepingTemplateDTO1).isNotEqualTo(bookkeepingTemplateDTO2);
        bookkeepingTemplateDTO2.setId(bookkeepingTemplateDTO1.getId());
        assertThat(bookkeepingTemplateDTO1).isEqualTo(bookkeepingTemplateDTO2);
        bookkeepingTemplateDTO2.setId(2L);
        assertThat(bookkeepingTemplateDTO1).isNotEqualTo(bookkeepingTemplateDTO2);
        bookkeepingTemplateDTO1.setId(null);
        assertThat(bookkeepingTemplateDTO1).isNotEqualTo(bookkeepingTemplateDTO2);
    }
}
