package uz.paykap.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PrefixMapperTest {

    private PrefixMapper prefixMapper;

    @BeforeEach
    public void setUp() {
        prefixMapper = new PrefixMapperImpl();
    }
}
