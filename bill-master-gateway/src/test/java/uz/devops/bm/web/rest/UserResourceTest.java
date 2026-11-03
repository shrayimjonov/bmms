package uz.devops.bm.web.rest;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import uz.devops.bm.IntegrationTest;
import uz.devops.bm.security.AuthoritiesConstants;
import uz.devops.bm.service.dto.ManagedUserVM;

@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
@IntegrationTest
@AutoConfigureWebTestClient(timeout = "36000000")
public class UserResourceTest {

    public static final String LOGIN = "abdurahim";
    public static final String PASSWORD = "123456";
    public static final String PHONE_NUMBER = "998901234567";
    public static final String CREATE_AGENT_API = "/api/admin/users/create-agent";

    @Autowired
    private UserResource userResource;

    @Autowired
    private WebTestClient webTestClient;

    @SneakyThrows
    @Test
    public void createAgent() {
        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setLogin(LOGIN);
        managedUserVM.setActivated(true);
        //        managedUserVM.setPassword(PASSWORD);
        //        managedUserVM.setPhoneNumber(PHONE_NUMBER);
        //
        //        webTestClient
        //            .post()
        //            .uri(CREATE_AGENT_API)
        //            .contentType(MediaType.APPLICATION_JSON)
        //            .bodyValue(TestUtil.convertObjectToJsonBytes(managedUserVM))
        //            .exchange()
        //            .expectStatus()
        //            .isOk();
    }
}
