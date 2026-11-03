package uz.paykap.recipient.p2p;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.javlon.commons.result.HasData;
import uz.javlon.svgate.service.SvGatePaymentServiceV2;
import uz.paykap.agent.tran.helper.P2pParamResolver;
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
import uz.paykap.service.p2p.dto.SenderAdditionalParam;
import uz.paykap.service.p2p.processor.UzcardParam;
import uz.paykap.service.p2p.support.AbstractP2pRecipient;
import uz.paykap.service.p2p.support.AbstractP2pSender;
import uz.paykap.service.p2p.support.BillHandler;
import uz.paykap.service.uzcard.UzcardCreditService;
import uz.paykap.service.uzcard.UzcardHelperService;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class UzcardPanRecipientService extends AbstractP2pRecipient<P2pPanDTO> {

    private final SvGatePaymentServiceV2 paymentService;
    private final UzcardHelperService uzcardHelperService;
    private final UzcardCreditService uzcardCreditService;
    private final P2pParamResolver p2pParamResolver;
    private final PcHelper pcHelper;

    @Override
    public BmResultData<String> isSupported(P2pPanDTO dto, P2pAccount p2pAccount) {

        String pan = dto.getPan();

        boolean matches = pcHelper.isValidPan(pan, P2pProcessorType.UZCARD);

        if (!matches) {
            return new BmResultData<>(BmResultStatus.RECIPIENT_NOT_FOUND, "Not valid Uzcard pan");
        }

//        BmResultData<Bank> bankResult = bankUtilService.getSupportedBank(pan);
//
//        if (!bankResult.isSuccess()) {
//            return new BmResultData<>(bankResult.getException());
//        }

        return new BmResultData<>("Valid Uzcard pan");
    }

    @Override
    public P2pProcessorType getProcessorType() {
        return P2pProcessorType.UZCARD;
    }

    @Override
    protected BmResultData<Map<String, Object>> getInfo(P2pPanDTO dto, BillHandler handler) {

        var p2pInfoDTO = paymentService.p2pInfoResult(dto.getPan());

        if (p2pInfoDTO == null || p2pInfoDTO.getData() == null) {
            return new BmResultData<>(BmResultStatus.RECIPIENT_NOT_FOUND, "Got null result from uzcard.p2pInfo");
        }

        if (!p2pInfoDTO.isSuccess()) {
            return new BmResultData<>(BmResultStatus.RECIPIENT_NOT_FOUND, p2pInfoDTO.getMessage());
        }

        if (!p2pInfoDTO.getData().isValidCard()) {
            return new BmResultData<>(BmResultStatus.RECIPIENT_NOT_FOUND, "Card status not valid");
        }

        Map<String, Object> data = Collections.singletonMap(Constants.FIO_PARAM, p2pInfoDTO.getData().getFio());

        handler.success(data);

        return new BmResultData<>(data);
    }

    @Override
    public BmResultData<Bill> credit(Transaction transaction, P2pDTO senderDto, P2pPanDTO recipientDto, P2pParamResult p2pParam) {

        return innerCredit(recipientDto, transaction, p2pParam, senderDto != null ? senderDto.getAdditionalParam() : null);
    }

    @Override
    public HasData<Bill> reverse(Transaction transaction) {
        return uzcardHelperService.reverse(transaction, false);
    }

    @Override
    public BmResultData<Bill> continueCredit(Transaction transaction, String senderBeanName, String recipientBeanName, SenderAdditionalParam senderAdditionalParam) {
        AbstractP2pSender senderBean = applicationContext.getBean(senderBeanName, AbstractP2pSender.class);
        AbstractP2pRecipient recipientBean = applicationContext.getBean(recipientBeanName, AbstractP2pRecipient.class);

        String senderCondition = transaction.getSenderAccount();
        if (senderBean.getProcessorType() == P2pProcessorType.NCI) {
            Long senderServiceId = transaction.getSenderServiceId();
            String mfo = String.valueOf(senderServiceId);
            while (mfo.length() < 5) {
                mfo = "0" + mfo;
            }
            senderCondition = mfo + senderCondition;
        }

        String recipientCondition = transaction.getRecipientAccount();
        if (recipientBean.getProcessorType() == P2pProcessorType.NCI) {
            Long recipientServiceId = transaction.getRecipientServiceId();
            String mfo = String.valueOf(recipientServiceId);
            while (mfo.length() < 5) {
                mfo = "0" + mfo;
            }
            recipientCondition = mfo + recipientCondition;
        }

        Optional<P2pParamResult> p2pParam = p2pParamResolver.getP2pParam(senderBean.getProcessorType(), recipientBean.getProcessorType(), senderCondition, recipientCondition, transaction.getPromo());
        if (!p2pParam.isPresent()) {
            return new BmResultData<>(BmResultStatus.INVALID_RECIPIENT_PARAMS);
        }

        P2pPanDTO p2pPanDTO = new P2pPanDTO();
        p2pPanDTO.setPan(transaction.getRecipientAccount());
//        p2pPanDTO.setExp();todo Javlon akadan bin source ni aniqlash kk

        return innerCredit(p2pPanDTO, transaction, p2pParam.get(), senderAdditionalParam);
    }

    private BmResultData<Bill> innerCredit(P2pPanDTO dto, Transaction transaction, P2pParamResult p2pParam, SenderAdditionalParam additionalParam) {

        BmResultData<UzcardParam> paramResult = getRecipientParam(p2pParam.getValue());

        if (!paramResult.isSuccess()) {
            return new BmResultData<>(paramResult);
        }

        // TODO - resolve sender and recipient params from new tables
        String eposMerchant = paramResult.getData().getMerchantId();
        String eposTerminal = paramResult.getData().getTerminalId();

        setEpos(transaction, eposMerchant, eposTerminal);

        log.info("#### Uzcard Credit by PAN ####: \n{}\n", dto);

        return uzcardCreditService.credit(transaction, p2pParam, paramResult.getData(), dto, additionalParam);
    }

}
