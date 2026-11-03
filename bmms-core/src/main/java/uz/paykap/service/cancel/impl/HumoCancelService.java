package uz.paykap.service.cancel.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.devops.humo.service.HumoGatewayService;
import uz.devops.humo.service.dto.GetPaymentRequestDTO;
import uz.devops.humo.service.dto.ReverseRequestDTO;
import uz.javlon.commons.result.CommonResultData;
import uz.javlon.commons.result.HasData;
import uz.javlon.commons.result.HasResult;
import uz.javlon.svgate.dto.enumeration.TranType;
import uz.paykap.domain.Bill;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.service.cancel.CancelService;
import uz.paykap.service.cancel.resolver.CancelTransactionService;
import uz.paykap.service.dto.cancel.CancelPaymentInfoDTO;
import uz.paykap.service.dto.cancel.CancelRequestDTO;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.utils.TransactionPrefixGenerator;

import java.util.HashMap;
import java.util.Map;

import static uz.paykap.service.cancel.resolver.CancelTransactionService.REQUEST_KEY_NAME;
import static uz.paykap.service.cancel.resolver.CancelTransactionService.RESPONSE_KEY_NANE;

@Slf4j
@Service(value = HumoCancelService.SERVICE_NAME)
public class HumoCancelService implements CancelService {

    public static final String SERVICE_NAME = "humoCancelService";
    public static final String ACTIVE = "active";
    public static final String CANCELLED = "cancelled";
    public static final String RETURNED = "returned";
    private final HumoGatewayService humoGatewayService;
    public static final String FINISHED = "finished";
    private final TransactionPrefixGenerator transactionPrefixGenerator;
    private final ObjectMapper objectMapper;

    public HumoCancelService(HumoGatewayService humoGatewayService, TransactionPrefixGenerator transactionPrefixGenerator, ObjectMapper objectMapper) {
        this.humoGatewayService = humoGatewayService;
        this.transactionPrefixGenerator = transactionPrefixGenerator;
        this.objectMapper = objectMapper;
    }

    @SneakyThrows
    @Override
    public HasData<Map<String, String>> cancel(CancelRequestDTO request) {
        Map<String, String> response = new HashMap<>();
        log.info("Humo cancel request : {}", request);
        if (request.getStatus().equalsIgnoreCase(FINISHED)) {
            ReverseRequestDTO returnRequest = new ReverseRequestDTO();
            returnRequest.setTerminalId(request.getTerminalId());
            returnRequest.setMerchantId(request.getMerchantId());
            returnRequest.setPaymentOriginator(request.getPaymentOriginator());
            returnRequest.setPaymentId(request.getPaymentId());
            returnRequest.setCentreID(request.getCentreId());
//            returnRequest.setPaymentRef(request.getPaymentRef());
            returnRequest.setSessionId(transactionPrefixGenerator.getCancelTranSessionId());

            log.info("Return payment request : {}", returnRequest);
            var returnPaymentResponse = humoGatewayService.returnPayment(returnRequest);
            log.info("Return payment response : {}", returnPaymentResponse);
            response.put(CancelTransactionService.REQUEST_KEY_NAME, objectMapper.writeValueAsString(returnRequest));
            response.put(CancelTransactionService.RESPONSE_KEY_NANE, objectMapper.writeValueAsString(returnPaymentResponse));
            return new CommonResultData<>(returnPaymentResponse.isSuccess() ? HasResult.SUCCESS : HasResult.UNKNOWN_ERROR, response, returnPaymentResponse.getMessage());
        } else {
            uz.devops.humo.service.dto.CancelRequestDTO cancelRequestDTO = new uz.devops.humo.service.dto.CancelRequestDTO();
            cancelRequestDTO.setPaymentRef(request.getPaymentRef());
            cancelRequestDTO.setPaymentId(request.getPaymentId());
            cancelRequestDTO.setSessionId(transactionPrefixGenerator.getCancelTranSessionId());

            log.info("Cancel payment request : {}", cancelRequestDTO);
            var cancelResponse = humoGatewayService.cancel(cancelRequestDTO);
            log.info("Cancel payment response : {}", cancelResponse);
            response.put(CancelTransactionService.REQUEST_KEY_NAME, objectMapper.writeValueAsString(cancelRequestDTO));
            response.put(CancelTransactionService.RESPONSE_KEY_NANE, objectMapper.writeValueAsString(cancelResponse));
//            return new SimpleResultData<>(cancelResponse.isSuccess(), response, cancelResponse.getException());
            return new CommonResultData<>(cancelResponse.isSuccess() ? HasResult.SUCCESS : HasResult.UNKNOWN_ERROR, response, cancelResponse.getMessage());
        }
    }

