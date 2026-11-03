package uz.paykap.service.munisWSDL.utils;

import java.util.Arrays;

public enum MunisAgentPaymentType {
    UNDEFINED("-1"),
    WALLET_PAY("06"),
    LEGAL_ACCOUNT_PAY("07"),
    ;

    private String paymentCode;

    MunisAgentPaymentType(String paymentCode) {
        this.paymentCode = paymentCode;
    }

    public String getPaymentCode() {
        return paymentCode;
    }

    public static MunisAgentPaymentType getByPaymentCode(String paymentCode){
        return Arrays.stream(MunisAgentPaymentType.values()).filter(code -> code.getPaymentCode().equals(paymentCode)).findAny().orElse(MunisAgentPaymentType.UNDEFINED);
    }


}
