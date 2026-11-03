package uz.paykap.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class SettingPropertyTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(SettingProperty.class);
        SettingProperty settingProperty1 = new SettingProperty();
        settingProperty1.setId(1L);
        SettingProperty settingProperty2 = new SettingProperty();
        settingProperty2.setId(settingProperty1.getId());
        assertThat(settingProperty1).isEqualTo(settingProperty2);
        settingProperty2.setId(2L);
        assertThat(settingProperty1).isNotEqualTo(settingProperty2);
        settingProperty1.setId(null);
        assertThat(settingProperty1).isNotEqualTo(settingProperty2);
    }
}
