package uz.paykap.service.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StringSerializer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import uz.devops.humo.service.dto.SenderAdditionalParam;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@Setter
@ToString
public class CardCreditRequest implements Serializable {
    @NotNull
    private String pan;
    @NotNull
    private Long amount;
    @NotNull
    private String merchantId;
    @NotNull
    private String terminalId;
    private Boolean fakePan = true;
    private Long transactionId;

    @JsonUnwrapped(prefix = "additional.")
    private SenderAdditionalParam additionalParam;

    public CardCreditRequest withPan(String pan) {
        this.pan = pan;
        return this;
    }

    public CardCreditRequest withAmount(Long amount) {
        this.amount = amount;
        return this;
    }

    public CardCreditRequest withMerchantId(String merchantId) {
        this.merchantId = merchantId;
        return this;
    }

    public CardCreditRequest withTerminalId(String terminalId) {
        this.terminalId = terminalId;
        return this;
    }

    public CardCreditRequest withFakePan(Boolean fakePan) {
        this.fakePan = fakePan;
        return this;
    }


    public CardCreditRequest withTransactionId(Long transactionId) {
        this.transactionId = transactionId;
        return this;
    }
}
