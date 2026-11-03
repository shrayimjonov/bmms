package uz.paykap.service.dto.p2p;

import lombok.Getter;
import uz.paykap.service.dto.result.HasCodeAndMessage;

// TODO - rename to BmResultStatus
public enum TkbResultStatus implements HasCodeAndMessage {

    /*
     * Common errors
     * */

    SUCCESS(HasCodeAndMessage.SUCCESS_CODE),
    UNKNOWN_ERROR(HasCodeAndMessage.UNKNOWN_ERROR_CODE),

    NOT_RESULT(-10001),
    ERROR_RESULT(-10002),
    PHONE_NOT_FOUND(-10003),
    WAITING(-8),
    ;

    @Getter
    private Integer code;
//    @Getter
//    private String message;

    TkbResultStatus(Integer code) {
        this.code = code;
    }

    @Override
    public boolean isSuccess() {
        return this == SUCCESS;
    }

//    @Override
//    public HasCodeAndMessage getUnknown() {
//        return UNKNOWN_ERROR;
//    }

    public String getMessage() {
        return this.name();
    }
}
