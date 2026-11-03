package uz.paykap.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import uz.javlon.svgate.dto.enumeration.CardStatus;

@Getter
@Setter
public class SvCardDTO {
    @JsonProperty(
        value = "id"
    )
    private String token;
    private String username;
    private String pan;
    private String expiry;
    private CardStatus status;
    private String phone;
    private String fullName;
    private Long balance;
    private Boolean sms;
    private Integer pincnt;
    private String aacct;
    private String cardtype;
    private Long holdAmount;
    private Long cashbackAmount;
}
