package uz.paykap.sender;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import uz.devops.bmcore.dto.request.CommonPayRequest;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.service.registry.HasMonoKey;
import uz.javlon.commons.base.ResultData;
import uz.javlon.commons.result.CommonResultData;
import uz.javlon.commons.result.HasData;
import uz.javlon.commons.result.HasResult;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.gateway.dto.PcSetting;
import uz.paykap.sender.dto.SenderOtpParams;
import uz.paykap.sender.dto.SenderStatusResponse;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.epos.EposResolver;

import java.util.Optional;

@Slf4j
public abstract class SenderService implements HasMonoKey<PayType> {

    @Autowired
    private EposResolver eposResolver;

    public abstract HasResult canAgentProceed(PayType payType, String agentLogin, CommonPayRequest payDTO, Long amount);

    public abstract HasData<Transaction> debit(CommonPayRequest payDTO, Transaction transaction, String agentLogin);

    public abstract ResultData<Transaction> reverse(PayType payType, Transaction transaction, String agentLogin);

    public abstract HasData<Transaction> debitConfirm(CommonPayRequest payDTO, Transaction transaction, String agentLogin);

    public HasData<SenderStatusResponse> status(final Transaction transaction, String agentLogin) {
        return new CommonResultData<>(BmResultStatus.NOT_IMPLEMENTED, null);
    }

    public HasData<Transaction> holdForAgent(CommonPayRequest payDTO, Transaction transaction, String agentLogin) {
        return new CommonResultData<>(BmResultStatus.NOT_IMPLEMENTED, null, "hold not implemented for type " + payDTO.getType());
    }

    public HasData<Bill> reverseHold(Transaction transaction, String login) {
        return new CommonResultData<>(BmResultStatus.NOT_IMPLEMENTED, null, "hold-reverse not implemented for type " + transaction.getPayType());
    }

    public HasData<Transaction> reHold(CommonPayRequest payDTO, Transaction transaction, String agentLogin) {
        return new CommonResultData<>(BmResultStatus.NOT_IMPLEMENTED, null, "hold-more not implemented for type " + payDTO.getType());
    }

    public HasData<Transaction> holdConfirm(CommonPayRequest payDTO, Transaction transaction, String agentLogin) {
        return new CommonResultData<>(BmResultStatus.NOT_IMPLEMENTED, null, "hold-confirm not implemented for type " + payDTO.getType());
    }

    public abstract PayType getSupportedPayType();

    public HasData<SenderOtpParams> getOtpParams(String pan, String expiry) {
        return new CommonResultData<>(BmResultStatus.NOT_IMPLEMENTED, null);
    }

    protected Optional<PcSetting> setEpos(CommonPayRequest payDTO, Transaction transaction) {

        if (payDTO.getSettingId() == null) {

            log.warn("'payDTO.getSettingId' is null, getting epos params from transaction");

            if (transaction.getEposMerchant() != null && transaction.getEposTerminal() != null) {

                log.info("Epos params found from transaction, merchant : {}, terminal : {}", transaction.getEposMerchant(), transaction.getEposTerminal());

                return Optional.of(new PcSetting()
                        .withEposMerchant(transaction.getEposMerchant())
                        .withEposTerminal(transaction.getEposTerminal())
                );
            }
            return Optional.empty();
        }

        Optional<PcSetting> eposSetting = eposResolver.resolve(payDTO, transaction);

        if (eposSetting.isEmpty()) {
            return Optional.empty();
        }

        transaction.setEposMerchant(eposSetting.get().getEposMerchant());
        transaction.setEposTerminal(eposSetting.get().getEposTerminal());

        return eposSetting;

    }

    @Override
    public PayType getKey() {
        return getSupportedPayType();
    }

    @Override
    public String getDetails() {
        return String.valueOf(getSupportedPayType());
    }
}
