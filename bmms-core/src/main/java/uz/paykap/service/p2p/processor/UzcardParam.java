package uz.paykap.service.p2p.processor;

import lombok.Data;
import uz.javlon.svgate.dto.enumeration.PurposeType;
import uz.paykap.service.uzcard.UzcardCreditService;
import uz.paykap.utils.PcUtil;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class UzcardParam implements HasEposParam {

    @NotNull
    private String merchantId;

    @NotNull
    private String terminalId;

    private PurposeType paymentPurpose;

    private String port;

    @Pattern(regexp = PcUtil.UZCARD_TOKEN_REGEXP)
    private String virtualCardToken;

    private UzcardCreditService.UzcardCreditType creditType;
}
