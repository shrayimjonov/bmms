package uz.paykap.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.domain.Deposit;
import uz.paykap.event.dto.SendToMailEvent;
import uz.paykap.repository.DepositRepository;

import java.time.Instant;
import java.util.List;

/**
 * Service Implementation for managing Deposit.
 */
@Slf4j
@AllArgsConstructor
@Service
@Transactional
public class DepositCheckService {

    @Scheduled(fixedDelay = 900000, initialDelay = 60000)
    public void checkDeposit() {
        log.debug("Check deposit time !!!");

        List<Deposit> informLimits = depositRepository.findAllLimitInformReached();
        List<Deposit> emptyLimit = depositRepository.findAllLimitDisableReached();

        Instant date = Instant.now();
        for (Deposit informLimit : informLimits) {
            log.debug("This deposit limit inform reached :{}", informLimit.toString());

            for (String mail : applicationProperties.getDefaultToMails()) {
                applicationEventPublisher.publishEvent(new SendToMailEvent(this, mail, "Баланс пуст !!!", fillInformContent(informLimit, date), false, false));
            }
//            applicationEventPublisher.publishEvent(new SendToMailEvent(this, applicationProperties.getDefaultEmailToKapital(), "Превышен лимит информирования баланса !!!", fillInformContent(informLimit, date), false, false));

//            mailService.sendEmail("user@example.com", "Превышен лимит информирования баланса !!!", fillInformContent(informLimit, date));
        }
        for (Deposit deposit : emptyLimit) {
            log.debug("This deposit limit disabled :{}", deposit.toString());
            for (String mail : applicationProperties.getDefaultToMails()) {
                applicationEventPublisher.publishEvent(new SendToMailEvent(this, mail, "Баланс пуст !!!", fillDisableContent(deposit, date), false, false));
            }
//            applicationEventPublisher.publishEvent(new SendToMailEvent(this, applicationProperties.getDefaultEmailToKapital(), "Баланс пуст !!!", fillDisableContent(deposit, date), false, false));

//            mailService.sendEmail("user@example.com", "Баланс пуст !!!", fillDisableContent(deposit, date));
        }

        log.info("Check deposit job ended !!!");
    }

    private String fillInformContent(Deposit deposit, Instant time) {
        return "ВНИМЕНИЕ! Достигнут минимальный порог депозита по поставщику: " + deposit.getGateway().getName() + " .Пополните баланс у поставщика!" +
            " \nПоставщик услуг: " + deposit.getGateway().getName() +
            " \nТекущий баланс: " + deposit.getBalance() +
            " \nПорог уведомления: " + deposit.getLimitInform() +
            " \nПорог отключения приема платежей: " + deposit.getLimitDisable() +
            " \nДата + время: " + time;
    }

    private String fillDisableContent(Deposit deposit, Instant time) {
        return "ВНИМАНИЕ! Прием платежей  остановлен по поставщику: " + deposit.getGateway().getName() + " .Пополните баланс у поставщика!" +
            " \nПоставщик услуг: " + deposit.getGateway().getName() +
            " \nТекущий баланс: " + deposit.getBalance() +
            " \nПорог уведомления: " + deposit.getLimitInform() +
            " \nПорог отключения приема платежей: " + deposit.getLimitDisable() +
            " \nДата + время: " + time;
    }

    private final DepositRepository depositRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ApplicationProperties applicationProperties;

}
