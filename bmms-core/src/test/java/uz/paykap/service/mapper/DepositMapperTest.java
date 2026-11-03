package uz.paykap.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DepositMapperTest {

    private DepositMapper depositMapper;

    @BeforeEach
    public void setUp() {
        depositMapper = new DepositMapperImpl();
    }
}
