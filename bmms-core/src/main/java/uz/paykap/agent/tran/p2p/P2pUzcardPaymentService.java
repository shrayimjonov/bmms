package uz.paykap.agent.tran.p2p;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.result.BmResult;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.p2p.dto.P2pParamResult;
import uz.paykap.service.p2p.dto.SenderAdditionalParam;
import uz.paykap.service.p2p.processor.UzcardParam;
import uz.paykap.service.p2p.support.P2pHelperService;
import uz.paykap.service.uzcard.UzcardCreditService;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@Order(2)
@AllArgsConstructor
public class P2pUzcardPaymentService extends P2pPaymentService {

    private final P2pHelperService p2pHelperService;
    private final UzcardCreditService uzcardCreditService;

    @Override
    public BmResult transactP2p(Transaction transaction, P2pParamResult p2pParamResult) {

        log.debug("Requested transactP2p for {} -> {}", transaction.getSenderAccount(), transaction.getRecipientAccount());

        BmResultData<UzcardParam> recipientParamResult = p2pHelperService.getParam2(p2pParamResult.getValue(), false, UzcardParam.class);

        if (!recipientParamResult.isSuccess()) {
            log.warn("UzcardParams not parsed : {}", recipientParamResult.getMessage());
            transaction.setRecipientStatus(String.valueOf(recipientParamResult.getStatus().getCode()));
            transaction.nextRecipientStatusText(recipientParamResult.getMessage());
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            return new BmResultData<>(recipientParamResult.getStatus(), recipientParamResult.getDetails());
        }

        log.debug("Casting recipientDto from context : {}", p2pRequestScopedContext.getRecipientDto());

//        String senderCard = p2pRequestScopedContext.getSenderDto().getAccount();
//        String recipientCard = p2pRequestScopedContext.getRecipientDto().getAccount();

        p2pHelperService.setSenderEpos(transaction, recipientParamResult.getData().getMerchantId(), recipientParamResult.getData().getTerminalId());

        BmResultData<Bill> credit = uzcardCreditService.credit(transaction, p2pParamResult, recipientParamResult.getData(), p2pRequestScopedContext.getRecipientDto(), getSenderAdditionalParams());

        if (Objects.equals(credit.getStatus(), BmResultStatus.WAITING) || credit.isTimeout()) {

            transaction.fillSenderPart(credit.getData());
            transaction.fillRecipientPart(credit.getData());
            transaction.setRecipientStatus(String.valueOf(credit.getCode()));
            transaction.nextRecipientStatusText(credit.getMessage());
            transaction.setStatus(TransactionStatus.R_NEED_CONFIRM);
            transactionRepository.save(transaction);
            return credit;
        }

        if (!credit.isSuccess() || !credit.getData().isSuccess()) {
            return credit;
        }

        transaction.fillSenderPart(credit.getData());
        transaction.fillRecipientPart(credit.getData());

        transactionRepository.save(transaction.status(TransactionStatus.SUCCESS));

        log.debug("Updated transaction : {}", transaction.getId());

        return new BmResultData<>(credit.getData());
    }

    private SenderAdditionalParam getSenderAdditionalParams() {
        return p2pRequestScopedContext != null && p2pRequestScopedContext.getSenderDto() != null ? p2pRequestScopedContext.getSenderDto().getAdditionalParam() : null;
    }

    @Override
    public List<P2pProcessorType> getSupportedSender() {
        return Collections.singletonList(P2pProcessorType.UZCARD);
    }

    @Override
    public List<P2pProcessorType> getSupportedRecipient() {
        return Collections.singletonList(P2pProcessorType.UZCARD);
    }
}
