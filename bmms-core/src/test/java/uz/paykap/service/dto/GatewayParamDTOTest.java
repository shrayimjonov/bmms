package uz.paykap.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class GatewayParamDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(GatewayParamDTO.class);
        GatewayParamDTO gatewayParamDTO1 = new GatewayParamDTO();
        gatewayParamDTO1.setId(1L);
        GatewayParamDTO gatewayParamDTO2 = new GatewayParamDTO();
        assertThat(gatewayParamDTO1).isNotEqualTo(gatewayParamDTO2);
        gatewayParamDTO2.setId(gatewayParamDTO1.getId());
        assertThat(gatewayParamDTO1).isEqualTo(gatewayParamDTO2);
        gatewayParamDTO2.setId(2L);
        assertThat(gatewayParamDTO1).isNotEqualTo(gatewayParamDTO2);
        gatewayParamDTO1.setId(null);
        assertThat(gatewayParamDTO1).isNotEqualTo(gatewayParamDTO2);
    }
}
