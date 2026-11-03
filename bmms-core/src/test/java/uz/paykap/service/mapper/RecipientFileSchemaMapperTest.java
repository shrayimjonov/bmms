package uz.paykap.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RecipientFileSchemaMapperTest {

    private RecipientFileSchemaMapper recipientFileSchemaMapper;

    @BeforeEach
    public void setUp() {
        recipientFileSchemaMapper = new RecipientFileSchemaMapperImpl();
    }
}
