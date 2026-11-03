package uz.paykap.domain.enumeration;

/**
 * The TransactionStatus enumeration.
 */
public enum UbRegisterType {
    ACTIVE(0, "Карта активна, подключено смс уведомление"),
    SMS_INFO_OFF(1, "Не подключено смс уведомление"),
    EXPIRED_CARD(2, "Карта просрочена"),
    TEMPORARILY_BLOCKED(3, "Карта временно заблокирована. Для использования карту необходимо разблокировать"),
    BLOCKED(4, "Карта заблокирована"),
    LOST(5, "Карта утеряна"),
    STOLEN(6, "Карта украдена"),
    STOP_LIST(7, "Карта в стоп листе "),
    ATTEMPTS_EXCEEDED(8, "Превышено количество попыток ввода PIN"),
    PIN_RESET(9, "PIN Сброшен. Необходимо установить PIN"),
    PIN_NOT_SET(10, "PIN не установлен. Необходимо установить PIN"),
    NOT_PERSONALIZED(11, "Карта не персонифицированная"),
    UNKNOWN(99, "Неизвестная ошибка карты");

    private Integer code;
    private String description;

    UbRegisterType(final Integer code, final String description) {
        this.code = code;
        this.description = description;
    }

    public static UbRegisterType getById(Integer code) {
        for (UbRegisterType e : values()) {
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
