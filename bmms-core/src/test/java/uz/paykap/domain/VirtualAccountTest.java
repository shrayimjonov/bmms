package uz.paykap.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class VirtualAccountTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(VirtualAccount.class);
        VirtualAccount virtualAccount1 = new VirtualAccount();
        virtualAccount1.setId(1L);
        VirtualAccount virtualAccount2 = new VirtualAccount();
        virtualAccount2.setId(virtualAccount1.getId());
        assertThat(virtualAccount1).isEqualTo(virtualAccount2);
        virtualAccount2.setId(2L);
        assertThat(virtualAccount1).isNotEqualTo(virtualAccount2);
        virtualAccount1.setId(null);
        assertThat(virtualAccount1).isNotEqualTo(virtualAccount2);
    }
}
