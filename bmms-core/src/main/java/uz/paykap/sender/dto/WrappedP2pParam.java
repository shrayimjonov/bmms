package uz.paykap.sender.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.paykap.domain.Transaction;
import uz.paykap.service.p2p.dto.P2pAccountDTO;
import uz.paykap.service.p2p.dto.P2pDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WrappedP2pParam {

    @JsonUnwrapped(prefix = "t.")
    private Transaction transaction;

    @JsonUnwrapped(prefix = "s.")
    private P2pAccountDTO senderDto;

    @JsonUnwrapped(prefix = "r.")
    private P2pDTO recipientDto;

    private String senderNciId;

    private String username;
    private String agentTranId;

    private String commission;
    private String requestAmount;
}
