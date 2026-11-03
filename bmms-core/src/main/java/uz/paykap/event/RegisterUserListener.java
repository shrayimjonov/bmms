package uz.paykap.event;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uz.devops.pusher.event.PusherEvent;
import uz.paykap.client.uaa.ManagedUserVM;
import uz.paykap.client.uaa.UaaClient;
import uz.paykap.client.uaa.UserDTO;
import uz.paykap.config.PusherConstants;
import uz.paykap.event.dto.RegisterUserEvent;

import java.util.Collections;

@Slf4j
@Component
@AllArgsConstructor
public class RegisterUserListener implements ApplicationListener<RegisterUserEvent> {

    private final UaaClient uaaClient;
    private final ApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(RegisterUserEvent registerUserEvent) {
        log.info("Creating user with details : {}", registerUserEvent);

        ResponseEntity<UserDTO> userDTOResponseEntity = uaaClient.createUserAndAgent(fillUser(registerUserEvent));
        log.info("Result : {}", userDTOResponseEntity);

        if (userDTOResponseEntity.getStatusCode() == HttpStatus.OK) {
            log.info("User created : {}", userDTOResponseEntity.getBody());
            String message = "Пользователь создан успешно : " + registerUserEvent.getLogin();
            applicationContext.publishEvent(
                new PusherEvent(this, Collections.singletonMap("message", message), PusherConstants.TAG_BM_PUSH)
            );
        } else {
            log.warn("User not created , status :{}", userDTOResponseEntity.getStatusCode());
            String message = "Пользователь НЕ создан : " + registerUserEvent.getLogin();
            applicationContext.publishEvent(
                new PusherEvent(this, Collections.singletonMap("message", message), PusherConstants.TAG_BM_PUSH_WARN)
            );
        }
    }

    private ManagedUserVM fillUser(RegisterUserEvent registerUserEvent) {
        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setPassword(registerUserEvent.getPassword());
        managedUserVM.setFirstName(registerUserEvent.getFirstName());
        managedUserVM.setLastName(registerUserEvent.getLastName());
        managedUserVM.setLogin(registerUserEvent.getLogin());
        managedUserVM.setEmail(registerUserEvent.getEMail());
        managedUserVM.setAuthorities(registerUserEvent.getAuthorities());

        return managedUserVM;
    }
}
