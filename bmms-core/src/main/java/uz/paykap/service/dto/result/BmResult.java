package uz.paykap.service.dto.result;

import lombok.Data;
import uz.javlon.commons.result.HasSuccess;
import uz.paykap.service.dto.p2p.BmResultStatus;

import java.util.Objects;

@Data
public class BmResult implements HasSuccess {

    private BmResult() {
        this(BmResultStatus.UNKNOWN_ERROR);
    }

    public BmResult(HasCodeAndMessage status) {
        if (status == null) {
            status = BmResultStatus.UNKNOWN_ERROR;
        }
        this.status = status;
    }

    public BmResult(HasCodeAndMessage status, String details) {
        if (status == null) {
            status = BmResultStatus.UNKNOWN_ERROR;
        }
        this.status = status;
        this.details = details != null ? details : status.getMessage();
    }

    private HasCodeAndMessage status;
    private String details;

    public boolean isSuccess() {
        return Objects.equals(status.getCode(), SUCCESS_CODE);
    }

    public BmResult getSuccess() {
        this.setStatus(BmResultStatus.SUCCESS);
        return this;
    }

    public String getMessage() {
        return status.getMessage() + (details != null ? " : " + details : "");
    }

    public BmResult withStatus(HasCodeAndMessage status) {
        this.status = status;
        return this;
    }

    public BmResult withDetails(String details) {
        this.details = details;
        return this;
    }
}
