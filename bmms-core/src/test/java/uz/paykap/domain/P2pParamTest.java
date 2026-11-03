package uz.paykap.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class P2pParamTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(P2pParam.class);
        P2pParam p2pParam1 = new P2pParam();
        p2pParam1.setId(1L);
        P2pParam p2pParam2 = new P2pParam();
        p2pParam2.setId(p2pParam1.getId());
        assertThat(p2pParam1).isEqualTo(p2pParam2);
        p2pParam2.setId(2L);
        assertThat(p2pParam1).isNotEqualTo(p2pParam2);
        p2pParam1.setId(null);
        assertThat(p2pParam1).isNotEqualTo(p2pParam2);
    }
}
