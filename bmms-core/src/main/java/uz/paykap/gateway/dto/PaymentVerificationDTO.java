package uz.paykap.gateway.dto;

import uz.devops.bmcore.dto.request.PayRequest;

public class PaymentVerificationDTO extends PayRequest {

    private String otp;

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    @Override
    public String toString() {
        return "PaymentVerificationDTO{" +
            "otp='" + otp + '\'' +
            "} " + super.toString();
    }
}
