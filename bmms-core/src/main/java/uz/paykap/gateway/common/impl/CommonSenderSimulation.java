package uz.paykap.gateway.common.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import uz.devops.bmcore.enumeration.RequestType;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.domain.enumeration.MethodType;
import uz.paykap.gateway.common.CommonSender;
import uz.paykap.gateway.common.simulation.SimulationBase;
import uz.paykap.gateway.common.simulation.SimulationMethod;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
//@Profile("!prod")
//@Service("commonSender")
public class CommonSenderSimulation implements CommonSender {

    private final ObjectMapper objectMapper;

    public static final String COMMON_RECIPIENTS_FOLDER = "simulation/";

    public CommonSenderSimulation(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ResponseEntity<String> sendAndReceive(RequestType requestType, String recipientShortName, MethodType httpMethod, String url, HttpHeaders headers, Map<String, Object> params) {
        return getStringResponseEntity(requestType, recipientShortName, url, null, headers, params);
    }

    @Override
    public ResponseEntity<String> sendAndReceive(RequestType requestType, String recipientShortName, MethodType HttpMethod, String url, String body, HttpHeaders headers, Map<String, Object> params) {
        return getStringResponseEntity(requestType, recipientShortName, url, body, headers, params);
    }

    @Override
    public ResponseEntity<String> sendAndReceive(RequestType requestType, String recipientShortName, String url, MultiValueMap<String, String> body, HttpHeaders headers, Map<String, Object> params) {
        //TODO
        return null;
    }

    @Override
    public ResponseEntity<String> sendAndReceive(RequestType requestType, String recipientShortName, String url, HttpHeaders headers, MultiValueMap<String, Object> map, Map<String, Object> params) {
        //TODO
        return null;
    }

    private ResponseEntity<String> getStringResponseEntity(RequestType requestType, String recipientShortName, String url, String body, MultiValueMap<String, String> headers, Map<String, Object> params) {
        log.info("### Enter simulation method!!");

        SimulationBase simulationBase = loadRecipientResources(recipientShortName);

        SimulationMethod simulationMethod;

        switch (requestType) {
            case CHECK:
                simulationMethod = simulationBase.getCheck();
                break;
            case PAY:
                simulationMethod = simulationBase.getPay();
                break;
            case RECONCILE:
                simulationMethod = simulationBase.getReconcile();
                break;
            case CANCEL:
                simulationMethod = simulationBase.getCancel();
                break;
            default:
                throw new CodedException(ProblemEnum.NOT_IMPLEMENTED);
        }

        if (simulationMethod == null) {
            log.warn("Simulation method {} for {} not given in 'simulation/{}.json'", requestType, recipientShortName, recipientShortName);
        }

        assert simulationMethod != null;

        if (simulationMethod.getSuccess().getRequest().getValidate().isUrl()) {
            String expected = new StringSubstitutor(params).replace(simulationMethod.getSuccess().getRequest().getUrl());
            if (!Objects.equals(url, expected)) {
                log.warn("\nNot equals :\nBuilt URL : {}\nExpected URL : {}\n", url, expected);
                throw new CodedException(ProblemEnum.INTERNAL_ERROR, "ASSERT occurred");
            }
        }

        if (body != null && simulationMethod.getSuccess().getRequest().getValidate().isBody()) {

            simulationMethod.getSuccess().getRequest().setBody(new StringSubstitutor(params).replace(simulationMethod.getSuccess().getRequest().getBody()));

            if (body.startsWith("<")) {
                try {
                    org.custommonkey.xmlunit.XMLUnit.setIgnoreWhitespace(true);
                    assert org.custommonkey.xmlunit.XMLUnit.compareXML(body, simulationMethod.getSuccess().getRequest().getBody()).similar();
                } catch (Exception e) {
                    assert false;
                }
            } else {
                assert Objects.equals(body.trim(), simulationMethod.getSuccess().getRequest().getBody().trim());
            }
        }

        if (simulationMethod.getSuccess().getRequest().getValidate().isHeaders()) {
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                assert Objects.equals(simulationMethod.getSuccess().getRequest().getHeaders().get(entry.getKey()), entry.getValue().get(0));
            }
        }

        HttpStatus resolve = HttpStatus.resolve(simulationMethod.getSuccess().getResponse().getStatus());

        assert resolve != null;

        return new ResponseEntity<>(simulationMethod.getSuccess().getResponse().getBody(), resolve);
    }

    private SimulationBase loadRecipientResources(String fileName) {
        String path = System.getProperty("user.dir") + File.separator + "local" + File.separator + "common" + File.separator + "simulation" + File.separator + fileName + ".json";
        log.info("path to simulation file : {}", path);
        try {

            return objectMapper.readValue(new File(path), SimulationBase.class);
        } catch (IOException e) {
            throw new CodedException(ProblemEnum.OBJECT_NOT_FOUND);
        }
    }
}
