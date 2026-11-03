package uz.paykap.service.abs.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * 1	 Наименование мерчанта	NAME
 * 2	 р/с мерчанта	ACCOUNT
 * 3	МФО банка мерчанта	CODE_BANK
 * 4	ИНН мерчанта	NUMBER_TAX_REGISTRATION
 * 5	Юр. адрес мерчанта	POST_ADDRESS
 * 6	Эл. почта	EMAIL
 * 7	Рабочий телефон	PHONE
 * 8	ФИО руководителя	DIRECTOR_NAME
 * 9	Телефон руководителя	-
 * 10	ФИО бухгалтера	CHIEF_ACCOUNTER_NAME
 * 11	Телефон бухгалтера	-
 * 12	Адрес торговой точки	-
 * 13	Дата заключения договора	-
 * 14	Дата расторжения договора	-
 * 15	Активный	???
 * 16	Статус мерчанта (CREATED, APPROVED, EXPIRED, CLOSED, DELETED)	STATE
 * 17	Мерчант ID в Humo ( agar bo'lsa )	-
 * 18	Вид деятельности мерчанта (agar bo'lsa)	CODE_SECTOR
 * 19	Вид реализуемых товаров мерчанта (agar bo'lsa)	-
 * 20	Количество торговых точек (agar bo'lsa)	-
 * 21	ID терминалы для подключения к сети мерчантов (agar bo'lsa)	-
 */

@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@Getter
@Setter
@ApiModel("Поиск юридического клиента по реквизитам")
public class AbsNciMerchantDTO {

    @ApiModelProperty("Код ощибки (успещный ответ = 0)")
    private String errorCode;
    @ApiModelProperty("Примечание об ошибке")
    private String errorNote;
    @ApiModelProperty("Уникальный идентификатор мерчанта")
    private String transId;
    @ApiModelProperty("ID клиента")
    private String codeCountry;
    private String codeType;
    private String codeResident;
    private String codeSubject;
    private String signRegistr;
    private String codeForm;
    private String dateOpen;
    private String dateClose;
    private String branch;
    private String id;
    @ApiModelProperty("Наименование мерчанта")
    private String name;
    private String shortName;
    private String dateRegistration;
    private String numberRegistrationDoc;
    private String codeTaxOrg;

    /**
     * * 4	ИНН мерчанта	NUMBER_TAX_REGISTRATION
     */
    @ApiModelProperty("ИНН мерчанта")
    private String inn;
    private String codeSector;

    /**
     * 18	Вид деятельности мерчанта (agar bo'lsa)	CODE_SECTOR
     */
    @ApiModelProperty("Вид деятельности мерчанта")
    private String activityName;
    private String codeOrganDirect;
    private String codeHeadOrganization;
    private String codeClassCredit;

    /**
     * * 8	ФИО руководителя	DIRECTOR_NAME
     */
    @ApiModelProperty("ФИО руководителя")
    private String directorFio;
    private String directorPassport;

    /**
     * 10	ФИО бухгалтера	CHIEF_ACCOUNTER_NAME
     */
    @ApiModelProperty("ФИО бухгалтера")
    private String chiefAccounterName;
    private String chiefAccounterPassport;

    /**
     * * 3	МФО банка мерчанта	CODE_BANK
     */
    @ApiModelProperty("МФО банка мерчанта")
    private String mfo;

    /**
     * рс мерчанта	ACCOUNT
     */
    @ApiModelProperty("р/с мерчанта")
    private String ban;

    /**
     * * 5	Юр. адрес мерчанта	POST_ADDRESS
     */
    @ApiModelProperty("Юр. адрес мерчанта")
    private String address;
    @ApiModelProperty("Рабочий телефон")
    private String phone;
    private String fax;
    @ApiModelProperty("Эл. почта ")
    private String email;
    private String signTrade;
    /**
     * 16	Статус мерчанта (CREATED, APPROVED, EXPIRED, CLOSED, DELETED)	STATE
     */
    @ApiModelProperty("Статус мерчанта (CREATED, APPROVED, EXPIRED, CLOSED, DELETED)")
    private String status;
    private String kodErr;
    private String fileName;
    private String placeRegistName;
    private String opf;
    private String soato;
    private String okpo;
    private String innHeadOrganization;
    private String region;
    private String distr;
    private String smallBusiness;
    private String patentExpiration;
    private String directorTypeDocument;
    private String directorPasspSerial;
    private String directorPasspNumber;
    private String directorPasspDateReg;
    private String directorPasspPlaceReg;
    private String directorPasspDateEnd;
    private String accountantTypeDocument;
    private String accountantPasspSerial;
    private String accountantPasspNumber;
    private String accountantPasspDateReg;
    private String accountantPasspPlaceReg;
    private String accountantPasspDateEnd;
    private String responsibleEmp;
    private String directorCodeCitizenship;
    private String directorBirthday;
    private String directorBirthPlace;
    private String directorAddress;
    private String accountantCodeCitizenship;
    private String accountantBirthday;
    private String accountantBirthPlace;
    private String accountantAddress;
    private String signVip;
    private String codeSectorOld;
    private String typeNonResident;
    private String signDepAcc;
    private String swiftId;
    private String typeActivity;
    private String directorFamily;
    private String directorFirstName;
    private String directorPatronymic;
    private String accountantFamily;
    private String accountantFirstName;
    private String accountantPatronymic;
    private String directorDrvPermitSer;
    private String directorDrvPermitNum;
    private String directorDrvPermitRegD;
    private String directorDrvPermitExpD;
    private String directorDrvPermitPlace;
    private String zipCode;
    private String contractNum;
    private String directorInn;
    private String directorPinfl;
    private String directorCodeResident;
    private String directorCodeGender;
    private String directorCodeRegion;
    private String directorCodeDistr;
    private String directorPhone;
    private String accountantInn;
    private String accountantPinfl;
    private String accountantCodeResident;
    private String accountantCodeGender;
    private String accountantCodeRegion;
    private String accountantCodeDistr;
    private String accountantPhone;

    private String accountantDrvPermitSer;
    private String accountantDrvPermitNum;
    private String accountantDrvPermitRegD;
    private String accountantDrvPermitExpD;
    private String accountantDrvPermitPlace;
    private String pinfl;
}
