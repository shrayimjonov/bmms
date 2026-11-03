package uz.paykap.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SettingPropertyMapperTest {

    private SettingPropertyMapper settingPropertyMapper;

    @BeforeEach
    public void setUp() {
        settingPropertyMapper = new SettingPropertyMapperImpl();
    }
}
