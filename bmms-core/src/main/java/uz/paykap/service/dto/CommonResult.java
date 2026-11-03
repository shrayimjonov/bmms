package uz.paykap.service.dto;

import java.io.Serializable;

public class CommonResult<T> implements Serializable {

    private boolean success;

    private String responseText;

    private T object;


    private Integer errorCode;

    public CommonResult(boolean success) {
        this.success = success;
    }

    public CommonResult(boolean success, String responseText) {
        this.success = success;
        this.responseText = responseText;
    }

    public CommonResult(boolean success, String responseText, T object) {
        this.success = success;
        this.responseText = responseText;
        this.object = object;
    }


    @Override
    public String toString() {
        return "CommonResult{" +
            "success=" + success +
            ", responseText='" + responseText + '\'' +
            ", object=" + object +
            '}';
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }

    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }

}
