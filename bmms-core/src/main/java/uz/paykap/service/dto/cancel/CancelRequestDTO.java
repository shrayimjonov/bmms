package uz.paykap.service.dto.cancel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelRequestDTO {

    @NotNull
    private String centreId;

    private String paymentOriginator = "default";

    @NotNull
    private String merchantId;

    @NotNull
    private String terminalId;

    private String paymentId;
    private String status;
    private String paymentRef;
    private String sessionId;
    private Instant transactionDate;
    private Long billId;
    private Map<String, String> params;
}
