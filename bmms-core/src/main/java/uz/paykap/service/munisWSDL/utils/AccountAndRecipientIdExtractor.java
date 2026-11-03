package uz.paykap.service.munisWSDL.utils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uz.devops.bm.ncitrans.adapter.NciTransAdapter;
import uz.devops.munisagetdto.GetPayee;
import uz.devops.munisagetdto.Subjectinfo;
import uz.paykap.domain.Recipient;
import uz.paykap.repository.RecipientRepository;

import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class AccountAndRecipientIdExtractor {

    public static final String RECIPIENT_SERVICE_ID = "RECIPIENT_SERVICE_ID";
    public static final String INN = "INN";
    public static String ACCOUNT = "ACCOUNT";
    public static String RECIPIENT_ID = "RECIPIENT_ID";
    public static final String RECIPIENT_BANK = "RECIPIENT_BANK";
    public static final String ANOR_RECIPIENT_ID = "063";
    public static final String PHONE = "PHONE";
    private final RecipientRepository recipientRepository;

    public void fillRecipientId(MunisAgentPaymentType paymentType, String amount, Map<String, String> requestMap) throws MunisAgentException {
        if (MunisAgentPaymentType.WALLET_PAY.equals(paymentType)){
            log.info("MUNIS agent pay type: 06 (Wallet Pay)");
            Optional<Recipient> optionalRecipient = recipientRepository.findTop1ByShortName(NciTransAdapter.WALLET_RECIPIENT_SHORT_NAME);
            if (!optionalRecipient.isPresent()){
                log.error("recipient with shortName: {} not found...", NciTransAdapter.WALLET_RECIPIENT_SHORT_NAME);
                throw new MunisAgentException(MunisAgentResponseCodes.INVALID_ABS_SERVICE_PROVIDER.getCodeString(), MunisAgentResponseCodes.INVALID_ABS_SERVICE_PROVIDER.getDescription());
            }
            validateAmount(amount, optionalRecipient.get());

            requestMap.put(RECIPIENT_ID, String.valueOf(optionalRecipient.get().getId()));
            requestMap.put(RECIPIENT_SERVICE_ID, optionalRecipient.get().getServiceId());
        }

        if (MunisAgentPaymentType.LEGAL_ACCOUNT_PAY.equals(paymentType)){
            log.info("MUNIS agent pay type: 07 (Legal entity account Pay)");
            Optional<Recipient> optionalRecipient = recipientRepository.findTop1ByShortName(NciTransAdapter.LEGAL_ACCOUNT_RECIPIENT_SHORT_NAME);
            if (!optionalRecipient.isPresent()){
                log.error("recipient with shortName: {} not found...", NciTransAdapter.LEGAL_ACCOUNT_RECIPIENT_SHORT_NAME);
                throw new MunisAgentException(MunisAgentResponseCodes.INVALID_ABS_SERVICE_PROVIDER.getCodeString(), MunisAgentResponseCodes.INVALID_ABS_SERVICE_PROVIDER.getDescription());
            }
            validateAmount(amount, optionalRecipient.get());

            requestMap.put(RECIPIENT_ID, String.valueOf(optionalRecipient.get().getId()));
            requestMap.put(RECIPIENT_SERVICE_ID, optionalRecipient.get().getServiceId());
        }
    }

    public void fillAccount(List<Subjectinfo> request, MunisAgentPaymentType paymentType, Map<String, String> requestMap) {
        for (Subjectinfo subjectinfo : request) {

            if (Objects.equals(subjectinfo.getElement(), RECIPIENT_BANK)) {
                requestMap.put(RECIPIENT_BANK, subjectinfo.getValue());
            }

            if (MunisAgentPaymentType.WALLET_PAY.equals(paymentType) && Objects.equals(subjectinfo.getElement(), PHONE)){
                requestMap.put(AccountAndRecipientIdExtractor.ACCOUNT, subjectinfo.getValue());

            }

            if (MunisAgentPaymentType.LEGAL_ACCOUNT_PAY.equals(paymentType) && Objects.equals(subjectinfo.getElement(), INN)){
                requestMap.put(AccountAndRecipientIdExtractor.ACCOUNT, subjectinfo.getValue());
            }
        }
    }

    private void validateAmount(String amount, Recipient recipient) throws MunisAgentException {
        if (amount != null){
            Long amountL = 0L;
            try {
                amountL = Long.parseLong(amount);
            }catch (NumberFormatException e){
                log.error(MunisAgentResponseCodes.INVALID_AMOUNT.getDescription());
                throw new MunisAgentException(MunisAgentResponseCodes.INVALID_AMOUNT.getCodeString(), MunisAgentResponseCodes.INVALID_AMOUNT.getDescription());
            }

            if (recipient.getMinAmount()  > amountL){
                log.error("Amount less than allowed...");
                throw new MunisAgentException(MunisAgentResponseCodes.MINIMAL_AMOUNT.getCodeString(), MunisAgentResponseCodes.MINIMAL_AMOUNT.getDescription());
            }
            if (recipient.getMaxAmount() < amountL){
                log.error("The amount is more than acceptable...");
                throw new MunisAgentException(MunisAgentResponseCodes.MAXIMAL_AMOUNT.getCodeString(), MunisAgentResponseCodes.MAXIMAL_AMOUNT.getDescription());
            }
        }
    }

    public static Long parseRecipientId(String recipientId){
        try {
            return Long.valueOf(recipientId);
        }catch (NumberFormatException e){
            log.error("NumberFormatException on parsing recipientId: {} to Long", recipientId);
            return 0L;
        }
    }
}
