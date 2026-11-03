package uz.paykap.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class GroupSettingDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(GroupSettingDTO.class);
        GroupSettingDTO groupSettingDTO1 = new GroupSettingDTO();
        groupSettingDTO1.setId(1L);
        GroupSettingDTO groupSettingDTO2 = new GroupSettingDTO();
        assertThat(groupSettingDTO1).isNotEqualTo(groupSettingDTO2);
        groupSettingDTO2.setId(groupSettingDTO1.getId());
        assertThat(groupSettingDTO1).isEqualTo(groupSettingDTO2);
        groupSettingDTO2.setId(2L);
        assertThat(groupSettingDTO1).isNotEqualTo(groupSettingDTO2);
        groupSettingDTO1.setId(null);
        assertThat(groupSettingDTO1).isNotEqualTo(groupSettingDTO2);
    }
}
