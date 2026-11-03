package uz.paykap.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class GatewayParamTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(GatewayParam.class);
        GatewayParam gatewayParam1 = new GatewayParam();
        gatewayParam1.setId(1L);
        GatewayParam gatewayParam2 = new GatewayParam();
        gatewayParam2.setId(gatewayParam1.getId());
        assertThat(gatewayParam1).isEqualTo(gatewayParam2);
        gatewayParam2.setId(2L);
        assertThat(gatewayParam1).isNotEqualTo(gatewayParam2);
        gatewayParam1.setId(null);
        assertThat(gatewayParam1).isNotEqualTo(gatewayParam2);
    }
}
