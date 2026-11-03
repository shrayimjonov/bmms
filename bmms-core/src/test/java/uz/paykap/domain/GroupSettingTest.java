package uz.paykap.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class GroupSettingTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(GroupSetting.class);
        GroupSetting groupSetting1 = new GroupSetting();
        groupSetting1.setId(1L);
        GroupSetting groupSetting2 = new GroupSetting();
        groupSetting2.setId(groupSetting1.getId());
        assertThat(groupSetting1).isEqualTo(groupSetting2);
        groupSetting2.setId(2L);
        assertThat(groupSetting1).isNotEqualTo(groupSetting2);
        groupSetting1.setId(null);
        assertThat(groupSetting1).isNotEqualTo(groupSetting2);
    }
}
