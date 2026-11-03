package uz.paykap.gateway.common.simulation;

public class SimulationResult {
    private SimulationRequest request;
    private SimulationResponse response;

    public SimulationRequest getRequest() {
        return request;
    }

    public void setRequest(SimulationRequest request) {
        this.request = request;
    }

    public SimulationResponse getResponse() {
        return response;
    }

    public void setResponse(SimulationResponse response) {
        this.response = response;
    }
}
