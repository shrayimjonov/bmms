package uz.paykap.service.p2p.dto;

import lombok.Data;
import uz.paykap.security.SecurityUtils;

@Data
public class P2pDepositDTO implements P2pDTO {
    private String shortName;
    private String inn;
    private String pinfl;

    @Override
    public String getCondition() {
        if (inn != null && !inn.isEmpty()) {
            return inn;
        }
        if (pinfl != null && !pinfl.isEmpty()) {
            return pinfl;
        }
        if (shortName != null && !shortName.isEmpty()) {
            return shortName;
        }
        return SecurityUtils.getCurrentUserLogin().orElse("");
    }

    @Override
    public String getAccount() {
        if (inn != null && !inn.isEmpty()) {
            return inn;
        }
        if (pinfl != null && !pinfl.isEmpty()) {
            return pinfl;
        }
        return shortName;
    }

    private SenderAdditionalParam additionalParam;

    @Override
    public SenderAdditionalParam getAdditionalParam() {
        return this.additionalParam;
    }

    @Override
    public String getAccountBin() {
        return shortName;
    }
}
