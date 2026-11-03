package uz.paykap.sender.p2p;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.javlon.commons.result.HasData;
import uz.paykap.config.Constants;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.service.HumoMiddlewareHelperService;
import uz.paykap.service.PcHelper;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.p2p.P2pAccount;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.humo.HumoP2pHelperService;
import uz.paykap.service.p2p.dto.P2pDTO;
import uz.paykap.service.p2p.dto.P2pParamResult;
import uz.paykap.service.p2p.dto.P2pTokenDTO;
import uz.paykap.service.p2p.processor.HasEposParam;
import uz.paykap.service.p2p.support.AbstractP2pSender;
import uz.paykap.service.p2p.support.BillHandler;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class HumoTokenSenderService extends AbstractP2pSender<P2pTokenDTO> {

    private final PcHelper pcHelper;
    private final HumoP2pHelperService humoHelperService;
    private final HumoMiddlewareHelperService humoMiddlewareHelperService;

    @Override
    public BmResultData<String> isSupported(P2pTokenDTO dto, P2pAccount p2pAccount) {
        String token = dto.getToken();

        boolean matches = pcHelper.isValidToken(token, P2pProcessorType.HUMO);

        if (!matches) {
            return new BmResultData<>(BmResultStatus.INVALID_SENDER_ACCOUNT, "Invalid HUMO token");
        }

        return new BmResultData<>("OK, valid humo token");
    }

    @Override
    public P2pProcessorType getProcessorType() {
        return P2pProcessorType.HUMO;
    }

    @Override
    protected BmResultData<Map<String, Object>> getInfo(P2pTokenDTO dto, BillHandler handler) {

        BmResultData<String> realPanResponse = humoHelperService.getRealPan(dto.getToken());

        if (!realPanResponse.isSuccess()) {
            handler.error(Collections.singletonMap("error", realPanResponse.getMessage()));
            return new BmResultData<>(realPanResponse.getStatus(), realPanResponse.getMessage());
        }

        String realPan = realPanResponse.getData();

        log.debug("Get info realPan : {}", realPan);

        HasData<String> phoneNumber = humoMiddlewareHelperService.getPhoneNumber(realPan);

        if (!phoneNumber.isSuccess()) {
            handler.error(String.valueOf(phoneNumber.getCode()), phoneNumber.getMessage());
            return new BmResultData<>(BmResultStatus.SENDER_NOT_FOUND, null, phoneNumber.getMessage());
        }

        Map<String, Object> data = Collections.singletonMap(Constants.PHONE_PARAM, phoneNumber.getData());

        handler.success(data);

        return new BmResultData<>(data);
    }

    @Override
    public BmResultData<Bill> debit(Transaction transaction, P2pTokenDTO senderDto, P2pDTO recipientDto, P2pParamResult p2pParam) {
        log.info("Humo debit hold by fakePan: {}", senderDto.getToken());
        BmResultData<String> realPanResult = humoHelperService.getRealPan(senderDto.getToken());

        if (!realPanResult.isSuccess()) {
            return new BmResultData<>(realPanResult);
        }

        return humoHelperService.hold(realPanResult.getData(), transaction, p2pParam);
    }

    @Override
    public BmResultData<Bill> debitConfirm(Transaction transaction) {
        log.info("Humo debit confirm by tranID: {}", transaction.getId());
        return humoHelperService.debitConfirm(transaction);
    }

    @Override
    public HasData<Bill> reverse(Transaction transaction) {

        BmResultData<HasEposParam> senderEposParams = p2PHelperService.getSenderEposParams(transaction);

        if (!senderEposParams.isSuccess()) {
            log.error("Epos params invalid");
            return new BmResultData<>(BmResultStatus.INTERNAL_ERROR, senderEposParams.getMessage());
        }

        return humoHelperService.reverse(
                transaction,
                senderEposParams.getData().getMerchantId(),
                senderEposParams.getData().getTerminalId(),
                true
        );
    }
}
