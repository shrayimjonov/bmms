package uz.paykap.gateway.humo.helperService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.devops.humo.service.HumoGatewayService;
import uz.devops.humo.service.dto.GetPaymentRequestDTO;
import uz.devops.humo.service.dto.v3.getpayment.GetPaymentResult;
import uz.javlon.commons.result.HasData;
import uz.paykap.service.GenerateNumberService;


@Slf4j
@Service
@AllArgsConstructor
public class HumoDebitStatusService {

    private final HumoGatewayService humoGatewayService;
    private final GenerateNumberService generateNumberService;

    public HasData<GetPaymentResult> checkStatus(String paymentId, String requestId) {

        GetPaymentRequestDTO requestDTO = new GetPaymentRequestDTO();
        requestDTO.setPaymentId(paymentId);
        requestDTO.setSessionId(generateNumberService.humoSessionId("humoDebitStatusService", "GetPayment" + requestId));

        return humoGatewayService.getPaymentV3(requestDTO);
    }
}
