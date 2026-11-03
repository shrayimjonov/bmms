package uz.paykap.gateway.dto;

public class CommonDTO {

    // Actually, It is tranID
    private Long billId;

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    @Override
    public String toString() {
        return "CommonDTO{" +
            "billId=" + billId +
            '}';
    }

    public CommonDTO billId(Long billId) {
        this.billId = billId;
        return this;
    }
}
