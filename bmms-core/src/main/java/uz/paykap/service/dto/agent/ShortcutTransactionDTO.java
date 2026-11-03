package uz.paykap.service.dto.agent;

import lombok.Data;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.TransactionStatus;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;

@Data
public class ShortcutTransactionDTO implements Serializable {
    private Long id;

    private Long requestAmount;

    private Integer requestCcy;

    @NotNull
    @Size(max = 64)
    private String requestTrId;

    @NotNull
    @Size(max = 64)
    private String requestLogin;

    private Instant senderDate;

    private Double gatewayAmount;

    @Size(max = 32)
    private String recipientName;

    private Instant recipientDate;

    private Long recipientBillId;

    private Long agentFeeAmount;

    @DecimalMin(value = "0")
    private Double roundAmount;

    private Integer ccyRate;

    private TransactionStatus status;

    @NotNull
    @Size(max = 128)
    private String comment;

    private Instant date;

    @NotNull
    private PayType payType;

    @NotNull
    private Integer amountRatio;

    @Size(max = 64)
    private String recipientAccount;

    @Size(max = 128)
    private String promo;
}
