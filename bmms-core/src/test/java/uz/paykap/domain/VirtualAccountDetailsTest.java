package uz.paykap.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class VirtualAccountDetailsTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(VirtualAccountDetails.class);
        VirtualAccountDetails virtualAccountDetails1 = new VirtualAccountDetails();
        virtualAccountDetails1.setId(1L);
        VirtualAccountDetails virtualAccountDetails2 = new VirtualAccountDetails();
        virtualAccountDetails2.setId(virtualAccountDetails1.getId());
        assertThat(virtualAccountDetails1).isEqualTo(virtualAccountDetails2);
        virtualAccountDetails2.setId(2L);
        assertThat(virtualAccountDetails1).isNotEqualTo(virtualAccountDetails2);
        virtualAccountDetails1.setId(null);
        assertThat(virtualAccountDetails1).isNotEqualTo(virtualAccountDetails2);
    }
}
