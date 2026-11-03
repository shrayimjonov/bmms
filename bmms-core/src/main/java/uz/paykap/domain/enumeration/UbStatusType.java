package uz.paykap.domain.enumeration;

/**
 * The TransactionStatus enumeration.
 */

public enum UbStatusType {
    SUCCESS(4, "Чек успешно оплачен"),
    WAITING(0, "Чек создан, находится в ожидании подтверждения оплаты"),
    CANCELLED(50, "Платеж отменен"),
    REVERSED(21, "Отмена дебетовых операций"),
    PAUSED(20, "Чек приостановлен для ручного управления"),
    ONLINE_CONFIRM(3, "Подтверждение транзакции в приложении поставщика"),
    DEBITING(2, "Проведение дебетовых операций, перевод денежных средств с карты в счет получателей"),
    BLOCKED(1, "Чек заблокирован для оплаты, проводятся первоначальные проверки"),
    UNKNOWN(99, "Неизвестная ошибка");

    private Integer code;
    private String description;

    UbStatusType(final Integer code, final String description) {
        this.code = code;
        this.description = description;
    }

    public static UbStatusType getById(Integer code) {
        for (UbStatusType e : values()) {
            if (e.code.equals(code)) return e;
        }
        return UNKNOWN;
    }

    public Integer getCode() {
        return this.code;
    }

    public String getDescription() {
        return this.description;
    }

    @Override
    public String toString() {
        return code + ": " + description;
    }

}
