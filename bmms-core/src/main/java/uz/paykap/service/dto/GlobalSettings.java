package uz.paykap.service.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GlobalSettings implements Serializable {
    private Boolean enableCheck;
    private Boolean enablePay;
    private Boolean allowOnPreHandle;
}
