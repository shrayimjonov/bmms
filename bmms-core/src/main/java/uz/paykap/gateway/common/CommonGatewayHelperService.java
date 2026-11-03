package uz.paykap.gateway.common;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import uz.devops.bmcore.dto.GatewayResult;
import uz.devops.bmcore.dto.request.CheckRequest;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.config.Constants;
import uz.paykap.domain.GatewayParam;
import uz.paykap.domain.Recipient;
import uz.paykap.domain.enumeration.MethodType;
import uz.paykap.gateway.common.dto.BaseParam;
import uz.paykap.repository.GatewayParamRepository;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.service.MapService;
import uz.paykap.service.dto.common.CommonHttpRequest;
import uz.paykap.service.dto.common.CommonHttpRequestWrapper;
import uz.paykap.service.dto.common.CommonHttpResponse;
import uz.paykap.utils.MapUtil;
import uz.paykap.utils.ParseUtils;

import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class CommonGatewayHelperService {

    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+05:00");

    private final GatewayParamRepository gatewayParamRepository;
    private final RecipientRepository recipientRepository;
    private final CommonSender commonSender;
    private final MapService mapService;

    public LinkedHashMap<RequestType, CommonHttpRequestWrapper> freeRequest(RequestType requestType, CheckRequest checkRequest) {

        LinkedHashMap<RequestType, CommonHttpRequestWrapper> set = new LinkedHashMap<>(3);

        switch (requestType) {
            case CHECK:
                return freeCheck(checkRequest, set);
            case PAY:
                return freePay(checkRequest, set);
            case CANCEL:
                return freeCancel(checkRequest, set);
            default:
                throw new CodedException(ProblemEnum.NOT_IMPLEMENTED, "Supported only CHECK, PAY or CANCEL");
        }
    }

    public LinkedHashMap<RequestType, CommonHttpRequestWrapper> freeCheck(CheckRequest checkRequest, LinkedHashMap<RequestType, CommonHttpRequestWrapper> set) {

//        commonRequestScopedContext.getData().put(RequestType.CHECK, new CommonHttpRequestWrapper());

        Optional<GatewayParam> gatewayParam = gatewayParamRepository.findTop1ByRecipientIdAndTypeAndActiveIsTrue(checkRequest.getRecipientId(), RequestType.CHECK);

        if (gatewayParam.isEmpty()) {
            set.put(RequestType.CHECK, new CommonHttpRequestWrapper(
                    new CommonHttpRequest(RequestType.CHECK, "NO", HttpHeaders.EMPTY, "NO", Collections.emptyMap()),
                    new CommonHttpResponse(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), HttpHeaders.EMPTY, "Check Gateway param not found for recipient id = (" + checkRequest.getRecipientId() + ")", Collections.emptyMap()),
                    getErrorGatewayResult("Check Gateway param not found for recipient id = (" + checkRequest.getRecipientId() + ")")
            ));
            return set;
        }

        CommonHttpRequestWrapper commonHttpRequestWrapper = sendAndReceive(
                RequestType.CHECK,
                gatewayParam.get(),
                checkRequest.getParams(),
                -1L,
                -1L,
                checkRequest.getAmount(),
                checkRequest.getAccount(),
                null,
                Instant.now(),
                Instant.now(),
                recipientRepository.findById(checkRequest.getRecipientId()).orElse(new Recipient()).getShortName(),
                Objects.equals(gatewayParam.get().isSuccessOn2xx(), Boolean.TRUE));

        set.put(RequestType.CHECK, commonHttpRequestWrapper);

        return set;
    }

    public LinkedHashMap<RequestType, CommonHttpRequestWrapper> freePay(CheckRequest checkRequest, LinkedHashMap<RequestType, CommonHttpRequestWrapper> set) {

        LinkedHashMap<RequestType, CommonHttpRequestWrapper> commonHttpRequestWrapper = freeCheck(checkRequest, set);

        GatewayResult checkGatewayResult = commonHttpRequestWrapper.get(RequestType.CHECK).getGatewayResult();

        if (!checkGatewayResult.isSuccess()) {
            log.warn("FreePay result failed");

            return set;
        }

        Optional<GatewayParam> gatewayParam = gatewayParamRepository.findTop1ByRecipientIdAndTypeAndActiveIsTrue(checkRequest.getRecipientId(), RequestType.PAY);

        if (gatewayParam.isEmpty()) {

            set.put(RequestType.PAY, new CommonHttpRequestWrapper(
                    new CommonHttpRequest(RequestType.PAY, "NO", HttpHeaders.EMPTY, "NO", Collections.emptyMap()),
                    new CommonHttpResponse(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), HttpHeaders.EMPTY, "Pay Gateway param not found for recipient (" + checkRequest.getRecipientId() + ")", Collections.emptyMap()),
                    getErrorGatewayResult("Pay Gateway param not found for recipient (" + checkRequest.getRecipientId() + ")")
            ));
            return set;
        }

        CommonHttpRequestWrapper payResult = sendAndReceive(
                RequestType.PAY,
                gatewayParam.get(),
                mergeAll(checkGatewayResult.getResponseJson(), checkRequest.getParams(), checkRequest.getAgentParams()),
                null,
                -2L,
                checkRequest.getAmount(),
                checkRequest.getAccount(),
                checkGatewayResult.getExtId(),
                Instant.now(),
                Instant.now(),
                recipientRepository.findById(checkRequest.getRecipientId()).orElse(new Recipient()).getShortName(),
                gatewayParam.get().isSuccessOn2xx() == Boolean.TRUE);

        set.put(RequestType.PAY, payResult);

        return set;
    }

    public LinkedHashMap<RequestType, CommonHttpRequestWrapper> freeCancel(CheckRequest checkRequest, LinkedHashMap<RequestType, CommonHttpRequestWrapper> set) {

        LinkedHashMap<RequestType, CommonHttpRequestWrapper> commonHttpRequestWrapper = freePay(checkRequest, set);

        GatewayResult payGatewayResult = commonHttpRequestWrapper.get(RequestType.PAY).getGatewayResult();

        if (!payGatewayResult.isSuccess()) {
            log.warn("FreeCheck result failed");
            return commonHttpRequestWrapper;
        }

        Optional<GatewayParam> gatewayParam = gatewayParamRepository.findTop1ByRecipientIdAndTypeAndActiveIsTrue(checkRequest.getRecipientId(), RequestType.CANCEL);

        if (gatewayParam.isEmpty()) {
            CommonHttpRequestWrapper cancelErrorResult = new CommonHttpRequestWrapper(
                    new CommonHttpRequest(RequestType.CANCEL, "NO", HttpHeaders.EMPTY, "NO", Collections.emptyMap()),
                    new CommonHttpResponse(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), HttpHeaders.EMPTY, "Cancel Gateway param not found for recipient id = (" + checkRequest.getRecipientId() + ")", Collections.emptyMap()),
                    getErrorGatewayResult("Cancel Gateway param not found for (" + checkRequest.getRecipientId() + ")")
            );

            set.put(RequestType.CANCEL, cancelErrorResult);

            return set;
        }

        CommonHttpRequestWrapper cancelResult = sendAndReceive(
                RequestType.CANCEL,
                gatewayParam.get(),
                mergeAll(payGatewayResult.getResponseJson(), checkRequest.getParams(), checkRequest.getAgentParams()),
                -3L,
                -3L,
                checkRequest.getAmount(),
                checkRequest.getAccount(),
                payGatewayResult.getExtId(),
                Instant.now(),
                Instant.now(),
                recipientRepository.findById(checkRequest.getRecipientId()).orElse(new Recipient()).getShortName(),
                gatewayParam.get().isSuccessOn2xx() == Boolean.TRUE);

        set.put(RequestType.CANCEL, cancelResult);

        return set;
    }


    private CommonHttpRequest getCommonHttpRequest(RequestType requestType, GatewayParam gatewayParam, Map<String, Object> requestParams) {

        String resultUrl = new StringSubstitutor(requestParams).replace(gatewayParam.getUrl());

        log.debug("Built Url : {}", resultUrl);

        HttpHeaders resultHeaders = buildHeadersFromParams(gatewayParam, requestParams);

        log.debug("Built Headers : {}", resultHeaders);

        String resultBody = null;

        if (gatewayParam.getHttpMethodType() == null) {
            gatewayParam.setHttpMethodType(MethodType.GET);
        }

        if (gatewayParam.getBody() != null) {
            switch (gatewayParam.getHttpMethodType()) {
                case GET:
                case DELETE:
                    resultBody = null;
                    break;
                case POST:
                case PUT:
                case PATCH:
                    resultBody = new StringSubstitutor(requestParams).replace(gatewayParam.getBody());
                    log.debug("Built Body : {}", resultBody);
                    break;
            }
        }

        return new CommonHttpRequest(requestType, resultUrl, resultHeaders, resultBody, requestParams);
    }

    public CommonHttpRequestWrapper sendAndReceive(RequestType requestType, GatewayParam gatewayParam, Map<String, Object> requestParams, Long billId, Long tranId, Long amount, String account, String extId, Instant requestTime, Instant checkResponseTime, String recipientShortName, boolean ignoreResponseBody) {

        log.debug("Using GatewayPram  : {}", gatewayParam);

        // Add GP props into params
        addBaseParams(gatewayParam, tranId, billId, amount, account, extId, checkResponseTime, requestParams);

        CommonHttpRequest commonHttpRequest = getCommonHttpRequest(requestType, gatewayParam, requestParams);

//        commonRequestScopedContext.getData().get(requestType).setCommonHttpRequest(commonHttpRequest);

//        eventPublisher.publishEvent(new PusherEvent(this, commonHttpRequest, PusherConstants.TAG_BM_PUSH_DEV));

        CommonHttpResponse result = getCommonResponse(requestType, recipientShortName, requestParams, commonHttpRequest, gatewayParam.getHttpMethodType());

//        if (result == null) {
//            return new CommonHttpRequestWrapper(
//                    commonHttpRequest,
//                    new CommonHttpResponse(-1, HttpHeaders.EMPTY, "", Collections.emptyMap()),
//                    getErrorGatewayResult("Http Result is null"));
//        }


//        HttpHeaders responseHeaders = result.getHeaders();

        boolean successHttpStatus = result.getStatusCode().is2xxSuccessful();

//        if (result.getBody() != null) {
//            eventPublisher.publishEvent(new PusherEvent(this, Collections.singletonMap(recipientShortName.replaceAll(" ", "_") + "_responseBody", result.getBody()), PusherConstants.TAG_BM_PUSH_DEV));
//        }

        if (ignoreResponseBody) {

            String responseText = result.getStatusCode().getReasonPhrase() + " (body ignored)";

            log.info(responseText);

            return new CommonHttpRequestWrapper(
                    commonHttpRequest,
//                    new CommonHttpResponse(result.getStatusCodeValue(), responseHeaders, result.getBody(), Collections.emptyMap()),
                    result,
                    new GatewayResult()
                            .success(successHttpStatus)
                            .responseCode(String.valueOf(result.getStatusCode().value()))
                            .responseText(responseText)
                            .responseTime(Instant.now())
                            .responseJson(Collections.emptyMap())
                            .extId("0")
            );

//            commonRequestScopedContext.getData().get(requestType).setCommonHttpResponse(new CommonHttpResponse(result.getStatusCodeValue(), responseHeaders, result.getBody(), Collections.emptyMap()));

//            return new GatewayResult()
//                .success(successHttpStatus)
//                .responseCode(String.valueOf(result.getStatusCodeValue()))
//                .responseText(responseText)
//                .responseTime(Instant.now())
//                .responseJson(Collections.emptyMap())
//                .extId("0")
//                //
//                ;
        }

        if (!successHttpStatus) {
//            commonRequestScopedContext.getData().get(requestType).setCommonHttpResponse(new CommonHttpResponse(result.getStatusCodeValue(), responseHeaders, result.getBody(), Collections.emptyMap()));

            return new CommonHttpRequestWrapper(
                    commonHttpRequest,
//                    new CommonHttpResponse(result.getStatusCodeValue(), responseHeaders, result.getBody(), Collections.emptyMap()),
                    result,
                    getErrorGatewayResult("Http status code expected 2xx but was " + result.getStatusCode().value(), result.getStatusCode() == HttpStatus.GATEWAY_TIMEOUT ? TransactionStatus.R_NEED_CONFIRM : TransactionStatus.FAILED)
            );
        }

        // --------- Got response ---------- //

        log.debug("Got reply body from partner billing : {}", result.getBody());

        boolean isJson = gatewayParam.getResponseMediaType().contains("json");

        String responseStatusCode = ParseUtils.extractValueFromResponse(result.getBody(), gatewayParam.getStatusQuery(), isJson);
        String responseExtId = ParseUtils.extractValueFromResponse(result.getBody(), gatewayParam.getExtIdQuery(), isJson);
        String responseText = ParseUtils.extractValueFromResponse(result.getBody(), gatewayParam.getMessageQuery(), isJson);

        Map<String, Object> responseJson = getDataFromJsonDataQueries(gatewayParam, result.getBody());

        boolean success = Objects.equals(responseStatusCode, gatewayParam.getSuccessCode());

        GatewayResult gatewayResult = new GatewayResult()
                .success(success)
                .responseCode(responseStatusCode)
                .responseText(responseText)
                .responseTime(Instant.now())
                .responseJson(responseJson)
                //
                ;

        /* Ext ID can be null (empty-value), otherwise set value for farther use */
        if (!Objects.equals(responseExtId, Constants.EMPTY_VALUE_TEXT)) {
            gatewayResult.extId(responseExtId);
        }

//        commonRequestScopedContext.getData().get(requestType).setCommonHttpResponse(new CommonHttpResponse(result.getStatusCodeValue(), responseHeaders, result.getBody(), responseJson));

        result.setData(responseJson);

        return new CommonHttpRequestWrapper(
                commonHttpRequest,
                result,
//                new CommonHttpResponse(result.getStatusCodeValue(), responseHeaders, result.getBody(), responseJson),
                gatewayResult
        );
    }

    private ResponseEntity<String> getStringResponseEntity(RequestType requestType, String recipientShortName, Map<String, Object> params, CommonHttpRequest commonHttpRequest, MethodType httpMethodType) {

        if (httpMethodType == null) {
            return new ResponseEntity<>("gatewayParam.httpMethodType should be one of : " + Arrays.toString(MethodType.values()), HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<String> result;

        try {

            switch (httpMethodType) {
                case GET:
                case DELETE:
                    result = commonSender.sendAndReceive(requestType, recipientShortName, httpMethodType, commonHttpRequest.getUrl(), commonHttpRequest.getHeaders(), params);
                    break;
                case POST:
                case PUT:
                case PATCH:
                    result = commonSender.sendAndReceive(requestType, recipientShortName, httpMethodType, commonHttpRequest.getUrl(), commonHttpRequest.getBody(), commonHttpRequest.getHeaders(), params);
                    break;
                default:
                    return new ResponseEntity<>("gatewayParam.httpMethodType should be one of : " + Arrays.toString(MethodType.values()), HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e) {
            log.debug("Connection error {}", e.getLocalizedMessage());
            result = new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return result;
    }

    private CommonHttpResponse getCommonResponse(RequestType requestType, String recipientShortName, Map<String, Object> params, CommonHttpRequest commonHttpRequest, MethodType httpMethodType) {

        ResponseEntity<String> result;

        try {

            switch (httpMethodType) {
                case GET:
                case DELETE:
                    result = commonSender.sendAndReceive(requestType, recipientShortName, httpMethodType, commonHttpRequest.getUrl(), commonHttpRequest.getHeaders(), params);
                    break;
                case POST:
                case PUT:
                case PATCH:
                    result = commonSender.sendAndReceive(requestType, recipientShortName, httpMethodType, commonHttpRequest.getUrl(), commonHttpRequest.getBody(), commonHttpRequest.getHeaders(), params);
                    break;
                default:
                    return new CommonHttpResponse(HttpStatus.BAD_REQUEST, "gatewayParam.httpMethodType should be one of : " + Arrays.toString(MethodType.values()), HttpHeaders.EMPTY, "", Collections.emptyMap());
            }

            return new CommonHttpResponse(result.getStatusCode(), result.getStatusCode().getReasonPhrase(), result.getHeaders(), result.getBody(), new HashMap<>());

        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                return new CommonHttpResponse(HttpStatus.GATEWAY_TIMEOUT, HttpStatus.GATEWAY_TIMEOUT.getReasonPhrase(), HttpHeaders.EMPTY, "", Collections.emptyMap());
            }
            return new CommonHttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, NestedExceptionUtils.getMostSpecificCause(e).getLocalizedMessage(), HttpHeaders.EMPTY, "", Collections.emptyMap());
        } catch (Exception e) {
            final String statusMessage = NestedExceptionUtils.getMostSpecificCause(e).getLocalizedMessage();
            log.debug("Connection error : {}", statusMessage);
            return new CommonHttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, statusMessage, HttpHeaders.EMPTY, "", Collections.emptyMap());
        }
    }

    private void addBaseParams(GatewayParam gatewayParam, Long tranId, Long billId, Long amount, String account, String extId, Instant responseTime, Map<String, Object> map) {
        addBaseParams(gatewayParam, tranId, null, billId, amount, account, extId, responseTime, map);
    }

    private void addBaseParams(GatewayParam gatewayParam, Long tranId, String timeWithZone, Long billId, Long amount, String account, String extId, Instant responseTime, Map<String, Object> map) {

        if (map == null) {
            map = new HashMap<>(20);
        }

        timeWithZone = timeWithZone != null ? timeWithZone : SIMPLE_DATE_FORMAT.format(new Date());

        if (tranId != null) {
            map.put(BaseParam.tranId.name(), String.valueOf(tranId));
        }

        map.put(BaseParam.billId.name(), String.valueOf(billId));
        map.put(BaseParam.account.name(), account);
        map.put(BaseParam.amount.name(), String.valueOf(amount));
        map.put(BaseParam.password.name(), gatewayParam.getPassword());
        map.put(BaseParam.username.name(), gatewayParam.getUsername());
        map.put(BaseParam.token.name(), gatewayParam.getToken());
        map.put(BaseParam.timestamp.name(), String.valueOf(Instant.now().getEpochSecond()));
        map.put(BaseParam.timeinstant.name(), String.valueOf(Instant.now()));
        if (extId != null) {
            map.put(BaseParam.extId.name(), extId);
        }
        map.put(BaseParam.timesoap.name(), new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSXXX").format(new Date()));
        map.put(BaseParam.timeWithZone.name(), timeWithZone);

        if (responseTime != null) {
            String payTime = StringUtils.substring(String.valueOf(responseTime), 0, 19);
            map.put(BaseParam.payTime.name(), payTime);
        }

    }

    public Map<String, Object> mergeAll(Map<String, Object>... maps) {

        HashMap<String, Object> result = new HashMap<>(0);

        if (maps == null || maps.length <= 0) {
            return result;
        }

        for (Map<String, Object> map : maps) {

            if (map == null || map.isEmpty()) {
                continue;
            }

            result.putAll(map);
        }

        return result;
    }

    private Map<String, Object> getDataFromJsonDataQueries(GatewayParam gatewayParam, String responseBody) {

        if (gatewayParam.getDataQueries() == null || gatewayParam.getDataQueries().isEmpty() || gatewayParam.getResponseMediaType() == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> dataMap = mapService.convert(gatewayParam.getDataQueries());

        boolean isJson = gatewayParam.getResponseMediaType().contains("json");

        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            dataMap.put(entry.getKey(), ParseUtils.extractValueFromResponse(responseBody, String.valueOf(entry.getValue()), isJson));
        }

        return dataMap;
    }

    public HttpHeaders buildHeadersFromParams(GatewayParam gatewayParam, Map<String, Object> params) {

        HttpHeaders headers = null;

        if (StringUtils.isNotBlank(gatewayParam.getRequestMediaType())) {
            headers = new HttpHeaders();
            headers.add("Content-Type", gatewayParam.getRequestMediaType());
        }

        if (gatewayParam.getHeaders() != null && !gatewayParam.getHeaders().isEmpty()) {
            if (headers == null) {
                headers = new HttpHeaders();
            }

            Map<String, String> hs = MapUtil.convert(gatewayParam.getHeaders());

            for (Map.Entry<String, String> entry : hs.entrySet()) {
                headers.add(entry.getKey(), new StringSubstitutor(params).replace(entry.getValue()));
            }
        }

        if (headers == null || headers.isEmpty()) {
            return HttpHeaders.EMPTY;
        }

        return headers;
    }

    public GatewayResult getErrorGatewayResult(String responseText) {
        return getErrorGatewayResult(responseText, TransactionStatus.FAILED);
    }

    public GatewayResult getErrorGatewayResult(String responseText, TransactionStatus failed) {

        log.warn(responseText);

        return new GatewayResult()
                .responseJson(Collections.singletonMap("error", responseText))
                .responseTime(Instant.now())
                .responseText(responseText)
                .responseCode("error")
                .withStatus(failed)
//                .extId(EXT_CHECK_ID)
                .success(false);
    }
}
