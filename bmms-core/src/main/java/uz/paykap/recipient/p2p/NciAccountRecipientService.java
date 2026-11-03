package uz.paykap.recipient.p2p;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.pusher.event.PusherWarnEvent;
import uz.javlon.commons.result.HasData;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.repository.BillRepository;
import uz.paykap.service.PcHelper;
import uz.paykap.service.abs.AbstractAbsService;
import uz.paykap.service.abs.NciCommonService;
import uz.paykap.service.abs.request.AbsTransactionRequest;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.p2p.P2pAccount;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.dto.result.HasCodeAndMessage;
import uz.paykap.service.p2p.dto.P2pAccountDTO;
import uz.paykap.service.p2p.dto.P2pDTO;
import uz.paykap.service.p2p.dto.P2pParamResult;
import uz.paykap.service.p2p.processor.NciParam;
import uz.paykap.service.p2p.support.AbstractP2pRecipient;
import uz.paykap.service.p2p.support.BillHandler;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class NciAccountRecipientService extends AbstractP2pRecipient<P2pAccountDTO> {

    public static final String COMMISSION_PARAM_NAME = "commission";
    public static final String REQUEST_AMOUNT_PARAM_NAME = "requestAmount";

    private final BillRepository billRepository;
    private final AbstractAbsService abstractAbsService;
    private final NciCommonService nciCommonService;
    private final ApplicationProperties properties;
    private final ApplicationEventPublisher eventPublisher;
    private final PcHelper pcHelper;

    @Override
    public BmResultData<?> isSupported(P2pAccountDTO dto, P2pAccount p2pAccount) {

        String account = dto.getAccount();

        if (!pcHelper.isValidAccount(account, P2pProcessorType.NCI)) {
            return new BmResultData<>(BmResultStatus.INVALID_SENDER_ACCOUNT, "Invalid Nci account");
        }

        return new BmResultData<>(account);
    }

    @Override
    public P2pProcessorType getProcessorType() {
        return P2pProcessorType.NCI;
    }

    @Override
    protected BmResultData<Map<String, Object>> getInfo(P2pAccountDTO dto, BillHandler handler) {

 if (!properties.getOurMfoPattern().contains(dto.getMfo())) {
            return new BmResultData<>(Collections.singletonMap("result", "get info skipped, bcz foreign bank mfo = " + dto.getMfo()));
        }

        return abstractAbsService.getInfo(dto.getAccount(), dto.getMfo(), handler);
    }

    @Override
    public BmResultData<Bill> credit(Transaction transaction, P2pDTO senderDto, P2pAccountDTO recipientDto, P2pParamResult p2pParam) {

        log.info("#### NCI CREDIT ####: \n{}\n", recipientDto);

        BmResultData<NciParam> paramResult = getRecipientParam(p2pParam.getValue());

        if (!paramResult.isSuccess()) {
            return new BmResultData<>(paramResult);
        }

        BmResultData<AbsTransactionRequest> transactionDtoResult = abstractAbsService.getRecipientTran(senderDto, recipientDto, paramResult.getData(), transaction.getId(), transaction.getRecipientAmount());

        if (!transactionDtoResult.isSuccess()) {
            return new BmResultData<>(transactionDtoResult);
        }

        AbsTransactionRequest transactionDto = transactionDtoResult.getData();
        transactionDto.setRequestType(RequestType.P2P_CREDIT);

        log.info("ABS transaction : {} ", transactionDto);

        /*
         * User want to send from owned X account to any PC
         * We are debiting from transit account (accCl) and crediting to X account (accCo),
         * so we store bankCl as merchantId and accCl as terminalId
         * */
        setEpos(transaction, transactionDto.getBankCl(), transactionDto.getAccCl());

        BmResultData<Bill> resultData = abstractAbsService.transfer(transaction, transactionDto, false);

        if (!(resultData.isSuccess() && resultData.getData().isSuccess())) {

            // Error
            log.error("nci.doTransaction exception : {}", resultData.getMessage());
            return this.errorResult(resultData.getData(), resultData.getStatus());
        }

        if (transaction.getSystemCommissionUpAmount() > 0) {

            if (paramResult.getData().getCommission() != null) {
                // TODO - think about error result
                BmResultData<Bill> commissionTranResult = abstractAbsService.doCommissionTran(transaction, senderDto, recipientDto, paramResult.getData());

                if (!commissionTranResult.isSuccess()) {
                    String message = "Error occurred while transacting commission tran : " + paramResult.getData();

                    log.warn(message);

                    eventPublisher.publishEvent(new PusherWarnEvent(this, message));
                }

            } else {
                log.warn("Has commission for nci trans, but no commission param in p2pParam.value");
            }

        } else {
            log.info("No commission for nci transaction : {} -> {}", transaction.getSenderAccount(), transaction.getRecipientAccount());
        }

        return new BmResultData<>(resultData.getData());
    }

    @Override
    public HasData<Bill> reverse(Transaction transaction) {
        return nciCommonService.reverse(transaction, false);
    }

    private BmResultData<Map<String, String>> errorResult(BillHandler handler, BmResultData<?> bmResultData) {
        log.error("return errorResult: {}", bmResultData.getMessage());
        handler.error(Collections.singletonMap("error", bmResultData.getMessage()));
        return new BmResultData<>(bmResultData.getStatus(), bmResultData.getMessage());
    }

    private BmResultData<Bill>  errorResult(Bill bill, HasCodeAndMessage codedException) {

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
