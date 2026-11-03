package uz.paykap.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class ForeignPayTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ForeignPay.class);
        ForeignPay foreignPay1 = new ForeignPay();
        foreignPay1.setId(1L);
        ForeignPay foreignPay2 = new ForeignPay();
        foreignPay2.setId(foreignPay1.getId());
        assertThat(foreignPay1).isEqualTo(foreignPay2);
        foreignPay2.setId(2L);
        assertThat(foreignPay1).isNotEqualTo(foreignPay2);
        foreignPay1.setId(null);
        assertThat(foreignPay1).isNotEqualTo(foreignPay2);
    }
}
