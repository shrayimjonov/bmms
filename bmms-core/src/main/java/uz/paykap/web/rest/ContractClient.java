package uz.paykap.web.rest;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uz.paykap.client.AuthorizedUserFeignClient;
import uz.paykap.config.FeignConfiguration;
import uz.paykap.service.dto.ContractPaymentStatusDTO;

@AuthorizedUserFeignClient(name = "${bm-custom-feign.contractmsUrl}", configuration = FeignConfiguration.class)
public interface ContractClient {
    @PostMapping(value = "/api/contract-schedules/change-status-schedules")
    ResponseEntity<ContractPaymentStatusDTO> updateContract(@RequestBody ContractPaymentStatusDTO paymentStatusDTO);

    @PostMapping(value = "/api/contract-schedules/change-list-status-schedules")
    ResponseEntity<List<ContractPaymentStatusDTO>> updateContractList(@RequestBody List<ContractPaymentStatusDTO> paymentStatusDTOs);
}
