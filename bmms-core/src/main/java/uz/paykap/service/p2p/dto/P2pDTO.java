package uz.paykap.service.p2p.dto;

public interface P2pDTO {
    String getAccount();
    String getAccountBin();

    default String getCondition() {
        return getAccount();
    }

    default String getName() {
        return "";
    }

    default SenderAdditionalParam getAdditionalParam() {
        return null;
    }
}
