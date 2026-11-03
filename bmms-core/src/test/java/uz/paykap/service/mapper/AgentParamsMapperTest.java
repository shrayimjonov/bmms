package uz.paykap.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AgentParamsMapperTest {

    private AgentParamsMapper agentParamsMapper;

    @BeforeEach
    public void setUp() {
        agentParamsMapper = new AgentParamsMapperImpl();
    }
}
