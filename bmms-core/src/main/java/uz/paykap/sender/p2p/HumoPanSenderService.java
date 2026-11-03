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
import uz.paykap.service.p2p.dto.P2pPanDTO;
import uz.paykap.service.p2p.dto.P2pParamResult;
import uz.paykap.service.p2p.support.AbstractP2pSender;
import uz.paykap.service.p2p.support.BillHandler;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class HumoPanSenderService extends AbstractP2pSender<P2pPanDTO> {

    private final HumoMiddlewareHelperService humoMiddlewareHelperService;
    private final HumoP2pHelperService humoHelperService;
    private final PcHelper pcHelper;

    @Override
    public BmResultData<?> isSupported(P2pPanDTO dto, P2pAccount p2pAccount) {
        String pan = dto.getPan();

        boolean matches = pcHelper.isValidPan(pan, P2pProcessorType.HUMO);

        if (!matches) {
            return new BmResultData<>(BmResultStatus.INVALID_SENDER_ACCOUNT, "Invalid humo pan");
        }

        return new BmResultData<>("OK, valid humo pan");
    }

    @Override
    public P2pProcessorType getProcessorType() {
        return P2pProcessorType.HUMO;
    }

    @Override
    protected BmResultData<Map<String, Object>> getInfo(P2pPanDTO dto, BillHandler handler) {
        String realPan = dto.getPan();

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
    public BmResultData<Bill> debit(Transaction transaction, P2pPanDTO senderDto, P2pDTO recipientDto, P2pParamResult p2pParam) {
        log.info("Humo debit hold by PAN: {}", senderDto.getPan());
        return humoHelperService.hold(senderDto.getPan(), transaction, p2pParam);
    }

    @Override
    public BmResultData<Bill> debitConfirm(Transaction transaction) {
        log.info("Humo debit confirm by PAN: {}", transaction.getId());
        return humoHelperService.debitConfirm(transaction);
    }

    @Override
    public HasData<Bill> reverse(Transaction transaction) {

        var senderEposParams = p2PHelperService.getSenderEposParams(transaction);

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
