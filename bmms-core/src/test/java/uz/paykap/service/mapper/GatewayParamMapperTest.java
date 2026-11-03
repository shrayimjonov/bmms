package uz.paykap.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GatewayParamMapperTest {

    private GatewayParamMapper gatewayParamMapper;

    @BeforeEach
    public void setUp() {
        gatewayParamMapper = new GatewayParamMapperImpl();
    }
}
