package uz.paykap.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResponseFieldMapperTest {

    private ResponseFieldMapper responseFieldMapper;

    @BeforeEach
    public void setUp() {
        responseFieldMapper = new ResponseFieldMapperImpl();
    }
}
