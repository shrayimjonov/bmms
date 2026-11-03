package uz.paykap.gateway.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UzcardSetting extends PcSetting {
    public UzcardSetting withEposMerchant(String eposMerchant) {
        super.setEposMerchant(eposMerchant);
        return this;
    }

    public UzcardSetting withEposTerminal(String eposTerminal) {
        this.setEposTerminal(eposTerminal);
        return this;
    }
}
