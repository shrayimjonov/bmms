package uz.paykap.service.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import uz.devops.bmcore.enumeration.RequestType;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class OverpayResult implements Serializable {
    private boolean success;
    private String resultText;
    private RequestType requestType;

    public boolean isSuccess() {
        return success;
    }


    public OverpayResult success(boolean success) {
        this.success = success;
        return this;
    }

    public OverpayResult resultText(String resultText) {
        this.resultText = resultText;
        return this;
    }

    public OverpayResult requestType(RequestType requestType) {
        this.requestType = requestType;
        return this;
    }
}
