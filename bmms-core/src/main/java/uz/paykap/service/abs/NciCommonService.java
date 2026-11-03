package uz.paykap.service.abs;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.devops.bm.ncitrans.dto.enums.NciTransResponseJsonKeys;
import uz.devops.bmcore.enumeration.RequestType;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.repository.BillRepository;
import uz.paykap.security.SecurityUtils;
import uz.paykap.service.abs.response.AbsTransactionResult;
import uz.paykap.service.abs.response.NciUpdateTranGroupType;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.result.BmResultData;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class NciCommonService {

    private final BillRepository billRepository;
    private final AbstractAbsService absService;

    public BmResultData<Bill> reverse(Transaction transaction, Boolean isSender) {
        String transactionId = "";
        if (isSender && transaction.getSenderTrId() != null && !transaction.getSenderTrId().isEmpty()) {
            transactionId = transaction.getSenderTrId();
        } else if (!isSender && transaction.getRecipientTrId() != null && !transaction.getRecipientTrId().isEmpty()) {
            transactionId = transaction.getRecipientTrId();
        }
        if (!transactionId.isEmpty()) {
            return reverse(transaction, isSender, NciUpdateTranGroupType.GENERAL);
        }
        Long billId;
        if (isSender) {
            billId = transaction.getSenderBillId();
        } else {
            billId = transaction.getRecipientBillId();
        }
        var bill = billRepository.findById(billId).orElse(null);
        if (bill == null) {
            log.info("Bill not found by id : {}", billId);
            return new BmResultData<>(BmResultStatus.BILL_NOT_FOUND, "Bill not found by id : " + billId);
        }
        var data = absService.getPcTransactionIdByExternalId(bill.getOutExtId());
        if (!data.isSuccess()) {
            return new BmResultData<>(BmResultStatus.PC_ERROR, data.getDetails());
        }
        if (isSender) {
            transaction.setSenderTrId(data.getData());
        } else {
            transaction.setRecipientTrId(data.getData());
        }
        log.info("Abs transaction reverse process request : {}", transaction);
        return reverse(transaction, isSender, NciUpdateTranGroupType.GENERAL);
    }

    public BmResultData<Bill> reverse(Transaction transaction, Boolean isSender, NciUpdateTranGroupType groupType) {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

        Bill bill = new Bill()
                .recipientShortName("nci-reverse")
                .account((isSender) ? transaction.getSenderAccount() : transaction.getRecipientAccount())
                .amount((isSender) ? transaction.getSenderAmount() : transaction.getRecipientAmount())
                .amountRatio(transaction.getAmountRatio())
                .currency((isSender) ? transaction.getSenderCcy() : transaction.getRecipientCcy())
                .type(RequestType.CANCEL)
                .login(login)
                .requestTime(Instant.now())
                .responseTime(Instant.now())
                .transactionId(transaction.getId())
                .success(false);

        String requestId = UUID.randomUUID().toString();
        bill.setOutExtId(requestId);
        billRepository.save(bill);
        log.debug("Initial save bill: {}", bill);
        String transactionId = "";
        if (isSender) {
            transactionId = transaction.getSenderTrId();
        } else {
            transactionId = transaction.getRecipientTrId();
        }

        BmResultData<AbsTransactionResult> resultData = absService.sendCancelRequest(transactionId);

        if (!resultData.isSuccess()) {
            bill
                    .responseJson(Collections.singletonMap("error", resultData.getStatus().getMessage()))
                    .responseTime(Instant.now())
                    .responseText(resultData.getStatus().getMessage())
                    .responseCode(resultData.getData() != null ? resultData.getData().getErrorCode() : "unknown")
                    .success(false)
            ;

            billRepository.save(bill);
            log.error("nci.cancelTransaction throw exception : {}", resultData.getMessage());
            return new BmResultData<>(resultData.getStatus(), bill, resultData.getStatus().getMessage());
        }

        bill
                .responseJson(Collections.singletonMap(NciTransResponseJsonKeys.TRANS_ID.name(), String.valueOf(resultData.getData().getTransId())))
                .responseTime(Instant.now())
                .extId(resultData.getData().getTransId())
                .responseText(resultData.getData().getErrorNote())
                .success(true)
        ;

        bill = billRepository.save(bill);

        log.debug("Final save bill: {}", bill);

        return new BmResultData<>(bill);
    }

}
