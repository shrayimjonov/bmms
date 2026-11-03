package uz.paykap.service.cancel.resolver;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.javlon.commons.result.CommonResultData;
import uz.javlon.commons.result.HasData;
import uz.javlon.svgate.dto.enumeration.TranType;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.repository.BillRepository;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.security.SecurityUtils;
import uz.paykap.service.cancel.CancelService;
import uz.paykap.service.dto.cancel.CancelPaymentInfoDTO;
import uz.paykap.service.dto.cancel.CancelRequestDTO;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.humo.HumoP2pHelperService;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class CancelTransactionService {

    public static final String GREATER_THAN_CHARACTER = ">";
    public static final String BILL_ID_KEY_NAME = "BILL_ID";
    public static final String CANCELLED = "cancelled";
    private final TransactionRepository transactionRepository;
    private final HumoP2pHelperService humoP2pHelperService;
    private final BillRepository billRepository;
    public static final String REQUEST_KEY_NAME = "request";
    public static final String RESPONSE_KEY_NANE = "response";

    public CancelTransactionService(TransactionRepository transactionRepository, HumoP2pHelperService humoP2pHelperService, BillRepository billRepository) {
        this.transactionRepository = transactionRepository;
        this.humoP2pHelperService = humoP2pHelperService;
        this.billRepository = billRepository;
    }

    public Transaction cancelSender(Transaction transaction, CancelService cancelService) {
        var senderCancelResponse = cancel(transaction, TranType.P2P_DEBIT, cancelService);
        log.info("Cancel sender response : {}", senderCancelResponse);
        if (senderCancelResponse.isSuccess()) {
            transaction
                    .senderStatus(CANCELLED)
                    .senderSuccess(false)
                    .status(TransactionStatus.SENDER_CANCELLED);
            transaction = transactionRepository.save(transaction);
        } else {
            transaction.status(TransactionStatus.S_CANCEL_FAILED);
        }
        return transactionRepository.save(transaction);
    }

    public Transaction cancelRecipient(Transaction transaction, CancelService cancelService) {
        var recipientCancelResponse = cancel(transaction, TranType.P2P_CREDIT, cancelService);
        log.info("Cancel recipient response : {}", recipientCancelResponse);
        if (recipientCancelResponse.isSuccess()) {
            transaction
                    .recipientStatus(CANCELLED)
                    .recipientSuccess(false)
                    .status(TransactionStatus.RECIPIENT_CANCELLED)
                    .nextRecipientStatusText(TransactionStatus.RECIPIENT_CANCELLED);
            transaction = transactionRepository.save(transaction);
        } else {
            transaction.status(TransactionStatus.R_CANCEL_FAILED);
        }
        return transactionRepository.save(transaction);
    }

    @SneakyThrows
    public HasData<CancelPaymentInfoDTO> cancel(Transaction transaction, TranType tranType, CancelService cancelService) {
        log.info("Begin cancel transaction {}\n, tranType {}\n, cancelService {}\n", transaction, tranType, cancelService);
        RequestType requestType = tranType == TranType.P2P_DEBIT ? RequestType.P2P_DEBIT : RequestType.P2P_CREDIT;
        var optionalBill = billRepository.findFirstByTransactionIdAndType(transaction.getId(), requestType);
        if (optionalBill.isEmpty()) {
            return new CommonResultData<>(BmResultStatus.FAILED, null, "P2p " + requestType + " bill not found on tran id " + transaction.getId());
        }

        var paymentInfo = cancelService.getInfo(optionalBill.get(), tranType);
        log.debug("Payment info result: {}", paymentInfo);
        if (paymentInfo == null || !paymentInfo.isSuccess() || paymentInfo.getData() == null || paymentInfo.getData().getPaymentId() == null || paymentInfo.getData().getPaymentId().isEmpty()) {
            return paymentInfo;
        }

        CancelRequestDTO requestDTO = new CancelRequestDTO();
        requestDTO.setCentreId(humoP2pHelperService.getCentreId(transaction.getSenderAccount()));

        if (transaction.getEposMerchant().contains(GREATER_THAN_CHARACTER)) {
            requestDTO.setMerchantId(StringUtils.substring(transaction.getEposMerchant(), 0, transaction.getEposMerchant().indexOf(GREATER_THAN_CHARACTER)));
        } else {
            requestDTO.setMerchantId(transaction.getEposMerchant());
        }

        if (transaction.getEposTerminal().contains(GREATER_THAN_CHARACTER)) {
            requestDTO.setTerminalId(StringUtils.substring(transaction.getEposTerminal(), 0, transaction.getEposTerminal().indexOf(GREATER_THAN_CHARACTER)));
        } else {
            requestDTO.setTerminalId(transaction.getEposTerminal());
        }

        log.debug("Cancel request params\n : {}", requestDTO);
        var bill = createBill(transaction, Objects.equals(tranType, TranType.P2P_DEBIT), cancelService.supportedType());

        if (paymentInfo.getData().getParams() != null) {
            paymentInfo.getData().getParams().put(BILL_ID_KEY_NAME, String.valueOf(bill.getId()));
        }

        requestDTO.setPaymentId(paymentInfo.getData().getPaymentId());
        requestDTO.setStatus(paymentInfo.getData().getStatus());
        requestDTO.setPaymentRef(paymentInfo.getData().getPaymentRef());
        requestDTO.setParams(paymentInfo.getData().getParams());
        requestDTO.setTransactionDate(transaction.getDate());
        if (tranType == TranType.P2P_DEBIT) {
            requestDTO.setBillId(transaction.getSenderBillId());
        } else {
            requestDTO.setBillId(transaction.getRecipientBillId());
        }

        log.info("Cancel request : {}", requestDTO);
        var cancelResult = cancelService.cancel(requestDTO);
        log.info("Cancel response : {}", cancelResult);

        Map<String, Object> request = new HashMap<>();
        request.put(REQUEST_KEY_NAME, cancelResult.getData().get(REQUEST_KEY_NAME));
        Map<String, Object> response = new HashMap<>();
        response.put(RESPONSE_KEY_NANE, cancelResult.getData().get(RESPONSE_KEY_NANE));

        bill
                .responseJson(response)
                .responseTime(Instant.now())
                .requestJson(request)
                .success(cancelResult.isSuccess());
        billRepository.save(bill);

        log.info("Finally save bill response {}", bill);
        if (cancelResult.isSuccess() && cancelResult.getData() != null) {
            CancelPaymentInfoDTO cancelPaymentInfoDTO = new CancelPaymentInfoDTO();
            cancelPaymentInfoDTO.setPaymentId(paymentInfo.getData().getPaymentId());
            cancelPaymentInfoDTO.setPaymentRef(paymentInfo.getData().getPaymentRef());
            cancelPaymentInfoDTO.setTransactionStatus(TransactionStatus.CANCELLED);
            return new CommonResultData<>(cancelPaymentInfoDTO);
        }

        return new CommonResultData<>(BmResultStatus.UNKNOWN_ERROR, null);
    }

    public Bill createBill(Transaction transaction, Boolean isSender, P2pProcessorType processorType) {
        var userLogin = SecurityUtils.getCurrentUserLogin();
        Bill bill = new Bill()
                .type(RequestType.CANCEL)
                .amountRatio(transaction.getAmountRatio())
                .login(userLogin.orElse(null))
                .requestTime(Instant.now())
                .transactionId(transaction.getId())
                .responseTime(Instant.now());

        if (isSender) {
            bill
                    .recipientShortName(processorType.name() + "-" + TranType.P2P_DEBIT + "-REVERSE")
                    .amount(transaction.getSenderAmount())
                    .account(transaction.getSenderAccount())
                    .currency(transaction.getSenderCcy())
                    .extId(transaction.getSenderTrId());
        } else {
            bill
                    .recipientShortName(processorType.name() + "-" + TranType.P2P_CREDIT + "-REVERSE")
                    .amount(transaction.getRecipientAmount())
                    .account(transaction.getRecipientAccount())
                    .currency(transaction.getRecipientCcy())
                    .extId(transaction.getRecipientTrId());
        }
        return billRepository.save(bill);
    }
}
