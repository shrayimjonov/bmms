package uz.paykap.gateway.standart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.enumeration.RequestType;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;

@Service("simpleBmSenderService")
@ConditionalOnProperty(prefix = "application", name = "simulate-simpleBm-requests", havingValue = "true", matchIfMissing = true)
public class SimpleBmSenderServiceSimulation implements SimpleBmSenderService {

    private final static Logger log = LoggerFactory.getLogger(SimpleBmSenderServiceSimulation.class);

    public SimpleBmSenderServiceSimulation() {
        log.info("############ Simulation SimpleBm ON ############");
    }

    @Override
    public ResponseEntity<String> send(RequestType requestType, String url, String body, HttpHeaders headers, Long timeout) {
        switch (requestType) {
            case CHECK:
                return ResponseEntity.ok(checkResponse(body));
            case PAY:
                return ResponseEntity.ok(payResponse(body));
            case STATUS:
                return ResponseEntity.ok(statusResponse());
            case RECONCILE:
                return ResponseEntity.ok(reconcileResponse());
            case DEPOSIT:
                throw new CodedException(ProblemEnum.NOT_IMPLEMENTED);
            case CANCEL:
                throw new CodedException(ProblemEnum.NOT_IMPLEMENTED);
            default:
                throw new CodedException(ProblemEnum.NOT_IMPLEMENTED);
        }
    }

    private String checkResponse(String body) {
        return "{\n" +
            "    \"id\": 1437237,\n" +
            "    \"recipientShortName\": \"hg_world_of_tanks_uzs\",\n" +
            "    \"sessionId\": \"1558090219209\",\n" +
            "    \"amount\": 500000,\n" +
            "    \"account\": \"user@example.com\",\n" +
            "    \"currency\": 840,\n" +
            "    \"extId\": \"2218672\",\n" +
            "    \"requestTime\": \"2019-05-17T10:50:19.209Z\",\n" +
            "    \"responseTime\": \"2019-05-17T10:50:22.434Z\",\n" +
            "    \"responseText\": \"Транзакция Успешна\",\n" +
            "    \"success\": true,\n" +
            "    \"status\": \"CHECK_OK\",\n" +
            "    \"data\": {\n" +
            "        \"serviceCurrency\": \"WGG\",\n" +
            "        \"amount\": \"0.59\",\n" +
            "        \"exchangeRate\": \"0.0037986\",\n" +
            "        \"rate\": \"845816\",\n" +
            "        \"ccy\": \"840\",\n" +
            "        \"userAmount\": \"155.320\",\n" +
            "        \"currency\": \"USD\",\n" +
            "        \"serviceId\": \"173\",\n" +
            "        \"account\": \"user@example.com\",\n" +
            "        \"recipientAmount\": \"0.59\"\n" +
            "    }\n" +
            "}";
    }

    private String payResponse(String body) {
        return "{\n" +
            "    \"id\": 10011251,\n" +
            "    \"recipientShortName\": \"dummy-recipient\",\n" +
            "    \"sessionId\": \"1547738592329\",\n" +
            "    \"amount\": 1000,\n" +
            "    \"account\": \"4111\",\n" +
            "    \"currency\": 860,\n" +
            "    \"extId\": \"EXT_PAY_ID\",\n" +
            "    \"requestTime\": \"2019-01-17T15:23:12.329Z\",\n" +
            "    \"responseTime\": \"2019-01-17T15:23:12.329Z\",\n" +
            "    \"responseText\": \"OK\",\n" +
            "    \"success\": true,\n" +
            "    \"status\": \"SUCCESS\",\n" +
            "    \"data\": {\n" +
            "        \"dummy\": \"2\"\n" +
            "    }\n" +
            "}";
    }

    private String reconcileResponse() {
        return "";
    }

    private String statusResponse() {
        return "";
    }

}
