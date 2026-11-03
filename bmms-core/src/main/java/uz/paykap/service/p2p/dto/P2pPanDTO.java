package uz.paykap.service.p2p.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class P2pPanDTO implements P2pDTO {

    @NotNull
    @Pattern(regexp = "\\d{16}")
    private String pan;

    @Pattern(regexp = "\\d{4}")
    private String exp;

    @Size(max = 128)
    private String name;
    private String ccy;
    private String comment;


    @Override
    public String getCondition() {
        if (ccy != null) {
            return ccy + getAccount();
        }
        return getAccount();
    }

    @Override
    public String getAccount() {
        return pan;
    }

    private SenderAdditionalParam additionalParam;

    @Override
    public SenderAdditionalParam getAdditionalParam() {
        return this.additionalParam;
    }

    @Override
    public String getAccountBin() {
        return StringUtils.substring(pan, 0, 6);
    }
}
