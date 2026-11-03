package uz.paykap.service.cancel;

import uz.javlon.commons.result.HasData;
import uz.javlon.svgate.dto.enumeration.TranType;
import uz.paykap.domain.Bill;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.service.dto.cancel.CancelPaymentInfoDTO;
import uz.paykap.service.dto.cancel.CancelRequestDTO;

import java.util.Map;

public interface CancelService {
    HasData<Map<String, String>> cancel(CancelRequestDTO request);

    HasData<CancelPaymentInfoDTO> getInfo(Bill bill, TranType tranType);

    P2pProcessorType supportedType();
}
