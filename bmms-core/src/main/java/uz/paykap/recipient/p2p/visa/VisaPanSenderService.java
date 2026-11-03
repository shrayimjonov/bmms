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
import uz.paykap.service.p2p.support.AbstractP2pSender;
import uz.paykap.service.p2p.support.BillHandler;

import java.util.Map;

@Profile("davr")
@Slf4j
@Service
@AllArgsConstructor
public class VisaPanSenderService extends AbstractP2pSender<P2pPanDTO> {

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
//        Long amount = null;
//        if (p2pRequestScopedContext != null && p2pRequestScopedContext.getRequest() != null && p2pRequestScopedContext.getRequest().getAmount() != null) {
//            amount = p2pRequestScopedContext.getRequest().getAmount();
//        }
//
//        return visaRecipientService.getInfoSender(dto.getPan(), amount, handler);
        return commonVisaService.getInfo(dto.getPan(), handler);
    }

    @Override
    public BmResultData<Bill> debit(Transaction transaction, P2pPanDTO senderDto, P2pDTO recipientDto, P2pParamResult p2pParam) {

        BmResultData<IsApiForexParam> paramResult = getSenderParam(p2pParam.getValue());
        if (!paramResult.isSuccess()) {
            return new BmResultData<>(paramResult);
        }

        return commonVisaService.debit(transaction, paramResult.getData(), senderDto.getPan(), senderDto.getComment(), senderDto, recipientDto);
    }

    @Override
    public HasData<Bill> reverse(Transaction transaction) {
        return commonVisaService.reverse(transaction, RequestType.P2P_DEBIT);
    }
}
