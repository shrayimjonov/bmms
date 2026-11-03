package uz.paykap.service.dto.result.nci;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uz.javlon.commons.result.HasResult;

@Getter
@AllArgsConstructor
public enum NciResultStatus implements HasResult {

    INVALID_REQUEST(-400, "Error on nci gateway"),

    PARSE_ERROR(-501, "Error on parse response data"),
    PC_ERROR(-502, "Error on nci gateway"),
    TIMEOUT(-503, "Probably timeout occurred"),
    BANK_DAY_IS_CLOSING(-504, "Bank day is closing"),

    ;

    private Integer code;
    private String message;

    public NciResultStatus withCode(Integer code) {
        this.code = code;
        return this;
    }

    public NciResultStatus withMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public Object getDetails() {
        return message;
    }
}
