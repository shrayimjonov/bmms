package uz.paykap.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AgentRecipientMapperTest {

    private AgentRecipientMapper agentRecipientMapper;

    @BeforeEach
    public void setUp() {
        agentRecipientMapper = new AgentRecipientMapperImpl();
    }
}
