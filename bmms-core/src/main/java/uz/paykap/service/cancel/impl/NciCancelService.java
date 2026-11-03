package uz.paykap.service.cancel.impl;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.javlon.commons.result.HasData;
import uz.javlon.svgate.dto.enumeration.TranType;
import uz.paykap.domain.Bill;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.service.abs.response.NciUpdateTranGroupType;
import uz.paykap.service.cancel.AbsCancelHelper;
import uz.paykap.service.cancel.CancelService;
import uz.paykap.service.dto.cancel.CancelPaymentInfoDTO;
import uz.paykap.service.dto.cancel.CancelRequestDTO;

import java.util.Map;

@Slf4j
@Service(value = NciCancelService.SERVICE_NAME)
@AllArgsConstructor
public class NciCancelService implements CancelService {
    public static final String SERVICE_NAME = "nciCancelService";
    private final AbsCancelHelper absCancelHelper;

    @SneakyThrows
    @Override
    public HasData<Map<String, String>> cancel(CancelRequestDTO request) {
        return absCancelHelper.cancel(request);
    }

    @SneakyThrows
    @Override
    public HasData<CancelPaymentInfoDTO> getInfo(Bill bill, TranType tranType) {
        return absCancelHelper.getInfo(bill, tranType, NciUpdateTranGroupType.GENERAL);
    }

    @Override
    public P2pProcessorType supportedType() {
        return P2pProcessorType.NCI;
    }
}
