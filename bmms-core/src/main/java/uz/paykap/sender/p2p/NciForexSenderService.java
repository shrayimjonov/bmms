package uz.paykap.sender.p2p;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.javlon.commons.result.HasData;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.service.PcHelper;
import uz.paykap.service.abs.AbstractAbsService;
import uz.paykap.service.abs.NciCommonService;
import uz.paykap.service.abs.response.NciUpdateTranGroupType;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.p2p.P2pAccount;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.p2p.dto.P2pDTO;
import uz.paykap.service.p2p.dto.P2pForexDTO;
import uz.paykap.service.p2p.dto.P2pParamResult;
import uz.paykap.service.p2p.support.AbstractP2pSender;
import uz.paykap.service.p2p.support.BillHandler;
import uz.paykap.service.p2p.support.ForexHelperService;

import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class NciForexSenderService extends AbstractP2pSender<P2pForexDTO> {

    private final AbstractAbsService abstractAbsService;
    private final ForexHelperService forexHelperService;
    private final NciCommonService nciCommonService;
    private final PcHelper pcHelper;

    @Override
    public BmResultData<?> isSupported(P2pForexDTO dto, P2pAccount p2pAccount) {

        String account = dto.getAccount();

        if (!pcHelper.isValidAccount(account, P2pProcessorType.FOREX)) {
            return new BmResultData<>(BmResultStatus.INVALID_SENDER_ACCOUNT, "Invalid Forex account");
        }

        return new BmResultData<>(account);
    }

    @Override
    public P2pProcessorType getProcessorType() {
        return P2pProcessorType.FOREX;
    }

    @Override
    protected BmResultData<Map<String, Object>> getInfo(P2pForexDTO dto, BillHandler handler) {
        return abstractAbsService.getInfo(dto.getAccount(), dto.getMfo(), handler);
    }

    @Override
    public BmResultData<Bill> debit(Transaction transaction, P2pForexDTO senderDto, P2pDTO recipientDto, P2pParamResult p2pParam) {
        return forexHelperService.doForexDebitTransaction(senderDto, transaction, p2pParam);
    }


    @Override
    public HasData<Bill> reverse(Transaction transaction) {
        return nciCommonService.reverse(transaction, true, NciUpdateTranGroupType.FOREX);
    }
}
