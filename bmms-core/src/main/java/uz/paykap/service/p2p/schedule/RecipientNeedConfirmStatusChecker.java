package uz.paykap.service.p2p.schedule;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.repository.BillRepository;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.p2p.dto.response.ConfirmResult;
import uz.paykap.service.p2p.support.AbstractP2pRecipient;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
@ConditionalOnProperty(prefix = "confirm", name = "enabled", havingValue = "true")
public class RecipientNeedConfirmStatusChecker {

    private final TransactionRepository transactionRepository;
    private final BillRepository billRepository;
    private final ApplicationContext applicationContext;

    @Scheduled(cron = "${confirm.cron:0 */5 * * * *}")
    public void checkStatusAndConfirmRecipient() {

        log.info("Starting confirming transactions with status {}", TransactionStatus.R_NEED_CONFIRM);

        List<Transaction> needConfirmTrans = transactionRepository.findByStatusAndDateAfterOrderByIdDesc(TransactionStatus.R_NEED_CONFIRM, Instant.now().minus(3, ChronoUnit.DAYS), PageRequest.of(0, 10));

        log.info("Found transaction to confirm : {}", needConfirmTrans.size());

        for (Transaction transaction : needConfirmTrans) {

            AbstractP2pRecipient<?> p2pRecipient;
            try {
                p2pRecipient = applicationContext.getBean(StringUtils.uncapitalize(transaction.getRecipientName()), AbstractP2pRecipient.class);
            } catch (Exception e) {
                log.warn(e.getLocalizedMessage());
                continue;
            }

            // TODO - fix, p2p_credit -> confirm
            Optional<Bill> creditBill = billRepository.findTop1ByTransactionIdAndTypeAndSuccessTrue(transaction.getId(), RequestType.P2P_CREDIT);

            if (!creditBill.isPresent()) {
                log.warn("Bill {} not found", transaction.getRecipientBillId());
                continue;
            }

            BmResultData<ConfirmResult> resultData = p2pRecipient.confirm(transaction, creditBill.get());

            if (!resultData.isSuccess()) {
                log.warn("Confirm result : {}", resultData.getMessage());
                continue;
            }

            switch (resultData.getData()) {
                case ACCEPTED:
                    log.info("Transaction {} accepted, mark as SUCCESS", transaction.getId());
                    transaction.setStatus(TransactionStatus.SUCCESS);
                    transactionRepository.save(transaction);
                    break;
                case REJECTED:
                    log.warn("Transaction {} rejected, need cancel sender", transaction.getId());
                    transaction.setStatus(TransactionStatus.S_CANCEL_FAILED);
                    transactionRepository.save(transaction);
                    break;
                case UNKNOWN:
                case FAILED:
                case IN_PROGRESS:
                    log.info("Transaction {} still in progress", transaction.getId());
                    break;
            }

        }
    }

}
