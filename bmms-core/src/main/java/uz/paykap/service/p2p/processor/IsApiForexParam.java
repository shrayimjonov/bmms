package uz.paykap.service.p2p.processor;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class IsApiForexParam implements ProcessorParam {

    @NotNull
    private String accCl;
    @NotNull
    private String accCo;
    @NotNull
    private String mfo;
    /*
    * ХУМОдан ВИЗАга конверсия буганда 62 сделка
    * ВИЗАдан ХУМОга обратный конверсия буганда 63 сделка
    * УЗКАДдан ВИЗАга конверсия буганда 64 сделка
    * ВИЗАдан УЗКАРДга обратный конверсия буганда 65 сделка
    *  */
    @NotNull
    private Integer fxDeal;

    private String purpose;
}
