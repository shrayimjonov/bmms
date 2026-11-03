package uz.paykap.recipient.p2p;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import uz.javlon.commons.result.HasData;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.config.Constants;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.repository.BillRepository;
import uz.paykap.service.abs.AbstractAbsService;
import uz.paykap.service.abs.NciCommonService;
import uz.paykap.service.abs.request.AnorPayTransactionRequest;
import uz.paykap.service.abs.response.AbsAccountResult;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.p2p.P2pAccount;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.dto.result.HasCodeAndMessage;
import uz.paykap.service.p2p.dto.P2pAnorDTO;
import uz.paykap.service.p2p.dto.P2pDTO;
import uz.paykap.service.p2p.dto.P2pParamResult;
import uz.paykap.service.p2p.dto.response.ConfirmResult;
import uz.paykap.service.p2p.processor.AnorParam;
import uz.paykap.service.p2p.support.AbstractP2pRecipient;
import uz.paykap.service.p2p.support.BillHandler;
import uz.paykap.utils.NciUtil;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class AnorPayRecipientService extends AbstractP2pRecipient<P2pAnorDTO> {

    private final BillRepository billRepository;
    private final AbstractAbsService abstractAbsService;
    private final NciCommonService nciCommonService;
    private final ApplicationProperties properties;

    @Override
    public BmResultData<?> isSupported(P2pAnorDTO dto, P2pAccount p2pAccount) {

        String account = dto.getAccount();

        boolean matches = NciUtil.nciAccountPattern.matcher(account).matches();

        if (!matches) {
            return new BmResultData<>(BmResultStatus.INVALID_SENDER_ACCOUNT, "Invalid Nci account");
        }

        return new BmResultData<>(account);
    }

    @Override
    public P2pProcessorType getProcessorType() {
        return P2pProcessorType.ANOR;
    }

    @Override
    public BmResultData<ConfirmResult> confirm(Transaction transaction, Bill confirmBill) {
        return abstractAbsService.confirm(transaction, confirmBill);
    }

    @Override
    protected BmResultData<Map<String, Object>> getInfo(P2pAnorDTO dto, BillHandler handler) {

        if (dto == null || dto.getMfo() == null) {
            return new BmResultData<>(Collections.singletonMap("error", "nci p2pAccountDto or mfo is null"));
        }

        if (!properties.getOurMfoPattern().contains(dto.getMfo())) {
            return new BmResultData<>(Collections.singletonMap("result", "get info skipped, bcz foreign bank mfo = " + dto.getMfo()));
        }

        BmResultData<AbsAccountResult> accountResult = abstractAbsService.sendGetAccountRequest(handler.getBillId(), dto.getAccount(), dto.getMfo());

        if (!accountResult.isSuccess()) {
            // Error
            return errorResult(handler, accountResult);
        }

        // Success

        Map<String, Object> responseJson = new HashMap<>();
        responseJson.put(Constants.FIO_PARAM, accountResult.getData().getName());
        responseJson.put(Constants.ACCOUNT_PARAM, accountResult.getData().getTransId());
//        responseJson.put(NciTransResponseJsonKeys.WALLET_TYPE.name(), accountResult.getData().getName());
        handler.success(responseJson);

        return new BmResultData<>(responseJson);
    }

    @Override
    public BmResultData<Bill> credit(Transaction transaction, P2pDTO senderDto, P2pAnorDTO recipientDto, P2pParamResult p2pParam) {

        log.info("#### ANOR PAY credit ####: \n{}\n", recipientDto);

        BmResultData<AnorParam> paramResult = getRecipientParam(p2pParam.getValue());

        if (!paramResult.isSuccess()) {
            return new BmResultData<>(paramResult);
        }

        AnorPayTransactionRequest transactionDto = abstractAbsService.getAnorPayRecipientTran(recipientDto, senderDto, paramResult.getData(), String.valueOf(transaction.getId()), transaction.getRecipientAmount(), "");

        BmResultData<Bill> resultData = abstractAbsService.anorPayTransfer(transaction, transactionDto);

        if (!(resultData.isSuccess() && resultData.getData().isSuccess())) {

            // Error
            log.error("nci.doTransaction exception : {}", resultData.getMessage());
            return this.errorResult(resultData.getData(), resultData.getStatus());
        }

        // Success

//        if (transaction.getSystemCommissionUpAmount() > 0) {
//
//            if (paramResult.getData().getCommission() != null) {
//                // TODO - think about error result
//                BmResultData<Bill> commissionTranResult = doCommissionTran(transaction, paramResult.getData());
//
//                if (!commissionTranResult.isSuccess()) {
//                    String message = "Error occurred while transacting commission tran : " + paramResult.getData();
//
//                    log.warn(message);
//
//                    eventPublisher.publishEvent(new PusherWarnEvent(this, message));
//                }
//
//            } else {
//                log.warn("Has commission for nci trans, but no commission param in p2pParam.value");
//            }
//
//        } else {
//            log.info("No commission for nci transaction : {} -> {}", transaction.getSenderAccount(), transaction.getRecipientAccount());
//        }

        return new BmResultData<>(resultData.getData());
    }

//    private BmResultData<Bill> doCommissionTran(Transaction transaction, NciParam nciParam) {
//
//        log.debug("Doing commission tran to NCI : {}", nciParam.getCommission());
//
//        String accCl = nciParam.getBankAccount();
//        String bankCl = nciParam.getBankMfo();
//        String nameCl = nciParam.getAccName();
//
//        String accCo = nciParam.getCommission().getBankAccount();
//        String nameCo = nciParam.getCommission().getAccName();
//        String bankCo = nciParam.getCommission().getBankMfo();
//
//        String purposeId = nciParam.getCommission().getPurposeId();
//        String purpose = nciParam.getCommission().getPurpose();
//
//        /*
//         * User want to send from owned X account to any PC
//         * We are debiting from X account (accCl) and crediting to transit account (accCo),
//         * so we store bankCo as merchantId and accCo as terminalId
//         * */
//        setEpos(transaction, bankCo, accCo);
//
//        // TODO - impl, multi commission
////        Long commissionUpAmount = nciParam.getCommission().getPercent().multiply(BigDecimal.valueOf(transaction.getSystemCommissionUpAmount())).divide(BigDecimal.valueOf(100L), RoundingMode.FLOOR).longValue();
//
//        AbsTransactionRequest transactionDto = abstractAbsService.getTransactionDto2(
//            transaction.getId(),
//            accCl,
//            bankCl,
//            nameCl,
//            accCo,
//            String.valueOf(transaction.getId()),
//            transaction.getSystemCommissionUpAmount(),
//            nameCo,
//            bankCo,
//            purposeId,
//            purpose,
//            "_kom", "", "", "", "");
//
////        log.info("NCI commission transaction : {} ", transactionDto);
//
//        Bill bill = p2PHelperService.createCommissionBill(transaction, Constants.UZS_CURRENCY, nameCo, RequestType.DEPOSIT, transactionDto, nciParam.getCommission().getBankAccount());
//
//        //used for checkStatus method
//        Map<String, String> requestJson = new HashMap<>();
//
//        if (bill.getRequestJson() != null) {
//            requestJson = bill.getRequestJson();
//        }
//
//        requestJson.put(Constants.CHECK_STATUS_ID, transactionDto.getRequestId());
//        bill.setRequestJson(requestJson);
//        bill.setOutExtId(transactionDto.getRequestId());
//        bill = billRepository.save(bill);
//
//        BmResultData<AbsTransactionResult> resultData = abstractAbsService.doTransaction(transactionDto);
//
//        if (!resultData.isSuccess()) {
//
//            // Error
//            log.error("nci.doTransaction exception : {}", resultData.getMessage());
//            return this.errorResult(bill, resultData.getStatus());
//        }
//
//        // Success
//
//        bill
//            .responseJson(Collections.singletonMap(NciTransResponseJsonKeys.TRANS_ID.name(), String.valueOf(resultData.getData().getTransId())))
//            .responseTime(Instant.now())
//            .extId(resultData.getData().getTransId())
//            .success(true)
//            .responseText(resultData.getData().getErrorNote())
//        ;
//
//        bill = billRepository.save(bill);
//
//        log.debug("Final save bill: {}", bill);
//
//        return new BmResultData<>(bill);
//    }

    @Override
    public HasData<Bill> reverse(Transaction transaction) {
        return nciCommonService.reverse(transaction, false);
    }

    private BmResultData<Map<String, Object>> errorResult(BillHandler handler, BmResultData<?> bmResultData) {
        log.error("return errorResult: {}", bmResultData.getMessage());
        handler.error(Collections.singletonMap("error", bmResultData.getMessage()));
        return new BmResultData<>(bmResultData.getStatus(), bmResultData.getMessage());
    }


    private BmResultData<Bill> errorResult(Bill bill, HasCodeAndMessage codedException) {

        log.warn("Exception when crediting : {}", codedException.getMessage());

        bill
                .responseTime(Instant.now())
                .responseText(codedException.getMessage())
                .success(false)
        ;
        bill = billRepository.save(bill);

        return new BmResultData<>(codedException, bill);
    }

}
