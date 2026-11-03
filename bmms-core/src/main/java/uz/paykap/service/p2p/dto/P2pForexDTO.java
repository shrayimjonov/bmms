package uz.paykap.service.p2p.dto;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class P2pForexDTO implements P2pDTO {

    @NotNull
//    @Pattern(regexp = "\\d{20}")
    private String account;

    @Pattern(regexp = "\\d{5}")
    private String mfo;

    private String comment;

    private Long fromSum;

    private Long toSum;

    private Double cource;

    private Integer fxDeal;
    private String codeFilial;
    private String con;
    private String dateExp;
    private String clientId;
    private String rollbackIs;
    private String isMasked;

    @Override
    public String getCondition() {
        if (mfo == null || mfo.isEmpty()) {
            return account;
        }

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
