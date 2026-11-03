package uz.paykap.event.dto;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import uz.javlon.svgate.dto.sv.SvTerminalType;

@Getter
public class UzcardTerminalEvent extends ApplicationEvent {

    private final String merchantId;
    private final String terminalId;
    private final String name;
    private final SvTerminalType terminalType;
    private final boolean added;

    public UzcardTerminalEvent(Object source, String merchantId, String terminalId, String name, SvTerminalType terminalType, boolean added) {
        super(source);
        this.merchantId = merchantId;
        this.terminalId = terminalId;
        this.name = name;
        this.terminalType = terminalType;
        this.added = added;
    }

    public UzcardTerminalEvent(Object source, String merchantId, String terminalId, String name, SvTerminalType terminalType) {
        super(source);
        this.merchantId = merchantId;
        this.terminalId = terminalId;
        this.name = name;
        this.terminalType = terminalType;
        this.added = true;
    }

    public UzcardTerminalEvent(Object source, String merchantId, String terminalId) {
        super(source);
        this.merchantId = merchantId;
        this.terminalId = terminalId;
        this.name = "";
        this.terminalType = SvTerminalType.P2P;
        this.added = false;
    }
}
