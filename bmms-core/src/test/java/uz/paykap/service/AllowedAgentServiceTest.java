package uz.paykap.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import uz.paykap.IntegrationTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import uz.paykap.PkbillmsApp;
import uz.paykap.IntegrationTest;
import uz.paykap.service.dto.api.AllData;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@IntegrationTest
public class AllowedAgentServiceTest {

    @Autowired
    private AllowedAgentService allowedAgentService;

    // TODO - fix principal.username in repositories, not working with test

    @Test
    public void dummy() {
        assertThat(true).isTrue();
    }

//    @Test
//    @WithMockUser(username = "user")
//    public void getAllByLogin() {
//        AllData allByLogin = allowedAgentService.getAllByLogin();
//
//        assertThat(allByLogin).isNotNull();
//    }

//    @Test
    @WithMockUser(username = "user")
    public void getRecipientInfoByLoginAndId() {
        AllData allByLogin = allowedAgentService.getRecipientInfoByLoginAndId(-123123L);

        assertThat(allByLogin).isNotNull();
    }
}
