package uz.paykap.service.p2p.processor;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class HumoParam implements HasEposParam {
    @NotNull
    private String merchantId;
    @NotNull
    private String terminalId;

    private Integer switchingId;

    private String language;
}
