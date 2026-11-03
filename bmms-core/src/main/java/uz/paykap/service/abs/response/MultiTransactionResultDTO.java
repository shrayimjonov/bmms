package uz.paykap.service.abs.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@Getter
@Setter
@ApiModel(value = "Объект передачи данных транзакции")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MultiTransactionResultDTO {

    @ApiModelProperty(value = "")
    private String transId;

    @ApiModelProperty(value = "")
    private String id;

    @ApiModelProperty(value = "")
    private String branch;

    @ApiModelProperty(value = "")
    private String docNum;

    @ApiModelProperty(value = "")
    private String dDate;

    @ApiModelProperty(value = "")
    private String bankCl;

    @ApiModelProperty(value = "")
    private String accCl;

    @ApiModelProperty(value = "")
    private String nameCl;

    @ApiModelProperty(value = "")
    private String bankCo;

    @ApiModelProperty(value = "")
    private String accCo;

    @ApiModelProperty(value = "")
    private String nameCo;

    @ApiModelProperty(value = "")
    private String purpose;

    @ApiModelProperty(value = "")
    private String summa;

    @ApiModelProperty(value = "")
    private String currency;

    @ApiModelProperty(value = "")
    private String typeDoc;

    @ApiModelProperty(value = "")
    private String sDealId;

    @ApiModelProperty(value = "")
    private String vDate;

    @ApiModelProperty(value = "")
    private String pdc;

    @ApiModelProperty(value = "")
    private String cashCode;

    @ApiModelProperty(value = "")
    private String state;

    @ApiModelProperty(value = "")
    private String parentGroupId;

    @ApiModelProperty(value = "")
    private String parentId;

    @ApiModelProperty(value = "")
    private String childId;

    @ApiModelProperty(value = "")
    private String kodErr;

    @ApiModelProperty(value = "")
    private String fileName;

    @ApiModelProperty(value = "")
    private String errorGeneral;

    @ApiModelProperty(value = "")
    private String empId;

    @ApiModelProperty(value = "")
    private String idTransh;

    @ApiModelProperty(value = "")
    private String idTranshPurpose;

    @ApiModelProperty(value = "")
    private String valDate;

    @ApiModelProperty(value = "")
    private String batchTypeId;

}
