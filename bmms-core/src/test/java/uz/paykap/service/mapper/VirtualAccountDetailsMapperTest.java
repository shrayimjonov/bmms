package uz.paykap.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VirtualAccountDetailsMapperTest {

    private VirtualAccountDetailsMapper virtualAccountDetailsMapper;

    @BeforeEach
    public void setUp() {
        virtualAccountDetailsMapper = new VirtualAccountDetailsMapperImpl();
    }
}
