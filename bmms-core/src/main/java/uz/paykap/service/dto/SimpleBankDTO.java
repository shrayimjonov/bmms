package uz.paykap.service.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString(exclude = {"logo"})
public class SimpleBankDTO implements Serializable {

    private String name;

    private String cardBin;

    private Double commission;

    private byte[] logo;
}
