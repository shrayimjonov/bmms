package uz.paykap.sender.p2p;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.devops.bm.ncitrans.dto.enums.NciTransResponseJsonKeys;
import uz.devops.bmcore.enumeration.RequestType;
import uz.javlon.commons.base.SimpleResultData;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.javlon.commons.result.HasData;
import uz.paykap.config.Constants;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Deposit;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.repository.BillRepository;
import uz.paykap.repository.DepositRepository;
import uz.paykap.security.SecurityUtils;
import uz.paykap.service.PcHelper;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.p2p.P2pAccount;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.p2p.dto.P2pDTO;
import uz.paykap.service.p2p.dto.P2pDepositDTO;
import uz.paykap.service.p2p.dto.P2pParamResult;
import uz.paykap.service.p2p.support.AbstractP2pSender;
import uz.paykap.service.p2p.support.BillHandler;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class DepositSenderService extends AbstractP2pSender<P2pDepositDTO> {

    private final PcHelper pcHelper;
    private final DepositRepository depositRepository;
    private final BillRepository billRepository;

    @Override
    public BmResultData<?> isSupported(P2pDepositDTO dto, P2pAccount p2pAccount) {

        boolean matches = pcHelper.isValidAccount(dto.getCondition(), P2pProcessorType.DEPOSIT);

        if (!matches) {
            return new BmResultData<>(BmResultStatus.INVALID_SENDER_ACCOUNT, "Invalid humo pan");
        }
        return new BmResultData<>("OK, valid");
    }

    @Override
    public P2pProcessorType getProcessorType() {
        return P2pProcessorType.DEPOSIT;
    }

    @Override
    protected BmResultData<Map<String, Object>> getInfo(P2pDepositDTO dto, BillHandler handler) {
        try {
            SimpleResultData<Deposit> depositSimpleResultData = getDeposit(dto);
            if (!depositSimpleResultData.isSuccess()) {
                return new BmResultData<>(BmResultStatus.DEPOSIT_NOT_FOUND, "Agent deposit not found");
            }
            Deposit deposit = depositSimpleResultData.getData();
            Map<String, Object> data = new HashMap<>();
            data.put(Constants.AGENT_NAME, deposit.getAgent().getShortName());
            data.put(Constants.ID, deposit.getId());
            if (!deposit.getBlocked() && deposit.getBalance() > 0 && deposit.getAgent().getActive()) {
                return new BmResultData<>(data);
            }
            return new BmResultData<>(BmResultStatus.DEPOSIT_BLOCKED);
        } catch (CodedException e) {
            handler.error(Collections.singletonMap("error", e.getLocalizedMessage()));
            return new BmResultData<>(BmResultStatus.INTERNAL_ERROR, e.getLocalizedMessage());
        }
    }

    private SimpleResultData<Deposit> getDeposit(P2pDepositDTO dto) {
        log.info("Deposit dto : {}", dto);
        Deposit deposit;
        if (dto.getShortName() != null && !dto.getShortName().isEmpty()) {
            deposit = depositRepository.findTop1ByAgent_ShortName(dto.getShortName()).orElse(null);
            return new SimpleResultData<>(deposit);
        }
        if (dto.getInn() != null && !dto.getInn().isEmpty()) {
            deposit = depositRepository.findTop1ByAgent_Inn(dto.getInn()).orElse(null);
            return new SimpleResultData<>(deposit);
        }
        if (dto.getPinfl() != null && !dto.getPinfl().isEmpty()) {
//                deposit = depositRepository.findTop1ByAgent_ShortName(dto.getShortName()).orElse(null);
            // TODO: 24.06.2022 keyinchalik agentga pinfl qo`wilsa shu bo`yicha logika qilsa bo`ladi
        }
        return new SimpleResultData<>(false, null, null);
    }

    @SneakyThrows
    @Override
    public BmResultData<Bill> debit(Transaction transaction, P2pDepositDTO senderDto, P2pDTO recipientDto, P2pParamResult p2pParam) {
        Map<String, Object> requestJson = new HashMap<>(3);
        requestJson.put(Constants.REQUEST, objectMapper.writeValueAsString(senderDto));

        Bill bill = p2PHelperService.createP2pBill(transaction, RequestType.P2P_DEBIT, P2pProcessorType.DEPOSIT, requestJson);

        SimpleResultData<Deposit> depositSimpleResultData = getDeposit(senderDto);
        if (!depositSimpleResultData.isSuccess()) {
            bill
                .responseJson(Collections.singletonMap("error", "Agent deposit not found"))
                .responseTime(Instant.now())
                .responseText("Agent deposit not found")
                .responseCode("FAILED")
                .success(false);
            billRepository.save(bill);
            return new BmResultData<>(BmResultStatus.DEPOSIT_NOT_FOUND, "Agent deposit not found");
        }
        if ((depositSimpleResultData.getData().getBalance() - transaction.getSenderAmount() * depositSimpleResultData.getData().getAgent().getAmountRatio() / 100) < 0) {
            bill
                .responseJson(Collections.singletonMap("error", "Agent deposit balance small greater than sender amount"))
                .responseTime(Instant.now())
                .responseText("Agent deposit balance small greater than sender amount")
                .responseCode("FAILED")
                .success(false);
            billRepository.save(bill);
            return new BmResultData<>(BmResultStatus.DEPOSIT_NOT_FOUND, "Agent deposit not found");
        }
        Deposit deposit = depositSimpleResultData.getData();

        requestJson.put(Constants.SHORT_NAME, depositSimpleResultData.getData().getAgent().getShortName());
        requestJson.put(Constants.AMOUNT, transaction.getSenderAmount() * deposit.getAgent().getAmountRatio() / 100);

        Long afterBalance = deposit.getBalance() - transaction.getSenderAmount() * deposit.getAgent().getAmountRatio() / 100;
        log.info("Before balance : {}", deposit.getBalance());
        deposit.setBalance(afterBalance);
        log.info("After deposit balance : {}", deposit.getBalance());
        depositRepository.save(deposit);

        Map<String, Object> responseJson = new HashMap<>();
        bill
            .responseJson(responseJson)
            .responseTime(Instant.now())
            .extId(String.valueOf(depositSimpleResultData.getData().getId()))
            .responseCode("0")
            .responseText("SUCCESS")
            .success(true);

        billRepository.save(bill);

        return new BmResultData<>(BmResultStatus.SUCCESS, bill);
    }

    @Override
    public HasData<Bill> reverse(Transaction transaction) {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

        Bill bill = new Bill()
            .recipientShortName("nci-reverse")
            .account(transaction.getSenderAccount())
            .amount(transaction.getSenderAmount())
            .amountRatio(transaction.getAmountRatio())
            .currency(transaction.getSenderCcy())
            .type(RequestType.CANCEL)
            .login(login)
            .requestTime(Instant.now())
            .responseTime(Instant.now())
            .transactionId(transaction.getId())
            .success(false);

        var optionalBill = billRepository.findFirstByTransactionIdAndType(transaction.getId(), RequestType.P2P_DEBIT);
        if (optionalBill.isEmpty()) {
            var responseMessage = "debit bill not found on tran id  :" + transaction.getId() + " and type : " + RequestType.P2P_DEBIT;
            bill
                .responseJson(Collections.singletonMap("error", responseMessage))
                .responseTime(Instant.now())
                .responseText(responseMessage)
                .responseCode("-9999")
                .success(false);
            billRepository.save(bill);
            return new BmResultData<>(BmResultStatus.BILL_NOT_FOUND, bill, responseMessage);
        }
        String agentShortName = (String) optionalBill.get().getRequestJson().getOrDefault(Constants.SHORT_NAME, null);
        Long cancelAmount = (Long) optionalBill.get().getRequestJson().getOrDefault(Constants.AMOUNT, null);
        if (agentShortName == null || cancelAmount == null) {
            bill
                .responseJson(Collections.singletonMap("error", "agent short name or cancel amount not found"))
                .responseTime(Instant.now())
                .responseText("agent short name or cancel amount not found")
                .responseCode("-9999")
                .success(false);
            billRepository.save(bill);
            return new BmResultData<>(BmResultStatus.DEPOSIT_NOT_FOUND, bill, "agent short name or cancel amount not found");
        }
        var optionalDeposit = depositRepository.findTop1ByAgent_ShortName(agentShortName);
        if (optionalDeposit.isEmpty()) {
            var responseMessage = "deposit not found on short name  :" + agentShortName;
            bill
                .responseJson(Collections.singletonMap("error", responseMessage))
                .responseTime(Instant.now())
                .responseText(responseMessage)
                .responseCode("-9999")
                .success(false);
            billRepository.save(bill);
            return new BmResultData<>(BmResultStatus.DEPOSIT_NOT_FOUND, bill, responseMessage);
        }
        optionalDeposit.get().setBalance(optionalDeposit.get().getBalance() + cancelAmount);
        depositRepository.save(optionalDeposit.get());
        bill
            .responseJson(Collections.singletonMap(NciTransResponseJsonKeys.TRANS_ID.name(), optionalBill.get().getId()))
            .responseTime(Instant.now())
            .extId(String.valueOf(optionalDeposit.get().getId()))
            .responseText("SUCCESS")
            .success(true);

        bill = billRepository.save(bill);
        log.debug("Final save bill: {}", bill);
        return new BmResultData<>(bill);
    }
}
