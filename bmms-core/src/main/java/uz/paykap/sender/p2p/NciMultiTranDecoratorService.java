package uz.paykap.sender.p2p;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.enumeration.RequestType;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Deposit;
import uz.paykap.domain.Transaction;
import uz.paykap.repository.BillRepository;
import uz.paykap.repository.DepositRepository;
import uz.paykap.service.abs.NciCommonService;
import uz.paykap.service.abs.response.NciUpdateTranGroupType;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.p2p.dto.P2pAccountDTO;
import uz.paykap.service.p2p.dto.P2pDTO;
import uz.paykap.service.p2p.dto.P2pParamResult;

import java.util.Optional;

import static uz.paykap.sender.p2p.NciMultiTranSenderService.AGENT_SHORT_NAME;

@Slf4j
@Service
@AllArgsConstructor
public class NciMultiTranDecoratorService {
    private final NciMultiTranSenderService nciMultiTranSenderService;
    private final DepositRepository depositRepository;
    private final NciCommonService nciCommonService;
    private final BillRepository billRepository;

    public BmResultData<Bill> debit(Transaction transaction, P2pAccountDTO senderDto, P2pDTO recipientDto, P2pParamResult p2pParam) {
        Optional<Deposit> optionalDeposit = depositRepository.findTop1ByAgent_ShortName(senderDto.getName());
        if (optionalDeposit.isEmpty()) {
            log.info("Agent deposit not found on short name {}", transaction.getRequestLogin());
            return new BmResultData<>(BmResultStatus.PC_ERROR, "Agent deposit not found on short name" + senderDto.getName());
        }
        Deposit deposit = optionalDeposit.get();
        log.debug("Found deposit : {} ", deposit);
        if (deposit.getBlocked() || deposit.getBalance() < transaction.getSenderAmount()) {
            log.info("Sender amount {} greater than deposit  {}", transaction.getSenderAmount(), deposit.getBalance());
            return new BmResultData<>(BmResultStatus.PC_ERROR, "Sender amount greater than deposit");
        }

        BmResultData<Bill> resultData = nciMultiTranSenderService.debit(transaction, senderDto, recipientDto, p2pParam);
        if (resultData.isSuccess()) {
            long afterDivideBalance = deposit.getBalance() - transaction.getSenderAmount() * deposit.getAgent().getAmountRatio() / 100;
            deposit.setBalance(afterDivideBalance);
            depositRepository.save(deposit);
        }
        return resultData;
    }

    public BmResultData<Bill> reverse(Transaction transaction) {
        var resultData = nciCommonService.reverse(transaction, true, NciUpdateTranGroupType.MULTI_INS);
        log.info("Reverse response : {}", resultData);
        if (!resultData.isSuccess()) {
            return resultData;
        }
        var optionalBill = billRepository.findFirstByTransactionIdAndType(transaction.getId(), RequestType.P2P_DEBIT);
        if (optionalBill.isEmpty()) {
            log.info("Debit bill not found on transaction id : {}", transaction.getId());
            return resultData;
        }
        log.info("Found bill : {}", optionalBill);
        var agentShortName = optionalBill.get().getRequestJson().getOrDefault(AGENT_SHORT_NAME, null);
        log.info("Agent shortname : {}", agentShortName);
        if (agentShortName == null) {
            log.info("Agent short name is null");
            return resultData;
        }
        Deposit deposit = depositRepository.findTop1ByAgent_ShortName(agentShortName.toString()).orElse(null);
        if (deposit == null) {
            log.info("Deposit not found on name : {}", agentShortName);
            return resultData;
        }
        deposit.setBalance(deposit.getBalance() + transaction.getSenderAmount() * deposit.getAgent().getAmountRatio() / 100);
        log.info("Agent deposit returned to : {}", deposit.getBalance());
        depositRepository.save(deposit);
        return resultData;
    }
}
