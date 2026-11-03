package uz.paykap.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class AgentParamsTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AgentParams.class);
        AgentParams agentParams1 = new AgentParams();
        agentParams1.setId(1L);
        AgentParams agentParams2 = new AgentParams();
        agentParams2.setId(agentParams1.getId());
        assertThat(agentParams1).isEqualTo(agentParams2);
        agentParams2.setId(2L);
        assertThat(agentParams1).isNotEqualTo(agentParams2);
        agentParams1.setId(null);
        assertThat(agentParams1).isNotEqualTo(agentParams2);
    }
}
