package uz.paykap.gateway.common.simulation;

import java.util.Map;

public class SimulationRequest {
    private String url;
    private String body;
    private Map<String, String> headers;
    private SimulationValidate validate;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public SimulationValidate getValidate() {
        return validate;
    }

    public void setValidate(SimulationValidate validate) {
        this.validate = validate;
    }
}
