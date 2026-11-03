package uz.paykap.domain.enumeration;

import uz.paykap.service.p2p.DepositParam;
import uz.paykap.service.p2p.processor.*;

/**
 * The P2pProcessorType enumeration.
 */
public enum P2pProcessorType {

    UZCARD(UzcardParam.class), HUMO(HumoParam.class),
    VISA(IsApiForexParam.class), NCI(NciParam.class),
    FOREX(NciForexParam.class), ANOR(AnorParam.class),
    DEPOSIT(DepositParam.class);

    private final Class<? extends ProcessorParam> paramClass;

    P2pProcessorType(Class<? extends ProcessorParam> paramClass) {
        this.paramClass = paramClass;
    }

    public Class<? extends ProcessorParam> getParamClass() {
        return paramClass;
    }
}
