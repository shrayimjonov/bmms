package uz.paykap.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class ForeignPayDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ForeignPayDTO.class);
        ForeignPayDTO foreignPayDTO1 = new ForeignPayDTO();
        foreignPayDTO1.setId(1L);
        ForeignPayDTO foreignPayDTO2 = new ForeignPayDTO();
        assertThat(foreignPayDTO1).isNotEqualTo(foreignPayDTO2);
        foreignPayDTO2.setId(foreignPayDTO1.getId());
        assertThat(foreignPayDTO1).isEqualTo(foreignPayDTO2);
        foreignPayDTO2.setId(2L);
        assertThat(foreignPayDTO1).isNotEqualTo(foreignPayDTO2);
        foreignPayDTO1.setId(null);
        assertThat(foreignPayDTO1).isNotEqualTo(foreignPayDTO2);
    }
}
