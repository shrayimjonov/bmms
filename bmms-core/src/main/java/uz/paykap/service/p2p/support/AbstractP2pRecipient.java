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
import uz.paykap.service.p2p.dto.SenderAdditionalParam;
import uz.paykap.service.p2p.dto.response.ConfirmResult;
import uz.paykap.service.p2p.processor.ProcessorParam;

@Slf4j
public abstract class AbstractP2pRecipient<T extends P2pDTO> extends BaseAbstractP2p<T> implements P2pReverseService {

    public <TParam extends ProcessorParam> BmResultData<TParam> getRecipientParam(String params) {
        return p2PHelperService.getRecipientParams(params, getProcessorType().getParamClass());
    }

    public abstract BmResultData<Bill> credit(Transaction transaction, P2pDTO senderDto, T recipientDto, P2pParamResult p2pParamResult);

    public abstract HasData<Bill> reverse(Transaction transaction);

    public BmResultData<ConfirmResult> confirm(Transaction transaction, Bill confirmBill) {
        return new BmResultData<>(BmResultStatus.NOT_IMPLEMENTED);
    }

    public BmResultData<Bill> continueCredit(Transaction transaction, String senderBeanName, String recipientBeanName, SenderAdditionalParam senderAdditionalParam) {
        return new BmResultData<>(BmResultStatus.NOT_IMPLEMENTED);
    }

    public void setEpos(Transaction transaction, String merchant, String terminal) {
        transaction.setEposMerchant(transaction.getEposMerchant() + ">" + merchant);
        transaction.setEposTerminal(transaction.getEposTerminal() + ">" + terminal);

        log.debug("Set recipient Epos merchant : {}", transaction.getEposMerchant());
        log.debug("Set recipient Epos terminal : {}", transaction.getEposTerminal());
    }

    public String getCurrencyGroup() {
        return RateService.CcyGroupNames.DEFAULT_GROUP.getGroup();
    }
}
