package uz.paykap.client.uaa;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uz.paykap.client.AuthorizedUserFeignClient;
import uz.paykap.config.FeignConfiguration;

@Service(value = "uaaClient")
@AuthorizedUserFeignClient(name = "${bm-custom-feign.uaaUrl}", configuration = FeignConfiguration.class)
public interface UaaClient {
    @PostMapping(value = "/api/admin/users/create-agent")
    ResponseEntity<UserDTO> createUserAndAgent(@RequestBody ManagedUserVM managedUserVM);

    @PostMapping(value = "/api/admin/users")
    ResponseEntity<UserDTO> createUser(@RequestBody ManagedUserVM managedUserVM);
}
