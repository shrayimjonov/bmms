package uz.paykap.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class I18nDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(I18nDTO.class);
        I18nDTO i18nDTO1 = new I18nDTO();
        i18nDTO1.setId(1L);
        I18nDTO i18nDTO2 = new I18nDTO();
        assertThat(i18nDTO1).isNotEqualTo(i18nDTO2);
        i18nDTO2.setId(i18nDTO1.getId());
        assertThat(i18nDTO1).isEqualTo(i18nDTO2);
        i18nDTO2.setId(2L);
        assertThat(i18nDTO1).isNotEqualTo(i18nDTO2);
        i18nDTO1.setId(null);
        assertThat(i18nDTO1).isNotEqualTo(i18nDTO2);
    }
}
