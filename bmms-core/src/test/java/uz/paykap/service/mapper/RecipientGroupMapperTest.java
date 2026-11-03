package uz.paykap.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RecipientGroupMapperTest {

    private RecipientGroupMapper recipientGroupMapper;

    @BeforeEach
    public void setUp() {
        recipientGroupMapper = new RecipientGroupMapperImpl();
    }
}
