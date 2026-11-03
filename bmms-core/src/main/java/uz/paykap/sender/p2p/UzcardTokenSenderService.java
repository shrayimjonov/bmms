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
import uz.paykap.service.p2p.dto.P2pParamResult;
import uz.paykap.service.p2p.dto.P2pTokenDTO;
import uz.paykap.service.p2p.support.AbstractP2pSender;
import uz.paykap.service.p2p.support.BillHandler;
import uz.paykap.service.uzcard.UzcardHelperService;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class UzcardTokenSenderService extends AbstractP2pSender<P2pTokenDTO> {

    private final PcHelper pcHelper;
    private final UzcardHelperService uzcardHelperService;

    @Override
    public BmResultData<String> isSupported(P2pTokenDTO dto, P2pAccount p2pAccount) {

        String token = dto.getToken();

        boolean matches = pcHelper.isValidToken(token, P2pProcessorType.UZCARD);

        if (!matches) {
            return new BmResultData<>(BmResultStatus.SENDER_NOT_FOUND, "Not valid uzcard token");
        }

        return new BmResultData<>(BmResultStatus.SUCCESS, "OK, valid uzcard token");
    }

    @Override
    public P2pProcessorType getProcessorType() {
        return P2pProcessorType.UZCARD;
    }

    @Override
    protected BmResultData<Map<String, Object>> getInfo(P2pTokenDTO dto, BillHandler handler) {

        BmResultData<SvCardV2DTO> svCard = uzcardHelperService.getSvCardByToken(dto.getToken());

        if (!svCard.isSuccess()) {
            handler.error(String.valueOf(svCard.getCode()), svCard.getMessage());
            return new BmResultData<>(svCard.getStatus());
        }

        Map<String, Object> data = Collections.singletonMap(Constants.PHONE_PARAM, svCard.getData().getPhone());

        handler.success(data);

        return new BmResultData<>(data);
    }

    @Override
    public BmResultData<Bill> debit(Transaction transaction, P2pTokenDTO senderDto, P2pDTO recipientDto, P2pParamResult p2pParam) {
        return uzcardHelperService.debit(transaction, senderDto.getToken(), p2pParam);
    }

    @Override
    public HasData<Bill> reverse(Transaction transaction) {
        return uzcardHelperService.reverse(transaction, true);
    }
}
