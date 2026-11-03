package uz.paykap.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbsParams {
    private String account;
    private String mfo;
    private String name;
    private String purpose;
    private String purposeId;
    private String tranType;
    private String tax;
}
