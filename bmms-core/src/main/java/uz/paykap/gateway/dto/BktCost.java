package uz.paykap.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BktCost implements Serializable {
    private Long amount;
    private Long commission;
    private Long fee;

}
