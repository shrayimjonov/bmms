package uz.paykap.service.cancel.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.javlon.commons.result.CommonResultData;
import uz.javlon.commons.result.HasData;
import uz.javlon.svgate.dto.TranDTO;
import uz.javlon.svgate.dto.enumeration.TranType;
import uz.javlon.svgate.service.SvGatePaymentServiceV2;
import uz.paykap.domain.Bill;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.service.cancel.CancelService;
import uz.paykap.service.cancel.resolver.CancelTransactionService;
import uz.paykap.service.dto.cancel.CancelPaymentInfoDTO;
import uz.paykap.service.dto.cancel.CancelRequestDTO;
import uz.paykap.service.dto.p2p.BmResultStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static uz.paykap.service.cancel.resolver.CancelTransactionService.REQUEST_KEY_NAME;
import static uz.paykap.service.cancel.resolver.CancelTransactionService.RESPONSE_KEY_NANE;

@Slf4j
@Service(value = UzcardCancelService.SERVICE_NAME)
public class UzcardCancelService implements CancelService {

    private final SvGatePaymentServiceV2 svGatePaymentService;
    public static final String SERVICE_NAME = "uzcardCancelService";
    private final ObjectMapper objectMapper;

    public UzcardCancelService(SvGatePaymentServiceV2 svGatePaymentService, ObjectMapper objectMapper) {
        this.svGatePaymentService = svGatePaymentService;
        this.objectMapper = objectMapper;
    }

    @SneakyThrows
    @Override
    public HasData<Map<String, String>> cancel(CancelRequestDTO request) {
        Map<String, String> response = new HashMap<>();
        log.debug("Cancel request : {}", request);
        var uzcardReverseResponse = svGatePaymentService.reverse(request.getPaymentId());
        log.debug("Uzcard cancel response : {}", uzcardReverseResponse);

        response.put(CancelTransactionService.REQUEST_KEY_NAME, objectMapper.writeValueAsString(request.getPaymentId()));
        response.put(CancelTransactionService.RESPONSE_KEY_NANE, objectMapper.writeValueAsString(uzcardReverseResponse));
        return new CommonResultData<>(uzcardReverseResponse != null && uzcardReverseResponse.isSuccess() ? BmResultStatus.SUCCESS : BmResultStatus.FAILED, response);
    }

    @SneakyThrows
    @Override
    public HasData<CancelPaymentInfoDTO> getInfo(Bill bill, TranType tranType) {
        String extId = bill.getOutExtId();
        log.debug("Bill outExtId: {}", extId);
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put(REQUEST_KEY_NAME, objectMapper.writeValueAsString(extId));
        CancelPaymentInfoDTO cancelPaymentInfoDTO = new CancelPaymentInfoDTO();
        cancelPaymentInfoDTO.setParams(requestParams);

        log.debug("Cancel paymentInfoDto: {}", cancelPaymentInfoDTO);
        if (extId == null || extId.isEmpty()) {
            return new CommonResultData<>(BmResultStatus.FAILED, cancelPaymentInfoDTO, "Uz card transaction id not found");
        }

        log.debug("get info request : {}", extId);
        var tranResponse = svGatePaymentService.statusByExtId(extId);

        if (!tranResponse.isSuccess() || tranResponse.getData() == null) {
            cancelPaymentInfoDTO.setTerminalId(null);
            cancelPaymentInfoDTO.setPaymentId(null);
            cancelPaymentInfoDTO.setStatus("ERROR");
            cancelPaymentInfoDTO.setSuccess(false);
            cancelPaymentInfoDTO.setPaymentRef(null);
            cancelPaymentInfoDTO.setTransactionStatus(TransactionStatus.FAILED);
            log.warn("Uzcard get info handle exception : {}", tranResponse.getMessage());
            cancelPaymentInfoDTO.getParams().put(RESPONSE_KEY_NANE, tranResponse.getMessage());
            return new CommonResultData<>(BmResultStatus.FAILED, cancelPaymentInfoDTO, tranResponse.getMessage());
        }

        requestParams.put(RESPONSE_KEY_NANE, objectMapper.writeValueAsString(tranResponse));
        log.debug("get info response : {}", tranResponse);
        return validateUzcardPaymentInfo(tranResponse.getData(), cancelPaymentInfoDTO);
    }

