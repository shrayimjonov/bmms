package uz.paykap.sender.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SenderStatusResponse {

    private boolean paid;
    private boolean cancelled;
    private String comment;

    public SenderStatusResponse withPaid(boolean paid) {
        this.paid = paid;
        return this;
    }

    public SenderStatusResponse withCancelled(boolean cancelled) {
        this.cancelled = cancelled;
        return this;
    }

    public SenderStatusResponse withComment(String comment) {
        this.comment = comment;
        return this;
    }
}
