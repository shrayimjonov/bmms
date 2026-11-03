package uz.paykap.recipient.p2p;

import java.util.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.devops.humo.service.dto.SenderAdditionalParam;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.result.HasData;
import uz.paykap.agent.tran.helper.P2pParamResolver;
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
import uz.paykap.service.p2p.processor.HumoParam;
import uz.paykap.service.p2p.support.AbstractP2pRecipient;
import uz.paykap.service.p2p.support.AbstractP2pSender;
import uz.paykap.service.p2p.support.BillHandler;

@Slf4j
@Service
@AllArgsConstructor
public class HumoTokenRecipientService extends AbstractP2pRecipient<P2pTokenDTO> {

    private final HumoMiddlewareHelperService humoMiddlewareHelperService;
    private final HumoP2pHelperService humoHelperService;
    private final P2pParamResolver p2pParamResolver;
    private final PcHelper pcHelper;

    @Override
    public BmResultData<String> isSupported(P2pTokenDTO dto, P2pAccount p2pAccount) {
        String token = dto.getToken();

        boolean matches = pcHelper.isValidToken(token, P2pProcessorType.HUMO);

        if (!matches) {
            return new BmResultData<>(BmResultStatus.INVALID_SENDER_ACCOUNT, "Invalid HUMO token");
        }

        return new BmResultData<>("Valid HUMO token");
    }

    @Override
    public P2pProcessorType getProcessorType() {
        return P2pProcessorType.HUMO;
    }

    @Override
    protected BmResultData<Map<String, Object>> getInfo(P2pTokenDTO dto, BillHandler handler) {
        try {
            Map<String, Object> requestJson = new HashMap<>();
            requestJson.put("token", dto.getToken());
            handler.setRequest(requestJson);

            //            String pan = humoHelperService.getRealPan(dto.getToken()).getData();

            final BmResultData<String> realPan = humoHelperService.getRealPan(dto.getToken());

            if (realPan == null) {
                return new BmResultData<>(BmResultStatus.PC_ERROR, "Humo getRealPan failed, bmResult is null");
            }

            if (
                !realPan.isSuccess() || Objects.isNull(realPan.getData()) || !pcHelper.isValidPan(realPan.getData(), P2pProcessorType.HUMO)
            ) {
                log.warn("Error when getting realPan from Humo : " + realPan.getMessage());
                return new BmResultData<>(realPan);
            }

            var middlewareSimpleResultData = humoMiddlewareHelperService.retrieveCardV3(realPan.getData()).getData();

            Map<String, Object> data = Collections.singletonMap(
                Constants.FIO_PARAM,
                middlewareSimpleResultData.getCard().getNameOnCard()
            );

            handler.success(data);

            return new BmResultData<>(data);
        } catch (CodedException e) {
            handler.error(Collections.singletonMap("error", e.getLocalizedMessage()));
            return new BmResultData<>(BmResultStatus.RECIPIENT_PC_ERROR, e.getLocalizedMessage());
        }
    }

    @Override
    public BmResultData<Bill> credit(Transaction transaction, P2pDTO senderDto, P2pTokenDTO recipientDto, P2pParamResult p2pParam) {
        return innerTran(recipientDto, transaction, p2pParam, senderDto != null ? senderDto.getAdditionalParam() : null);
    }

    @Override
    public HasData<Bill> reverse(Transaction transaction) {
        BmResultData<HasEposParam> senderEposParams = p2PHelperService.getRecipientEposParams(transaction);

        if (!senderEposParams.isSuccess()) {
            log.error("Epos params invalid");
            return new BmResultData<>(BmResultStatus.INTERNAL_ERROR, senderEposParams.getMessage());
        }

        return humoHelperService.reverse(
            transaction,
            senderEposParams.getData().getMerchantId(),
            senderEposParams.getData().getTerminalId(),
            false
        );
    }

    @Override
    public BmResultData<Bill> continueCredit(
        Transaction transaction,
        String senderBeanName,
        String recipientBeanName,
        uz.paykap.service.p2p.dto.SenderAdditionalParam senderAdditionalParam
    ) {
        AbstractP2pSender<?> senderBean = applicationContext.getBean(senderBeanName, AbstractP2pSender.class);
        AbstractP2pRecipient<?> recipientBean = applicationContext.getBean(recipientBeanName, AbstractP2pRecipient.class);

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

        Optional<P2pParamResult> p2pParam = p2pParamResolver.getP2pParam(
            senderBean.getProcessorType(),
            recipientBean.getProcessorType(),
            senderCondition,
            recipientCondition,
            transaction.getPromo()
        );
        if (!p2pParam.isPresent()) {
            return new BmResultData<>(BmResultStatus.INVALID_RECIPIENT_PARAMS);
        }

        P2pTokenDTO p2pPanDTO = new P2pTokenDTO();
        p2pPanDTO.setToken(transaction.getRecipientAccount());
        //        p2pPanDTO.setBin();todo Javlon akadan bin source ni aniqlash kk
        //        if (p2pRequestScopedContext != null && p2pRequestScopedContext.getSenderDto() != null) {
        //            return innerTran(p2pPanDTO, transaction, p2pParam.get(), p2pRequestScopedContext.getSenderDto().getAdditionalParam());
        //        }
        return innerTran(p2pPanDTO, transaction, p2pParam.get(), senderAdditionalParam);
    }

    private BmResultData<Bill> innerTran(
        P2pTokenDTO dto,
        Transaction transaction,
        P2pParamResult p2pParam,
        uz.paykap.service.p2p.dto.SenderAdditionalParam senderAdditionalParam
    ) {
        BmResultData<HumoParam> paramResult = getRecipientParam(p2pParam.getValue());

        if (!paramResult.isSuccess()) {
            return new BmResultData<>(paramResult);
        }

        String eposMerchant = paramResult.getData().getMerchantId();
        String eposTerminal = paramResult.getData().getTerminalId();

        setEpos(transaction, eposMerchant, eposTerminal);

        String fakePan = dto.getToken();

        SenderAdditionalParam humoAdditionalParam = null;
        if (senderAdditionalParam != null) {
            humoAdditionalParam = new SenderAdditionalParam();
            humoAdditionalParam.setBirthDate(senderAdditionalParam.getBirthDate());
            humoAdditionalParam.setCitizen(senderAdditionalParam.getCitizen());
            humoAdditionalParam.setFirstName(senderAdditionalParam.getFirstName());
            humoAdditionalParam.setLastName(senderAdditionalParam.getLastName());
            humoAdditionalParam.setMiddleName(senderAdditionalParam.getMiddleName());
            humoAdditionalParam.setDocDate(senderAdditionalParam.getDocDate());
            humoAdditionalParam.setDocNumber(senderAdditionalParam.getDocNumber());
            humoAdditionalParam.setDocType(senderAdditionalParam.getDocType());
            humoAdditionalParam.setPinFl(senderAdditionalParam.getPinFl());
        }
        return humoHelperService.credit(
            transaction.getId(),
            fakePan,
            transaction.getRecipientAmount(),
            eposMerchant,
            eposTerminal,
            humoAdditionalParam
        );
    }
}
