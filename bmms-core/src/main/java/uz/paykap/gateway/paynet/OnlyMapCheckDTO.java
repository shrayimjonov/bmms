package uz.paykap.gateway.paynet;

import javax.validation.constraints.NotEmpty;
import java.util.Map;

public class OnlyMapCheckDTO {
    @NotEmpty
    private Map<String, String> params;

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return "SimpleCheckDTO{" +
            ", params=" + params +
            '}';
    }
}
