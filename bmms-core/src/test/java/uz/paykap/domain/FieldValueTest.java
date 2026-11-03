package uz.paykap.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class FieldValueTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(FieldValue.class);
        FieldValue fieldValue1 = new FieldValue();
        fieldValue1.setId(1L);
        FieldValue fieldValue2 = new FieldValue();
        fieldValue2.setId(fieldValue1.getId());
        assertThat(fieldValue1).isEqualTo(fieldValue2);
        fieldValue2.setId(2L);
        assertThat(fieldValue1).isNotEqualTo(fieldValue2);
        fieldValue1.setId(null);
        assertThat(fieldValue1).isNotEqualTo(fieldValue2);
    }
}
