package uz.paykap.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class PrefixDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(PrefixDTO.class);
        PrefixDTO prefixDTO1 = new PrefixDTO();
        prefixDTO1.setId(1L);
        PrefixDTO prefixDTO2 = new PrefixDTO();
        assertThat(prefixDTO1).isNotEqualTo(prefixDTO2);
        prefixDTO2.setId(prefixDTO1.getId());
        assertThat(prefixDTO1).isEqualTo(prefixDTO2);
        prefixDTO2.setId(2L);
        assertThat(prefixDTO1).isNotEqualTo(prefixDTO2);
        prefixDTO1.setId(null);
        assertThat(prefixDTO1).isNotEqualTo(prefixDTO2);
    }
}
