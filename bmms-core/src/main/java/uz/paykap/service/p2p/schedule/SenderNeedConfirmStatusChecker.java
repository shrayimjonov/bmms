package uz.paykap.service.p2p.schedule;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.paykap.agent.tran.P2pTranServiceV3;
import uz.paykap.agent.tran.helper.P2pParamResolver;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.repository.BillRepository;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.result.BmResult;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.p2p.dto.SenderAdditionalParam;
import uz.paykap.service.p2p.dto.response.ConfirmResult;
import uz.paykap.service.p2p.support.AbstractP2pSender;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
@ConditionalOnProperty(prefix = "confirm", name = "enabled", havingValue = "true")
public class SenderNeedConfirmStatusChecker {

    private final TransactionRepository transactionRepository;
    private final BillRepository billRepository;
    private final ApplicationContext applicationContext;
    protected final P2pParamResolver p2pParamResolver;
    private final P2pTranServiceV3 p2pTranServiceV3;
    public static final String CITIZEN = ".citizen";
    public static final String BIRTH_DATE = ".birthDate";
    public static final String FIRST_NAME = ".firstName";
    public static final String LAST_NAME = ".lastName";
    public static final String MIDDLE_NAME = ".middleName";
    public static final String DOC_DATE = ".docDate";
    public static final String DOC_NUMBER = ".docNumber";
    public static final String DOC_TYPE = ".docType";
    public static final String PIN_FL = ".pinFl";

