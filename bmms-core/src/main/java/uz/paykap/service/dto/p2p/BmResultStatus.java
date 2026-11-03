package uz.paykap.service.dto.p2p;

import lombok.Getter;
import uz.javlon.commons.result.HasResult;
import uz.paykap.service.dto.result.HasCodeAndMessage;

// TODO - rename to BmResultStatus
public enum BmResultStatus implements HasCodeAndMessage, HasResult {

    /*
     * Common errors
     * */
    SUCCESS(HasCodeAndMessage.SUCCESS_CODE),
    UNKNOWN_ERROR(HasCodeAndMessage.UNKNOWN_ERROR_CODE),

    INVALID_REQUEST(-2),
    OPERATION_NOT_SUPPORTED(-3),
    P2P_NOT_PERMITTED(-4),
    INTERNAL_ERROR(-5),
    PC_ERROR(-6),
    PC_UNREACHABLE(-7),
    WAITING(-8),
    INVALID_PARAMS(-9),
    INVALID_STATUS(-10),
    FAILED(-11),
    USERNAME_NOT_FOUND(-12),
    BILL_NOT_FOUND(-13),
    CANCEL_FAILED(-14),
    INVALID_BEAN_NAME(-15),
    PARSE_ERROR(-16),
    TRANSACTION_NOT_FOUND(-17),
    MIN_LIMIT_EXCEEDED(-18),
    MAX_LIMIT_EXCEEDED(-19),
    DEPOSIT_NOT_FOUND(-20),
    DEPOSIT_BLOCKED(-21),
    NOT_FOUND(-22),

    /*
     * Sender errors
     * */

    UNSUPPORTED_SENDER(-100),
    SENDER_NOT_FOUND(-101),
    INVALID_SENDER_ACCOUNT(-102),
    INVALID_SENDER_TYPE(-103),
    INSUFFICIENT_FUNDS_ON_SENDER(-104),
    SENDER_PC_ERROR(-105),
    INVALID_SENDER_PARAMS(-106),
    SENDER_PC_UNREACHABLE(-107),
    S_NEED_CONFIRM(-108),
    S_REJECTED(-109),

    /*
     * Recipient errors
     * */

    UNSUPPORTED_RECIPIENT(-200),
    RECIPIENT_NOT_FOUND(-201),
    INVALID_RECIPIENT_ACCOUNT(-202),
    INVALID_RECIPIENT_TYPE(-203),
    //    INSUFFICIENT_FUNDS_ON_RECIPIENT(-204),
    RECIPIENT_PC_ERROR(-205),
    INVALID_RECIPIENT_PARAMS(-206),
    RECIPIENT_PC_UNREACHABLE(-207),
    R_NEED_CONFIRM(-208),
    R_REJECTED(-209),

    NOT_IMPLEMENTED(-300),
    USE_REVERSE(-301),
    INCORRECT_AMOUNT(-302),
    TRANSACTION_WITH_THIS_USER_NOT_FOUND(-303),
    PARTIAL_REVERSE_ERROR(-304),
//    SENDER_NOT_FOUND(2),
//    INVALID_SENDER_ACCOUNT(4),

//    INVALID_PARAMS(6),
//    INVALID_ACCOUNT(6),
//    PC_UNREACHABLE(5),
//    PC_UNKNOWN_ERROR(5),

    //    P2P_PARAMS_NOT_FOUND(5),
    TIMEOUT(HasResult.TIMEOUT.getCode()),
    IN_PROCESS(-305),
    INVALID_RESPONSE(-306),
    //
    ;

    @Getter
    private Integer code;
//    @Getter
//    private String message;

    BmResultStatus(Integer code) {
        this.code = code;
    }

    @Override
    public boolean isSuccess() {
        return this == SUCCESS;
    }

    public boolean isTimeout() {
        return this == TIMEOUT;
    }

//    @Override
//    public HasCodeAndMessage getUnknown() {
//        return UNKNOWN_ERROR;
//    }

    public String getMessage() {
        return this.name();
    }

    @Override
    public Object getDetails() {
        return null;
    }

//
//    @Override
//    public I18nDTO[] getI18n() {
//        I18nDTO[] i18nDTOS = new I18nDTO[3];
//        i18nDTOS[0] = new I18nDTO(Lang.UZ, this.name());
//        i18nDTOS[1] = new I18nDTO(Lang.RU, this.name());
//        i18nDTOS[2] = new I18nDTO(Lang.EN, this.name());
//        return i18nDTOS;
//    }
}
