package uz.paykap.gateway.dto;

import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.HashSet;

public class ReconcileGatewayDTO implements Serializable {

    @ApiModelProperty(value = "Список данных о транзакциях на стороне поставщика")
    private HashSet<GatewayTranDTO> gatewayTranDTOS;

    @ApiModelProperty(value = "Общая сумма")
    private Long totalAmountTiyin;

    @ApiModelProperty(value = "Общее число транзакций")
    private Long totalCount;

    public Long getTotalAmountTiyin() {
        return totalAmountTiyin;
    }

    public void setTotalAmountTiyin(Long totalAmountTiyin) {
        this.totalAmountTiyin = totalAmountTiyin;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public void addAmount(Long amount) {
        this.totalAmountTiyin += amount;
    }

    public void addCount(Long aLong) {
        this.totalCount += aLong;
    }

    public HashSet<GatewayTranDTO> getGatewayTranDTOS() {
        return gatewayTranDTOS;
    }

    public void setGatewayTranDTOS(HashSet<GatewayTranDTO> gatewayTranDTOS) {
        this.gatewayTranDTOS = gatewayTranDTOS;
    }

    public ReconcileGatewayDTO() {
    }

    public ReconcileGatewayDTO(HashSet<GatewayTranDTO> gatewayTranDTOS, Long totalAmountTiyin, Long totalCount) {
        this.gatewayTranDTOS = gatewayTranDTOS;
        this.totalAmountTiyin = totalAmountTiyin;
        this.totalCount = totalCount;
    }

    public ReconcileGatewayDTO(HashSet<GatewayTranDTO> gatewayTranDTOS) {
        this.gatewayTranDTOS = gatewayTranDTOS;
    }

    @Override
    public String toString() {
        return "ReconcileGatewayDTO{" +
            "gatewayTranDTOS=" + gatewayTranDTOS +
            ", totalAmountTiyin=" + totalAmountTiyin +
            ", totalCount=" + totalCount +
            '}';
    }


}
