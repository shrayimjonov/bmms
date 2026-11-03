package uz.paykap.service.abs.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AbsClientRequest {
    private String transId;
    private String family;
    private String firstName;
    private String patronymic;
    private LocalDate birthday;
    private String birthPlace;
    private String codeCitizenship;
    private String codeAdrRegion;
    private String codeAdrDistr;
    private String codeResident;
    private String codeCountry;
    private String codeGender;
    private String codeNation;
    private String postAddress;
    private String passportSerial;
    private String passportNumber;
    private String passportPlaceRegistration;
    private String codePlaceRegist;
    private LocalDate passportDateRegistration;
    private LocalDate passportDateExpiration;
    private String numberTaxRegistration;
    private String phoneHome;
    private String phoneMobile;
    private String emailAddress;
    private String pinFl;
    private Long requestId;
}
