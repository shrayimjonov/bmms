package uz.paykap.domain.enumeration;

import java.io.Serializable;

/**
 * The Currency enumeration.
 */
public enum Currency implements Serializable {
    USD("840"),
    UZS("860"),
    EUR("978"),
    RUB("643");

    private String code;

    Currency(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static Currency get(Integer code) {

        if (code == null) {
            return UZS;
        }

        switch (code) {
            case 840:
                return USD;
//            case 810:
//                return RUB;
            case 643:
                return RUB;
            case 860:
                return UZS;
            case 978:
                return EUR;
            default:
                return UZS;
        }
    }
}
