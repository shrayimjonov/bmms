package uz.paykap.service.dto.p2p.calc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class P2pCalcParticipantDto {

    private Long commissionAmount;
    private BigDecimal commission;
    private Integer currency;

    public P2pCalcParticipantDto withCommission(BigDecimal commission) {
        this.commission = commission;
        return this;
    }

    public P2pCalcParticipantDto withCurrency(Integer currency) {
        this.currency = currency;
        return this;
    }


    public P2pCalcParticipantDto withCommissionAmount(Long commissionAmount) {
        this.commissionAmount = commissionAmount;
        return this;
    }
}
