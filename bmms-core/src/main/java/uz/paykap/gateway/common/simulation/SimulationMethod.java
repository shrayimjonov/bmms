package uz.paykap.gateway.common.simulation;

public class SimulationMethod {
    private String account;
    private Long amount;
    private SimulationResult success;
    private SimulationResult fail;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public SimulationResult getSuccess() {
        return success;
    }

    public void setSuccess(SimulationResult success) {
        this.success = success;
    }

    public SimulationResult getFail() {
        return fail;
    }

    public void setFail(SimulationResult fail) {
        this.fail = fail;
    }
}
