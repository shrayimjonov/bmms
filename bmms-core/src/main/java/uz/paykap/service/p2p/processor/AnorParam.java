package uz.paykap.service.p2p.processor;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
public class AnorParam implements ProcessorParam {

    @NotNull
    private String bankAccount;

    @NotNull
    private String accName;

    @NotNull
    private String bankMfo;

//    @NotNull
//    private String purposeId;
//
//    @NotNull
//    private String purpose;

    private BigDecimal percent = new BigDecimal("100");

    private List<AnorParam> commissions;

    @Deprecated
    private AnorParam commission;

}
