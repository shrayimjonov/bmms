package uz.paykap.gateway.humo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HumoDebitErrorCode {
    INVALID_PARAMS("HD_111", "invalid params"),
    GET_REAL_PAM_EXCEPTION("HD_112", "get real pan throw exception"),
    NO_REPLY_FROM_PC("HD_113", "no reply from pc"),
    CARD_STATUS_NOT_FOUND("HD_114", "card status not found"),
    CARD_NOT_ACTIVE("HD_115", "card not active"),
    CREATE_DEBIT_ERROR("HD_116", "humo create debit error"),
    CREATE_DEBIT_NOT_SUCCESS("HD_117", "humo create debit not success"),
    CONFIRM_DEBIT_ERROR("HD_118", "humo confirm debit error"),
    CONFIRM_DEBIT_NOT_SUCCESS("HD_119", "humo confirm debit not success"),
    REVERSE_ERROR("HD_120", "humo reverse error"),
    REVERSE_NO_REPLY("HD_121", "humo reverse no reply"),
    REVERSE_NOT_SUCCESS("HD_122", "reverse not success"),
    CREDIT_ERROR("HD_123", "humo create credit error"),
    CREDIT_NOT_SUCCESS("HD_124", "humo create credit not success"),
    INSUFFICIENT_FUNDS_ON_SENDER("HD_125", "insufficient humo balance")
    ;

    private String code;
    private String desc;
}
