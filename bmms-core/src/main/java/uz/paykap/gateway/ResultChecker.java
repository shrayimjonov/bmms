package uz.paykap.gateway;

import uz.paykap.domain.Bill;

public interface ResultChecker<RESPONSE> {
    boolean success(RESPONSE response);

    default void beforeSaveBillResult(Bill bill, RESPONSE result) {

    }
}
