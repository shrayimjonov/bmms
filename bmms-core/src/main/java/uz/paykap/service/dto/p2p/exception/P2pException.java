package uz.paykap.service.dto.p2p.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.paykap.service.dto.p2p.BmResultStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class P2pException extends RuntimeException {
    private BmResultStatus status;
    private String details;
}
