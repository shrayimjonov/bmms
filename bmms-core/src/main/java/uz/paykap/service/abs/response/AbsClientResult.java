package uz.paykap.service.abs.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AbsClientResult {

    private String errorCode;
    private String errorNote;
    private String checkRequestId;
    private String family;
    private String firstName;
    private String patronymic;
    private Timestamp birthday;
    private String birthPalace;
    private String codeCitizenship;
    private String codeAdrRegion;
    private String codeAdrDistr;
    private String codeResident;
    private String codeCountry;
    private String codeGender;
    private String codeNation;
    private String postAddress;
    private String typeDocument;
    private String passportSerial;
    private String passportNumber;
    private String passportPlaceRegistration;
    private String codePlaceRegist;
    private Timestamp passportDateRegistration;
    private Timestamp passportDateExpiration;
    private String numberTaxRegistration;
    private String phoneHome;
    private String phoneMobile;
    private String emailAddress;
    private String pinFl;
    private String typeBlack;
    private String blackCode;
    private String blackNote;
    private String transId;
}
