package uz.paykap.gateway.common.simulation;

public class SimulationBase {
    private SimulationMethod check;
    private SimulationMethod pay;
    private SimulationMethod reconcile;
    private SimulationMethod cancel;

    public SimulationMethod getCancel() {
        return cancel;
    }

    public void setCancel(SimulationMethod cancel) {
        this.cancel = cancel;
    }

    public SimulationMethod getReconcile() {
        return reconcile;
    }

    public void setReconcile(SimulationMethod reconcile) {
        this.reconcile = reconcile;
    }

    public SimulationMethod getCheck() {
        return check;
    }

    public void setCheck(SimulationMethod check) {
        this.check = check;
    }

    public SimulationMethod getPay() {
        return pay;
    }

    public void setPay(SimulationMethod pay) {
        this.pay = pay;
    }
}
