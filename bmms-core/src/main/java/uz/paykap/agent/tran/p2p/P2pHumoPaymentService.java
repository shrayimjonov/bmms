package uz.paykap.agent.tran.p2p;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.devops.humoMiddleware.dto.response.RetrieveCardV3ResponseDTO;
import uz.javlon.commons.result.CommonResultData;
import uz.javlon.commons.result.HasData;
import uz.javlon.commons.result.HasResult;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.gateway.CommonGatewaySender;
import uz.paykap.recipient.p2p.P2pReverseService;
import uz.paykap.service.HumoMiddlewareHelperService;
import uz.paykap.service.PcHelper;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.result.BmResult;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.humo.HumoP2pHelperService;
import uz.paykap.service.p2p.dto.P2pPanDTO;
import uz.paykap.service.p2p.dto.P2pParamResult;
import uz.paykap.service.p2p.processor.HumoParam;
import uz.paykap.service.p2p.support.P2pHelperService;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@Order(3)
@AllArgsConstructor
public class P2pHumoPaymentService extends P2pPaymentService implements P2pReverseService {

    private final P2pHelperService p2pHelperService;
    private final HumoP2pHelperService humoP2pHelperService;
    private final HumoMiddlewareHelperService humoMiddlewareHelperService;
    private final CommonGatewaySender commonGatewaySender;
    private final PcHelper pcHelper;

    @Override
    public BmResult transactP2p(Transaction transaction, P2pParamResult p2pParamResult) {

        transaction.setSenderName(this.getClass().getSimpleName());
        transaction.setRecipientName(this.getClass().getSimpleName());
        transactionRepository.save(transaction);

        log.debug("Requested transactP2p for {} -> {}", transaction.getSenderAccount(), transaction.getRecipientAccount());

        BmResultData<HumoParam> recipientParamResult = p2pHelperService.getParam2(p2pParamResult.getValue(), false, HumoParam.class);

        if (!recipientParamResult.isSuccess()) {
            log.warn("HumoParams not parsed : {}", recipientParamResult.getMessage());
            return new BmResultData<>(recipientParamResult.getStatus(), recipientParamResult.getMessage());
        }

        log.debug("Casting recipientDto from context : {}", p2pRequestScopedContext.getRecipientDto());

        String senderCard = p2pRequestScopedContext.getSenderDto().getAccount();
        String recipientCard = p2pRequestScopedContext.getRecipientDto().getAccount();

        p2pHelperService.setSenderEpos(transaction, recipientParamResult.getData().getMerchantId(), recipientParamResult.getData().getTerminalId());

        BmResultData<String> senderExpiry = resolveExpiry(senderCard);

        if (!senderExpiry.isSuccess() || senderExpiry.getData() == null || senderExpiry.getData().isEmpty()) {
            return senderExpiry;
        }

        if (pcHelper.isValidToken(senderCard, P2pProcessorType.HUMO)) {
            final HasData<String> realPan = commonGatewaySender.sendAndReceive(senderCard, humoP2pHelperService::getRealPan, s -> s != null && !s.isEmpty());

            if (!realPan.isSuccess()) {
                return new BmResultData<>(BmResultStatus.PC_ERROR, null, realPan.getMessage());
            }

            senderCard = realPan.getData();
        }

        // TODO - fix
        BmResultData<Bill> p2pResultData = humoP2pHelperService.p2p(transaction, senderCard, senderExpiry.getData(), recipientCard, transaction.getSenderAmount(), recipientParamResult.getData().getSwitchingId(), recipientParamResult.getData().getLanguage());

        if (Objects.equals(p2pResultData.getStatus(), BmResultStatus.WAITING) || p2pResultData.isTimeout()) {
            transaction.fillSenderPart(p2pResultData.getData());
            transaction.fillRecipientPart(p2pResultData.getData());
            transaction.setRecipientStatus(String.valueOf(p2pResultData.getCode()));
            transaction.nextRecipientStatusText(p2pResultData.getMessage());
            transaction.setStatus(TransactionStatus.R_NEED_CONFIRM);
            transactionRepository.save(transaction);
            return p2pResultData;
        }

        if (!p2pResultData.isSuccess() || !p2pResultData.getData().isSuccess()) {
            return p2pResultData;
        }

        final BmResultData<Bill> p2pConfirmResult = humoP2pHelperService.p2pConfirm(transaction);

        if (!p2pConfirmResult.isSuccess() || !p2pConfirmResult.getData().isSuccess()) {
            return p2pConfirmResult;
        }

        transaction.fillSenderPart(p2pResultData.getData());
        transaction.fillRecipientPart(p2pResultData.getData());

        transactionRepository.save(transaction.status(TransactionStatus.SUCCESS));

        log.debug("Updated transaction : {}", transaction.getId());

        return new BmResultData<>(p2pResultData.getData());
    }

    private BmResultData<String> resolveExpiry(String pan) {

        log.debug("Resolving expiry from senderDTO : {}", p2pRequestScopedContext.getSenderDto());

        final HasData<String> humoPan = humoP2pHelperService.getHumoPan(pan);

        if (!humoPan.isSuccess() || humoPan.getData() == null || humoPan.getData().isEmpty()) {
            return new BmResultData<>(BmResultStatus.SENDER_PC_ERROR, humoPan.getMessage());
        }

        if (p2pRequestScopedContext.getSenderDto() instanceof P2pPanDTO) {
            String senderExpiry = ((P2pPanDTO) p2pRequestScopedContext.getSenderDto()).getExp();

            if (senderExpiry != null && !senderExpiry.isEmpty()) {
                return new BmResultData<>(senderExpiry);
            }
        }

        log.debug("Resolving expiry from retrieveCardV3");

        final HasData<RetrieveCardV3ResponseDTO> cardV3ResultData = commonGatewaySender.sendAndReceive(humoPan.getData(), humoMiddlewareHelperService::retrieveCardV3, result -> result.getCard() != null && result.getCard().getExpiry() != null);

        if (!cardV3ResultData.isSuccess()) {
            return new BmResultData<>(BmResultStatus.PC_ERROR, null, cardV3ResultData.getMessage());
        }

        return new BmResultData<>(cardV3ResultData.getData().getCard().getExpiry());
    }

    @Override
    public List<P2pProcessorType> getSupportedSender() {
        return Collections.singletonList(P2pProcessorType.HUMO);
    }

    @Override
    public List<P2pProcessorType> getSupportedRecipient() {
        return Collections.singletonList(P2pProcessorType.HUMO);
    }

    @Override
    public HasData<Bill> reverse(Transaction transaction) {

        if (transaction.getStatus() == TransactionStatus.RECIPIENT_CANCELLED) {
            return new CommonResultData<>(HasResult.SUCCESS, new Bill().success(true), "skipped bcz already cancelled");
        }

        return humoP2pHelperService.reverse(transaction, transaction.getEposMerchant(), transaction.getEposTerminal(), false);
    }
}
