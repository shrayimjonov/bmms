package uz.paykap.sender;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.sender.dto.SenderOtpParams;
import uz.paykap.sender.dto.SenderStatusResponse;
import uz.paykap.service.PcHelper;
import uz.paykap.service.dto.p2p.BmResultStatus;

import java.util.Optional;

@Slf4j
@Service(SenderResolverV3.NAME)
@AllArgsConstructor
public class SenderResolverV3 {

    public static final String NAME = "senderResolverV3";

    //    private final ApplicationContext applicationContext;
//    private final PaymentProperties paymentProperties;
    private final PcHelper pcHelper;

    private final SenderServiceRegistry senderServiceRegistry;

//    @PostConstruct
//    public void init() {
//
//        Map<String, SenderService> beansOfType = applicationContext.getBeansOfType(SenderService.class);
//
//        beansOfType.forEach((key, value) -> {
//            if (!Objects.equals(key, NAME) && value.getSupportedPayType() != null && value.getSupportedPayType() != PayType.UNKNOWN) {
//                log.info("{} registered for payType {}", key, value.getSupportedPayType());
//                sendersMap.put(value.getSupportedPayType(), value);
//            }
//
//            if (paymentProperties.getValidations().get(value.getSupportedPayType()) == null) {
//                log.info("Uzcard validation options not found, setting defaults");
//                paymentProperties.getValidations().put(value.getSupportedPayType(), new PaymentProperties.Validation().setDefaults(true, true, true));
//            }
//        });
//
//    }

    public HasResult canAgentProceed(PayType payType, String agentLogin, CommonPayRequest payDTO, Long amount) {

        final Optional<SenderService> senderService = senderServiceRegistry.resolve(payType);

        if (senderService.isEmpty()) {
            log.warn("debit with payType {} not supported", payType);
            return new CommonResult(BmResultStatus.UNSUPPORTED_SENDER, "Pay type not supported : " + payType);
        }

        return senderService.get().canAgentProceed(payType, agentLogin, payDTO, amount);
    }

    public HasData<Transaction> debit(CommonPayRequest payDTO, Transaction transaction, String agentLogin) {

        PayType payType = transaction.getPayType();
        final Optional<SenderService> senderService = senderServiceRegistry.resolve(payType);

        if (senderService.isEmpty()) {
            return new CommonResultData<>(BmResultStatus.UNSUPPORTED_SENDER, null, "debit with payType " + payType + " not supported");
        }

        return senderService.get().debit(payDTO, transaction, agentLogin);
    }

    public ResultData<Transaction> reverse(PayType payType, Transaction transaction, String agentLogin) {

        final Optional<SenderService> senderService = senderServiceRegistry.resolve(payType);

        if (senderService.isEmpty()) {
            return new ResultData<>(new CodedException(ProblemEnum.NOT_FOUND, "reverse with payType " + payType + " not supported"));
        }

        return senderService.get().reverse(payType, transaction, agentLogin);
    }


    public HasData<Bill> reverseHold(Transaction transaction, String login) {

        final Optional<SenderService> senderService = senderServiceRegistry.resolve(transaction.getPayType());

        if (senderService.isEmpty()) {
            log.error("OPERATION NOT SUPPORTED");
            return new CommonResultData<>(BmResultStatus.UNSUPPORTED_SENDER, null);
        }
        return senderService.get().reverseHold(transaction, login);
    }


    public HasData<Transaction> debitConfirm(CommonPayRequest payDTO, Transaction transaction, String agentLogin) {

        PayType payType = transaction.getPayType();

        final Optional<SenderService> senderService = senderServiceRegistry.resolve(payType);

        if (senderService.isEmpty()) {
            return new CommonResultData<>(BmResultStatus.UNSUPPORTED_SENDER, null, "debit confirm with payType " + payType + " not supported");
        }

        return senderService.get().debitConfirm(payDTO, transaction, agentLogin);
    }


    public HasData<SenderStatusResponse> status(Transaction transaction, String agentLogin) {

        PayType payType = transaction.getPayType();

        final Optional<SenderService> senderService = senderServiceRegistry.resolve(payType);

        if (senderService.isEmpty()) {
            return new CommonResultData<>(BmResultStatus.UNSUPPORTED_SENDER, null, "status with payType " + payType + " not supported");
        }

        return senderService.get().status(transaction, agentLogin);
    }


    public HasData<Transaction> holdForAgent(CommonPayRequest payDTO, Transaction transaction, String agentLogin) {
        PayType payType = transaction.getPayType();

        final Optional<SenderService> senderService = senderServiceRegistry.resolve(payType);

        if (senderService.isEmpty()) {
            return new CommonResultData<>(BmResultStatus.UNSUPPORTED_SENDER, null, "hold with payType " + payType + " not supported");
        }

        return senderService.get().holdForAgent(payDTO, transaction, agentLogin);
    }


    public HasData<Transaction> reHold(CommonPayRequest payDTO, Transaction transaction, String agentLogin) {
        PayType payType = transaction.getPayType();

        final Optional<SenderService> senderService = senderServiceRegistry.resolve(payType);

        if (senderService.isEmpty()) {
            return new CommonResultData<>(BmResultStatus.UNSUPPORTED_SENDER, null, "hold-more with payType " + payType + " not supported");
        }

        return senderService.get().reHold(payDTO, transaction, agentLogin);
    }


    public HasData<Transaction> holdConfirm(CommonPayRequest payDTO, Transaction transaction, String agentLogin) {
        PayType payType = transaction.getPayType();

        final Optional<SenderService> senderService = senderServiceRegistry.resolve(payType);

        if (senderService.isEmpty()) {
            return new CommonResultData<>(BmResultStatus.UNSUPPORTED_SENDER, null, "hold-confirm with payType " + payType + " not supported");
        }
        return senderService.get().holdConfirm(payDTO, transaction, agentLogin);
    }

    public HasData<SenderOtpParams> getOtpParams(String pan, String expiry, PayType payType) {
        final Optional<SenderService> senderService = senderServiceRegistry.resolve(payType);

        if (senderService.isEmpty()) {
            return new CommonResultData<>(BmResultStatus.UNSUPPORTED_SENDER, null, "hold-confirm with payType " + payType + " not supported");
        }

        return senderService.get().getOtpParams(pan, expiry);
    }

    public HasData<SenderOtpParams> getOtpParams(String pan, String expiry) {
        PayType payType = null;
        if (pcHelper.isValidToken(pan, P2pProcessorType.UZCARD) || pcHelper.isValidPan(pan, P2pProcessorType.UZCARD)) {
            payType = PayType.UZCARD;
            log.info("begin get Uzcard otp params");
        } else if (pcHelper.isValidToken(pan, P2pProcessorType.HUMO) || pcHelper.isValidPan(pan, P2pProcessorType.HUMO)) {
            payType = PayType.HUMO;
            log.info("begin get Humo otp params");
        }

        return getOtpParams(pan, expiry, payType);
    }


}

