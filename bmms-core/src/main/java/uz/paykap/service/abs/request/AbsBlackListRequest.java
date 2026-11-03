package uz.paykap.service.abs.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AbsBlackListRequest {
    private String requestId;
    @ApiModelProperty("Имя")
    private String transId;
    @ApiModelProperty("Фамилия")
    private String family;
    @ApiModelProperty("Имя")
    private String firstName;
    @ApiModelProperty("Отчество")
    private String patronymic;
    @ApiModelProperty("Дата рождения. Например: 2000-01-20")
    private LocalDate birthday;
    @ApiModelProperty("Место рождения. Например: Tashkent")
    private String birthPlace;
    @ApiModelProperty("Гражданство")
    private String codeCitizenship;
    @ApiModelProperty("Область проживания")
    private String codeAdrRegion;
    @ApiModelProperty("Район проживания")
    private String codeAdrDistr;
    @ApiModelProperty("Резидент. Например: 1")
    private String codeResident;
    @ApiModelProperty("Страна. Например: 860 -для Узбекистан")
    private String codeCountry;
    @ApiModelProperty("Пол: Например: 1 -мужское пол")
    private String codeGender;
    @ApiModelProperty("Национальность. Например: 01")
    private String codeNation;
    @ApiModelProperty("Адрес. Например: Tashkent, Sag'bon 18")
    private String postAddress;
    @ApiModelProperty("Тип документа")
    private String typeDocument;
    @ApiModelProperty("Серия паспорта")
    private String passportSerial;
    @ApiModelProperty("Номер паспорта")
    private String passportNumber;
    @ApiModelProperty("Место регистрация паспорта. Например: Tashkent Mirobod IIB")
    private String passportPlaceRegistration;
    @ApiModelProperty("Код место регистрация.")
    private String codePlaceRegist;
    @ApiModelProperty("Дата регистрация паспорта. Например: 2020-01-25")
    private LocalDate passportDateRegistration;
    @ApiModelProperty("Дата истечения паспорта: Например: 2020-01-25")
    private LocalDate passportDateExpiration;
    @ApiModelProperty("ИНН")
    private String numberTaxRegistration;
    @ApiModelProperty("Домашний номер телефона")
    private String phoneHome;
    @ApiModelProperty("Мобильный номер телефона")
    private String phoneMobile;
    @ApiModelProperty("Емайл")
    private String emailAddress;
    @ApiModelProperty("Пин-ФЛ")
    private String pinFl;
    @ApiModelProperty("Тип клиента черный")
    private String typeBlack;
    @ApiModelProperty("Черный код типа клиента")
    private String blackCode;
}
