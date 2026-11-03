package uz.paykap.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class P2pCommissionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(P2pCommission.class);
        P2pCommission p2pCommission1 = new P2pCommission();
        p2pCommission1.setId(1L);
        P2pCommission p2pCommission2 = new P2pCommission();
        p2pCommission2.setId(p2pCommission1.getId());
        assertThat(p2pCommission1).isEqualTo(p2pCommission2);
        p2pCommission2.setId(2L);
        assertThat(p2pCommission1).isNotEqualTo(p2pCommission2);
        p2pCommission1.setId(null);
        assertThat(p2pCommission1).isNotEqualTo(p2pCommission2);
    }
}
