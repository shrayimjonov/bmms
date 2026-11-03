package uz.paykap.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FieldMapperTest {

    private FieldMapper fieldMapper;

    @BeforeEach
    public void setUp() {
        fieldMapper = new FieldMapperImpl();
    }
}
