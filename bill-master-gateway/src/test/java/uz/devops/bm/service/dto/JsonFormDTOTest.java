package uz.devops.bm.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.devops.bm.web.rest.TestUtil;

class JsonFormDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(JsonFormDTO.class);
        JsonFormDTO jsonFormDTO1 = new JsonFormDTO();
        jsonFormDTO1.setId(1L);
        JsonFormDTO jsonFormDTO2 = new JsonFormDTO();
        assertThat(jsonFormDTO1).isNotEqualTo(jsonFormDTO2);
        jsonFormDTO2.setId(jsonFormDTO1.getId());
        assertThat(jsonFormDTO1).isEqualTo(jsonFormDTO2);
        jsonFormDTO2.setId(2L);
        assertThat(jsonFormDTO1).isNotEqualTo(jsonFormDTO2);
        jsonFormDTO1.setId(null);
        assertThat(jsonFormDTO1).isNotEqualTo(jsonFormDTO2);
    }
}
