package uz.paykap.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GroupSettingMapperTest {

    private GroupSettingMapper groupSettingMapper;

    @BeforeEach
    public void setUp() {
        groupSettingMapper = new GroupSettingMapperImpl();
    }
}
