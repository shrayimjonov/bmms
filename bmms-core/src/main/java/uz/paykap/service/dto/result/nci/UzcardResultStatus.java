package uz.paykap.service.dto.result.nci;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uz.javlon.commons.result.HasResult;

@Getter
@AllArgsConstructor
public enum UzcardResultStatus implements HasResult {

    INVALID_REQUEST(-400, "Error on uzcard gateway"),
    OBJECT_NOT_FOUND(-404, "Object not found"),

    PARSE_ERROR(-501, "Error on parse response data"),
    UZCARD_PC_ERROR(-502, "Error on uzcard gateway"),
    TIMEOUT(-503, "Probably timeout occurred"),
    ;

    private Integer code;
    private String message;

    public UzcardResultStatus withCode(Integer code) {
        this.code = code;
        return this;
    }

    public UzcardResultStatus withMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public Object getDetails() {
        return message;
    }
}
