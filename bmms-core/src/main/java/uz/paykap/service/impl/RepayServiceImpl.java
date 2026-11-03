package uz.paykap.service.impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.domain.Transaction;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.service.OverpayService;
import uz.paykap.service.RepayService;
import uz.paykap.service.dto.TransactionDTO;

@Slf4j
@Service
public class RepayServiceImpl implements RepayService {

    public static final int DAY_COUNT = 2;

    @Override
    @Scheduled(fixedDelay = 900000, initialDelay = 60000)
    public void startRepay() {
        if (!applicationProperties.getRepay().isActive()) {
            log.info("Repay service is off ");
            return;
        }

        log.info(" Start repay error transactions !!!");

        List<Transaction> errorTransactions = transactionRepository
            .findAllBySenderSuccessAndStatusAndRequestLoginAndRecipientDateBetween(
                false,
                TransactionStatus.FAILED,
                applicationProperties.getRepay().getTerminalLogin(),
                getFromDate(applicationProperties.getRepay().getDaysBefore()),
                Instant.now()
            )
            .orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));

        log.debug("Transaction needs to repay : {}", errorTransactions.toString());

        for (Transaction transaction : errorTransactions) {
            try {
                TransactionDTO simpleBillDTO = overpayService.toPayGateway(transaction, "internal");
                log.debug("Transaction" + transaction + " repay ended with result " + simpleBillDTO.isRecipientSuccess());
            } catch (Exception e) {
                log.warn("Transaction " + transaction + " not repayed  because" + e.getLocalizedMessage());
            }
        }
    }

    private Instant getFromDate(Integer dayCount) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'");
        LocalDateTime then = LocalDateTime.now().minusDays(dayCount);
        return Instant.parse(then.format(format) + "00:00:00.000Z");
    }

    private final TransactionRepository transactionRepository;
    private final OverpayService overpayService;
    private final ApplicationProperties applicationProperties;

    public RepayServiceImpl(
        TransactionRepository transactionRepository,
        OverpayService overpayService,
        ApplicationProperties applicationProperties
    ) {
        this.transactionRepository = transactionRepository;
        this.overpayService = overpayService;
        this.applicationProperties = applicationProperties;
    }
}
