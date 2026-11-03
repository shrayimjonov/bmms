package uz.paykap.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ForeignPayMapperTest {

    private ForeignPayMapper foreignPayMapper;

    @BeforeEach
    public void setUp() {
        foreignPayMapper = new ForeignPayMapperImpl();
    }
}
