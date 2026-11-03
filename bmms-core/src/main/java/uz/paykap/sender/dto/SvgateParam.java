package uz.paykap.sender.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.javlon.svgate.dto.enumeration.PurposeType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SvgateParam {
    private PurposeType paymentPurpose;
}
