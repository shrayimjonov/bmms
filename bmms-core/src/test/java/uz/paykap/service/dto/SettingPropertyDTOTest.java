package uz.paykap.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class SettingPropertyDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(SettingPropertyDTO.class);
        SettingPropertyDTO settingPropertyDTO1 = new SettingPropertyDTO();
        settingPropertyDTO1.setId(1L);
        SettingPropertyDTO settingPropertyDTO2 = new SettingPropertyDTO();
        assertThat(settingPropertyDTO1).isNotEqualTo(settingPropertyDTO2);
        settingPropertyDTO2.setId(settingPropertyDTO1.getId());
        assertThat(settingPropertyDTO1).isEqualTo(settingPropertyDTO2);
        settingPropertyDTO2.setId(2L);
        assertThat(settingPropertyDTO1).isNotEqualTo(settingPropertyDTO2);
        settingPropertyDTO1.setId(null);
        assertThat(settingPropertyDTO1).isNotEqualTo(settingPropertyDTO2);
    }
}
