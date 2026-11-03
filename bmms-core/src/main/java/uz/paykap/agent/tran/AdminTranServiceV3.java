package uz.paykap.agent.tran;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.javlon.commons.base.Result;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.domain.Transaction;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.service.P2pCheckRequestDecoratorService;
import uz.paykap.service.dto.p2p.P2pCancelRequest;
import uz.paykap.service.p2p.dto.response.P2pResponse;

import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class AdminTranServiceV3 {

    private final TransactionRepository transactionRepository;
    private final P2pCheckRequestDecoratorService p2pCheckRequestDecoratorService;
    private final CommonTranServiceV3 commonTranServiceV3;

    public Result cancel(Long tranId) {
        log.info("Request to cancel: {}", tranId);

        Optional<Transaction> optionalTransaction = transactionRepository.findTop1ById(tranId);

        if (!optionalTransaction.isPresent()) {
            log.error("Transaction with id: {} not found", tranId);
            throw new CodedException(ProblemEnum.OBJECT_NOT_FOUND, "transaction");
        }

        Transaction transaction = optionalTransaction.get();
        log.debug("Transaction: {}", transaction);

        switch (transaction.getStatus()) {
            case DEBITED:
            case CANCEL_FAILED:
            case FAILED:
            case PAYING:
            case SUCCESS: {
                if (transaction.getPayType() == PayType.P2P) {
                    P2pResponse p2pResponse = p2pCheckRequestDecoratorService.p2pCancel(new P2pCancelRequest(transaction.getRequestTrId()));
                    return new Result(p2pResponse.isSuccess());
                }

                Transaction transactionCancel = commonTranServiceV3.cancel(tranId);
                if(transactionCancel.getStatus()== TransactionStatus.CANCELLED || transactionCancel.getStatus() == TransactionStatus.REVERSED){
                    return Result.SUCCESS;
                }

                return Result.FAILED;
            }
            default:{
                log.error("Transaction status invalid");
                return Result.FAILED;
            }

        }
    }
}
