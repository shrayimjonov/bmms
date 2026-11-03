//package uz.paykap.gateway.dto;
//
//import org.apache.commons.lang3.builder.ToStringBuilder;
//import uz.devops.bmcore.dto.request.CommonCheckRequest;
//import uz.devops.bmcore.dto.GatewayQueryResult;
//
//public class SharedCheckDTO {
//
//    private String login;
//    private GatewayQueryResult recipientParams;
//    private CommonCheckRequest checkDTO;
//    private Long tranId;
//    private Double gatewayAmount;
//
//    @Override
//    public String toString() {
//        return new ToStringBuilder(this)
//            .append("login", login)
//            .append("recipientParams", recipientParams)
//            .append("checkDTO", checkDTO)
//            .append("tranId", tranId)
//            .append("gatewayAmount", gatewayAmount)
//            .toString();
//    }
//
//    public String getLogin() {
//        return login;
//    }
//
//    public GatewayQueryResult getRecipientParams() {
//        return recipientParams;
//    }
//
//    public CommonCheckRequest getCheckDTO() {
//        return checkDTO;
//    }
//
//    public Long getTranId() {
//        return tranId;
//    }
//
//    public Double getGatewayAmount() {
//        return gatewayAmount;
//    }
//
//    public SharedCheckDTO login(String login) {
//        this.login = login;
//        return this;
//    }
//
//    public SharedCheckDTO recipientParams(GatewayQueryResult recipientParams) {
//        this.recipientParams = recipientParams;
//        return this;
//    }
//
//    public SharedCheckDTO checkDTO(CommonCheckRequest checkDTO) {
//        this.checkDTO = checkDTO;
//        return this;
//    }
//
//    public SharedCheckDTO tranId(Long tranId) {
//        this.tranId = tranId;
//        return this;
//    }
//
//    public SharedCheckDTO gatewayAmount(Double gatewayAmount) {
//        this.gatewayAmount = gatewayAmount;
//        return this;
//    }
//}
