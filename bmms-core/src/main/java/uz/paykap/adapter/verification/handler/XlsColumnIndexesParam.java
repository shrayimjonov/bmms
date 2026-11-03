package uz.paykap.adapter.verification.handler;

import uz.paykap.adapter.verification.dto.AmountRatio;

public class XlsColumnIndexesParam {

    private int id = -1;
    private int recipientId = -1;
    private int amount = -1;
    private int account = -1;
    private int currency = -1;
    private int extId = -1;
    private int extTime = -1;
    private int success = -1;
    private int reversal = -1;
    private AmountRatio amountRatio = AmountRatio.TIYIN;
    private String successText = "";
    private String reversalText = "";
    private String extTimeFormat = "";

    /**
     * Set column indexes (0-based).  Default -1, means not given in xls.
     *
     * @param id          0 based id column index
     * @param amount      0 based amount column index
     * @param amountRatio amountRatio for amount value, default TIYIN(100)
     * @param account     0 based account column index
     * @param extId       0 based extId column index
     * @return Cell representing that column or null if undefined.
     */
    public XlsColumnIndexesParam(int id, int amount, AmountRatio amountRatio, int account, int extId) {
        this.id = id;
        this.amount = amount;
        this.amountRatio = amountRatio == null ? AmountRatio.TIYIN : amountRatio;
        this.account = account;
        this.extId = extId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(int recipientId) {
        this.recipientId = recipientId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getAccount() {
        return account;
    }

    public void setAccount(int account) {
        this.account = account;
    }

    public int getCurrency() {
        return currency;
    }

    public void setCurrency(int currency) {
        this.currency = currency;
    }

    public int getExtId() {
        return extId;
    }

    public void setExtId(int extId) {
        this.extId = extId;
    }

    public int getExtTime() {
        return extTime;
    }

    public void setExtTime(int extTime) {
        this.extTime = extTime;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getReversal() {
        return reversal;
    }

    public void setReversal(int reversal) {
        this.reversal = reversal;
    }

    public String getSuccessText() {
        return successText;
    }

    public void setSuccessText(String successText) {
        this.successText = successText;
    }

    public String getReversalText() {
        return reversalText;
    }

    public void setReversalText(String reversalText) {
        this.reversalText = reversalText;
    }

    public String getExtTimeFormat() {
        return extTimeFormat;
    }

    public void setExtTimeFormat(String extTimeFormat) {
        this.extTimeFormat = extTimeFormat;
    }

    public XlsColumnIndexesParam id(int id) {
        this.id = id;
        return this;
    }

    public XlsColumnIndexesParam recipientId(int recipientId) {
        this.recipientId = recipientId;
        return this;
    }

    public XlsColumnIndexesParam amount(int amount) {
        this.amount = amount;
        return this;
    }

    public XlsColumnIndexesParam account(int account) {
        this.account = account;
        return this;
    }

    public XlsColumnIndexesParam currency(int currency) {
        this.currency = currency;
        return this;
    }

    public XlsColumnIndexesParam extId(int extId) {
        this.extId = extId;
        return this;
    }

    public XlsColumnIndexesParam extTime(int extTime) {
        this.extTime = extTime;
        return this;
    }

    public XlsColumnIndexesParam success(int success) {
        this.success = success;
        return this;
    }

    public XlsColumnIndexesParam reversal(int reversal) {
        this.reversal = reversal;
        return this;
    }

    public XlsColumnIndexesParam reversalText(String reversalText) {
        this.reversalText = reversalText;
        return this;
    }

    public XlsColumnIndexesParam extTimeFormat(String extTimeFormat) {
        this.extTimeFormat = extTimeFormat;
        return this;
    }

    public AmountRatio getAmountRatio() {
        return amountRatio;
    }

    public void setAmountRatio(AmountRatio amountRatio) {
        this.amountRatio = amountRatio;
    }

    public XlsColumnIndexesParam amountRatio(AmountRatio amountRatio) {
        this.amountRatio = amountRatio;
        return this;
    }

    public XlsColumnIndexesParam successText(String successText) {
        this.successText = successText;
        return this;
    }
}
