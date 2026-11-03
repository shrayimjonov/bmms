package uz.paykap.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class AgentRecipientTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AgentRecipient.class);
        AgentRecipient agentRecipient1 = new AgentRecipient();
        agentRecipient1.setId(1L);
        AgentRecipient agentRecipient2 = new AgentRecipient();
        agentRecipient2.setId(agentRecipient1.getId());
        assertThat(agentRecipient1).isEqualTo(agentRecipient2);
        agentRecipient2.setId(2L);
        assertThat(agentRecipient1).isNotEqualTo(agentRecipient2);
        agentRecipient1.setId(null);
        assertThat(agentRecipient1).isNotEqualTo(agentRecipient2);
    }
}
