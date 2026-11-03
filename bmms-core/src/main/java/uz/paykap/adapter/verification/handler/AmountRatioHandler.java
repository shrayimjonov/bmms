package uz.paykap.adapter.verification.handler;

public interface AmountRatioHandler {
//    Integer getAmountRatio();

    default Integer getAmountRatio() {
        return 100;
    }
}
