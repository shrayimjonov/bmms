package uz.paykap.gateway.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class HumoSetting extends PcSetting {

    public HumoSetting withEposMerchant(String eposMerchant) {
        super.setEposMerchant(eposMerchant);
        return this;
    }

    public HumoSetting withEposTerminal(String eposTerminal) {
        this.setEposTerminal(eposTerminal);
        return this;
    }

}
