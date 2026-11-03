package uz.paykap.gateway.nci.forex;

import lombok.Getter;
import uz.paykap.service.dto.result.HasCodeAndMessage;

public enum NciForexErrorCodes implements HasCodeAndMessage {

    /*
     * Common errors
     * */

    SUCCESS(HasCodeAndMessage.SUCCESS_CODE),
    UNKNOWN_ERROR(HasCodeAndMessage.UNKNOWN_ERROR_CODE),

    INVALID_PARAMS(1, "Неверные параметры"),
    CLIENT_UNAVAILABLE(2, "Участник перевода недоступен"),
    CLIENT_NOT_FOUND(3, "Получатель не найден"),
    CLIENT_VALIDATE_SYSTEM_ERROR(4, "Ошибка внутренней системы получателя перевода при проверке"),
    MAX_LIMIT_ERROR(5, "Сумма превышает допустимую сумму пополнения на стороне получателя"),
    MIN_LIMIT_ERROR(6, "Сумма меньше допустимую сумму пополнения на стороне получателя"),
    ACCOUNT_NOT_FOUND(8, "Не найден счет стороне получателя"),
    UNAUTHORIZED(9, "Логин не найден"),
    INSUFFICIENT_FUNDS_DEPOSIT(10, "Недостаточно средств на депозите"),
    SENDER_ACCOUNT_IS_EMPTY(11, "Cчет отправителя пустой"),
    SENDER_ACCOUNT_NOT_FOUND(12, "Cчет отправителя не найден"),
    CLIENT_IN_BLACK_LIST(13, "Клиент в черном списке"),
    NCI_GET_BLACK_LIST_ERROR(14, "Ошибка при преверка клиента в черном списке"),
    TRANSACTION_FAILED(15, "Ошибка в переводе"),
    CANCEL_TRANSACTION_FAILED(16, "Ошибка в отмена транзакция"),
    FIND_MANY_CLIENTS(17, "С таким номером зарегистрирован более одного клиента"),
    CLIENT_ID_NOT_FOUND(18, "Найден номер клиента в АБС"),
    CURRENCY_NOT_SUPPORTED(20, "Валюта не поддерживается"),
    NOT_IMPLEMENTED(21, "Не реализовано"),

    ;

    @Getter
    private Integer code;
    private String desc;

    NciForexErrorCodes(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    NciForexErrorCodes(int code) {
        this.code = code;
    }

    public String getCodeString(){
        return String.valueOf(code);
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public String getMessage() {
        return this.getDesc();
    }

    @Override
    public boolean isSuccess() {
        return this == SUCCESS;
    }

//    @Override
//    public HasCodeAndMessage getUnknown() {
//        return UNKNOWN_ERROR;
//    }
}
