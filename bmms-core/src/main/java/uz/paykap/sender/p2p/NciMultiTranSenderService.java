package uz.paykap.sender.p2p;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;
import uz.devops.bm.ncitrans.dto.enums.NciTransResponseJsonKeys;
import uz.devops.bmcore.enumeration.RequestType;
import uz.paykap.agent.tran.dto.CalculationService;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.config.Constants;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.repository.BillRepository;
import uz.paykap.sender.dto.WrappedP2pParam;
import uz.paykap.service.MapService;
import uz.paykap.service.abs.AbstractAbsService;
import uz.paykap.service.abs.request.AbsMultiTransactionDTO;
import uz.paykap.service.abs.response.MultiTransactionResultDTO;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.p2p.P2pRequestScopedContext;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.p2p.dto.NciTranDTO;
import uz.paykap.service.p2p.dto.P2pAccountDTO;
import uz.paykap.service.p2p.dto.P2pDTO;
import uz.paykap.service.p2p.dto.P2pParamResult;
import uz.paykap.service.p2p.processor.NciParam;
import uz.paykap.service.p2p.support.P2pHelperService;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class NciMultiTranSenderService {

    public static final String FIO = "fio";
    public static final String REQUEST_KEY = "request";
    public static final String RECIPIENT_SHORT_NAME = "nci-multi-ins-debit";
    public static final String AGENT_SHORT_NAME = "agent_short_name";
    private final AbstractAbsService nciHelperService;
    private final P2pHelperService p2PHelperService;
    private final MapService mapService;
    private final ApplicationProperties applicationProperties;
    private final ObjectMapper objectMapper;
    private final BillRepository billRepository;
    private final P2pRequestScopedContext p2pRequestScopedContext;


    @SneakyThrows
    public BmResultData<Bill> debit(Transaction transaction, P2pAccountDTO senderDto, P2pDTO recipientDto, P2pParamResult p2pParam) {
        if (!applicationProperties.getOurMfoPattern().contains(senderDto.getMfo())) {
            return new BmResultData<>(BmResultStatus.INVALID_SENDER_PARAMS, "get info skipped, bcz foreign bank mfo = " + senderDto.getMfo());
        }
        log.info("#### NCI multi transaction debit ####: \n{}\n", senderDto);
        String nciParams = getNciParam(transaction, senderDto, recipientDto, p2pParam);
        if (nciParams.isEmpty()) {
            return new BmResultData<>(BmResultStatus.PC_ERROR);
        }
        BmResultData<NciParam> paramResult = p2PHelperService.getSenderParams(nciParams, P2pProcessorType.NCI.getParamClass());
        log.info("Finally param result : {}", paramResult);

        if (!paramResult.isSuccess()) {
            return new BmResultData<>(paramResult.getStatus());
        }
        AbsMultiTransactionDTO absMultiTransactionDTO = new AbsMultiTransactionDTO();
        absMultiTransactionDTO.setTransId(transaction.getId());
        absMultiTransactionDTO.setStateId(applicationProperties.getAbs().getNciTranStateId());
        absMultiTransactionDTO.setData(paramResult.getData().getMultiTranParams());

        var filteredNciTran = absMultiTransactionDTO
                .getData()
                .stream()
                .filter(nciTranDTO -> nciTranDTO.getSum() != null && !nciTranDTO.getSum().isEmpty() && Long.parseLong(nciTranDTO.getSum()) > 0)
                .sorted(Comparator.comparing(NciTranDTO::getOrder))
                .collect(Collectors.toList());

        absMultiTransactionDTO.setData(filteredNciTran);
        Map<String, String> requestJson = new HashMap<>(3);

        requestJson.put(Constants.ACCOUNT_PARAM, senderDto.getAccount());
        requestJson.put(REQUEST_KEY, objectMapper.writeValueAsString(absMultiTransactionDTO));
        requestJson.put(AGENT_SHORT_NAME, senderDto.getName());

        Bill bill = p2PHelperService.createP2pBill(transaction, RequestType.P2P_DEBIT, P2pProcessorType.NCI, requestJson, String.valueOf(absMultiTransactionDTO.getTransId()));

        log.info("Abs multi transaction filtered request : {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(absMultiTransactionDTO));
        if (filteredNciTran.isEmpty()) {
            return new BmResultData<>(BmResultStatus.FAILED, "Nci multi tran not found");
        }
        BmResultData<List<MultiTransactionResultDTO>> resultData = nciHelperService.doMultiTransaction(absMultiTransactionDTO);
        log.info("Abs multi transaction response : {}", resultData);

        if (!resultData.isSuccess()) {

            bill
                    .responseJson(Collections.singletonMap("error", resultData.getMessage()))
                    .responseTime(Instant.now())
                    .responseText(StringUtils.substring(resultData.getMessage(), 0, 126))
                    .responseCode("FAILED")
                    .success(false);
            billRepository.save(bill);
            log.error("nci.doMultiTransaction throw exception...");
            return new BmResultData<>(resultData.getStatus(), resultData.getMessage());
        }
        Map<String, Object> responseJson = mapService.writeToMap(resultData.getData(), false);
        responseJson.put(NciTransResponseJsonKeys.TRANS_ID.name(), String.valueOf(absMultiTransactionDTO.getTransId()));

        bill
                .responseJson(responseJson)
                .responseTime(Instant.now())
                .extId(String.valueOf(absMultiTransactionDTO.getTransId()))
                .responseCode("0")
                .responseText(objectMapper.writeValueAsString(resultData.getData()))
                .success(true);

        bill = billRepository.save(bill);

        log.debug("Final save bill: {}", bill);

        return new BmResultData<>(bill);
    }

    public String getNciParam(Transaction transaction, P2pAccountDTO senderDto, P2pDTO recipientDto, P2pParamResult p2pParam) {
        WrappedP2pParam wrappedP2PParam = new WrappedP2pParam();
        wrappedP2PParam.setSenderDto(senderDto);
        wrappedP2PParam.setTransaction(transaction);
        wrappedP2PParam.setRecipientDto(recipientDto);
        wrappedP2PParam.setSenderNciId(StringUtils.substring(senderDto.getAccount(), 9, 17));
        wrappedP2PParam.setUsername(p2pRequestScopedContext.getUsername());
        wrappedP2PParam.setAgentTranId(p2pRequestScopedContext.getAgentTranId());
        wrappedP2PParam.setCommission(CalculationService.decimal2Format.format(Double.valueOf(transaction.getSystemCommissionUpAmount()) / transaction.getRequestAmount() * 100));
        wrappedP2PParam.setRequestAmount(CalculationService.decimal2Format.format(Double.valueOf(transaction.getRequestAmount()) / 100d));
        Map<String, Object> map = objectMapper.convertValue(wrappedP2PParam, Map.class);
        map.putAll(p2pRequestScopedContext.getRecipientInfo());
        return new StringSubstitutor(map).replace(p2pParam.getValue());
    }
}
