package uz.paykap.service.abs.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.paykap.service.p2p.dto.response.ConfirmResult;

@Data
@NoArgsConstructor
public class AnorPayTransactionResult {

    private String id;
    private String resultCode;
    private String resultNote;
    private ConfirmResult result;

    public AnorPayTransactionResult(String id, String resultCode, String resultNote) {
        this.id = id;
        this.resultCode = resultCode;
        this.resultNote = resultNote;
    }

    public AnorPayTransactionResult(String id, String resultCode, String resultNote, ConfirmResult result) {
        this.id = id;
        this.resultCode = resultCode;
        this.resultNote = resultNote;
        this.result = result;
    }
}
