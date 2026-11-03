//package uz.paykap.gateway.dto;
//
//import uz.devops.bmcore.dto.request.CommonPayRequest;
//import uz.devops.bmcore.dto.GatewayQueryResult;
//
////TODO - move common fields with SharedCheckDTO to base class
//public class SharedPayDTO {
//
//    private String login;
//    private GatewayQueryResult recipientParams;
//    private Long tranId;
//    private CommonPayRequest payDTO;
//    private Double gatewayAmount;
//
//    public Long getTranId() {
//        return tranId;
//    }
//
//    public void setTranId(Long tranId) {
//        this.tranId = tranId;
//    }
//
//    public CommonPayRequest getPayDTO() {
//        return payDTO;
//    }
//
//    public void setPayDTO(CommonPayRequest payDTO) {
//        this.payDTO = payDTO;
//    }
//
//    public Double getGatewayAmount() {
//        return gatewayAmount;
//    }
//
//    public String getLogin() {
//        return login;
//    }
//
//    public void setLogin(String login) {
//        this.login = login;
//    }
//
//    public GatewayQueryResult getRecipientParams() {
//        return recipientParams;
//    }
//
//    public void setRecipientParams(GatewayQueryResult recipientParams) {
//        this.recipientParams = recipientParams;
//    }
//
//    public void setGatewayAmount(Double gatewayAmount) {
//        this.gatewayAmount = gatewayAmount;
//    }
//
//    public SharedPayDTO tranId(Long tranId) {
//        this.tranId = tranId;
//        return this;
//    }
//
//    public SharedPayDTO payDTO(CommonPayRequest payDTO) {
//        this.payDTO = payDTO;
//        return this;
//    }
//
//    public SharedPayDTO gatewayAmount(Double gatewayAmount) {
//        this.gatewayAmount = gatewayAmount;
//        return this;
//    }
//
//    public SharedPayDTO login(String login) {
//        this.login = login;
//        return this;
//    }
//
//    public SharedPayDTO recipientParams(GatewayQueryResult recipientParams) {
//        this.recipientParams = recipientParams;
//        return this;
//    }
//}
