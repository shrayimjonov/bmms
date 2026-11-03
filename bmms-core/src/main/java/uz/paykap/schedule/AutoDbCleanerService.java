package uz.paykap.schedule;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.devops.pusher.event.PusherEvent;
import uz.paykap.repository.TransactionRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
@ConditionalOnProperty(prefix = "cleaner.db", name = "active", havingValue = "true")
public class AutoDbCleanerService {

    private final TransactionRepository transactionRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${cleaner.db.pusherTags:cleaner-push,bm-push}")
    private String pusherTags;

    @Value("${cleaner.db.beforeDays:3}")
    private Integer beforeDays;

    public static final String LINE_SEPARATOR = "-----------------------------------";

    @Scheduled(cron = "${cleaner.db.cron:0 5 3 * * *}")
    public void cleanDB() {

        log.info(LINE_SEPARATOR + "\nStarted cleaning data from DB\nCondition : transaction.status=CHECK_FAILED & older 3 days\n" + LINE_SEPARATOR);

        List<TransactionStatus> deleteStatuses = Collections.singletonList(TransactionStatus.CHECK_FAIL);
        int deletedCount = transactionRepository.deleteAllByStatusInAndDateBefore(deleteStatuses, LocalDateTime.now().minusDays(beforeDays).toInstant(ZoneOffset.ofHours(0)));

        String message = "Удалено транзакций у которых проверка неуспешна и 3 дневной давности : " + deletedCount;

        log.info(message);

        eventPublisher.publishEvent(new PusherEvent(this, Collections.singletonMap("message", message), pusherTags.split(",")));

        log.info(LINE_SEPARATOR + "\nDB cleaned \n" + LINE_SEPARATOR);
    }

}
