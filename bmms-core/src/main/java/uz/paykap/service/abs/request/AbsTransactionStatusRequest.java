package uz.paykap.service.abs.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AbsTransactionStatusRequest {
    @NotNull
    private String checkStatusId;
    @NotNull
    private String requestId;
}
