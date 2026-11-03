package uz.paykap.service.dto;


import lombok.*;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UzcardVirtualAccount {
    private String terminalId;

    private String merchantId;

    private String token;

    @Override
    public int hashCode() {
        return Objects.hashCode(token);
    }
}
