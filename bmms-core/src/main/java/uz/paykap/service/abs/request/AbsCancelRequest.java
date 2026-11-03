package uz.paykap.service.abs.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AbsCancelRequest {
    @NotNull
    private Long transId;
    @NotNull
    private String requestId;

    private Integer groupId;
}
