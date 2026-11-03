package uz.paykap.service.p2p.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class P2pTokenDTO implements P2pDTO {

    @NotNull
    @NotBlank
    private String token;

    @NotBlank
    @Pattern(regexp = "(\\d{6}|\\d{8})")
    private String bin;

    @Size(max = 128)
    private String name;

    private String comment;

    private String exp;

    @Override
    public String getAccount() {
        return token;
    }

    @Override
    public String getCondition() {
        return bin;
    }

    private SenderAdditionalParam additionalParam;

    @Override
    public SenderAdditionalParam getAdditionalParam() {
        return this.additionalParam;
    }

    @Override
    public String getAccountBin() {
        return bin;
    }
}
