package uz.paykap.service.cancel.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.javlon.commons.result.HasData;
import uz.javlon.svgate.dto.enumeration.TranType;
import uz.paykap.domain.Bill;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.service.abs.AbstractAbsService;
import uz.paykap.service.abs.response.NciUpdateTranGroupType;
import uz.paykap.service.cancel.AbsCancelHelper;
import uz.paykap.service.cancel.CancelService;
import uz.paykap.service.cancel.resolver.CancelTransactionService;
import uz.paykap.service.dto.cancel.CancelPaymentInfoDTO;
import uz.paykap.service.dto.cancel.CancelRequestDTO;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.result.BmResultData;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service(value = ForexCancelService.SERVICE_NAME)
public class ForexCancelService implements CancelService {
    public static final String SERVICE_NAME = "forexCancelService";
    private final AbsCancelHelper absCancelHelper;
    private final AbstractAbsService abstractAbsService;
    private final ObjectMapper objectMapper;

    public ForexCancelService(AbsCancelHelper absCancelHelper, AbstractAbsService abstractAbsService, ObjectMapper objectMapper) {
        this.absCancelHelper = absCancelHelper;
        this.abstractAbsService = abstractAbsService;
        this.objectMapper = objectMapper;
    }

    @SneakyThrows
    @Override
    public HasData<Map<String, String>> cancel(CancelRequestDTO request) {
        log.info("ForexCancelService cancel request : {}", request);
        var resultData = abstractAbsService.reverseForex(request.getBillId());
        log.info("ForexCancelService cancel result : {}", resultData);
        Map<String, String> response = new HashMap<>();
        response.put(CancelTransactionService.REQUEST_KEY_NAME, objectMapper.writeValueAsString(resultData.getData().getDetails().getOrDefault(CancelTransactionService.REQUEST_KEY_NAME, null)));
        response.put(CancelTransactionService.RESPONSE_KEY_NANE, objectMapper.writeValueAsString(resultData.isSuccess() ? resultData.getData() : resultData.getDetails()));
        return new BmResultData<>(resultData.isSuccess() ? BmResultStatus.SUCCESS : BmResultStatus.PC_ERROR, response);
    }

    @SneakyThrows
    @Override
    public HasData<CancelPaymentInfoDTO> getInfo(Bill bill, TranType tranType) {
        log.info("ForexCancelService get info request : {} and type : {}", bill, tranType);
        return absCancelHelper.getInfo(bill, tranType, NciUpdateTranGroupType.FOREX);
    }

    @Override
    public P2pProcessorType supportedType() {
        return P2pProcessorType.FOREX;
    }
}
