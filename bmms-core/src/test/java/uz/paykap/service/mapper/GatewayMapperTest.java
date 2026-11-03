package uz.paykap.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GatewayMapperTest {

    private GatewayMapper gatewayMapper;

    @BeforeEach
    public void setUp() {
        gatewayMapper = new GatewayMapperImpl();
    }
}
