package uz.paykap.service.p2p.processor;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class NciForexParam implements ProcessorParam {

    @NotNull
    private String bankAccount;

    private Integer fxDeal;

    private String purpose;

    private String codeFilial;
    private String con;
    private String rollbackIs;
    private String isMasked;
}
