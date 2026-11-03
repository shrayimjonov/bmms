package uz.paykap.service.abs.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.devops.bmcore.enumeration.TransactionStatus;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AbsTransactionInfo {
    private String errorCode;
    private String errorNote;
    private String transId;
    private String branch;
    private String docNum;
    private String dateTime;
    private String bankCl;
    private String accCl;
    private String nameCl;
    private String bankCo;
    private String accCo;
    private String nameCo;
    private String purpose;
    private String summa;
    private String currency;
    private String typeDoc;
    private String sDealId;
    private String vDate;
    private String state;
    private String purposeId;
    private TransactionStatus transactionStatus;
    private Map<String, Object> jsonText;
}
