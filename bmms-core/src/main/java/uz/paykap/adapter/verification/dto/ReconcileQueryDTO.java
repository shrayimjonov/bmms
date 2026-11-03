package uz.paykap.adapter.verification.dto;

public class ReconcileQueryDTO {

    private String queryRootName;
    private String queryAmount;
    private String queryTranId;
    private String queryExtId;
    private String queryExtTime;
    private String querySuccess;
    private String queryReversal;
    private String queryCurrency;
    private String queryAccount;
    private Character fieldSeparator;
    private Character groupSeparator;
    private String requestDateFormat;
    private String responseDateFormat;

    public String getRequestDateFormat() {
        return requestDateFormat;
    }

    public void setRequestDateFormat(String requestDateFormat) {
        this.requestDateFormat = requestDateFormat;
    }

    public String getResponseDateFormat() {
        return responseDateFormat;
    }

    public void setResponseDateFormat(String responseDateFormat) {
        this.responseDateFormat = responseDateFormat;
    }

    public Character getFieldSeparator() {
        return fieldSeparator;
    }

    public void setFieldSeparator(Character fieldSeparator) {
        this.fieldSeparator = fieldSeparator;
    }

    public Character getGroupSeparator() {
        return groupSeparator;
    }

    public void setGroupSeparator(Character groupSeparator) {
        this.groupSeparator = groupSeparator;
    }

    public String getQueryAccount() {
        return queryAccount;
    }

    public void setQueryAccount(String queryAccount) {
        this.queryAccount = queryAccount;
    }

    public String getQueryExtTime() {
        return queryExtTime;
    }

    public void setQueryExtTime(String queryExtTime) {
        this.queryExtTime = queryExtTime;
    }

    public String getQuerySuccess() {
        return querySuccess;
    }

    public void setQuerySuccess(String querySuccess) {
        this.querySuccess = querySuccess;
    }

    public String getQueryReversal() {
        return queryReversal;
    }

    public void setQueryReversal(String queryReversal) {
        this.queryReversal = queryReversal;
    }

    public String getQueryCurrency() {
        return queryCurrency;
    }

    public void setQueryCurrency(String queryCurrency) {
        this.queryCurrency = queryCurrency;
    }

    public String getQueryRootName() {
        return queryRootName;
    }

    public void setQueryRootName(String queryRootName) {
        this.queryRootName = queryRootName;
    }

    public String getQueryAmount() {
        return queryAmount;
    }

    public void setQueryAmount(String queryAmount) {
        this.queryAmount = queryAmount;
    }

    public String getQueryTranId() {
        return queryTranId;
    }

    public void setQueryTranId(String queryTranId) {
        this.queryTranId = queryTranId;
    }

    public String getQueryExtId() {
        return queryExtId;
    }

    public void setQueryExtId(String queryExtId) {
        this.queryExtId = queryExtId;
    }

    @Override
    public String toString() {
        return "ReconcileQueryDTO{" +
            "queryRootName='" + queryRootName + '\'' +
            ", queryAmount='" + queryAmount + '\'' +
            ", queryTranId='" + queryTranId + '\'' +
            ", queryExtId='" + queryExtId + '\'' +
            ", queryExtTime='" + queryExtTime + '\'' +
            ", querySuccess='" + querySuccess + '\'' +
            ", queryReversal='" + queryReversal + '\'' +
            ", queryCurrency='" + queryCurrency + '\'' +
            ", queryAccount='" + queryAccount + '\'' +
            ", fieldSeparator=" + fieldSeparator +
            ", groupSeparator=" + groupSeparator +
            ", requestDateFormat='" + requestDateFormat + '\'' +
            ", responseDateFormat='" + responseDateFormat + '\'' +
            '}';
    }
}
