package uz.paykap.recipient.p2p;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.javlon.commons.result.HasData;
import uz.javlon.svgate.dto.sv.SvCardV2DTO;
import uz.javlon.svgate.service.SvGateCardServiceV2;
import uz.paykap.agent.tran.helper.P2pParamResolver;
import uz.paykap.config.Constants;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.gateway.CommonGatewaySender;
import uz.paykap.service.PcHelper;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.p2p.P2pAccount;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.p2p.dto.P2pDTO;
import uz.paykap.service.p2p.dto.P2pParamResult;
import uz.paykap.service.p2p.dto.P2pTokenDTO;
import uz.paykap.service.p2p.dto.SenderAdditionalParam;
import uz.paykap.service.p2p.processor.UzcardParam;
import uz.paykap.service.p2p.support.AbstractP2pRecipient;
import uz.paykap.service.p2p.support.AbstractP2pSender;
import uz.paykap.service.p2p.support.BillHandler;
import uz.paykap.service.uzcard.UzcardCreditService;
import uz.paykap.service.uzcard.UzcardHelperService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class UzcardTokenRecipientService extends AbstractP2pRecipient<P2pTokenDTO> {

    private final SvGateCardServiceV2 cardService;
    private final UzcardHelperService uzcardHelperService;
    private final UzcardCreditService uzcardCreditService;
    private final P2pParamResolver p2pParamResolver;
    private final PcHelper pcHelper;
    private final CommonGatewaySender commonGatewaySender;

    @Override
    public BmResultData<String> isSupported(P2pTokenDTO dto, P2pAccount p2pAccount) {

        String token = dto.getToken();

        boolean matches = pcHelper.isValidToken(token, P2pProcessorType.UZCARD);

        if (!matches) {
            return new BmResultData<>(BmResultStatus.INVALID_RECIPIENT_ACCOUNT, "Invalid Uzcard token");
        }

        return new BmResultData<>("Valid Uzcard token");
    }

    @Override
    public P2pProcessorType getProcessorType() {
        return P2pProcessorType.UZCARD;
    }

    @Override
    protected BmResultData<Map<String, Object>> getInfo(P2pTokenDTO dto, BillHandler handler) {
        BmResultData<SvCardV2DTO> svCard = getSvCard(dto.getToken());

        if (!svCard.isSuccess()) {
            return new BmResultData<>(svCard.getStatus(), svCard.getMessage());
        }

        Map<String, Object> data = Collections.singletonMap(Constants.FIO_PARAM, svCard.getData().getFullName());

        handler.success(data);

        return new BmResultData<>(data);
    }

    private BmResultData<SvCardV2DTO> getSvCard(String cardToken) {

        final HasData<List<SvCardV2DTO>> listHasData = commonGatewaySender.sendAndReceive(Collections.singletonList(cardToken), cardService::getCards, svCardV2DTOS -> !svCardV2DTOS.isEmpty() && svCardV2DTOS.get(0) != null);

        if (!listHasData.isSuccess()) {
            return new BmResultData<>(BmResultStatus.RECIPIENT_NOT_FOUND, listHasData.getMessage());
        }

        log.debug("Card list result : {}", listHasData.getData());

        return new BmResultData<>(listHasData.getData().get(0));
    }

    @Override
    public BmResultData<Bill> credit(Transaction transaction, P2pDTO senderDto, P2pTokenDTO recipientDto, P2pParamResult p2pParam) {

        return innerTran(recipientDto, transaction, p2pParam, senderDto != null ? senderDto.getAdditionalParam() : null);
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
            StringBuilder mfo = Optional.ofNullable(String.valueOf(senderServiceId)).map(StringBuilder::new).orElse(null);
            while (mfo.length() < 5) {
                mfo.insert(0, "0");
            }
            senderCondition = mfo + senderCondition;
        }

        String recipientCondition = transaction.getRecipientAccount();
        if (recipientBean.getProcessorType() == P2pProcessorType.NCI) {
            Long recipientServiceId = transaction.getRecipientServiceId();
            StringBuilder mfo = Optional.ofNullable(String.valueOf(recipientServiceId)).map(StringBuilder::new).orElse(null);
            while (mfo.length() < 5) {
                mfo.insert(0, "0");
            }
            recipientCondition = mfo + recipientCondition;
        }

        Optional<P2pParamResult> p2pParam = p2pParamResolver.getP2pParam(senderBean.getProcessorType(), recipientBean.getProcessorType(), senderCondition, recipientCondition, transaction.getPromo());
        if (p2pParam.isEmpty()) {
            return new BmResultData<>(BmResultStatus.INVALID_RECIPIENT_PARAMS);
        }

        P2pTokenDTO p2pTokenDTO = new P2pTokenDTO();
        p2pTokenDTO.setToken(transaction.getRecipientAccount());
//        p2pTokenDTO.setBin();todo Javlon akadan bin source ni aniqlash kk

        return innerTran(p2pTokenDTO, transaction, p2pParam.get(), senderAdditionalParam);
    }

    private BmResultData<Bill> innerTran(P2pTokenDTO dto, Transaction transaction, P2pParamResult p2pParam, SenderAdditionalParam senderAdditionalParam) {

        BmResultData<UzcardParam> paramResult = getRecipientParam(p2pParam.getValue());

        if (!paramResult.isSuccess()) {
            return new BmResultData<>(paramResult);
        }

        // TODO - resolve sender and recipient params from new tables
        String eposMerchant = paramResult.getData().getMerchantId();
        String eposTerminal = paramResult.getData().getTerminalId();

        setEpos(transaction, eposMerchant, eposTerminal);

        log.info("#### Uzcard Credit by Token ####: \n{}\n", dto);

        String cardToken = dto.getToken();

//        return uzcardHelperService.creditSvGateV2(transaction, p2pParam, paramResult.getData(), cardToken);
        return uzcardCreditService.credit(transaction, p2pParam, paramResult.getData(), dto, senderAdditionalParam);
    }

}
