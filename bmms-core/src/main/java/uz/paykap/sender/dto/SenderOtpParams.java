package uz.paykap.sender.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SenderOtpParams {

    private String maskedCardInfo;
    private String phone;
}
