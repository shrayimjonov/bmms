package uz.paykap.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BookkeepingTransactionMapperTest {

    private BookkeepingTransactionMapper bookkeepingTransactionMapper;

    @BeforeEach
    public void setUp() {
        bookkeepingTransactionMapper = new BookkeepingTransactionMapperImpl();
    }
}
