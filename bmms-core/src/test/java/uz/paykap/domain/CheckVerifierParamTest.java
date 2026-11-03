package uz.paykap.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class CheckVerifierParamTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CheckVerifierParam.class);
        CheckVerifierParam checkVerifierParam1 = new CheckVerifierParam();
        checkVerifierParam1.setId(1L);
        CheckVerifierParam checkVerifierParam2 = new CheckVerifierParam();
        checkVerifierParam2.setId(checkVerifierParam1.getId());
        assertThat(checkVerifierParam1).isEqualTo(checkVerifierParam2);
        checkVerifierParam2.setId(2L);
        assertThat(checkVerifierParam1).isNotEqualTo(checkVerifierParam2);
        checkVerifierParam1.setId(null);
        assertThat(checkVerifierParam1).isNotEqualTo(checkVerifierParam2);
    }
}
