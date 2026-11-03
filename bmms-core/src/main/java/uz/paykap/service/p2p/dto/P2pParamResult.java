package uz.paykap.service.p2p.dto;

import uz.paykap.domain.enumeration.P2pProcessorType;

import java.math.BigDecimal;

public interface P2pParamResult {

    Long getPriority();

    P2pProcessorType getSenderPc();

    String getSenderCondition();

    String getSenderCommissionName();

    @Deprecated
    BigDecimal getSenderCommissionDown();

    BigDecimal getSenderCommissionUp();

    @Deprecated
    BigDecimal getSenderCommissionFee();

    P2pProcessorType getRecipientPc();

    String getRecipientCondition();

    String getRecipientCommissionName();

    @Deprecated
    BigDecimal getRecipientCommissionUp();

    BigDecimal getRecipientCommissionDown();

    @Deprecated
    BigDecimal getRecipientCommissionFee();

    String getValue();

    String getName();

    String getPromo();

    default String print() {
        return "P2P param : "
                + "\n\tname : " + getName()
                + "\n\tpromo : " + getPromo()
                + "\n\tpriority : " + getPriority()
                + "\n\tsender : " + getSenderPc() + "\n\t\tcondition : " + getSenderCondition() + "\n\t\tcommission : " + getSenderCommissionName()
                + "\n\trecipient : " + getRecipientPc() + "\n\t\tcondition : " + getRecipientCondition() + "\n\t\tcommission : " + getRecipientCommissionName()
                + "\n\tvalue : " + getValue()
                + "\n"
                //
                ;
    }

}
