package uz.paykap.service.p2p.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class P2pAnorDTO implements P2pDTO {

    @NotNull
    @Pattern(regexp = "\\d{20}")
    private String account;

    @NotNull
    @Pattern(regexp = "\\d{5}")
    private String mfo;

    @NotNull
    @Pattern(regexp = "[A-Z ']{1,128}")
    private String name;

    @NotNull
    @Pattern(regexp = "\\d{5}")
    private String purposeId;

    @NotNull
    @JsonAlias("comment")
    private String purpose;

    @NotNull
    @Pattern(regexp = "\\d{9}")
    private String inn;

    @Override
    public String getCondition() {
        return mfo + account;
    }

    private SenderAdditionalParam additionalParam;

    @Override
    public SenderAdditionalParam getAdditionalParam() {
        return this.additionalParam;
    }

    @Override
    public String getAccountBin() {
        return StringUtils.substring(getCondition(), 0, 13);
    }
}
