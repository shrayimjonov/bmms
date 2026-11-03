package uz.paykap.sender.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.dto.request.CommonPayRequest;
import uz.devops.bmcore.enumeration.PayType;
import uz.javlon.commons.base.ResultData;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.javlon.commons.result.CommonResult;
import uz.javlon.commons.result.CommonResultData;
import uz.javlon.commons.result.HasData;
import uz.javlon.commons.result.HasResult;
import uz.paykap.config.Constants;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.sender.SenderResolverV3;
import uz.paykap.sender.SenderService;
import uz.paykap.sender.dto.SenderOtpParams;
import uz.paykap.service.PcHelper;
import uz.paykap.service.dto.p2p.BmResultStatus;

@Slf4j
@Service(PcSenderService.NAME)
//@AllArgsConstructor
public class PcSenderService extends SenderService {

    public static final String NAME = "pcSenderService";

    @Lazy
    @Autowired
    private SenderResolverV3 senderResolverV3;
    private final PcHelper pcHelper;

    public PcSenderService(PcHelper pcHelper) {
        this.pcHelper = pcHelper;
    }

    @Override
    public HasResult canAgentProceed(PayType payType, String agentLogin, CommonPayRequest payDTO, Long amount) {

        var senderAccount = getSenderAccount(payDTO);

        if (!senderAccount.isSuccess()) {
            return new CommonResult(senderAccount, senderAccount.getMessage());
        }

        if (pcHelper.isValidToken(senderAccount.getData(), P2pProcessorType.UZCARD) || pcHelper.isValidPan(senderAccount.getData(), P2pProcessorType.UZCARD)) {
            payDTO.setType(PayType.UZCARD);
            return senderResolverV3.canAgentProceed(PayType.UZCARD, agentLogin, payDTO, amount);
        } else if (pcHelper.isValidToken(senderAccount.getData(), P2pProcessorType.HUMO) || pcHelper.isValidPan(senderAccount.getData(), P2pProcessorType.HUMO)) {
            payDTO.setType(PayType.HUMO);
            return senderResolverV3.canAgentProceed(PayType.HUMO, agentLogin, payDTO, amount);
        }

        log.warn("senderId {} not supported by PcSender", senderAccount.getData());

        return new CommonResult(BmResultStatus.INVALID_PARAMS, "senderId '" + senderAccount.getData() + "' not supported");
    }

    @Override
    public HasData<Transaction> debit(CommonPayRequest payDTO, Transaction transaction, String agentLogin) {

        var senderAccount = getSenderAccount(payDTO);

        if (!senderAccount.isSuccess()) {
            return new CommonResultData<>(senderAccount, null, senderAccount.getMessage());
        }

        if (pcHelper.isValidToken(senderAccount.getData(), P2pProcessorType.UZCARD) || pcHelper.isValidPan(senderAccount.getData(), P2pProcessorType.UZCARD)) {
            transaction.setPayType(PayType.UZCARD);
            payDTO.setType(PayType.UZCARD);
            return senderResolverV3.debit(payDTO, transaction, agentLogin);
        } else if (pcHelper.isValidToken(senderAccount.getData(), P2pProcessorType.HUMO) || pcHelper.isValidPan(senderAccount.getData(), P2pProcessorType.HUMO)) {
            transaction.setPayType(PayType.HUMO);
            payDTO.setType(PayType.HUMO);
            return senderResolverV3.debit(payDTO, transaction, agentLogin);
        }

        return new CommonResultData<>(BmResultStatus.NOT_FOUND, null, "senderId '" + senderAccount.getData() + "' not supported");
    }

    @Override
    public ResultData<Transaction> reverse(PayType payType, Transaction transaction, String agentLogin) {

        String senderId = transaction.getSenderAccount();

        if (pcHelper.isValidToken(senderId, P2pProcessorType.UZCARD) || pcHelper.isValidPan(senderId, P2pProcessorType.UZCARD)) {
            return senderResolverV3.reverse(payType, transaction, agentLogin);
        } else if (pcHelper.isValidToken(senderId, P2pProcessorType.HUMO)) {
            return senderResolverV3.reverse(payType, transaction, agentLogin);
        }

        return new ResultData<>(new CodedException(ProblemEnum.NOT_FOUND, "senderId '" + senderId + "' not supported"));
    }

    @Override
    public HasData<Transaction> debitConfirm(CommonPayRequest payDTO, Transaction transaction, String agentLogin) {
        throw new CodedException(ProblemEnum.NOT_IMPLEMENTED);
    }

    @Override
    public PayType getSupportedPayType() {
        return PayType.PC;
    }

    @Override
    public HasData<SenderOtpParams> getOtpParams(String pan, String expiry) {
        return senderResolverV3.getOtpParams(pan, expiry);
    }

    private HasData<String> getSenderAccount(CommonPayRequest payDTO) {

        String senderId;

        if (payDTO.getParams().containsKey(Constants.TOKEN_PARAM)) {
            senderId = String.valueOf(payDTO.getParams().get(Constants.TOKEN_PARAM));
        } else if (payDTO.getParams().containsKey(Constants.CARD_PAN_PARAM)) {
            senderId = String.valueOf(payDTO.getParams().get(Constants.CARD_PAN_PARAM));
        } else if (payDTO.getParams().containsKey(Constants.PAN_PARAM)) {
            senderId = String.valueOf(payDTO.getParams().get(Constants.PAN_PARAM));
        } else {
            return new CommonResultData<>(BmResultStatus.INVALID_PARAMS, null, "'card_pan' or 'card_token' or 'pan' not given");
        }

        return new CommonResultData<>(senderId);
    }

    @Override
    public String getDescription() {
        return "Снятие средств с карт Uzcard или Humo";
    }
}
