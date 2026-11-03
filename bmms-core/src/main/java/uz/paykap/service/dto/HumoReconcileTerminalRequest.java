package uz.paykap.service.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;


@Getter
@Setter
@ToString
public class HumoReconcileTerminalRequest implements Serializable {
    private String terminalId;

}
