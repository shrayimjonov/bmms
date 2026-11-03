package uz.paykap.service.p2p.processor;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class EposParam implements HasEposParam {

    private String merchantId;

    private String terminalId;
}
