package uz.paykap.agent.tran.p2p;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.service.abs.AbstractAbsService;
import uz.paykap.service.abs.request.AbsTransactionRequest;
import uz.paykap.service.dto.result.BmResult;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.p2p.dto.P2pAccountDTO;
import uz.paykap.service.p2p.dto.P2pParamResult;
import uz.paykap.service.p2p.processor.NciParam;
import uz.paykap.service.p2p.support.P2pHelperService;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@Order(1)
@AllArgsConstructor
public class P2pNciPaymentService extends P2pPaymentService {

    private final P2pHelperService p2PHelperService;
    private final AbstractAbsService abstractAbsService;

    @Override
    public BmResult transactP2p(Transaction transaction, P2pParamResult p2pParamResult) {

        log.debug("Requested transactP2p for {} -> {}", transaction.getSenderAccount(), transaction.getRecipientAccount());

        BmResultData<NciParam> paramResult = p2PHelperService.getParam2(p2pParamResult.getValue(), false, NciParam.class);

        if (!paramResult.isSuccess()) {
            log.warn("NciParams not parsed : {}", paramResult.getMessage());
            return new BmResultData<>(paramResult.getStatus());
        }

        log.debug("Casting recipientDto from context : {}", p2pRequestScopedContext.getRecipientDto());

        P2pAccountDTO senderDto = (P2pAccountDTO) p2pRequestScopedContext.getSenderDto();
        P2pAccountDTO recipientDto = (P2pAccountDTO) p2pRequestScopedContext.getRecipientDto();

        /*
         * User want to send from owned X account to any PC
         * We are debiting from X account (accCl) and crediting to transit account (accCo),
         * so we store bankCo as merchantId and accCo as terminalId
         * */
        p2PHelperService.setSenderEpos(transaction, recipientDto.getMfo(), recipientDto.getAccount());

        BmResultData<AbsTransactionRequest> transactionDtoResult = abstractAbsService.getDirectTran(senderDto, recipientDto, paramResult.getData(), String.valueOf(transaction.getId()), transaction.getSenderAmount(), "");

        if (!transactionDtoResult.isSuccess()) {
            return new BmResultData<>(transactionDtoResult);
        }

        AbsTransactionRequest transactionDto = transactionDtoResult.getData();

        BmResultData<Bill> transactionResult = abstractAbsService.transfer(transaction, transactionDto, true);

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
        return Collections.singletonList(P2pProcessorType.NCI);
    }
}
