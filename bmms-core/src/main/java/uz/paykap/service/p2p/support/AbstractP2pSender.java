package uz.paykap.service.p2p.support;

import lombok.extern.slf4j.Slf4j;
import uz.javlon.commons.result.HasData;
import uz.paykap.agent.tran.dto.RateService;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.recipient.p2p.P2pReverseService;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.p2p.dto.P2pDTO;
import uz.paykap.service.p2p.dto.P2pParamResult;
import uz.paykap.service.p2p.dto.response.ConfirmResult;
import uz.paykap.service.p2p.processor.ProcessorParam;

@Slf4j
public abstract class AbstractP2pSender<T extends P2pDTO> extends BaseAbstractP2p<T> implements P2pReverseService {

    private final Bill successBill = new Bill() {{
        setSuccess(true);
    }};

    public abstract BmResultData<Bill> debit(Transaction transaction, T senderDto, P2pDTO recipientDto, P2pParamResult p2pParam);

    public BmResultData<Bill> debitConfirm(Transaction transaction) {
        log.info("Default debit confirm!");
        return new BmResultData<>(BmResultStatus.SUCCESS, successBill, "Confirm skipped");
    }

    public BmResultData<ConfirmResult> confirm(Transaction transaction, Bill confirmBill) {
        return new BmResultData<>(BmResultStatus.NOT_IMPLEMENTED);
    }

    public abstract HasData<Bill> reverse(Transaction transaction);

    public <TParam extends ProcessorParam> BmResultData<TParam> getSenderParam(String params) {
        return p2PHelperService.getSenderParams(params, getProcessorType().getParamClass());
    }

    protected void setEpos(Transaction transaction, String merchant, String terminal) {
        transaction.setEposMerchant(merchant);
        transaction.setEposTerminal(terminal);

        log.debug("Set sender Epos merchant : {}", transaction.getEposMerchant());
        log.debug("Set sender Epos terminal : {}", transaction.getEposTerminal());
    }

    public String getCurrencyGroup() {
        return RateService.CcyGroupNames.DEFAULT_GROUP.getGroup();
    }
}
