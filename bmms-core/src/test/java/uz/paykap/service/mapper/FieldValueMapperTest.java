package uz.paykap.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FieldValueMapperTest {

    private FieldValueMapper fieldValueMapper;

    @BeforeEach
    public void setUp() {
        fieldValueMapper = new FieldValueMapperImpl();
    }
}
