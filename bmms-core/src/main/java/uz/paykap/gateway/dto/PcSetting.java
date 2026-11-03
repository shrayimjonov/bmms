package uz.paykap.gateway.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PcSetting implements Serializable {
    private String eposMerchant;
    private String eposTerminal;

    public PcSetting withEposMerchant(String eposMerchant) {
        this.eposMerchant = eposMerchant;
        return this;
    }

    public PcSetting withEposTerminal(String eposTerminal) {
        this.eposTerminal = eposTerminal;
        return this;
    }
}
