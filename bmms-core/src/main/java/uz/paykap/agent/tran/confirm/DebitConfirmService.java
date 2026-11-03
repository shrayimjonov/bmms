package uz.paykap.agent.tran.confirm;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.dto.request.PayRequest;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.javlon.commons.base.ResultData;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.javlon.commons.result.HasData;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.repository.BillRepository;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.security.SecurityUtils;
import uz.paykap.sender.SenderResolverV3;
import uz.paykap.service.mapper.BillMapper;

@Slf4j
@Service
@AllArgsConstructor
public class DebitConfirmService {

    public static final String HOLD_SUCCESS = "HOLD_SUCCESS";

    public SimpleBillResponse checkParamsAndConfirm(PayRequest payDto) {
        if (payDto == null || payDto.getBillId() == null) {
            log.warn("check params error: billId not found");
            throw new CodedException(ProblemEnum.INVALID_PARAMS, "billId not found");
        }
        return findTransactionAndConfirm(payDto);
    }

    private SimpleBillResponse findTransactionAndConfirm(PayRequest payDto) {
        Transaction holdTransaction = getLoginAndFindTransaction(payDto.getBillId());
        log.debug("find transaction for confirm: id - {}, status - {}, senderStatus - {}", holdTransaction.getId(), holdTransaction.getStatus(), holdTransaction.getSenderStatus());
        return confirmAndReturn(payDto, holdTransaction);
    }

    private Transaction getLoginAndFindTransaction(Long billId) {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));
        log.debug("username is: {}", login);
        return transactionRepository.findTop1ByIdAndAndSenderStatusAndRequestLogin(billId, HOLD_SUCCESS, login).orElseThrow(() -> new CodedException(ProblemEnum.NOT_FOUND, "hold transaction not found"));
    }

    private SimpleBillResponse confirmAndReturn(PayRequest payDto, Transaction holdTransaction) {
        HasData<Transaction> confirmTransaction = senderResolver.debitConfirm(payDto, holdTransaction, holdTransaction.getRequestLogin());
        log.debug("confirm result: {}", confirmTransaction);
        return verifyAndReturnResult(confirmTransaction);
    }

    private SimpleBillResponse verifyAndReturnResult(HasData<Transaction> confirmTransaction) {
        verifyConfirmBillId(confirmTransaction);
        log.debug("confirm result verified!!!");
        return findBillAndReturnSimpleBill(confirmTransaction.getData().getSenderBillId());
    }

    private void verifyConfirmBillId(HasData<Transaction> confirmTransaction) {
        if (confirmTransaction == null || !confirmTransaction.isSuccess()) {
            throw new CodedException(ProblemEnum.DEBIT_ERROR, "debit confirm result error");
        }
        if (confirmTransaction.getData() == null || confirmTransaction.getData().getSenderBillId() == null) {
            throw new CodedException(ProblemEnum.BILL_NOT_FOUND, "confirm transaction or billId not found");
        }
    }

    private SimpleBillResponse findBillAndReturnSimpleBill(Long confirmBillId) {
        Bill confirmBill = billRepository.findById(confirmBillId).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND, "confirm bill not found"));
        log.debug("find and return confirm bill: {}", confirmBill);
        return billMapper.toSimple(confirmBill);
    }

    private final BillMapper billMapper;
    private final BillRepository billRepository;
    private final SenderResolverV3 senderResolver;
    private final TransactionRepository transactionRepository;
}
