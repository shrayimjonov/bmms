package uz.paykap.service.p2p.dto;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;

@Data
public class P2pFidoForexDTO implements P2pDTO {

    @NotNull
    private String pan;
    private String exp;
    private String comment;
    private String name;
    private String clientId;
    private Integer mobileCourse;

    @Override
    public String getAccount() {
        return pan;
    }

    @Override
    public String getCondition() {
        return pan;
    }

    private SenderAdditionalParam additionalParam;

    @Override
    public SenderAdditionalParam getAdditionalParam() {
        return this.additionalParam;
    }

    @Override
    public String getAccountBin() {
        return StringUtils.substring(getCondition(), 0, 8);
    }
}
