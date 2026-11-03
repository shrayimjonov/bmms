package uz.paykap.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class ResponseFieldTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ResponseField.class);
        ResponseField responseField1 = new ResponseField();
        responseField1.setId(1L);
        ResponseField responseField2 = new ResponseField();
        responseField2.setId(responseField1.getId());
        assertThat(responseField1).isEqualTo(responseField2);
        responseField2.setId(2L);
        assertThat(responseField1).isNotEqualTo(responseField2);
        responseField1.setId(null);
        assertThat(responseField1).isNotEqualTo(responseField2);
    }
}
