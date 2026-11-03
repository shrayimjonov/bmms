package uz.paykap.client.uaa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import feign.RetryableException;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;
import uz.paykap.IntegrationTest;

@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
public class UaaClientTest {

    @Autowired
    private UaaClient uaaClient;

    //    @Ignore
    //    @Test
    //    @Transactional
    public void testCreateUser() {
        ManagedUserVM managedUserVM = new ManagedUserVM();

        Throwable exception = assertThrows(RetryableException.class, () -> uaaClient.createUserAndAgent(managedUserVM));
        assertEquals("bmgw executing POST http://bmgw/api/admin/users/create-agent", exception.getMessage());
    }
}
