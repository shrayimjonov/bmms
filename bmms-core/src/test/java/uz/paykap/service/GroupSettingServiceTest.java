package uz.paykap.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import uz.paykap.IntegrationTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uz.paykap.PkbillmsApp;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.GroupSetting;
import uz.paykap.domain.SettingProperty;
import uz.paykap.domain.SettingType;
import uz.paykap.domain.enumeration.Prop;
import uz.paykap.repository.GroupSettingRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@IntegrationTest
public class GroupSettingServiceTest {

    @Autowired
    private GroupSettingRepository groupSettingRepository;
//    @Autowired
//    private SettingTypeRepository settingTypeRepository;

    @Test
    @Transactional
    public void findOne() {

        SettingType settingTypePayment = new SettingType().name("Payment");
        SettingType settingTypeCharity = new SettingType().name("Charity");

//        settingTypePayment = settingTypeRepository.saveAndFlush(settingTypePayment);
//        settingTypeCharity = settingTypeRepository.saveAndFlush(settingTypeCharity);

        GroupSetting groupSettingPayment = groupSettingRepository.saveAndFlush(create(settingTypePayment));
        GroupSetting groupSettingCharity = groupSettingRepository.saveAndFlush(create(settingTypeCharity));

//        Optional<GroupSetting> settingOptionalPayment = groupSettingRepository.findOne(settingTypePayment.getId(), groupSettingPayment.getRequestGroup());
        Optional<GroupSetting> settingOptionalPayment = groupSettingRepository.findOne(groupSettingPayment.getRequestGroup());
        assertThat(settingOptionalPayment.isPresent()).isTrue();
        assertThat(settingOptionalPayment.get().getProps()).hasSize(3);

//        Optional<GroupSetting> settingOptionalCharity = groupSettingRepository.findOne(settingTypeCharity.getId(), groupSettingCharity.getRequestGroup());
        Optional<GroupSetting> settingOptionalCharity = groupSettingRepository.findOne(groupSettingCharity.getRequestGroup());
        assertThat(settingOptionalCharity.isPresent()).isTrue();
        assertThat(settingOptionalCharity.get().getProps()).hasSize(3);
    }

    public GroupSetting create(SettingType settingType) {

        String prefix = settingType.getName();

        return new GroupSetting()
            .requestGroup(prefix + "-test-request-group")
//            .settingType(settingType)
            .addProp(new SettingProperty()
                .key(Prop.UZCARD_MERCHANT)
                .value(prefix + "-test-merchant")
            )
            .addProp(new SettingProperty()
                .key(Prop.UZCARD_TERMINAL)
                .value(prefix + "-test-terminal")
            )
            .addProp(new SettingProperty()
                .key(Prop.UZCARD_PORT)
                .value(prefix + "-test-port")
            );
    }
}
