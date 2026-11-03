package uz.paykap.service.abs.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AbsCoursesResult {

    private String errorCode;
    private String errorNote;
    private String transId;
    private String baseCurCode;
    private String baseCurIsoCode;
    private String branchExtid;
    @ApiModelProperty(value = "860")
    private String curCode;
    @ApiModelProperty(value = "UZS")
    private String curIsoCode;
    private String endDate;
    private String rateBuy;
    private String rateDate;
    private String rateSell;
    private String scale;
    private String nameCurrency;
}
