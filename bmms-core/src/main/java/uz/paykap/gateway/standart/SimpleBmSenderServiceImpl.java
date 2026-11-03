package uz.paykap.gateway.standart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.enumeration.RequestType;

@Service("simpleBmSenderService")
@ConditionalOnProperty(prefix = "application", name = "simulate-simpleBm-requests", havingValue = "false")
public class SimpleBmSenderServiceImpl implements SimpleBmSenderService {

    private final static Logger log = LoggerFactory.getLogger(SimpleBmSenderServiceImpl.class);

    public SimpleBmSenderServiceImpl() {
        log.info("############ Simulation SimpleBm OFF ############");
    }

    @Override
    public ResponseEntity<String> send(RequestType requestType, String url, String body, HttpHeaders headers, Long timeout) {
        return null;
    }
}
