package uz.paykap.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SenderMapperTest {

    private SenderMapper senderMapper;

    @BeforeEach
    public void setUp() {
        senderMapper = new SenderMapperImpl();
    }
}
