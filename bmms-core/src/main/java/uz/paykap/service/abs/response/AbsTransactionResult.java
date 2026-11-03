package uz.paykap.service.abs.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
//@AllArgsConstructor
@NoArgsConstructor
public class AbsTransactionResult {

    private String transId;
    private String errorCode;
    private String errorNote;
    private String checkRequestId;
    private Map<String, Object> details;

    private boolean isWaiting = false;

    public AbsTransactionResult(String transId, String errorCode, String errorNote, String checkRequestId) {
        this.transId = transId;
        this.errorCode = errorCode;
        this.errorNote = errorNote;
        this.checkRequestId = checkRequestId;
        this.isWaiting = false;
    }

    public String getTransId() {
        return transId != null ? transId : checkRequestId;
    }
}
