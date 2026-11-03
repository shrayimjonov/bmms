package uz.paykap.sender.p2p;

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
import uz.paykap.service.p2p.support.AbstractP2pSender;
import uz.paykap.service.p2p.support.BillHandler;
import uz.paykap.utils.NciUtil;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@AllArgsConstructor
public class NciAccountSenderService extends AbstractP2pSender<P2pAccountDTO> {

    private final NciCommonService nciCommonService;
    private final ApplicationProperties properties;
    private final AbstractAbsService abstractAbsService;
    private final NciMultiTranDecoratorService nciMultiTranDecoratorService;
    private final PcHelper pcHelper;
    private final BillRepository billRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public BmResultData<String> isSupported(P2pAccountDTO dto, P2pAccount p2pAccount) {

        String account = dto.getAccount();

        if (!pcHelper.isValidAccount(account, P2pProcessorType.NCI)) {
            log.debug("Account {} did not match with pattern {}", account, NciUtil.NCI_ACCOUNT_REGEXP);
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
    public BmResultData<Bill> debit(Transaction transaction, P2pAccountDTO senderDto, P2pDTO recipientDto, P2pParamResult p2pParam) {

        log.info("#### NCI DEBIT ####: \n{}\n", senderDto);

        if (!properties.getOurMfoPattern().contains(senderDto.getMfo())) {
            return new BmResultData<>(BmResultStatus.INVALID_SENDER_PARAMS, "get info skipped, bcz foreign bank mfo = " + senderDto.getMfo());
        }

        BmResultData<NciParam> paramResult = getSenderParam(p2pParam.getValue());

        if (!paramResult.isSuccess()) {
            return new BmResultData<>(paramResult.getStatus());
        }
        if (paramResult.getData().getMultiTranParams() != null && !paramResult.getData().getMultiTranParams().isEmpty()) {
            return nciMultiTranDecoratorService.debit(transaction, senderDto, recipientDto, p2pParam);
        }

        BmResultData<AbsTransactionRequest> transactionDtoResult = abstractAbsService.getSenderTran(senderDto, recipientDto, paramResult.getData(), transaction.getId(), transaction.getSenderAmount());

        if (!transactionDtoResult.isSuccess()) {
            return new BmResultData<>(transactionDtoResult);
        }

        AbsTransactionRequest transactionDto = transactionDtoResult.getData();
        transactionDto.setRequestType(RequestType.P2P_DEBIT);

        log.info("ABS debit transaction : {} ", transactionDto);

        /*
         * User want to send from owned X account to any PC
         * We are debiting from X account (accCl) and crediting to transit account (accCo),
         * so we store bankCo as merchantId and accCo as terminalId
         * */
        setEpos(transaction, transactionDto.getBankCo(), transactionDto.getAccCo());

        log.info("NCI transaction : {} ", transactionDto);

        BmResultData<Bill> resultData = abstractAbsService.transfer(transaction, transactionDto, true);

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
        if (Objects.equals(String.valueOf(transaction.getId()), transaction.getSenderTrId())) {
            return nciMultiTranDecoratorService.reverse(transaction);
        }
        return nciCommonService.reverse(transaction, true);
    }

    private BmResultData<Bill> errorResult(Bill bill, HasCodeAndMessage codedException) {

        log.warn("Exception when crediting : {}", codedException.getMessage());

        bill.responseTime(Instant.now()).responseText(codedException.getMessage()).success(false);
        bill = billRepository.save(bill);

        return new BmResultData<>(codedException, bill);
    }
}
