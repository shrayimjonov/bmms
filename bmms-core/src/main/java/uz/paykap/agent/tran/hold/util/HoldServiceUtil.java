package uz.paykap.agent.tran.hold.util;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.dto.GatewayQueryResult;
import uz.devops.bmcore.dto.request.CommonPayRequest;
import uz.devops.bmcore.dto.request.SharedPayDTO;
import uz.devops.bmcore.dto.response.P2pParticipantDto;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.devops.bmcore.gateway.GatewayBillingService;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.config.Constants;
import uz.paykap.domain.Transaction;
import uz.paykap.repository.TransactionRepository;

import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

@Slf4j
@Service
@AllArgsConstructor
public class HoldServiceUtil {
    private final ApplicationContext applicationContext;
    private final TransactionRepository transactionRepository;


    public SharedPayDTO getSharedPayDTO(CommonPayRequest payDTO, String login, Transaction transaction, GatewayQueryResult gatewayQueryResult) {
        payDTO.setBillId(transaction.getRecipientBillId());
        payDTO.setTranId(transaction.getId());
        payDTO.setGatewayAmount(transaction.getGatewayAmount());

        SharedPayDTO sharedPayDTO = new SharedPayDTO()
                .login(login)
                .payDTO(payDTO)
                .recipientParams(gatewayQueryResult)
                .tranId(transaction.getId())
                .gatewayAmount(transaction.getGatewayAmount());

        if (payDTO.getAgentParams() == null) {
            payDTO.setAgentParams(new HashMap<>());
        }
        return sharedPayDTO;
    }

    public GatewayBillingService getGatewayService(Transaction transaction, GatewayQueryResult gatewayQueryResult) {
        GatewayBillingService gatewayService = getServiceBean(gatewayQueryResult.getGatewayBean());

        if (!Objects.equals(transaction.getEposMerchant(), gatewayQueryResult.getEposMerchant())) {
            log.debug("Update epos in gatewayQueryResult before {} : after : {} ", gatewayQueryResult.getEposMerchant(), transaction.getEposMerchant());
            gatewayQueryResult.setEposMerchant(transaction.getEposMerchant());
        }

        if (!Objects.equals(transaction.getEposTerminal(), gatewayQueryResult.getEposTerminal())) {
            gatewayQueryResult.setEposTerminal(transaction.getEposTerminal());
        }
        return gatewayService;
    }

    public GatewayBillingService getServiceBean(String gatewayBean) {
        try {
            return applicationContext.getBean(gatewayBean, GatewayBillingService.class);
        } catch (Exception e) {
            log.warn(e.getLocalizedMessage());
            throw new CodedException(ProblemEnum.SERVICE_NOT_FOUND, "Bean not found");
        }
    }

    public void prepareWithAdditionalResult(Transaction transaction, SimpleBillResponse result) {
        prepareResult(transaction, result);

        if (result.getSender() == null) {
            result.setSender(new P2pParticipantDto());
        }
        fillSender(result.getSender(), transaction);

        if (result.getReceiver() == null) {
            result.setReceiver(new P2pParticipantDto());
        }
        fillReceiver(result.getReceiver(), transaction);
    }

    private void prepareResult(Transaction transaction, SimpleBillResponse result) {
        result.setId(transaction.getId());
        result.setStatus(transaction.getStatus());
        result.setAmount(transaction.getRequestAmount());

        if (result.getData() == null) {
            result.setData(new HashMap<>(3));
        }

        result.getData().put("rate", String.valueOf(transaction.getCcyRate()));
        result.getData().put("recipientCcy", String.valueOf(transaction.getRecipientCcy()));
        result.getData().put("recipientAmount", String.format(Locale.US, "%.2f", transaction.getGatewayAmount()));
        result.getData().put("senderCcy", String.valueOf(transaction.getSenderCcy()));
    }

    private void fillSender(P2pParticipantDto sender, Transaction transaction) {
        sender.setAmount(sender.getAmount() != null ? sender.getAmount() : transaction.getSenderAmount());
        sender.setCurrency(sender.getCurrency() != null ? sender.getCurrency() : transaction.getSenderCcy());
        sender.setCommissionAmount(sender.getCommissionAmount() != null ? sender.getCommissionAmount() : calculateCommission(transaction));
        sender.setName(sender.getName() != null ? sender.getName() : transaction.getSenderName());
        sender.addDataAccount(!StringUtils.isEmpty(sender.fetchDataAccount()) ? sender.fetchDataAccount() : transaction.getSenderAccount());
        sender.addDataExtId(!StringUtils.isEmpty(sender.fetchDataExtId()) ? sender.fetchDataExtId() : transaction.getSenderTrId());
    }

    private Long calculateCommission(Transaction transaction) {
        if (transaction.getSystemCommissionDownAmount() == null && transaction.getSystemCommissionUpAmount() == null) {
            return 0L;
        }
        long amount = transaction.getSystemCommissionUpAmount() != null ? transaction.getSystemCommissionUpAmount() : 0L;
        amount += transaction.getSystemCommissionDownAmount() != null ? transaction.getSystemCommissionDownAmount() : 0L;

        return amount;
    }

    public void fillReceiver(P2pParticipantDto receiver, Transaction transaction) {
        receiver.setAmount(receiver.getAmount() != null ? receiver.getAmount() : transaction.getRecipientAmount());
        receiver.setCurrency(receiver.getCurrency() != null ? receiver.getCurrency() : transaction.getRecipientCcy());
        receiver.setCommissionAmount(receiver.getCommissionAmount() != null ? receiver.getCommissionAmount() : 0);
        receiver.setName(receiver.getName() != null ? receiver.getName() : transaction.getRecipientName());
        receiver.addDataAccount(!StringUtils.isEmpty(receiver.fetchDataAccount()) ? receiver.fetchDataAccount() : transaction.getRecipientAccount());
        receiver.addDataExtId(!StringUtils.isEmpty(receiver.fetchDataExtId()) ? receiver.fetchDataExtId() : transaction.getRecipientTrId());
    }

    public Transaction findTransaction(CommonPayRequest payDTO, String login) {
        Transaction transaction;

        if (!Objects.equals(login, Constants.ADMIN_USERNAME)) {

            log.debug("AGENT wants to hold");
            // AGENT
            transaction = transactionRepository.findTop1ByRequestLoginAndIdAndStatus(login, payDTO.getBillId(), TransactionStatus.HOLD_SUCCESS).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));
        } else {

            log.debug("ADMIN wants to hold");
            // ADMIN
            transaction = transactionRepository.findTop1ById(payDTO.getBillId()).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));
        }
        return transaction;
    }
}