    @SneakyThrows
    @Override
    public HasData<CancelPaymentInfoDTO> getInfo(Bill bill, TranType tranType) {
        GetPaymentRequestDTO requestDTO = new GetPaymentRequestDTO();
        requestDTO.setPaymentRef(bill.getOutExtId());
        log.info("Get payment request : {}", requestDTO);
        var paymentInfo = humoGatewayService.getPayment(requestDTO);
        log.info("Get payment response : {}", paymentInfo);

        Map<String, String> requestParams = new HashMap<>();
        requestParams.put(REQUEST_KEY_NAME, objectMapper.writeValueAsString(requestDTO));
        requestParams.put(RESPONSE_KEY_NANE, objectMapper.writeValueAsString(paymentInfo.isSuccess() ? paymentInfo.getData() : (paymentInfo.getMessage())));

        CancelPaymentInfoDTO cancelPaymentInfoDTO = new CancelPaymentInfoDTO();
        cancelPaymentInfoDTO.setParams(requestParams);

        if (!paymentInfo.isSuccess() || paymentInfo.getData() == null) {
            cancelPaymentInfoDTO.setPaymentId(null);
            cancelPaymentInfoDTO.setStatus("ERROR");
            cancelPaymentInfoDTO.setPaymentRef(null);
            cancelPaymentInfoDTO.setTransactionStatus(TransactionStatus.FAILED);
            cancelPaymentInfoDTO.setSuccess(false);
            return new CommonResultData<>(BmResultStatus.BILL_NOT_FOUND, cancelPaymentInfoDTO, paymentInfo.getMessage());
        }
        var infoData = paymentInfo.getData();
        cancelPaymentInfoDTO.setTerminalId(infoData.getTerminalId());
        cancelPaymentInfoDTO.setPaymentId(infoData.getPaymentId());
        cancelPaymentInfoDTO.setStatus(infoData.getStatus().name());
        cancelPaymentInfoDTO.setPaymentRef(infoData.getPaymentRef());
        cancelPaymentInfoDTO.setTransactionStatus(getStatus(infoData.getStatus().name(), tranType));
        cancelPaymentInfoDTO.setSuccess(infoData.getStatus().name().equalsIgnoreCase(FINISHED) || infoData.getStatus().name().equalsIgnoreCase(ACTIVE));
        log.debug("Cancel Payment info : {}", cancelPaymentInfoDTO);
        return new CommonResultData<>(cancelPaymentInfoDTO);
    }

    public TransactionStatus getStatus(String status, TranType tranType) {
        if (status.equalsIgnoreCase(ACTIVE) && tranType == TranType.P2P_CREDIT) {
            return TransactionStatus.SUCCESS;
        }
        if (status.equalsIgnoreCase(ACTIVE) && tranType == TranType.P2P_DEBIT) {
            return TransactionStatus.S_NEED_CONFIRM;
        }
        if (status.equalsIgnoreCase(FINISHED)) {
            return TransactionStatus.SUCCESS;
        }
        if (status.equalsIgnoreCase(CANCELLED)) {
            return TransactionStatus.CANCELLED;
        }
        if (status.equalsIgnoreCase(RETURNED)) {
            return TransactionStatus.CANCELLED;
        }
        return TransactionStatus.FAILED;
    }

    @Override
    public P2pProcessorType supportedType() {
        return P2pProcessorType.HUMO;
    }
}
