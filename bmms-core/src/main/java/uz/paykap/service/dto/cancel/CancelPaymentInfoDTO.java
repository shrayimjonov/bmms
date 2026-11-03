package uz.paykap.service.dto.cancel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.devops.bmcore.enumeration.TransactionStatus;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelPaymentInfoDTO {
    private String terminalId;
    private String paymentId;
    private String status;
    private Boolean success;
    private String paymentRef;
    private Map<String, String> params;
    private TransactionStatus transactionStatus;
}
