package uz.paykap.adapter.verification.handler;

import uz.paykap.gateway.dto.ReconcileGatewayDTO;

import java.time.Instant;

public interface GatewayTransactionsHandler {
    ReconcileGatewayDTO getGatewayTransactions(Instant fromTime, Instant toTime);
}
