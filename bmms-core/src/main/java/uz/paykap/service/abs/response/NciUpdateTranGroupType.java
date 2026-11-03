package uz.paykap.service.abs.response;

public enum NciUpdateTranGroupType {
    GENERAL(0),
    FOREX(1),
    MULTI_INS(2),
    ;

    private Integer code;

    NciUpdateTranGroupType(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
