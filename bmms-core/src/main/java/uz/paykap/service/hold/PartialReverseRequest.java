package uz.paykap.service.hold;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartialReverseRequest {
    private Long billId;
    private Long reverseAmount;
}
