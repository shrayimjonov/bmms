package uz.paykap.service.reconciliation.p2p;

import org.springframework.beans.factory.annotation.Autowired;
import uz.javlon.commons.base.BasicResult;
import uz.paykap.domain.enumeration.P2pProcessorType;

import javax.validation.constraints.NotNull;
import java.time.Instant;

public abstract class AbstractP2pRecoService {

    @Autowired
    private P2pRecoHelperService p2pRecoHelperService;

    public AbstractP2pRecoService() {
    }

    public abstract P2pProcessorType getProcessorType();

    public abstract BasicResult reconcile(@NotNull Instant var1, @NotNull Instant var2);

    public P2pRecoHelperService getRecoHelperService() {
        return this.p2pRecoHelperService;
    }
}
