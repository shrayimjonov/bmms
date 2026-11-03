package uz.paykap.service.abs.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.paykap.service.abs.response.AbsClientResult;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NciForexClientResultDTO {
    private String responseCode;
    private String responseText;
    private AbsClientResult client;
}
