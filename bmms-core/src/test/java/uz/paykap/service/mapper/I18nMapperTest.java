package uz.paykap.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class I18nMapperTest {

    private I18nMapper i18nMapper;

    @BeforeEach
    public void setUp() {
        i18nMapper = new I18nMapperImpl();
    }
}
