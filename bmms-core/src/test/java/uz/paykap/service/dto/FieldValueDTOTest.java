package uz.paykap.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class FieldValueDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(FieldValueDTO.class);
        FieldValueDTO fieldValueDTO1 = new FieldValueDTO();
        fieldValueDTO1.setId(1L);
        FieldValueDTO fieldValueDTO2 = new FieldValueDTO();
        assertThat(fieldValueDTO1).isNotEqualTo(fieldValueDTO2);
        fieldValueDTO2.setId(fieldValueDTO1.getId());
        assertThat(fieldValueDTO1).isEqualTo(fieldValueDTO2);
        fieldValueDTO2.setId(2L);
        assertThat(fieldValueDTO1).isNotEqualTo(fieldValueDTO2);
        fieldValueDTO1.setId(null);
        assertThat(fieldValueDTO1).isNotEqualTo(fieldValueDTO2);
    }
}
