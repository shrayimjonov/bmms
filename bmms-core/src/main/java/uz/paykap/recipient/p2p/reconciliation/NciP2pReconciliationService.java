package uz.paykap.recipient.p2p.reconciliation;

import uz.javlon.commons.base.BasicResult;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.service.reconciliation.p2p.AbstractP2pRecoService;

import java.time.Instant;

public class NciP2pReconciliationService extends AbstractP2pRecoService {

    @Override
    public P2pProcessorType getProcessorType() {
        return P2pProcessorType.NCI;
    }

    @Override
    public BasicResult reconcile(Instant var1, Instant var2) {
        throw new CodedException(ProblemEnum.NOT_IMPLEMENTED);
    }
}
