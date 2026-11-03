package uz.paykap.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class P2pConditionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(P2pCondition.class);
        P2pCondition p2pCondition1 = new P2pCondition();
        p2pCondition1.setId(1L);
        P2pCondition p2pCondition2 = new P2pCondition();
        p2pCondition2.setId(p2pCondition1.getId());
        assertThat(p2pCondition1).isEqualTo(p2pCondition2);
        p2pCondition2.setId(2L);
        assertThat(p2pCondition1).isNotEqualTo(p2pCondition2);
        p2pCondition1.setId(null);
        assertThat(p2pCondition1).isNotEqualTo(p2pCondition2);
    }
}
