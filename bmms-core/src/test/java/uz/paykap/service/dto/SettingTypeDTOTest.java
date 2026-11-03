package uz.paykap.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class SettingTypeDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(SettingTypeDTO.class);
        SettingTypeDTO settingTypeDTO1 = new SettingTypeDTO();
        settingTypeDTO1.setId(1L);
        SettingTypeDTO settingTypeDTO2 = new SettingTypeDTO();
        assertThat(settingTypeDTO1).isNotEqualTo(settingTypeDTO2);
        settingTypeDTO2.setId(settingTypeDTO1.getId());
        assertThat(settingTypeDTO1).isEqualTo(settingTypeDTO2);
        settingTypeDTO2.setId(2L);
        assertThat(settingTypeDTO1).isNotEqualTo(settingTypeDTO2);
        settingTypeDTO1.setId(null);
        assertThat(settingTypeDTO1).isNotEqualTo(settingTypeDTO2);
    }
}
