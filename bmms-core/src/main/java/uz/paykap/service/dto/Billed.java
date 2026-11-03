package uz.paykap.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface Billed {
    @JsonIgnore
    Long getBmTranId();

    @JsonIgnore
    String getAccount();

    @JsonIgnore
    Long getAmount();

    @JsonIgnore
    Object requestJson();
}
