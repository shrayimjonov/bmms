package uz.devops.bm.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.devops.bm.web.rest.TestUtil;

class JsonFormTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(JsonForm.class);
        JsonForm jsonForm1 = new JsonForm();
        jsonForm1.setId(1L);
        JsonForm jsonForm2 = new JsonForm();
        jsonForm2.setId(jsonForm1.getId());
        assertThat(jsonForm1).isEqualTo(jsonForm2);
        jsonForm2.setId(2L);
        assertThat(jsonForm1).isNotEqualTo(jsonForm2);
        jsonForm1.setId(null);
        assertThat(jsonForm1).isNotEqualTo(jsonForm2);
    }
}
