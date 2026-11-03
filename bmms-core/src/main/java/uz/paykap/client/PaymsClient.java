package uz.paykap.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uz.paykap.config.FeignConfiguration;
import uz.paykap.service.dto.MachineGroupDTO;
import uz.paykap.service.dto.UserMachineGroupDTO;

@AuthorizedUserFeignClient(name = "${bm-custom-feign.paymsUrl}", configuration = FeignConfiguration.class)
public interface PaymsClient {
    @PostMapping(value = "/api/user-machine-groups/machine-groups/get-by-login")
    ResponseEntity<UserMachineGroupDTO> getByLogin(@RequestBody String login);

    @PostMapping(value = "api/machine-groups/get-by-id")
    ResponseEntity<MachineGroupDTO> getGroupById(Long id);
}
