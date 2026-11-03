package uz.paykap.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DepositTransactionMapperTest {

    private DepositTransactionMapper depositTransactionMapper;

    @BeforeEach
    public void setUp() {
        depositTransactionMapper = new DepositTransactionMapperImpl();
    }
}
