package uz.paykap.service.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@Setter
@ToString
public class P2pRequest implements Serializable {
    @NotNull
    private String fromPan;

    private String fromExpiry;
    @NotNull
    private String toPan;
    @NotNull
    private Long amount;
    @NotNull
    private String merchantId;
    @NotNull
    private String terminalId;

    private Boolean fakeFromPan = true;

    private Boolean fakeToPan = true;
}
