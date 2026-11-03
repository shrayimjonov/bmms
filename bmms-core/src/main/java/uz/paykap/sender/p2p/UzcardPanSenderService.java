package uz.paykap.sender.p2p;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.javlon.commons.result.HasData;
import uz.javlon.svgate.dto.sv.SvCardV2DTO;
import uz.paykap.config.Constants;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.service.PcHelper;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.p2p.P2pAccount;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.p2p.dto.P2pDTO;
import uz.paykap.service.p2p.dto.P2pPanDTO;
import uz.paykap.service.p2p.dto.P2pParamResult;
import uz.paykap.service.p2p.support.AbstractP2pSender;
import uz.paykap.service.p2p.support.BillHandler;
import uz.paykap.service.uzcard.UzcardHelperService;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class UzcardPanSenderService extends AbstractP2pSender<P2pPanDTO> {

    private final PcHelper pcHelper;
    private final UzcardHelperService uzcardHelperService;

    @Override
    public BmResultData<String> isSupported(P2pPanDTO dto, P2pAccount p2pAccount) {

        String pan = dto.getPan();

        boolean matches = pcHelper.isValidPan(pan, P2pProcessorType.UZCARD);

        if (!matches) {
            return new BmResultData<>(BmResultStatus.INVALID_SENDER_ACCOUNT, "Not valid Uzcard pan");
        }

        return new BmResultData<>(pan);
    }

    @Override
    public P2pProcessorType getProcessorType() {
        return P2pProcessorType.UZCARD;
    }

    @Override
    protected BmResultData<Map<String, Object>> getInfo(P2pPanDTO dto, BillHandler handler) {

        BmResultData<SvCardV2DTO> cardV2DTOResultData = uzcardHelperService.getSvCard(dto.getPan(), dto.getExp());

        if (!cardV2DTOResultData.isSuccess()) {
            handler.error(String.valueOf(cardV2DTOResultData.getCode()), cardV2DTOResultData.getDetails());
            return new BmResultData<>(cardV2DTOResultData.getStatus(), "error while getting uzcard token");
        }

        Map<String, Object> data = Collections.singletonMap(Constants.PHONE_PARAM, cardV2DTOResultData.getData().getPhone());

        handler.success(data);

        return new BmResultData<>(data);
    }

    @Override
    public BmResultData<Bill> debit(Transaction transaction, P2pPanDTO senderDto, P2pDTO recipientDto, P2pParamResult p2pParam) {

        final BmResultData<SvCardV2DTO> cardV2DTOResultData = uzcardHelperService.getSvCard(senderDto.getPan(), senderDto.getExp());

        if (!cardV2DTOResultData.isSuccess()) {
            return new BmResultData<>(cardV2DTOResultData.getStatus(), "error while getting uzcard token");
        }

        return uzcardHelperService.debit(transaction, cardV2DTOResultData.getData().getToken(), p2pParam);
    }

    @Override
    public HasData<Bill> reverse(Transaction transaction) {

        return uzcardHelperService.reverse(transaction, true);
    }

}
