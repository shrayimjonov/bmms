package uz.paykap.service.dto;

import lombok.Data;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.TransactionStatus;

import java.time.Instant;

@Data
public class PurposeDTO {


    private String mfo;

    /**
     * ID транзакции в ЕОПЦ
     */
    private String tranIdOfSOPC;


    private String extComment;

    /**
     * debiting amount
     */
    private Long senderAmount;

    /**
     * sender fullName
     */
    private String senderFio;

    /**
     * recipient fullName
     */
    private String recipientFio;

    /**
     * sender pan
     */
    private String senderAccount;

    private Integer ccyRate;

    /**
     * billing id
     */
    private Long tranId;

    private Long requestAmount;

    private Integer requestCcy;

    private String requestTrId;

    private String requestLogin;

    private String requestGroup;

    private String senderName;

    private Long senderServiceId;

    private Integer senderCcy;

    private Boolean senderSuccess;

    private Instant senderDate;

    private String senderStatus;

    private String senderStatusText;

    private Long senderBillId;

    private String senderGateway;

    private Double gatewayAmount;

    private String recipientName;

    private String recipientAccount;

    private Boolean recipientSuccess;

    private String recipientTrId;

    private Integer recipientCcy;

    private Long recipientAmount;

    private Long recipientServiceId;

    private String recipientGateway;

    private Long recipientBillId;

    private String recipientStatusText;

    private String recipientStatus;

    private Instant recipientDate;

    private Double roundAmount;

    private Long agentFeeAmount;

    private Long systemFeeAmount;

    private Long systemCommissionDownAmount;

    private Long systemCommissionUpAmount;

    private String comment;

    private TransactionStatus status;

    private Integer systemCurrency;

    private Double systemFee;

    private String eposTerminal;

    private String eposMerchant;

    private String requestSubAccount;

    private PayType payType;

    private Instant date;

    private String promo;

    private String category;

    private String senderBean;

    private Integer amountRatio;

    private Boolean main;
}
