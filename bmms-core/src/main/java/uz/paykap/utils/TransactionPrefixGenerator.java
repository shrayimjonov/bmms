package uz.paykap.utils;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uz.paykap.config.ApplicationProperties;

import java.util.UUID;

@Component
@AllArgsConstructor
public class TransactionPrefixGenerator {

    private final ApplicationProperties applicationProperties;

    public String getP2pCreditPrefix(Long uniqueId) {
        return applicationProperties.getTransactionPrefix().getMainPrefix() + applicationProperties.getTransactionPrefix().getP2pCreditPrefix() + uniqueId;
    }

    public String getP2pPrefix(Long uniqueId) {
        return applicationProperties.getTransactionPrefix().getMainPrefix() + applicationProperties.getTransactionPrefix().getP2pPrefix() + uniqueId;
    }

    public String getP2pDebitPrefix(Long uniqueId) {
        return applicationProperties.getTransactionPrefix().getMainPrefix() + applicationProperties.getTransactionPrefix().getP2pDebitPrefix() + uniqueId;
    }

    public String getSenderDebitPrefix(Long uniqueId) {
        return applicationProperties.getTransactionPrefix().getMainPrefix() + applicationProperties.getTransactionPrefix().getSenderDebitPrefix() + uniqueId;
    }

    public String getSenderCreditPrefix(Long uniqueId) {
        return applicationProperties.getTransactionPrefix().getMainPrefix() + applicationProperties.getTransactionPrefix().getSenderCreditPrefix() + uniqueId;
    }

    public String getGatewayCreditPrefix(Long uniqueId) {
        return applicationProperties.getTransactionPrefix().getMainPrefix() + applicationProperties.getTransactionPrefix().getGatewayCreditPrefix() + uniqueId;
    }

    public String getGatewayDebitPrefix(Long uniqueId) {
        return applicationProperties.getTransactionPrefix().getMainPrefix() + applicationProperties.getTransactionPrefix().getGatewayDebitPrefix() + uniqueId;
    }

    public String getP2pCreditCommission(Long uniqueId) {
        return applicationProperties.getTransactionPrefix().getMainPrefix() + applicationProperties.getTransactionPrefix().getP2pCreditCommissionPrefix() + uniqueId;
    }

    public String getCancelTranSessionId() {
        return UUID.randomUUID().toString();
    }

    public String getCancelTranSessionId(Long uniqueId) {
        return UUID.randomUUID().toString();
    }

    public String getNciUniqueExtId() {
        return "bmms_" + UUID.randomUUID();
    }
}
