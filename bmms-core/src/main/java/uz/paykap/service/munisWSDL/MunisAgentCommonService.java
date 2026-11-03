package uz.paykap.service.munisWSDL;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.devops.munisagetdto.*;
import uz.paykap.service.munisWSDL.services.MunisAgentCancelTransferService;
import uz.paykap.service.munisWSDL.services.MunisAgentGetPayeeService;
import uz.paykap.service.munisWSDL.services.MunisAgentReceiveTransferService;

/**
 * Munis soap web service impl
 */
@Slf4j
@Service
@AllArgsConstructor
public class MunisAgentCommonService {

    private final MunisAgentGetPayeeService munisAgentGetPayeeService;
    private final MunisAgentReceiveTransferService munisAgentReceiveTransferService;
    private final MunisAgentCancelTransferService munisAgentCancelTransferService;

    /**
     * MUNIS getPayee (check) method impl
     * get info of recipient
     * @param request
     * @return GetPayeeResponse
     */
    public GetPayeeResponse getPayee(GetPayee request) {
        log.info("do getPayee method with params: {}", request);
        return munisAgentGetPayeeService.getPayee(request);
    }

    /**
     * MUNIS receiveTransfer (pay) method impl
     * do payment
     * @param request
     * @return ReceiveTransferResponse
     */
    public ReceiveTransferResponse receiveTransfer(ReceiveTransfer request) {
        log.info("do receiveTransfer method with params: {}", request);
        return munisAgentReceiveTransferService.receiveTransfer(request);
    }

    /**
     * MUNIS cancelTransfer (cancel) method impl
     * cancel transaction
     * @param request
     * @return CancelTransferResponse
     */
    public CancelTransferResponse cancelTransfer(CancelTransfer request) {
        log.info("do cancelTransfer method with params: {}", request);
        return munisAgentCancelTransferService.cancelTransfer(request);
    }
}
