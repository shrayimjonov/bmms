package uz.paykap.adapter.verification.common;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import uz.devops.bmcore.enumeration.RequestType;
import uz.javlon.commons.base.ResultData;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.adapter.verification.ReconcileAdapter;
import uz.paykap.adapter.verification.dto.ReconcileQueryDTO;
import uz.paykap.adapter.verification.dto.ReconciliationRequestDTO;
import uz.paykap.config.Constants;
import uz.paykap.domain.GatewayParam;
import uz.paykap.domain.enumeration.MethodType;
import uz.paykap.gateway.common.CommonSender;
import uz.paykap.gateway.common.dto.BaseParam;
import uz.paykap.gateway.dto.CompareSchema;
import uz.paykap.gateway.dto.GatewayTranDTO;
import uz.paykap.gateway.dto.ReconcileGatewayDTO;
import uz.paykap.repository.GatewayParamRepository;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service(CommonReconcileAdapter.NAME)
public class CommonReconcileAdapter extends ReconcileAdapter {

    public static final String NAME = "commonReconcileAdapter";
    public static final String TEXT_XML = "text/xml";
    public static final String APPLICATION_XML = "application/xml";
    public static final String TEXT_PLAIN = "text/plain";
    public static final String APPLICATION_JSON = "application/json";
    public static final String DEFAULT = "default";
    public static final String XML = "text/xml";
    public static final String TEXT = "TEXT";
    public static final String JSON = "JSON";
    public static final String PATH_FILE = "reconcile/";

    private final Logger log = LoggerFactory.getLogger(CommonReconcileAdapter.class);

    @Override
    public ResultData<ReconcileGatewayDTO> getReconcileList(ReconciliationRequestDTO paramsDTO) {

        log.info("Request to get reconcile with params : {}", paramsDTO.toString());

        Optional<GatewayParam> gatewayParam = getGatewayParam(paramsDTO.getRecipientShortName());

        log.debug("Gateway param :{}", gatewayParam);

        if (!gatewayParam.isPresent()) {
            return new ResultData<>(new CodedException(ProblemEnum.SERVICE_NOT_FOUND));
        }

        log.debug("Gateway param data queries :{}", gatewayParam.get().getDataQueries());

//        ReconcileQueryDTO reconcileQueryDTO = loadQueryData(paramsDTO.getRecipientShortName());

        ReconcileQueryDTO reconcileQueryDTO = getQueryData(gatewayParam.get().getDataQueries());

        String result = getDataFromGateway(paramsDTO, gatewayParam.get(), reconcileQueryDTO);

        if (result == null) {
            log.warn("result null");
            return new ResultData<>(new CodedException("Reconcile", "Reconcile data null"));
        }

        log.debug("Reconcile data to parse : {}", result);

        log.debug("Recipient : {}", paramsDTO.getRecipientShortName());

        ReconcileGatewayDTO reconcileGatewayDTO;

        try {
            switch (gatewayParam.get().getResponseMediaType()) {
                case TEXT_XML:
                case APPLICATION_XML: {
                    log.info("Reconcile media type = {}", TEXT_XML);
                    reconcileGatewayDTO = parseXmlData(reconcileQueryDTO, result);
                    break;
                }

                case TEXT_PLAIN: {
                    log.info("Reconcile media type = {}", TEXT_PLAIN);
                    reconcileGatewayDTO = parseTextData(reconcileQueryDTO, result);
                    break;
                }

                case APPLICATION_JSON: {
                    log.info("Reconcile media type = {}", APPLICATION_JSON);
                    reconcileGatewayDTO = new ReconcileGatewayDTO();
                    break;
                }

                default: {
                    log.info("Reconcile media type = {}", DEFAULT);
                    reconcileGatewayDTO = new ReconcileGatewayDTO();
//                    throw new CodedException(ProblemEnum.INVALID_REQUEST);

                }

            }
        } catch (Exception e) {
            log.warn("Error with parse Reconcile data = {}", e.getMessage());
            return new ResultData<>(new CodedException(ProblemEnum.PARSE_ERROR));
        }
        log.info("Reconcile success! , response = {}", reconcileGatewayDTO);

        return new ResultData<>(reconcileGatewayDTO);
    }


    private Optional<GatewayParam> getGatewayParam(String recipientShortName) {
        return gatewayParamRepository.findTop1ByRecipientShortNameAndTypeAndActiveIsTrue(recipientShortName, RequestType.RECONCILE);
    }

