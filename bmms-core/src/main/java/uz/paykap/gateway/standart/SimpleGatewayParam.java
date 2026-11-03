package uz.paykap.gateway.standart;

import uz.devops.bmcore.enumeration.RequestType;
import uz.paykap.domain.enumeration.ModeType;
import uz.paykap.domain.enumeration.ProtocolType;

import javax.validation.constraints.NotNull;
import java.util.Map;

public class SimpleGatewayParam {

    @NotNull
    private String recipientName;

    @NotNull
    private ProtocolType protocolType;

    @NotNull
    private String username;

    @NotNull
    private String password;

    private Long minAmount = 5000L;

    private Long maxAmount = 5000000L;

    private ModeType modeType;

    @NotNull
    private Integer timeout;

    @NotNull
    private Map<RequestType, String> urlMap;

    @Override
    public String toString() {
        return "SimpleGatewayParam{" +
            "recipientName='" + recipientName + '\'' +
            ", protocolType=" + protocolType +
            ", username='" + username + '\'' +
            ", password=''\'' +
            ", minAmount=" + minAmount +
            ", maxAmount=" + maxAmount +
            ", modeType=" + modeType +
            ", timeout=" + timeout +
            ", urlMap=" + urlMap +
            '}';
    }

    public ModeType getModeType() {
        return modeType;
    }

    public void setModeType(ModeType modeType) {
        this.modeType = modeType;
    }

    public Map<RequestType, String> getUrlMap() {
        return urlMap;
    }

    public void setUrlMap(Map<RequestType, String> urlMap) {
        this.urlMap = urlMap;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }


    public ProtocolType getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(ProtocolType protocolType) {
        this.protocolType = protocolType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(Long minAmount) {
        this.minAmount = minAmount;
    }

    public Long getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(Long maxAmount) {
        this.maxAmount = maxAmount;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

}
