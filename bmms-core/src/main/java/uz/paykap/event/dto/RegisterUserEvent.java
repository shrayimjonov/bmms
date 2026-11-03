package uz.paykap.event.dto;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Set;

@Getter
public class RegisterUserEvent extends ApplicationEvent {

    private final String login;
    private final String password;
    private final String firstName;
    private final String lastName;
    private final String eMail;
    private final Set<String> authorities;

    public RegisterUserEvent(Object source, String login, String password, String firstName, String lastName, String eMail, Set<String> authorities) {
        super(source);
        this.login = login;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.eMail = eMail;
        this.authorities = authorities;
    }

}
