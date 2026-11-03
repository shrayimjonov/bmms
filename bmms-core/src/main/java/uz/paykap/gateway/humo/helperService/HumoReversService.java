package uz.paykap.gateway.humo.helperService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.dto.request.SharedCancelDTO;
import uz.devops.bmcore.gateway.CancelHandler;
import uz.devops.humo.service.HumoGatewayService;
import uz.devops.humo.service.dto.ReverseRequestDTO;
import uz.devops.humo.service.dto.ReverseResultDTO;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.javlon.commons.result.HasData;
import uz.paykap.config.Constants;
import uz.paykap.domain.Transaction;
import uz.paykap.gateway.humo.HumoCreditGatewayServiceV3;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.service.GenerateNumberService;


@Slf4j
@Service
@AllArgsConstructor
public class HumoReversService {

    private final HumoGatewayService humoGatewayService;
    private final GenerateNumberService generateNumberService;
    private final TransactionRepository transactionRepository;

    public HasData<ReverseResultDTO> cancel(SharedCancelDTO reverseBill, CancelHandler cancelHandler) {

        ReverseRequestDTO reverseRequestDTO = new ReverseRequestDTO();

        if (cancelHandler.getPayRequestJson() != null && cancelHandler.getPayRequestJson().get(Constants.CENTRE_ID_PARAM) != null)
            reverseRequestDTO.setCentreID(String.valueOf(cancelHandler.getPayRequestJson().get(Constants.CENTRE_ID_PARAM)));

        reverseRequestDTO.setPaymentId(cancelHandler.getExtId());
        reverseRequestDTO.setPaymentRef(generateNumberService.humoPaymentRef(String.valueOf(reverseBill.getBillId())));
        reverseRequestDTO.setSessionId(generateNumberService.humoSessionId("humoDebitReversService", String.valueOf(reverseBill.getBillId())));
        reverseRequestDTO.setMerchantId(reverseBill.getRecipientParams().getEposMerchant());
        reverseRequestDTO.setTerminalId(reverseBill.getRecipientParams().getEposTerminal());

        if (reverseBill.getRecipientParams().getRecipientShortName().equals(HumoCreditGatewayServiceV3.NAME)) {
            Transaction transaction = transactionRepository.findTop1ById(reverseBill.getTranId()).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND, "transaction not found"));
            reverseRequestDTO.setSessionId(generateNumberService.humoSessionId("humoCreditReversService", String.valueOf(reverseBill.getBillId())));
            reverseRequestDTO.setMerchantId(transaction.getEposMerchant());
            reverseRequestDTO.setTerminalId(transaction.getEposTerminal());
        }

        log.info("### Sending Humo reverse request: {}", reverseRequestDTO);
        HasData<ReverseResultDTO> reversResult = humoGatewayService.reverse(reverseRequestDTO);
        log.info("#### Humo reverse response : {}", reversResult);
        return reversResult;
    }
}
