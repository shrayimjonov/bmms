package uz.paykap.adapter.verification;

import uz.javlon.commons.base.Result;
import uz.paykap.adapter.verification.common.ReconcileDelegateService;
import uz.paykap.adapter.verification.handler.RawSystemTransactionsHandler;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.domain.Transaction;

import java.time.Instant;
import java.util.Set;

public abstract class ToFileReconcileServiceV2 extends ReconcileServiceV2 {


    private final RawSystemTransactionsHandler rawSystemTransactionsHandler;
    private final ApplicationProperties applicationProperties;

    ToFileReconcileServiceV2(String gatewayName, RawSystemTransactionsHandler rawSystemTransactionsHandler, ReconcileDelegateService reconcileDelegateService, ApplicationProperties applicationProperties) {
        super(gatewayName, reconcileDelegateService);
        this.rawSystemTransactionsHandler = rawSystemTransactionsHandler;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public Result reconcile(Instant fromTime, Instant toTime) {

        String filePath = writeToFile(rawSystemTransactionsHandler.getRawTransactions(fromTime, toTime), getGatewayName(), applicationProperties.getPrefixFileName(), fromTime, null);

        return sendToMail(filePath, getGatewayName(), fromTime);
    }

    abstract String writeToFile(Set<Transaction> transactions, String gateway, String login, Instant fromTime, Instant toTime);

    abstract Result sendToMail(String filePath, String gatewayName, Instant fromTime);
}
