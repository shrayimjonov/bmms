package uz.paykap.event.dto;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ReindexCachesEvent extends ApplicationEvent {

    private final String message;

    public ReindexCachesEvent(Object source, String message) {
        super(source);
        this.message = message;
    }

}
