package uz.paykap.service.p2p.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SenderAdditionalParam {

    private String citizen;
    private LocalDate birthDate;
    private String docType;
    private String docNumber;
    private LocalDate docDate;
    private String pinFl;
    private String firstName;
    private String lastName;
    private String middleName;
}
