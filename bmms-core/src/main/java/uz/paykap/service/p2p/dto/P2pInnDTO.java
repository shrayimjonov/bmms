package uz.paykap.service.p2p.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.Data;

@Data
public class P2pInnDTO implements P2pDTO {

    @NotNull
    @Pattern(regexp = "\\d{20}")
    private String inn;

    @Override
    public String getAccount() {
        return inn;
    }

    private SenderAdditionalParam additionalParam;

    @Override
    public SenderAdditionalParam getAdditionalParam() {
        return this.additionalParam;
    }

    @Override
    public String getAccountBin() {
        return inn;
    }
}
