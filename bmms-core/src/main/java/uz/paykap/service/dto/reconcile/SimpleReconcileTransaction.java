package uz.paykap.service.dto.reconcile;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Setter
@Getter
@ToString
@EqualsAndHashCode
public class SimpleReconcileTransaction {
    private String tranId;
    private Long amount;
    private String account;
    private Instant date;

    public SimpleReconcileTransaction() {
    }

    public SimpleReconcileTransaction(Long tranId, Long amount, String account, Instant date) {
        this.tranId = String.valueOf(tranId);
        this.amount = amount;
        this.account = account;
        this.date = date;
    }

    public SimpleReconcileTransaction(String tranId, Long amount, String account, Instant date) {
        this.tranId = tranId;
        this.amount = amount;
        this.account = account;
        this.date = date;
    }
}
