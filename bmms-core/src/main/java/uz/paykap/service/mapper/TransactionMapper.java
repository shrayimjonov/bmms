package uz.paykap.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.paykap.domain.Transaction;
import uz.paykap.service.dto.TransactionDTO;
import uz.paykap.service.dto.agent.ShortcutTransactionDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper for the entity {@link Transaction} and its DTO {@link TransactionDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface TransactionMapper extends EntityMapper<TransactionDTO, Transaction> {
    //TODO check
    default SimpleBillResponse toSimpleBill(Transaction transaction) {
        return new SimpleBillResponse()
            .amount(transaction.getRequestAmount())
            .account(transaction.getRecipientAccount())
            .currency(transaction.getRecipientCcy())
            .recipientShortName(transaction.getRecipientName())
            .responseText(transaction.getRecipientStatusText())
            .requestTime(transaction.getDate())
            .responseTime(transaction.getRecipientDate())
            .extId(transaction.getRecipientTrId())
            .sessionId("") //TODO
            .success(transaction.getRecipientSuccess())
            .status(transaction.getStatus())
            .id(transaction.getId());
    }

    @Mapping(
        expression = "java( uz.javlon.commons.util.CardUtil.maskIfOnlyUzcard(tran.getRecipientAccount()) )",
        target = "recipientAccount"
    )
    TransactionDTO toDto(Transaction tran);

    default Transaction fromId(Long id) {
        if (id == null) {
            return null;
        }
        Transaction transaction = new Transaction();
        transaction.setId(id);
        return transaction;
    }

    Transaction clone(Transaction transaction);

    default ShortcutTransactionDTO toShortcut(TransactionDTO transactionDTO) {
        ShortcutTransactionDTO shortcutTransactionDTO = new ShortcutTransactionDTO();
        shortcutTransactionDTO.setId(transactionDTO.getId());
        shortcutTransactionDTO.setRequestAmount(transactionDTO.getRequestAmount());
        shortcutTransactionDTO.setRequestTrId(transactionDTO.getRequestTrId());
        shortcutTransactionDTO.setRequestLogin(transactionDTO.getRequestLogin());
        shortcutTransactionDTO.setSenderDate(transactionDTO.getSenderDate());
        shortcutTransactionDTO.setGatewayAmount(transactionDTO.getGatewayAmount());
        shortcutTransactionDTO.setRecipientName(transactionDTO.getRecipientName());
        shortcutTransactionDTO.setRecipientDate(transactionDTO.getRecipientDate());
        shortcutTransactionDTO.setRecipientBillId(transactionDTO.getRecipientBillId());
        shortcutTransactionDTO.setAgentFeeAmount(transactionDTO.getAgentFeeAmount());
        shortcutTransactionDTO.setRoundAmount(transactionDTO.getRoundAmount());
        shortcutTransactionDTO.setCcyRate(transactionDTO.getCcyRate());
        shortcutTransactionDTO.setStatus(transactionDTO.getStatus());
        shortcutTransactionDTO.setComment(transactionDTO.getComment());
        shortcutTransactionDTO.setDate(transactionDTO.getDate());
        shortcutTransactionDTO.setPayType(transactionDTO.getPayType());
        shortcutTransactionDTO.setAmountRatio(transactionDTO.getAmountRatio());
        shortcutTransactionDTO.setRecipientAccount(transactionDTO.getRecipientAccount());
        shortcutTransactionDTO.setPromo(transactionDTO.getPromo());
        return shortcutTransactionDTO;
    }

    default ShortcutTransactionDTO toShortcut(Transaction transaction) {
        ShortcutTransactionDTO shortcutTransactionDTO = new ShortcutTransactionDTO();
        shortcutTransactionDTO.setId(transaction.getId());
        shortcutTransactionDTO.setRequestAmount(transaction.getRequestAmount());
        shortcutTransactionDTO.setRequestTrId(transaction.getRequestTrId());
        shortcutTransactionDTO.setRequestLogin(transaction.getRequestLogin());
        shortcutTransactionDTO.setSenderDate(transaction.getSenderDate());
        shortcutTransactionDTO.setGatewayAmount(transaction.getGatewayAmount());
        shortcutTransactionDTO.setRecipientName(transaction.getRecipientName());
        shortcutTransactionDTO.setRecipientDate(transaction.getRecipientDate());
        shortcutTransactionDTO.setRecipientBillId(transaction.getRecipientBillId());
        shortcutTransactionDTO.setAgentFeeAmount(transaction.getAgentFeeAmount());
        shortcutTransactionDTO.setRoundAmount(transaction.getRoundAmount());
        shortcutTransactionDTO.setCcyRate(transaction.getCcyRate());
        shortcutTransactionDTO.setStatus(transaction.getStatus());
        shortcutTransactionDTO.setComment(transaction.getComment());
        shortcutTransactionDTO.setDate(transaction.getDate());
        shortcutTransactionDTO.setAmountRatio(transaction.getAmountRatio());
        shortcutTransactionDTO.setPayType(transaction.getPayType());
        shortcutTransactionDTO.setRecipientAccount(transaction.getRecipientAccount());
        shortcutTransactionDTO.setPromo(transaction.getPromo());
        return shortcutTransactionDTO;
    }

    default List<ShortcutTransactionDTO> toShortcut(List<TransactionDTO> transactionDTOS) {
        if (transactionDTOS == null) {
            return null;
        }
        List<ShortcutTransactionDTO> list = new ArrayList<ShortcutTransactionDTO>(transactionDTOS.size());

        for (TransactionDTO transactionDTO : transactionDTOS) {
            list.add(toShortcut(transactionDTO));
        }

        return list;
    }
}
