package uz.paykap.service.p2p.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class P2pAccountDTO implements P2pDTO {

    @NotNull
    @Pattern(regexp = "\\d{20}")
    private String account;

    @NotNull
    @Pattern(regexp = "\\d{5}")
    private String mfo;

    @NotNull
    @Size(min = 1, max = 128)
    //    @Pattern(regexp = "[A-Z ']{1,128}")
    private String name;

    @JsonAlias("comment")
    private String purpose;

    @Pattern(regexp = "\\d{5}")
    private String purposeId;

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
