package uz.paykap.service.munisWSDL.utils;

import java.util.Arrays;

public enum MunisAgentResponseCodes {

    UNKNOWN(-1, "Неизвестно"),
    INVALID_PARAMS(1, "Неверные параметры в запросе"),
    INVALID_SETTLEMENT_SUPPLIER(2, "Неверно указан код поставщика услуг"),
    INVALID_SETTLEMENT_CODE(3, "Неверно указан код вида оплаты"),
    INVALID_SUBJECT_ELEMENT(4, "Неверно указан название реквизита для идентификации плательщика"),
    INVALID_ABS_SERVICE_PROVIDER(5, "Ошибка при подключении к системе банка"),
    BILLING_THROW_EXCEPTION(6, "Ошибка с биллингом"),
    SUCCESS(0, "Успешно"),
    BILLING_RETURN_NULL(8, "Ошибка. Пустой ответ от биллинга"),
    BILLING_RETURN_ERROR(9, "Ошибочный ответ от биллинга"),
    USER_NOT_FOUND(10, "Ошибка авторизации"),
    CANCEL_TRANSACTION_NOT_FOUND(11, "Транзакция для отмены не найдена"),
    CANCEL_TRANSACTION_IS_NOT_SUCCESS(12, "Отмена транзакции неуспешна"),
    INVALID_SIGN(13, "Неверный ключ авторизации"),
    ACCESS_DENIED(14, "Нет доступа"),
    INVALID_REQUEST_STRUCTURE(15, "Неправильная структура запроса. Возможно несанкционированная попытка на доступ!!!"),
    INVALID_CURRENCY(16, "Неправильная Валюта"),
    MINIMAL_AMOUNT(17, "Сумма меньше чем допустимо"),
    MAXIMAL_AMOUNT(18, "Сумма больше чем допустимо"),
    INVALID_AMOUNT(19, "Неверная сумма"),
    TRANSACTION_ALREADY_EXISTS(20, "Транзакция с этом идентификатором уже существует"),
    BANK_NOT_SUPPORTED(21, "Транзакция с этом идентификатором уже существует"),
    NCI_GET_CLIENT_THROW_EXCEPTION(102, "Ошибка при получении информации о клиенте банка"),
    NCI_GET_CLIENT_RETURN_EMPTY_RESULT(103, "Не найден клиент"),
    NCI_GET_CLIENT_RETURN_ERROR_RESULT(104, "Не найден клиент"),
    NCI_GET_ACCOUNT_THROW_EXCEPTION(105, "Ошибка при получении информации о счете банка"),
    NCI_GET_ACCOUNT_RETURN_EMPTY_RESULT(106, "Не найден счет клиента"),
    NCI_GET_ACCOUNT_RETURN_ERROR_RESULT(107, "Ошибка при получении информации о счете банка"),
    NCI_DO_TRANSACTION_THROW_EXCEPTION(108, "Неизвестная ошибка в проведении оплаты в банке. Пожалуйста, обратитесь в службу поддержки банка"),
    NCI_DO_TRANSACTION_RETURN_EMPTY_RESULT(109, "Неизвестная ошибка в проведении оплаты в банке. Пожалуйста, обратитесь в службу поддержки банка"),
    NCI_DO_TRANSACTION_RETURN_ERROR_RESULT(110, "Ошибка в проведении оплаты в банке"),
    NCI_CANCEL_TRANSACTION_THROW_EXCEPTION(111, "Неизвестная ошибка в проведении отмены в банке. Пожалуйста, обратитесь в службу поддержки банка"),
    NCI_CANCEL_TRANSACTION_RETURN_EMPTY_RESULT(112, "Неизвестная ошибка в проведении отмены в банке. Пожалуйста,обратитесь в службу поддержки банка"),
    NCI_CANCEL_TRANSACTION_RETURN_ERROR_RESULT(113, "Ошибка в проведении отмены в банке"),
    ;

    private int code;
    private String description;

    MunisAgentResponseCodes(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static MunisAgentResponseCodes getByDescription(String description){
        return Arrays.stream(MunisAgentResponseCodes.values()).filter(code -> code.getDescription().equals(description)).findAny().orElse(MunisAgentResponseCodes.UNKNOWN);
    }

    public int getCode() {
        return code;
    }

    public String getCodeString() {
        return String.valueOf(code);
    }

    public String getDescription() {
        return description;
    }
}
