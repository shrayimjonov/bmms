package uz.paykap.adapter.verification.handler;

import uz.paykap.gateway.dto.GatewayTranDTO;

import java.time.Instant;
import java.util.HashSet;

//Separate into 2 classes
@Deprecated
public interface SystemTransactionsHandler {
    HashSet<GatewayTranDTO> getTransactions(Instant fromTime, Instant toTime);
}
