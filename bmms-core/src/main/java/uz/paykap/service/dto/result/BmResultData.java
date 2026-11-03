package uz.paykap.service.dto.result;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import uz.javlon.commons.result.HasData;
import uz.paykap.service.dto.p2p.BmResultStatus;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class BmResultData<TData> extends BmResult implements HasData<TData> {

    public BmResultData(TData data) {
        super(BmResultStatus.SUCCESS);
        this.data = data;
    }

    public BmResultData(BmResult bmResult) {
        super(bmResult.getStatus(), bmResult.getMessage());
    }

    public BmResultData() {
        super(BmResultStatus.UNKNOWN_ERROR);
    }

    public BmResultData(HasCodeAndMessage status) {
        super(status);
    }

    public BmResultData(HasCodeAndMessage status, String details) {
        super(status);
        this.setDetails(details);
    }

    public BmResultData(HasCodeAndMessage status, TData data) {
        super(status);
        this.data = data;
    }

    public BmResultData(HasCodeAndMessage status, TData data, String details) {
        super(status);
        this.data = data;
        this.setDetails(details);
    }

    private TData data;

    public BmResultData<TData> getUnknownError() {
        this.setStatus(BmResultStatus.UNKNOWN_ERROR);
        return this;
    }

    @Override
    public Integer getCode() {
        return getStatus().getCode();
    }
}
