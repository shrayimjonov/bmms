package uz.paykap.service.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import uz.devops.bmcore.enumeration.PayType;

@Getter
@Setter
@ToString
public class FailedPayDTO {

    private Long amount;
    private Long numerator;
    private PayType paymentType;
    private String errorMsg;
}
