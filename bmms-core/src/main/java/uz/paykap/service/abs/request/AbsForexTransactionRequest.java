package uz.paykap.service.abs.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AbsForexTransactionRequest {

    private String docNum;
    private String accCl;
    private Long amountCl;
    private Long requestAmount;
    private Double exchangeRate;
    private String accCo;
    private Long amountCo;
    private String purpose;
    private Integer fxDeal;
    @NotNull
    private String requestId;
    private String mfo;
    /* тип сделки (8-покупка 9-продажа) */
    private Integer deal;
    private String dollars;
    private String clientId;
    private String senderPan;
    private String recipientPan;
    private String codeFilial;
    private String con;
    private String dateExp;
    private String rollbackIs;
    private String isMasked;
    private String operationCode;
    private String pan;
    private String maskedPan;
}
