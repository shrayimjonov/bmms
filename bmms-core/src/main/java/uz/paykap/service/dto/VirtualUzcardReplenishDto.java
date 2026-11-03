package uz.paykap.service.dto;


import lombok.*;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class VirtualUzcardReplenishDto {

    private Boolean enabled = false;
    private String virtualCardToken;
    private Long periodMinutes = 180L;
    private String merchantId;
    private String terminalId;

    @Override
    public int hashCode() {
        return Objects.hashCode(virtualCardToken);
    }
}
