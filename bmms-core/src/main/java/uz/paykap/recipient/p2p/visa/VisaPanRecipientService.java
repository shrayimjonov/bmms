package uz.paykap.recipient.p2p.visa;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.enumeration.RequestType;
import uz.javlon.commons.result.HasData;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.service.dto.p2p.P2pAccount;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.p2p.dto.P2pDTO;
import uz.paykap.service.p2p.dto.P2pPanDTO;
import uz.paykap.service.p2p.dto.P2pParamResult;
import uz.paykap.service.p2p.processor.IsApiForexParam;
import uz.paykap.service.p2p.support.AbstractP2pRecipient;
import uz.paykap.service.p2p.support.BillHandler;

import java.util.Map;

@Profile("davr")
@Slf4j
@Service
@AllArgsConstructor
public class VisaPanRecipientService extends AbstractP2pRecipient<P2pPanDTO> {

    private final CommonVisaService commonVisaService;

    @Override
    public BmResultData<String> isSupported(P2pPanDTO dto, P2pAccount p2pAccount) {
        return commonVisaService.isSupported(dto.getPan(), p2pAccount);
    }

    @Override
    public P2pProcessorType getProcessorType() {
        return P2pProcessorType.VISA;
    }

    @Override
    protected BmResultData<Map<String, Object>> getInfo(P2pPanDTO dto, BillHandler handler) {
        return commonVisaService.getInfo(dto.getPan(), handler);
    }

    @Override
    public BmResultData<Bill> credit(Transaction transaction, P2pDTO senderDto, P2pPanDTO recipientDto, P2pParamResult p2pParam) {

        BmResultData<IsApiForexParam> paramResult = getRecipientParam(p2pParam.getValue());
        if (!paramResult.isSuccess()) {
            return new BmResultData<>(paramResult);
        }

        return commonVisaService.credit(transaction, paramResult.getData(), recipientDto.getPan(), recipientDto.getComment(), senderDto);
    }

    @Override
    public HasData<Bill> reverse(Transaction transaction) {
        return commonVisaService.reverse(transaction, RequestType.P2P_CREDIT);
    }
}
