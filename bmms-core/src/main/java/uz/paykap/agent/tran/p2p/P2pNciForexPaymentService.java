package uz.paykap.agent.tran.p2p;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.service.dto.p2p.P2pRequestScopedContext;
import uz.paykap.service.dto.result.BmResult;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.p2p.dto.P2pForexDTO;
import uz.paykap.service.p2p.dto.P2pParamResult;
import uz.paykap.service.p2p.processor.NciForexParam;
import uz.paykap.service.p2p.support.ForexHelperService;
import uz.paykap.service.p2p.support.P2pHelperService;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@Order(4)
@AllArgsConstructor
public class P2pNciForexPaymentService extends P2pPaymentService {

    private final ForexHelperService abstractAbsService;
    private final P2pHelperService p2pHelperService;
    private final P2pRequestScopedContext p2pRequestScopedContext;

    @Override
    public BmResult transactP2p(Transaction transaction, P2pParamResult p2pParamResult) {

        BmResultData<NciForexParam> paramResult = p2pHelperService.getRecipientParams(p2pParamResult.getValue(), NciForexParam.class);

        if (!paramResult.isSuccess()) {
            return new BmResultData<>(paramResult);
        }

        log.debug("Requested transactP2p for {} -> {}", transaction.getSenderAccount(), transaction.getRecipientAccount());

//        P2pAccountDTO senderDto = (P2pAccountDTO) p2pRequestScopedContext.getSenderDto();
        P2pForexDTO recipientDto = (P2pForexDTO) p2pRequestScopedContext.getRecipientDto();

//        final BmResultData<Bill> transactionResult = abstractAbsService.doForexCreditTransaction(transaction, p2pRequestScopedContext.getSenderDto(), recipientDto, p2pParamResult);
        final BmResultData<Bill> transactionResult = abstractAbsService.doDirectForex(transaction, paramResult.getData(), recipientDto, false);

        if (!transactionResult.isSuccess() || !transactionResult.getData().isSuccess()) {
            return transactionResult;
        }

        transaction.fillSenderPart(transactionResult.getData());
        transaction.fillRecipientPart(transactionResult.getData());

        transactionRepository.save(transaction.status(TransactionStatus.SUCCESS));

        log.debug("Updated transaction : {}", transaction.getId());

        return new BmResultData<>(transactionResult.getData());
    }

    @Override
    public List<P2pProcessorType> getSupportedSender() {
        return Collections.singletonList(P2pProcessorType.NCI);
    }

    @Override
    public List<P2pProcessorType> getSupportedRecipient() {
        return Collections.singletonList(P2pProcessorType.FOREX);
    }
}
