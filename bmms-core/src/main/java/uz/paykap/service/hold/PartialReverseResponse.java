package uz.paykap.service.hold;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.devops.bmcore.dto.response.SimpleBillResponse;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartialReverseResponse {
    private SimpleBillResponse errorResponse;
    private Boolean success;
    private String username;
    private String recipientAccount;
    private Long recipientId;
    private Long amount;
    private Map<String, Object> params;

    public PartialReverseResponse(SimpleBillResponse errorResponse, Boolean success) {
        this.errorResponse = errorResponse;
        this.success = success;
    }
}
