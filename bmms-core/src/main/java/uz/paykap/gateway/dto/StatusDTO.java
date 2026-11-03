package uz.paykap.gateway.dto;

import java.io.Serializable;

public class StatusDTO extends CommonDTO implements Serializable {

    private String requestGroup;

    private String requestSubGroup;

    public String getRequestGroup() {
        return requestGroup;
    }

    public void setRequestGroup(String requestGroup) {
        this.requestGroup = requestGroup;
    }

    public String getRequestSubGroup() {
        return requestSubGroup;
    }

    public void setRequestSubGroup(String requestSubGroup) {
        this.requestSubGroup = requestSubGroup;
    }

    @Override
    public String toString() {
        return "StatusDTO{" +
            "requestGroup='" + requestGroup + '\'' +
            ", requestSubGroup='" + requestSubGroup + '\'' +
            "} " + super.toString();
    }


    public StatusDTO requestGroup(String requestGroup) {
        this.requestGroup = requestGroup;
        return this;
    }

    public StatusDTO requestSubGroup(String requestSubGroup) {
        this.requestSubGroup = requestSubGroup;
        return this;
    }
}
