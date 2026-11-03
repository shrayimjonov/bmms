package uz.paykap.service.uzcard;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.gateway.GatewayResultHandler;
import uz.javlon.commons.result.HasData;
import uz.javlon.svgate.config.SvgateResult;
import uz.javlon.svgate.dto.TranDTO;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.repository.BillRepository;
import uz.paykap.service.MapService;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.p2p.dto.P2pDTO;
import uz.paykap.service.p2p.dto.P2pParamResult;
import uz.paykap.service.p2p.dto.SenderAdditionalParam;
import uz.paykap.service.p2p.processor.UzcardParam;
import uz.paykap.utils.BillUtil;
import uz.paykap.utils.TransactionPrefixGenerator;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
public class UzcardCreditService {

    private final HashMap<UzcardCreditType, AbstractUzcardCreditService> uzcardCreditTypeMap = new HashMap<>();
    private final BillRepository billRepository;
    private final TransactionPrefixGenerator transactionPrefixUtil;
    private final MapService mapService;

    public UzcardCreditService(List<AbstractUzcardCreditService> uzcardCreditServices, BillRepository billRepository, TransactionPrefixGenerator transactionPrefixUtil, MapService mapService) {
        this.billRepository = billRepository;
        this.transactionPrefixUtil = transactionPrefixUtil;
        this.mapService = mapService;

        for (AbstractUzcardCreditService uzcardCreditService : uzcardCreditServices) {
            uzcardCreditTypeMap.put(uzcardCreditService.supportsUzcardCreditType(), uzcardCreditService);
        }
    }

    public BmResultData<Bill> credit(Transaction transaction, P2pParamResult p2pParam, UzcardParam recipientParam, P2pDTO recipientDto, SenderAdditionalParam senderAdditionalParam) {
        if (recipientParam == null) {
            return new BmResultData<>(BmResultStatus.INVALID_RECIPIENT_PARAMS, "recipientUzcardParam is null");
        }

        /* When credit type not given, then throw exception */
        if (Objects.equals(recipientParam.getCreditType(), null)) {
            return new BmResultData<>(BmResultStatus.INVALID_RECIPIENT_PARAMS, "p2p.param.creditType is null");
        }

        UzcardCreditType creditType = recipientParam.getCreditType();

        log.debug("UzcardCreditType resolved : {}", creditType);

        transaction.comment(transaction.getComment() + " (type:" + creditType + ")");

        /* Create new bill */
        final Bill bill = BillUtil.getInitialBill(
                transaction.getRequestLogin(),
                transaction.getRecipientCcy(),
                transaction.getRecipientAccount(),
                String.valueOf(creditType),
                transaction.getId(),
                transaction.getRecipientAmount(),
                RequestType.P2P_CREDIT
        );

        transaction.setRecipientBillId(bill.getId());

        var extId = transactionPrefixUtil.getP2pCreditPrefix(transaction.getId());
        bill.outExtId(extId);
        billRepository.save(bill);

        /* Select suitable credit service */
        AbstractUzcardCreditService uzcardCreditService = uzcardCreditTypeMap.get(creditType);

        if (uzcardCreditService == null) {
            final String details = "Recipient not found for " + creditType;
            updateErrorBill(transaction.getId(), bill, details);
            return new BmResultData<>(BmResultStatus.RECIPIENT_NOT_FOUND, details);
        }

        log.debug("Calling UzcardCreditService : {}", uzcardCreditService.getClass().getSimpleName());

        String senderCard = transaction.getSenderAccount();

        Optional<String> senderPanOrTokenResult = uzcardCreditService.resolveSenderToken(senderCard);

        if (senderPanOrTokenResult.isEmpty()) {
            final String details = "Card pan or token not resolved for sender " + senderCard;
            updateErrorBill(transaction.getId(), bill, details);
            return new BmResultData<>(BmResultStatus.INVALID_RECIPIENT_PARAMS, details);
        }

        String recipientCard = recipientDto.getAccount();

        Optional<String> recipientPanOrTokenResult = uzcardCreditService.resolveRecipientPanOrToken(recipientCard);

        if (recipientPanOrTokenResult.isEmpty()) {
            final String details = "Card pan or token not resolved for recipient " + recipientCard;
            updateErrorBill(transaction.getId(), bill, details);
            return new BmResultData<>(
                    BmResultStatus.INVALID_RECIPIENT_PARAMS,
                    details
            );
        }

        /* Do credit on implemented service */
        HasData<TranDTO> bmResult = uzcardCreditService.credit(
                new GatewayResultHandler() {
                    @Override
                    public void preSaveRequestParams(Map<String, Object> requestJson, Instant requestTime) {
                        bill
                                .requestJson(requestJson)
                                .requestTime(requestTime);
                        billRepository.save(bill);
                    }

                    @Override
                    public Long getBillId() {
                        return bill.getId();
                    }
                },
                transaction,
                p2pParam,
                recipientParam,
                senderPanOrTokenResult.get(),
                recipientPanOrTokenResult.get(),
                extId,
                senderAdditionalParam
        );

        if (bmResult == null) {
            final String details = "No response from SvGate";
            updateErrorBill(transaction.getId(), bill, details);
            return new BmResultData<>(BmResultStatus.RECIPIENT_PC_ERROR, details);
        } else if (bmResult.getData() == null) {
            String responseIsEmptyText = updateErrorBill(transaction, bill, bmResult);
            return new BmResultData<>(BmResultStatus.RECIPIENT_PC_ERROR, responseIsEmptyText);
        }

        if (!bmResult.isSuccess() || !bmResult.getData().isSuccess()) {

            log.warn("#### Uzcard Credit Error Response : {}", bmResult.getMessage());

            if (Objects.equals(bmResult.getCode(), SvgateResult.TIMEOUT.getCode()) || bmResult.isTimeout()) {
                bill.setResponseTime(Instant.now());
                bill.setResponseText(BmResultStatus.TIMEOUT.name());
                bill.setResponseCode(String.valueOf(BmResultStatus.TIMEOUT.getCode()));
                bill.setSuccess(false);
                billRepository.save(bill);
                return new BmResultData<>(BmResultStatus.TIMEOUT, bill);
            }

            billRepository.save(fillBill(bill, bmResult.getData()));

            return new BmResultData<>(BmResultStatus.RECIPIENT_PC_ERROR, bill, bmResult.getData() != null ? bmResult.getData().getRespDesc() : bmResult.getMessage());
        }

        log.info("#### Uzcard Credit Success Response : \n{}\n", bmResult.getData());

        billRepository.save(fillBill(bill, bmResult.getData()));

        return new BmResultData<>(bill);
    }

