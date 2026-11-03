package uz.paykap.service.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import uz.devops.bmcore.dto.request.PayRequest;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class HoldPayRequest extends PayRequest {
    private Boolean isOnlyHold = false;

    public HoldPayRequest() {
    }

    public HoldPayRequest(Boolean isOnlyHold) {
        this.isOnlyHold = isOnlyHold;
    }
}
