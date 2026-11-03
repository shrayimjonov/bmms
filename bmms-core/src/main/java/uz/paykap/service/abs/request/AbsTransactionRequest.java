package uz.paykap.service.abs.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.devops.bmcore.enumeration.RequestType;
import uz.paykap.service.dto.Billed;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AbsTransactionRequest implements Billed {

    @NotNull
    @ApiModelProperty(name = "Abs sender account", notes = "accCl param in NCI", example = "19997000500001183021", required = true)
    private String accCl;

    @ApiModelProperty(name = "Abs sender account name", notes = "nameCl param in NCI", example = "Транзитный счет Узкард")
    private String nameCl;

    @NotNull
    @ApiModelProperty(name = "Abs sender bank MFO", notes = "bankCl param in NCI", example = "01183", required = true)
    private String bankCl;

    @NotNull
    @ApiModelProperty(name = "Abs recipient account", notes = "accCo param in NCI", example = "22616000990000582001")
    private String accCo;

    @ApiModelProperty(name = "Abs recipient account name", notes = "nameCo param in NCI", example = "JAVLON ERALIYEV")
    private String nameCo;

    @NotNull
    @ApiModelProperty(name = "Abs recipient bank MFO", notes = "bankCo param in NCI", example = "01158")
    private String bankCo;

    @Min(1)
    @NotNull
    @ApiModelProperty(name = "Amount to transfer", notes = "summa param in NCI, ratio is cents", example = "120000")
    private Long summa;

    @NotNull
    @ApiModelProperty(name = "Document number", notes = "docNum param in NCI, external id, may be used value of transactionId ", example = "112233")
    private String docNum;

    @NotNull
    @ApiModelProperty(name = "Transfer date", notes = "date param in NCI, type is timestamp", example = "")
    private Timestamp date;

    @NotNull
    @Size(min = 5, max = 5)
    @ApiModelProperty(name = "Transfer purpose code", notes = "purposeId param in NCI, 5 digits", example = "00667")
    private String purposeId;

    @NotNull
    @ApiModelProperty(name = "Transfer purpose text", notes = "purpose param in NCI", example = "Оплата по договору номер 123 от 01.01.21")
    private String purpose;

    @NotNull
    private String requestId;

    /* Additional properties */

    @ApiModelProperty(name = "Sender Full Name", notes = "Sender person/card holder/account owner Full Name", example = "IVAN IVANOV IVANOVICH")
    private String senderName;

    @ApiModelProperty(name = "Recipient Full Name", notes = "Recipient person/card holder/account owner Full Name", example = "IVAN IVANOV IVANOVICH")
    private String recipientName;

    @ApiModelProperty(name = "Sender Account", notes = "Sender PAN/Account", example = "860014....")
    private String senderAccount;

    @ApiModelProperty(name = "Recipient Account", notes = "Recipient PAN/Account", example = "22616....")
    private String recipientAccount;

    private String state;
    private String typeDocument;
    private String transactionId;
    private String transactionPurposeId;
    private String cashCode;
    private String tranType;
    private String codeFilial;
    private String tax;
    @NotNull
    private RequestType requestType;
    private String purposeComment;
    @Override
    public Long getBmTranId() {
        return Long.valueOf(transactionId);
    }

    @Override
    public String getAccount() {
        return accCo;
    }

    @Override
    public Long getAmount() {
        return summa;
    }

    @Override
    public Object requestJson() {
        return this;
    }

    public AbsTransactionRequest withAccCl(String accCl) {
        this.accCl = accCl;
        return this;
    }

    public AbsTransactionRequest withNameCl(String nameCl) {
        this.nameCl = nameCl;
        return this;
    }

    public AbsTransactionRequest withBankCl(String bankCl) {
        this.bankCl = bankCl;
        return this;
    }

    public AbsTransactionRequest withAccCo(String accCo) {
        this.accCo = accCo;
        return this;
    }

    public AbsTransactionRequest withNameCo(String nameCo) {
        this.nameCo = nameCo;
        return this;
    }

    public AbsTransactionRequest withBankCo(String bankCo) {
        this.bankCo = bankCo;
        return this;
    }

    public AbsTransactionRequest withSumma(Long summa) {
        this.summa = summa;
        return this;
    }

    public AbsTransactionRequest withDocNum(String docNum) {
        this.docNum = docNum;
        return this;
    }

    public AbsTransactionRequest withDate(Timestamp date) {
        this.date = date;
        return this;
    }

    public AbsTransactionRequest withPurposeId(String purposeId) {
        this.purposeId = purposeId;
        return this;
    }

    public AbsTransactionRequest withPurpose(String purpose) {
        this.purpose = purpose;
        return this;
    }

    public AbsTransactionRequest withRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public AbsTransactionRequest withState(String state) {
        this.state = state;
        return this;
    }

    public AbsTransactionRequest withTypeDocument(String typeDocument) {
        this.typeDocument = typeDocument;
        return this;
    }

    public AbsTransactionRequest withTransactionId(String transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    public AbsTransactionRequest withTransactionPurposeId(String transactionPurposeId) {
        this.transactionPurposeId = transactionPurposeId;
        return this;
    }

    public AbsTransactionRequest withCashCode(String cashCode) {
        this.cashCode = cashCode;
        return this;
    }

    public AbsTransactionRequest withRequestType(RequestType requestType) {
        this.requestType = requestType;
        return this;
    }

    public AbsTransactionRequest withSenderName(String senderName) {
        this.senderName = senderName;
        return this;
    }

    public AbsTransactionRequest withRecipientName(String recipientName) {
        this.recipientName = recipientName;
        return this;
    }

    public AbsTransactionRequest withSenderAccount(String senderAccount) {
        this.senderAccount = senderAccount;
        return this;
    }

    public AbsTransactionRequest withRecipientAccount(String recipientAccount) {
        this.recipientAccount = recipientAccount;
        return this;
    }
}