    private ReconcileGatewayDTO parseXmlData(ReconcileQueryDTO reconcileQueryDTO, String xmlResultFromGateway) throws ParserConfigurationException, IOException, SAXException {

        if (xmlResultFromGateway == null || xmlResultFromGateway.isEmpty()) {
            throw new CodedException(ProblemEnum.PARSE_ERROR);
        }

        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(xmlResultFromGateway));

        NodeList nodes = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is).getElementsByTagName(reconcileQueryDTO.getQueryRootName());

        HashSet<GatewayTranDTO> gatewayTranDTOS = new HashSet<>();

        //TODO - Zafar, надо учитивать разрядность суммы (Recipient.amountRatio)
        Double totalAmount = 0.0;

        Long totalCount = 0L;
        for (int i = 0; i < nodes.getLength(); i++) {

            GatewayTranDTO gatewayTranDTO = new GatewayTranDTO();
            Element element = (Element) nodes.item(i);

            //set RecipientID
//            gatewayTranDTO.setRecipientId(recipientId); //TODO - fix
            Element line;

            //set Amount
            if (reconcileQueryDTO.getQueryAmount() != null && !reconcileQueryDTO.getQueryAmount().isEmpty()) {
                NodeList amount = element.getElementsByTagName(reconcileQueryDTO.getQueryAmount());
                line = (Element) amount.item(0);
                double amountDouble = Double.parseDouble(getCharacterDataFromElement(line));
                gatewayTranDTO.setAmount(amountDouble);
                totalAmount += amountDouble;
            }

            //set TranId
            if (reconcileQueryDTO.getQueryTranId() != null && !reconcileQueryDTO.getQueryTranId().isEmpty()) {
                NodeList tranId = element.getElementsByTagName(reconcileQueryDTO.getQueryTranId());
                line = (Element) tranId.item(0);
                gatewayTranDTO.setId(Long.valueOf(getCharacterDataFromElement(line)));
                totalCount++;
            }

            //set ExtId
            if (reconcileQueryDTO.getQueryExtId() != null && !reconcileQueryDTO.getQueryExtId().isEmpty()) {
                NodeList extId = element.getElementsByTagName(reconcileQueryDTO.getQueryExtId());
                line = (Element) extId.item(0);
                gatewayTranDTO.setExtId(getCharacterDataFromElement(line));
            }

            //set Success
            if (reconcileQueryDTO.getQuerySuccess() != null && !reconcileQueryDTO.getQuerySuccess().isEmpty()) {
                NodeList success = element.getElementsByTagName(reconcileQueryDTO.getQuerySuccess());
                line = (Element) success.item(0);
                gatewayTranDTO.setSuccess(Boolean.valueOf(getCharacterDataFromElement(line)));
            } else {
                gatewayTranDTO.setSuccess(true);
            }

            //set Revers
            if (reconcileQueryDTO.getQueryReversal() != null && !reconcileQueryDTO.getQueryReversal().isEmpty()) {
                NodeList revers = element.getElementsByTagName(reconcileQueryDTO.getQueryReversal());
                line = (Element) revers.item(0);
                gatewayTranDTO.setReversal(Boolean.valueOf(getCharacterDataFromElement(line)));
            } else {
                gatewayTranDTO.setReversal(false);
            }

            //set Account
            if (reconcileQueryDTO.getQueryAccount() != null && !reconcileQueryDTO.getQueryAccount().isEmpty()) {
                NodeList account = element.getElementsByTagName(reconcileQueryDTO.getQueryAccount());
                line = (Element) account.item(0);
                gatewayTranDTO.setAccount(getCharacterDataFromElement(line));
            }

            //set Time
            if (reconcileQueryDTO.getQueryExtTime() != null && !reconcileQueryDTO.getQueryExtTime().isEmpty()) {
                NodeList time = element.getElementsByTagName(reconcileQueryDTO.getQueryExtTime());
                line = (Element) time.item(0);
                gatewayTranDTO.setExtTime((getCharacterDataFromElement(line)));
            }

            //set Currency
            if (reconcileQueryDTO.getQueryCurrency() != null && !reconcileQueryDTO.getQueryCurrency().isEmpty()) {
                NodeList currency = element.getElementsByTagName(reconcileQueryDTO.getQueryCurrency());
                line = (Element) currency.item(0);
                gatewayTranDTO.setCurrency(Integer.valueOf(getCharacterDataFromElement(line)));
            } else {
                gatewayTranDTO.setCurrency(Constants.DEFAULT_CURRENCY_CODE);
            }

            gatewayTranDTO.setGatewayTransaction(true);

            gatewayTranDTOS.add(gatewayTranDTO);
        }

        //TODO - Zafar - totalAmount.longValue()
        return new ReconcileGatewayDTO(gatewayTranDTOS, totalAmount.longValue(), totalCount);
    }

    private ReconcileGatewayDTO parseTextData(ReconcileQueryDTO reconcileQueryDTO, String data) {
        if (reconcileQueryDTO == null) {
            throw new CodedException(ProblemEnum.PARSE_ERROR);
        }
        String[] dataAfterGroupSplit = data.split(String.valueOf(reconcileQueryDTO.getGroupSeparator()));

        HashSet<GatewayTranDTO> gatewayTranDTOS = new HashSet<>();

        //TODO - Zafar, надо учитивать разрядность суммы (Recipient.amountRatio)
        Double totalAmount = 0.0;

        Long totalCount = 0L;
        try {

            for (String str : dataAfterGroupSplit) {

                GatewayTranDTO gatewayTranDTO = new GatewayTranDTO();

                gatewayTranDTO.compareSchema(CompareSchema.ONLY_ID);

                String[] dataAfterFieldSplit = str.split(String.valueOf(reconcileQueryDTO.getFieldSeparator()));

                if (reconcileQueryDTO.getQueryAmount() != null && !reconcileQueryDTO.getQueryAmount().isEmpty()) {
                    double amount = Double.parseDouble(dataAfterFieldSplit[Integer.valueOf(reconcileQueryDTO.getQueryAmount())]);
                    gatewayTranDTO.setAmount(amount);
                    totalAmount += amount;
                    totalCount++;
                }

                if (reconcileQueryDTO.getQueryAccount() != null) {
                    gatewayTranDTO.setAccount(dataAfterFieldSplit[Integer.valueOf(reconcileQueryDTO.getQueryAccount())]);
                }

                if (reconcileQueryDTO.getQueryCurrency() != null) {
                    gatewayTranDTO.setCurrency(Integer.valueOf(dataAfterFieldSplit[Integer.valueOf(reconcileQueryDTO.getQueryCurrency())]));
                }

                if (reconcileQueryDTO.getQueryExtId() != null) {
                    gatewayTranDTO.setExtId(dataAfterFieldSplit[Integer.valueOf(reconcileQueryDTO.getQueryExtId())]);
                }

                if (reconcileQueryDTO.getQueryExtTime() != null) {
                    gatewayTranDTO.setExtTime(dataAfterFieldSplit[Integer.valueOf(reconcileQueryDTO.getQueryExtTime())]);
                }

                if (reconcileQueryDTO.getQueryReversal() != null) {
                    gatewayTranDTO.setReversal(Boolean.parseBoolean(dataAfterFieldSplit[Integer.valueOf(reconcileQueryDTO.getQueryReversal())]));
                } else {
                    gatewayTranDTO.setReversal(false);
                }

                if (reconcileQueryDTO.getQuerySuccess() != null) {
                    gatewayTranDTO.setSuccess(Boolean.parseBoolean(dataAfterFieldSplit[Integer.valueOf(reconcileQueryDTO.getQuerySuccess())]));
                } else {
                    gatewayTranDTO.setSuccess(true);
                }

                if (reconcileQueryDTO.getQueryTranId() != null) {
                    gatewayTranDTO.setId(Long.valueOf(dataAfterFieldSplit[Integer.valueOf(reconcileQueryDTO.getQueryTranId())]));
                }

                gatewayTranDTO.setGatewayTransaction(true);
                log.debug("gateway tran :{}", gatewayTranDTO.toString());
                gatewayTranDTOS.add(gatewayTranDTO);
//                gatewayTranDTOS1.add(gatewayTranDTO);
            }
        } catch (Exception e) {
            throw new CodedException(ProblemEnum.PARSE_ERROR);
        }
//(amount)+ fee * fee1
//        gatewayTranDTOS.addAll(gatewayTranDTOS1);
        log.debug("Gateway trans size :{}", gatewayTranDTOS.size());
//        log.debug("List trans size : {}", gatewayTranDTOS1.size());
//        log.debug("List :{}", gatewayTranDTOS1.toString());

        //TODO - Zafar - totalAmount.longValue()
        return new ReconcileGatewayDTO(gatewayTranDTOS, totalAmount.longValue(), totalCount);
    }

    private HttpHeaders getHeadersFromJson(GatewayParam gatewayParam) {
        if (gatewayParam.getHeaders() == null || gatewayParam.getHeaders().isEmpty() || gatewayParam.getResponseMediaType() == null) {
            return HttpHeaders.EMPTY;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", gatewayParam.getRequestMediaType());

        Map<String, String> hs = getMapFromString(gatewayParam.getHeaders());

        for (Map.Entry<String, String> entry : hs.entrySet()) {
            headers.add(entry.getKey(), entry.getValue());
        }

        return headers;
    }

    private Map<String, String> getMapFromString(String jsonObjectAsString) {
        if (jsonObjectAsString == null || jsonObjectAsString.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(jsonObjectAsString, new TypeReference<Map<String, String>>() {
            });
        } catch (IOException e) {
            return Collections.emptyMap();
        }
    }


    private String getCharacterDataFromElement(Element e) {
        if (e == null) {
            return null;
        }
        Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return null;
    }

    private void addBaseParamsReconcile(Map<String, Object> map, GatewayParam gatewayParam, ReconciliationRequestDTO paramsDTO, ReconcileQueryDTO reconcileQueryDTO) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(reconcileQueryDTO.getRequestDateFormat()).withZone(ZoneId.of("UTC"));
        String formattedDate = timeFormatter.format(paramsDTO.getFromTime());

        map.put(BaseParam.date.name(), formattedDate);
        map.put(BaseParam.dateFrom.name(), formattedDate);
        map.put(BaseParam.dateTo.name(), timeFormatter.format(paramsDTO.getToTime()));

        map.put(BaseParam.password.name(), gatewayParam.getPassword());
        map.put(BaseParam.username.name(), gatewayParam.getUsername());
    }

    public String getDataFromGateway(ReconciliationRequestDTO paramsDTO, GatewayParam gatewayParam, ReconcileQueryDTO reconcileQueryDTO) {

        log.info("Request to get reconcile with params : {} , {}", paramsDTO, gatewayParam);

        Map<String, Object> baseParams = new HashMap<>();

        addBaseParamsReconcile(baseParams, gatewayParam, paramsDTO, reconcileQueryDTO);

        String url = new StringSubstitutor(baseParams).replace(gatewayParam.getUrl());

        log.debug("Url built : {}", url);

        String body = new StringSubstitutor(baseParams).replace(gatewayParam.getBody());

        log.debug("Body built : {}", body);

        ResponseEntity<String> result;

        try {
            if (gatewayParam.getHttpMethodType() == MethodType.POST || gatewayParam.getHttpMethodType() == MethodType.PUT) {
                result = commonSender.sendAndReceive(RequestType.RECONCILE, paramsDTO.getRecipientShortName(), gatewayParam.getHttpMethodType(), url, body, getHeadersFromJson(gatewayParam), baseParams);
            } else if (gatewayParam.getHttpMethodType() == MethodType.GET || gatewayParam.getHttpMethodType() == MethodType.DELETE) {
                result = commonSender.sendAndReceive(RequestType.RECONCILE, paramsDTO.getRecipientShortName(), gatewayParam.getHttpMethodType(), url, getHeadersFromJson(gatewayParam), baseParams);
            } else {
                throw new CodedException(ProblemEnum.NOT_IMPLEMENTED, "Not supported http method type : " + gatewayParam.getHttpMethodType());
            }
        } catch (Exception e) {
            log.warn("Error : {}", e.getLocalizedMessage());
            return null;
        }

        if (!result.getStatusCode().is2xxSuccessful() || result.getBody() == null) {
            log.warn("Http Result is null or not success");
            throw new CodedException(ProblemEnum.UNKNOWN_ERROR);
        }

        return result.getBody();
    }

    private ReconcileQueryDTO loadQueryData(String fileName) {
        try {
            return objectMapper.readValue(IOUtils.toString(new ClassPathResource(PATH_FILE + fileName + "_reconcile.json").getInputStream(), StandardCharsets.UTF_8), ReconcileQueryDTO.class);
        } catch (IOException e) {
            throw new CodedException(ProblemEnum.OBJECT_NOT_FOUND);
        }
    }

    private ReconcileQueryDTO getQueryData(String dataQueries) {
        try {
            return objectMapper.readValue(dataQueries, ReconcileQueryDTO.class);
        } catch (Exception e) {
            throw new CodedException(ProblemEnum.PARSE_ERROR);
        }
    }

    private final ObjectMapper objectMapper;
    private final GatewayParamRepository gatewayParamRepository;
    private final CommonSender commonSender;

    public CommonReconcileAdapter(ObjectMapper objectMapper, GatewayParamRepository gatewayParamRepository, CommonSender commonSender) {
        this.objectMapper = objectMapper;
        this.gatewayParamRepository = gatewayParamRepository;
        this.commonSender = commonSender;
    }

}
