package uz.paykap.adapter.verification.dto;

public enum AmountRatio {
    TIYIN(100), SUM(1);

    private int ratio;

    AmountRatio(int ratio) {
        this.ratio = ratio;
    }

    public int getRatio() {
        return ratio;
    }
}
