package uz.paykap.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class AgentParamsDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(AgentParamsDTO.class);
        AgentParamsDTO agentParamsDTO1 = new AgentParamsDTO();
        agentParamsDTO1.setId(1L);
        AgentParamsDTO agentParamsDTO2 = new AgentParamsDTO();
        assertThat(agentParamsDTO1).isNotEqualTo(agentParamsDTO2);
        agentParamsDTO2.setId(agentParamsDTO1.getId());
        assertThat(agentParamsDTO1).isEqualTo(agentParamsDTO2);
        agentParamsDTO2.setId(2L);
        assertThat(agentParamsDTO1).isNotEqualTo(agentParamsDTO2);
        agentParamsDTO1.setId(null);
        assertThat(agentParamsDTO1).isNotEqualTo(agentParamsDTO2);
    }
}
