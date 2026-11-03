package uz.paykap.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VirtualAccountMapperTest {

    private VirtualAccountMapper virtualAccountMapper;

    @BeforeEach
    public void setUp() {
        virtualAccountMapper = new VirtualAccountMapperImpl();
    }
}