    @Scheduled(cron = "${confirm.cron:0 */5 * * * *}")
    public void checkStatusAndConfirmRecipient() {
        List<Transaction> needConfirmTrans = transactionRepository.findByStatusAndDateAfterOrderByIdDesc(TransactionStatus.S_NEED_CONFIRM, Instant.now().minus(3, ChronoUnit.DAYS), PageRequest.of(0, 10));

        log.info("Found transaction : {}", needConfirmTrans.size());

        for (Transaction transaction : needConfirmTrans) {

            UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(transaction.getRequestLogin(), "");
            SecurityContextHolder.getContext().setAuthentication(authReq);

            AbstractP2pSender p2pSenderBean;
            try {
                p2pSenderBean = applicationContext.getBean(StringUtils.uncapitalize(transaction.getSenderName()), AbstractP2pSender.class);
            } catch (Exception e) {
                log.warn(e.getLocalizedMessage());
                continue;
            }

            // TODO - fix, p2p_credit -> confirm
            Optional<Bill> debitBill = billRepository.findTop1ByTransactionIdAndTypeAndSuccessTrue(transaction.getId(), RequestType.P2P_DEBIT);

            if (!debitBill.isPresent()) {
                log.warn("Bill {} not found", transaction.getRecipientBillId());
                continue;
            }

            BmResultData<ConfirmResult> resultData = p2pSenderBean.confirm(transaction, debitBill.get());
            log.info("confirmResultData: {}", resultData);

            if (!resultData.isSuccess()) {
                log.warn("Confirm result : {}", resultData.getMessage());
                continue;
            }

            SenderAdditionalParam additionalParam = createAdditionalParam(debitBill.get().getRequestJson());

            switch (resultData.getData()) {
                case ACCEPTED:
                    log.info("Transaction {} accepted, mark as SUCCESS", transaction.getId());
                    transaction.setSenderStatus(TransactionStatus.SUCCESS.name());
                    transaction.setStatus(TransactionStatus.DEBITED);
                    transactionRepository.save(transaction);
                    doCredit(transaction, p2pSenderBean, additionalParam);
                    break;
                case REJECTED:
                    log.warn("Transaction {} rejected, need cancel sender", transaction.getId());
                    transaction.setSenderStatus(TransactionStatus.FAILED.name());
                    transaction.setStatus(TransactionStatus.FAILED);
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

    private SenderAdditionalParam createAdditionalParam(Map<String, Object> requestJson) {
        if (requestJson == null) {
            return null;
        }
        SenderAdditionalParam senderAdditionalParam = new SenderAdditionalParam();
//        if (requestJson.get(TkbTokenSenderDebitHelper.SENDER_PREFIX + FIRST_NAME) != null) {
//            senderAdditionalParam.setFirstName(String.valueOf(requestJson.get(TkbTokenSenderDebitHelper.SENDER_PREFIX + FIRST_NAME)));
//        }
//        if (requestJson.get(TkbTokenSenderDebitHelper.SENDER_PREFIX + LAST_NAME) != null) {
//            senderAdditionalParam.setLastName(String.valueOf(requestJson.get(TkbTokenSenderDebitHelper.SENDER_PREFIX + LAST_NAME)));
//        }
//        if (requestJson.get(TkbTokenSenderDebitHelper.SENDER_PREFIX + MIDDLE_NAME) != null) {
//            senderAdditionalParam.setMiddleName(String.valueOf(requestJson.get(TkbTokenSenderDebitHelper.SENDER_PREFIX + MIDDLE_NAME)));
//        }
//        if (requestJson.get(TkbTokenSenderDebitHelper.SENDER_PREFIX + BIRTH_DATE) != null) {
//            senderAdditionalParam.setBirthDate(parseToLocalDate(String.valueOf(requestJson.get(TkbTokenSenderDebitHelper.SENDER_PREFIX + BIRTH_DATE))));
//        }
//        if (requestJson.get(TkbTokenSenderDebitHelper.SENDER_PREFIX + CITIZEN) != null) {
//            senderAdditionalParam.setCitizen(String.valueOf(requestJson.get(TkbTokenSenderDebitHelper.SENDER_PREFIX + CITIZEN)));
//        }
//        if (requestJson.get(TkbTokenSenderDebitHelper.SENDER_PREFIX + DOC_TYPE) != null) {
//            senderAdditionalParam.setDocType(String.valueOf(requestJson.get(TkbTokenSenderDebitHelper.SENDER_PREFIX + DOC_TYPE)));
//        }
//        if (requestJson.get(TkbTokenSenderDebitHelper.SENDER_PREFIX + DOC_NUMBER) != null) {
//            senderAdditionalParam.setDocNumber(String.valueOf(requestJson.get(TkbTokenSenderDebitHelper.SENDER_PREFIX + DOC_NUMBER)));
//        }
//        if (requestJson.get(TkbTokenSenderDebitHelper.SENDER_PREFIX + DOC_DATE) != null) {
//            senderAdditionalParam.setDocDate(parseToLocalDate(String.valueOf(requestJson.get(TkbTokenSenderDebitHelper.SENDER_PREFIX + DOC_DATE))));
//        }
//        if (requestJson.get(TkbTokenSenderDebitHelper.SENDER_PREFIX + PIN_FL) != null) {
//            senderAdditionalParam.setPinFl(String.valueOf(requestJson.get(TkbTokenSenderDebitHelper.SENDER_PREFIX + PIN_FL)));
//        }

        return senderAdditionalParam;
    }

    private LocalDate parseToLocalDate(String stringDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return LocalDate.parse(stringDate, formatter);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private BmResult doCredit(Transaction transaction, AbstractP2pSender p2pSender, SenderAdditionalParam senderAdditionalParam) {
        BmResultData<Bill> creditResult = null;
        try {
            creditResult = p2pTranServiceV3.continueP2pTransaction(transaction, senderAdditionalParam);
        } catch (Exception e) {
            e.printStackTrace();
            p2pSender.reverse(transaction);
            return new BmResult(BmResultStatus.FAILED, e.getMessage());
        }

        if (creditResult.getStatus() == BmResultStatus.WAITING || creditResult.isTimeout()) {

            transaction.fillRecipientPart(creditResult.getData());
            transaction.setRecipientStatus(String.valueOf(creditResult.getCode()));
            transaction.nextRecipientStatusText(creditResult.getMessage());
            transaction.setStatus(TransactionStatus.R_NEED_CONFIRM);
            transactionRepository.save(transaction);
            return creditResult;
        }
        if (!creditResult.isSuccess() || creditResult.getData() == null || !creditResult.getData().isSuccess()) {

            transaction
                    .status(TransactionStatus.FAILED)
                    .nextRecipientStatusText(creditResult.getMessage());
            transactionRepository.save(transaction);

            p2pSender.reverse(transaction);
            // It is failed result, just return it
            return creditResult;
        }

        Bill recipientBill = creditResult.getData();

        transaction.fillRecipientPart(recipientBill);

        transactionRepository.save(
                transaction.getStatus() == TransactionStatus.R_NEED_CONFIRM ? transaction.status(TransactionStatus.R_NEED_CONFIRM) : transaction.status(TransactionStatus.SUCCESS)
        );

        // It is success result, just return it
        return creditResult;
    }

}
