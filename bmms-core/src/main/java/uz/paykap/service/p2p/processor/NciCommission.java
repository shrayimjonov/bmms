package uz.paykap.service.p2p.processor;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class NciCommission extends NciParam {

    private BigDecimal percent = new BigDecimal("100");

}