    public HasData<CancelPaymentInfoDTO> validateUzcardPaymentInfo(List<TranDTO> uzcardResponse, CancelPaymentInfoDTO infoDTO) {
        log.debug("Start validate uzcard payment info with infoDto: {}", infoDTO);

        if (uzcardResponse == null || uzcardResponse.isEmpty()) {
            return new CommonResultData<>(BmResultStatus.FAILED, infoDTO, "Uzcard return empty response");
        }

        var reversedTran = uzcardResponse.stream().filter(tranDTO -> Objects.equals(TranType.REVERSAL, tranDTO.getTranType())).findFirst().orElse(null);
        if (reversedTran != null) {
            log.debug("Reversal transaction from uzcardResponse: {}", reversedTran);
            infoDTO.setTerminalId(reversedTran.getTerminalId());
            infoDTO.setPaymentId(reversedTran.getRefNum());
            infoDTO.setStatus(String.valueOf(reversedTran.getStatus()));
            infoDTO.setSuccess(false);
            infoDTO.setPaymentRef(reversedTran.getExternalId());
            infoDTO.setTransactionStatus(reversedTran.getResp() == 0 ? TransactionStatus.CANCELLED : TransactionStatus.SUCCESS);
            return new CommonResultData<>(BmResultStatus.SUCCESS, infoDTO, "Transaction reversed from uzcard. Size: {}" + uzcardResponse.size());
        }

        if (uzcardResponse.size() == 1 && !getP2PTransactions(uzcardResponse).isEmpty()) {
            log.debug("Uzcard response size is equal to 1 and tranType = P2P");
            return createResultData(uzcardResponse.get(0), infoDTO);
        }

        var debitedTransactions = getDebitedTransactions(uzcardResponse);
        log.debug("Debited transactions: {}", debitedTransactions);

        if (uzcardResponse.size() > 0 && !debitedTransactions.isEmpty()) {
            log.debug("Uzcard response size is greater than 1");
            return createResultData(uzcardResponse.get(0), infoDTO);
        }

        return new CommonResultData<>(BmResultStatus.FAILED, infoDTO, "Unknown error uzcard response size " + uzcardResponse.size());
    }

    private HasData<CancelPaymentInfoDTO> createResultData(TranDTO tranDTO, CancelPaymentInfoDTO infoDTO) {
        log.debug("Start create resultDto with tranDto: {}", tranDTO);
        infoDTO.setTerminalId(tranDTO.getTerminalId());
        infoDTO.setPaymentId(tranDTO.getRefNum());
        infoDTO.setStatus(String.valueOf(tranDTO.getStatus()));
        infoDTO.setSuccess(Objects.equals(tranDTO.getResp(), 0));
        infoDTO.setPaymentRef(tranDTO.getExternalId());
        infoDTO.setTransactionStatus(Objects.equals(tranDTO.getResp(), 0) ? TransactionStatus.SUCCESS : TransactionStatus.FAILED);
        return new CommonResultData<>(infoDTO);
    }

    private List<TranDTO> getDebitedTransactions(List<TranDTO> uzcardResponse) {
        log.debug("Start getDebitedTransactions with tranDtos: {}", uzcardResponse);
        return uzcardResponse
                .stream()
                .filter(
                        tranDTO ->
                                (tranDTO.getTranType().equals(TranType.DEBIT) || tranDTO.getTranType().equals(TranType.P2P_DEBIT)) &&
                                        !tranDTO.getTranType().equals(TranType.REVERSAL)
                )
                .collect(Collectors.toList());
    }

    private List<TranDTO> getP2PTransactions(List<TranDTO> uzcardResponse) {
        log.debug("Start getP2PTransactions with tranDtos: {}", uzcardResponse);
        return uzcardResponse
                .stream()
                .filter(
                        tranDTO ->
                                (tranDTO.getTranType().equals(TranType.P2P) || tranDTO.getTranType().equals(TranType.P2P_CREDIT)) &&
                                        !tranDTO.getTranType().equals(TranType.REVERSAL)
                )
                .collect(Collectors.toList());
    }

    @Override
    public P2pProcessorType supportedType() {
        return P2pProcessorType.UZCARD;
    }
}
