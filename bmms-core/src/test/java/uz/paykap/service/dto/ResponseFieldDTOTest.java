package uz.paykap.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class ResponseFieldDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ResponseFieldDTO.class);
        ResponseFieldDTO responseFieldDTO1 = new ResponseFieldDTO();
        responseFieldDTO1.setId(1L);
        ResponseFieldDTO responseFieldDTO2 = new ResponseFieldDTO();
        assertThat(responseFieldDTO1).isNotEqualTo(responseFieldDTO2);
        responseFieldDTO2.setId(responseFieldDTO1.getId());
        assertThat(responseFieldDTO1).isEqualTo(responseFieldDTO2);
        responseFieldDTO2.setId(2L);
        assertThat(responseFieldDTO1).isNotEqualTo(responseFieldDTO2);
        responseFieldDTO1.setId(null);
        assertThat(responseFieldDTO1).isNotEqualTo(responseFieldDTO2);
    }
}
