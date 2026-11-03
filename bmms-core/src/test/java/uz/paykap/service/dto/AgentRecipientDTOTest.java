package uz.paykap.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class AgentRecipientDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(AgentRecipientDTO.class);
        AgentRecipientDTO agentRecipientDTO1 = new AgentRecipientDTO();
        agentRecipientDTO1.setId(1L);
        AgentRecipientDTO agentRecipientDTO2 = new AgentRecipientDTO();
        assertThat(agentRecipientDTO1).isNotEqualTo(agentRecipientDTO2);
        agentRecipientDTO2.setId(agentRecipientDTO1.getId());
        assertThat(agentRecipientDTO1).isEqualTo(agentRecipientDTO2);
        agentRecipientDTO2.setId(2L);
        assertThat(agentRecipientDTO1).isNotEqualTo(agentRecipientDTO2);
        agentRecipientDTO1.setId(null);
        assertThat(agentRecipientDTO1).isNotEqualTo(agentRecipientDTO2);
    }
}
