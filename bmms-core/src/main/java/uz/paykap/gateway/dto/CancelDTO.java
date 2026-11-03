package uz.paykap.gateway.dto;

import java.io.Serializable;

public class CancelDTO extends CommonDTO implements Serializable {
      private Long tranId;

    public Long getTranId() {
        return tranId;
    }

    public void setTranId(Long tranId) {
        this.tranId = tranId;
    }

    @Override
    public String toString() {
        return "CancelDTO{" +
            "tranId=" + tranId +
            "} " + super.toString();
    }
}
