//package uz.paykap.gateway.dto;
//
//import uz.devops.bmcore.dto.request.CommonCheckRequest;
//
//import javax.validation.constraints.NotNull;
//
//public class CommonCheckDTO extends CheckRequest {
//
//    public CommonCheckDTO(CommonCheckRequest simpleCheckDTO, String accountParamName, String amountParamName) {
//        super.setParams(simpleCheckDTO.getParams());
//        super.setRecipientId(simpleCheckDTO.getRecipientId());
//        this.account = simpleCheckDTO.getParams().getOrDefault(accountParamName, "");
//        if (simpleCheckDTO.getParams().containsKey(amountParamName)) {
//            this.amount = Long.valueOf(simpleCheckDTO.getParams().get(amountParamName));
//        }
//    }
//
//    @NotNull
//    private String account;
//    @NotNull
//    private Long amount;
//
//    public String getAccount() {
//        return account;
//    }
//
//    public void setAccount(String account) {
//        this.account = account;
//    }
//
//    public Long getAmount() {
//        return amount;
//    }
//
//    public void setAmount(Long amount) {
//        this.amount = amount;
//    }
//
//    public CommonCheckDTO account(String account) {
//        this.account = account;
//        return this;
//    }
//
//    public CommonCheckDTO amount(Long amount) {
//        this.amount = amount;
//        return this;
//    }
//
//    @Override
//    public String toString() {
//        return "CommonCheckDTO{" +
//            "account='" + account + '\'' +
//            ", amount=" + amount +
//            "} " + super.toString();
//    }
//}
