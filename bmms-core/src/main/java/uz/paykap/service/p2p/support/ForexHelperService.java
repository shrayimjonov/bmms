package uz.paykap.service.p2p.support;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uz.devops.bm.ncitrans.dto.enums.NciTransResponseJsonKeys;
import uz.devops.bmcore.enumeration.RequestType;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.repository.BillRepository;
import uz.paykap.service.abs.AbstractAbsService;
import uz.paykap.service.abs.request.AbsForexTransactionRequest;
import uz.paykap.service.abs.response.AbsTransactionResult;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.p2p.dto.*;
import uz.paykap.service.p2p.processor.NciForexParam;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class ForexHelperService {

    private final AbstractAbsService abstractAbsService;
    private final P2pHelperService p2pHelperService;
    private final BillRepository billRepository;

    public BmResultData<Bill> doDirectForex(Transaction transaction, NciForexParam nciForexParam, P2pForexDTO p2pForexDTO, boolean forexIsSender) {


        AbsForexTransactionRequest forexTransactionDTO = abstractAbsService.getForexDirectRequestDto(nciForexParam, p2pForexDTO, transaction.getId(), transaction.getSenderAccount(), transaction.getSenderAmount(), transaction.getRecipientAccount(), transaction.getRecipientAmount(), p2pHelperService.convertToSum(transaction.getCcyRate()), forexIsSender, transaction, null);

        Bill bill = p2pHelperService.createP2pBill(transaction, RequestType.P2P_CREDIT, P2pProcessorType.FOREX, forexTransactionDTO, forexTransactionDTO.getRequestId());

        BmResultData<AbsTransactionResult> resultData = abstractAbsService.doForexTransaction(forexTransactionDTO);

        if (!resultData.isSuccess()) {

            // Error

            log.error("nci.doTransaction throw exception...");
            return abstractAbsService.errorResult(bill, resultData.getStatus());
        }

        // Success

        bill
                .responseJson(Collections.singletonMap(NciTransResponseJsonKeys.TRANS_ID.name(), String.valueOf(resultData.getData().getTransId())))
                .responseTime(Instant.now())
                .extId(resultData.getData().getTransId())
                .success(true)
                .responseText(resultData.getData().getErrorNote())
        ;

        bill = billRepository.save(bill);

        log.debug("Final bill saved : {}", bill);

        return new BmResultData<>(bill);
    }

    public BmResultData<Bill> doForexCreditTransaction(Transaction transaction, P2pDTO senderDto, P2pForexDTO recipientDto, P2pParamResult p2pParam) {

        BmResultData<NciForexParam> paramResult = p2pHelperService.getRecipientParams(p2pParam.getValue(), NciForexParam.class);

        if (!paramResult.isSuccess()) {
            return new BmResultData<>(paramResult);
        }
        String exp = null;
        if (senderDto instanceof P2pPanDTO) {
            exp = ((P2pPanDTO) senderDto).getExp();
        }
        if (senderDto instanceof P2pTokenDTO) {
            exp = ((P2pTokenDTO) senderDto).getExp();
        }
        AbsForexTransactionRequest forexTransactionDTO = abstractAbsService.getForexRecipientRequestDto(paramResult.getData(), recipientDto, transaction.getId(), transaction.getSenderAmount(), transaction.getRecipientAccount(), transaction.getRecipientAmount(), p2pHelperService.convertToSum(transaction.getCcyRate()), transaction, exp);

        Bill bill = p2pHelperService.createP2pBill(transaction, RequestType.P2P_CREDIT, P2pProcessorType.FOREX, forexTransactionDTO, forexTransactionDTO.getRequestId());

        BmResultData<AbsTransactionResult> resultData = abstractAbsService.doForexTransaction(forexTransactionDTO);

        if (!resultData.isSuccess()) {

            // Error

            log.error("nci.doTransaction throw exception...");
            return abstractAbsService.errorResult(bill, resultData.getStatus());
        }

        // Success

        bill
                .responseJson(Collections.singletonMap(NciTransResponseJsonKeys.TRANS_ID.name(), resultData.getData().getTransId()))
                .responseTime(Instant.now())
                .extId(resultData.getData().getTransId())
                .success(true)
                .responseText(resultData.getData().getErrorNote())
        ;

        bill = billRepository.save(bill);

        log.debug("Final bill saved : {}", bill);

        return new BmResultData<>(bill);
    }

//    public BmResultData<Bill> doForexCreditTransaction(Transaction transaction, P2pDTO senderDto, P2pFidoForexDTO recipientDto, P2pParamResult p2pParam) {
//
//        BmResultData<FidoValParam> paramResult = p2pHelperService.getRecipientParams(p2pParam.getValue(), FidoValParam.class);
//
//        AbsForexTransactionRequest forexTransactionDTO = abstractAbsService.getForexCreditRequestDTO(paramResult.getData(), recipientDto, transaction, p2pHelperService.convertToSum(transaction.getCcyRate()));
//
//        Bill bill = p2pHelperService.createP2pBill(transaction, RequestType.P2P_CREDIT, P2pProcessorType.FOREX, forexTransactionDTO, forexTransactionDTO.getRequestId());
//
//        BmResultData<AbsTransactionResult> resultData = abstractAbsService.doForexTransaction(forexTransactionDTO);
//
//        if (!resultData.isSuccess()) {
//
//            // Error
//
//            log.error("nci.doTransaction throw exception...");
//            return abstractAbsService.errorResult(bill, resultData.getStatus());
//        }
//
//        // Success
//
//        Map<String, Object> responseJson = new HashMap<>(resultData.getData().getDetails());
//        responseJson.put(NciTransResponseJsonKeys.TRANS_ID.name(), resultData.getData().getTransId());
//
//        bill
//                .responseJson(responseJson)
//                .responseTime(Instant.now())
//                .extId(resultData.getData().getTransId())
//                .success(true)
//                .responseText(resultData.getData().getErrorNote())
//        ;
//
//        bill = billRepository.save(bill);
//
//        log.debug("Final bill saved : {}", bill);
//
//        return new BmResultData<>(bill);
//    }

    public BmResultData<Bill> doForexDebitTransaction(P2pForexDTO dto, Transaction transaction, P2pParamResult p2pParam) {

        BmResultData<NciForexParam> paramResult = p2pHelperService.getSenderParams(p2pParam.getValue(), NciForexParam.class);

        if (!paramResult.isSuccess()) {
            return new BmResultData<>(paramResult.getStatus());
        }

        log.info("#### NCI Forex ####: \n{}\n", dto);

        AbsForexTransactionRequest forexTransaction = abstractAbsService.getForexSenderRequestDto(paramResult.getData(), dto, transaction.getId(), transaction.getSenderAccount(), transaction.getSenderAmount(), transaction.getRecipientAmount(), p2pHelperService.convertToSum(transaction.getCcyRate()), transaction, null);

        Bill bill = p2pHelperService.createP2pBill(transaction, RequestType.P2P_DEBIT, P2pProcessorType.FOREX, forexTransaction, forexTransaction.getRequestId());

        bill = billRepository.save(bill);

        BmResultData<AbsTransactionResult> resultData = abstractAbsService.doForexTransaction(forexTransaction);

        if (!resultData.isSuccess()) {

            // Error

            bill
                    .responseJson(resultData.getData().getDetails())
                    .responseTime(Instant.now())
                    .responseText(StringUtils.substring(resultData.getMessage(), 0, 126))
                    .responseCode("FAILED")
                    .success(false)
            ;
            billRepository.save(bill);
            log.error("nci.doTransaction throw exception...");
            return abstractAbsService.errorResult(bill, resultData.getStatus());
        }

        // Success

        Map<String, Object> responseJson = new HashMap<>();
        responseJson.put(NciTransResponseJsonKeys.TRANS_ID.name(), resultData.getData().getTransId());

        bill
                .responseJson(responseJson)
                .responseTime(Instant.now())
                .extId(resultData.getData().getTransId())
                .success(true)
                .responseText(resultData.getData().getErrorNote())
        ;

        bill = billRepository.save(bill);

        log.debug("Final bill saved : {}", bill);

        return new BmResultData<>(bill);
    }

    public BmResultData<Bill> reverseForex(Transaction transaction, Boolean isSender) {
        log.info("Reverse forex with transaction : {}", transaction);
        var billId = isSender ? transaction.getSenderBillId() : transaction.getRecipientBillId();
        var requestJson = new HashMap<>();
        requestJson.put("billId", billId);
        Bill bill = p2pHelperService.createP2pBill(transaction, RequestType.CANCEL, P2pProcessorType.FOREX, requestJson);

        bill = billRepository.save(bill);

        BmResultData<AbsTransactionResult> resultData = abstractAbsService.reverseForex(transaction.getSenderBillId());

        if (!resultData.isSuccess()) {
            bill
                    .responseJson(resultData.getData().getDetails())
                    .responseTime(Instant.now())
                    .responseText(StringUtils.substring(resultData.getMessage(), 0, 126))
                    .responseCode("FAILED")
                    .success(false)
            ;
            billRepository.save(bill);
            log.error("nci cancel forex throw exception...");
            return abstractAbsService.errorResult(bill, resultData.getStatus());
        }
        Map<String, Object> responseJson = new HashMap<>();
        responseJson.put(NciTransResponseJsonKeys.TRANS_ID.name(), resultData.getData().getTransId());

        bill
                .responseJson(responseJson)
                .responseTime(Instant.now())
                .extId(resultData.getData().getTransId())
                .success(true)
                .responseText(resultData.getData().getErrorNote());

        bill = billRepository.save(bill);

        log.debug("Final bill saved : {}", bill);

        return new BmResultData<>(bill);
    }
}
