package uz.paykap.recipient.p2p;

import uz.javlon.commons.result.HasData;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Transaction;

public interface P2pReverseService {
    HasData<Bill> reverse(Transaction transaction);
}
