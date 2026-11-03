package uz.paykap.service.abs.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AbsTransactionStatusResult {
    private String errorCode;
    private String errorNote;
    private String oldErrorCode;
    private String oldErrorNote;
    private String oldTransId;
    private Integer typeQuery;
    private String dateTime;
    private String transId;
}
