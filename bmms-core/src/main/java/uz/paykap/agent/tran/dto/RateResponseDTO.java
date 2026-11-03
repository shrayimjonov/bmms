package uz.paykap.agent.tran.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RateResponseDTO {

    @ApiModelProperty(value = "")
    private String currencyName;

    @ApiModelProperty(value = "")
    private String currencyISO;

    @ApiModelProperty(value = "")
    private String currencyCBU;

    @ApiModelProperty(value = "")
    private String centralBankRate;

    @ApiModelProperty(value = "")
    private String exOfficeSellRate;

    @ApiModelProperty(value = "")
    private String exOfficePurchaseRate;

    @ApiModelProperty(value = "")
    private String atmSellRate;

    @ApiModelProperty(value = "")
    private String atmPurchaseRate;

    @ApiModelProperty(value = "")
    private Long mobileSellRate;

    @ApiModelProperty(value = "")
    private Long mobilePurchaseRate;
}
