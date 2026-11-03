package uz.paykap.service.p2p.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class P2pParticipantDto {

    public static final String ACCOUNT_KEY = "ACCOUNT";
    public static final String EXT_ID_KEY = "EXT_ID";
    private String name;
    private Long amount;
    private Long commissionAmount;
    private BigDecimal commission;
    private Integer currency;
    private Map<String, Object> data = new HashMap<>(2);

    public P2pParticipantDto(String name, Long amount, Long commission, Integer currency, Map<String, Object> data) {
        this.name = name;
        this.amount = amount;
        this.commission = BigDecimal.valueOf(commission);
        this.currency = currency;
        this.data = data;
    }

    public P2pParticipantDto withName(String name) {
        this.name = name;
        return this;
    }

    public P2pParticipantDto withAmount(Long amount) {
        this.amount = amount;
        return this;
    }

    public P2pParticipantDto withCommission(BigDecimal commission) {
        this.commission = commission.setScale(2, BigDecimal.ROUND_FLOOR);
        return this;
    }

    public P2pParticipantDto withCommission(Long commission) {
        this.commission = BigDecimal.valueOf(commission);
        return this;
    }

    public P2pParticipantDto withCurrency(Integer currency) {
        this.currency = currency;
        return this;
    }

    public P2pParticipantDto withData(Map<String, Object> data) {
        this.data = data;
        return this;
    }

    public P2pParticipantDto withDataAccount(String account) {
        this.data.put(ACCOUNT_KEY, account);
        return this;
    }

    public void addDataAccount(String account) {
        this.data.put(ACCOUNT_KEY, account);
    }

    public String fetchDataAccount() {
        if (this.data != null && this.data.get(ACCOUNT_KEY) != null) {
            this.data.get(ACCOUNT_KEY);
        }
        return "";
    }

    public P2pParticipantDto withDataExtId(String extId) {
        this.data.put(EXT_ID_KEY, extId);
        return this;
    }

    public void addDataExtId(String extId) {
        this.data.put(EXT_ID_KEY, extId);
    }

    public String fetchDataExtId() {
        if (this.data != null && this.data.get(EXT_ID_KEY) != null) {
            this.data.get(EXT_ID_KEY);
        }
        return "";
    }
}
