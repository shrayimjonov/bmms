package uz.paykap.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RecipientMapperTest {

    private RecipientMapper recipientMapper;

    @BeforeEach
    public void setUp() {
        recipientMapper = new RecipientMapperImpl();
    }
}
