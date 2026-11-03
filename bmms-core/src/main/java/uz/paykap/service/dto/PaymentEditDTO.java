package uz.paykap.service.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import uz.devops.bmcore.dto.request.CheckRequest;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.TransactionStatus;

@Getter
@Setter
@ToString
public class PaymentEditDTO extends CheckRequest {

    private Long transactionId;

//    private Long amount;

//    private Map<String, String> params;

//    private String account;

//    private Long recipientId;

    private TransactionStatus status;

    private PayType payType;

}