    private String updateErrorBill(Transaction transaction, Bill bill, HasData<TranDTO> bmResult) {

        log.warn("#### Uzcard Credit Response is NULL for transaction : {}", transaction.getId());

        String responseIsEmptyText = "Uzcard p2p response is empty" + (bmResult != null ? " : " + bmResult.getDetails() : "");

        bill
                //                    .responseJson(Collections.singletonMap("error", responseIsEmptyText))
                .responseTime(Instant.now())
                .responseText(responseIsEmptyText)
                .responseCode("Empty response")
                .success(false);

        billRepository.save(bill);
        return responseIsEmptyText;
    }

    private void updateErrorBill(Long tranId, Bill bill, String errorText) {

        log.warn("#### Uzcard Credit Response error : {} : {}", tranId, errorText);

        bill
                //                    .responseJson(Collections.singletonMap("error", responseIsEmptyText))
                .responseTime(Instant.now())
                .responseText(errorText)
                .responseCode("FAILED")
                .success(false);

        billRepository.save(bill);
    }

    public enum UzcardCreditType {
        UZCARD_CREDIT, // Just credit, calling SvGate -> trans.credit(UZCARD_B)
        UZCARD_P2P, // Do common p2p, calling SvGate -> p2p.id2id(UZCARD_A, UZCARD_B);
        VIRTUAL_CARD, // Do p2p using Tech card, calling SvGate -> p2p.id2id(UZCARD_TECH, UZCARD_B)
        UNIVERSAL, // Do p2p using SvGate -> p2p.universal(UZCARD_A, UZCARD_B)
        UNIVERSAL_CREDIT, // Do p2p using SvGate -> p2p.universal.credit(NOT_UZCARD_A, UZCARD_B)
    }

    protected Bill fillBill(Bill bill, TranDTO tranDTO) {
        if (tranDTO == null) {
            return bill
                    .responseJson(Collections.singletonMap("error", "tranDTO is null"))
                    .responseTime(Instant.now())
                    .responseText("tranDTO is null")
                    .responseCode("-1")
                    .extId("-1")
                    .success(false);
        }

        return bill
                .responseJson(mapService.convert(tranDTO))
                .responseTime(Instant.now())
                .responseText(tranDTO.getRespDesc())
                .responseCode(String.valueOf(tranDTO.getResp()))
                .extId(tranDTO.getRefNum())
                .success(tranDTO.getResp() == 0);
    }
}
