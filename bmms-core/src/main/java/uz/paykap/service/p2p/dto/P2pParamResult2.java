package uz.paykap.service.p2p.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.paykap.domain.enumeration.P2pProcessorType;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class P2pParamResult2 {

    Long priority;
    // sender
    P2pProcessorType sender_pc;
    String senderCondition;
    String senderCommissionName;
    BigDecimal senderCommissionDown;
    BigDecimal senderCommissionUp;
    BigDecimal senderCommissionFee;
    // recipient
    P2pProcessorType recipient_pc;
    String recipientCondition;
    String recipientCommissionName;
    BigDecimal recipientCommissionDown;
    BigDecimal recipientCommissionUp;
    BigDecimal recipientCommissionFee;
    // common
    String value;

}
