package uz.paykap.gateway.standart;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.domain.CommonBill;
import uz.devops.bmcore.dto.request.*;
import uz.devops.bmcore.dto.response.DepositBalanceResponse;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.devops.bmcore.gateway.GatewayService;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.domain.Bill;
import uz.paykap.domain.GatewayParam;
import uz.paykap.domain.Recipient;
import uz.paykap.gateway.dto.CancelDTO;
import uz.paykap.gateway.dto.StatusDTO;
import uz.paykap.repository.GatewayParamRepository;
import uz.paykap.security.SecurityUtils;
import uz.paykap.service.RecipientService;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;

import static uz.paykap.config.Constants.SKIP_CHECK_IS_ON;

@Service("simpleBmGatewayService")
public class SimpleBmGatewayService extends GatewayService {

    private final static Logger log = LoggerFactory.getLogger(SimpleBmGatewayService.class);

    public static final String NAME = "simpleBmGatewayService";
    public static final long DEFAULT_AMOUNT = 0L;
    private static final String SKIP_CANCEL_IS_ON = "SKIP_CANCEL_IS_ON";
    private static final String DEFAULT_ERROR_MESSAGE = "Произошла ошибка (SimpleBmGS)";


    @Override
    public SimpleBillResponse check(CommonCheckRequest checkDTO) {
        if (checkDTO == null) {
            throw new CodedException(ProblemEnum.INVALID_REQUEST);
        }

        log.debug("SimpleBm Check : {}", checkDTO);

        Recipient recipient = recipientService.getRecipientOrThrow(checkDTO.getRecipientId());

        log.debug("Found Recipient : {}", recipient);

        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

        log.debug("User : {}", login);

        Long amount = checkDTO.getAmount() == null ? DEFAULT_AMOUNT : checkDTO.getAmount();

        CommonBill bill = new Bill()
//            .requestJson(checkDTO.getParams())
            .type(RequestType.CHECK)
            .login(login)
//            .sessionId(String.valueOf(System.currentTimeMillis()))
            .currency(checkDTO.getCurrency())
            .amountRatio(recipient.getAmountRatio())
            // .profit(0L)
            .recipientId(checkDTO.getRecipientId())
            .recipientShortName(recipient.getShortName())
            .account(checkDTO.getAccount())
            .amount(amount)
            .requestTime(Instant.now())
            .responseTime(Instant.now())
            .requestJson(checkDTO.getParams())
//            .fraudId(0L)
                ;

        bill = commonBillService.save(bill);

        //If no check service return success
        if (!recipient.isCheck()) {

            bill.success(true)
                .responseText(SKIP_CHECK_IS_ON)
                .responseTime(Instant.now())
                .responseJson(Collections.singletonMap("skipCheck", "true"))
                .extId("0")
            ;

            log.debug("Check id = NULL, saving : {}", bill);

            return commonBillService.saveToSimple(bill);
        }

        //Validate amount and account params
        if (checkDTO.getAccount() == null || checkDTO.getAccount().isEmpty()) {
            log.debug("Required fields amount or account not found in params : {}", checkDTO.getParams());
            throw new CodedException(ProblemEnum.INVALID_PARAMS);
        }

        GatewayParam gatewayParam = gatewayParamRepository.findTop1ByRecipientIdAndTypeAndActiveIsTrue(checkDTO.getRecipientId(), RequestType.CHECK).orElseThrow(() -> new CodedException(ProblemEnum.SERVICE_NOT_ACTIVE));

        log.debug("Using GatewayPram  : {}", gatewayParam);

        if (checkDTO.getParams() == null) {
            checkDTO.setParams(new HashMap<>());
        }

        String url = gatewayParam.getUrl();

        log.debug("Url built : {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        CommonCheckRequest simpleCheckDTO = new CheckRequest()
            .recipientId(recipient.getId())
            .account(checkDTO.getAccount())
            .currency(recipient.getCcy())
            .amountRatio(recipient.getAmountRatio());


        String body = "";

        try {
            body = objectMapper.writeValueAsString(simpleCheckDTO);
        } catch (JsonProcessingException e) {
            log.warn("Error with parsing to json string : {}", e.getMessage());
        }

        ResponseEntity<String> result = simpleBmSenderService.send(RequestType.CHECK, url, body, headers, Long.valueOf(gatewayParam.getTimeout()));

        if (!result.getStatusCode().is2xxSuccessful()) {
            log.warn("Http Result is null or not success");

            bill.success(false)
                .responseText(DEFAULT_ERROR_MESSAGE)
                .responseJson(Collections.singletonMap("Ошибка", "RESPONSE_FROM_RECIPIENT_IS_NULL"))
                .responseTime(Instant.now())
            ;
            return commonBillService.saveToSimple(bill);
        }

        // --------- Got response ---------- //

        log.debug("Got reply from billing : {}", result.getBody());

        SimpleBillResponse simpleBillDTO = new SimpleBillResponse();
        try {
            simpleBillDTO = objectMapper.readValue(result.getBody(), SimpleBillResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        bill.success(simpleBillDTO.isSuccess())
            .responseCode(simpleBillDTO.getResponseText())
            .responseText(simpleBillDTO.getResponseText())
            .responseTime(simpleBillDTO.getResponseTime())
//            .responseJson(objectMapper.readValue(result.getBody()))
            .extId(String.valueOf(simpleBillDTO.getId()))
        ;

        return commonBillService.saveToSimple(bill);
//        return null;
    }

    @Override
    public SimpleBillResponse pay(CommonPayRequest payDTO) {

        if (payDTO == null || payDTO.getBillId() == null) {
            throw new CodedException(ProblemEnum.INVALID_REQUEST);
        }

        log.info("SimpleBm Pay : {}", payDTO);

        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

        log.info("User : {}", login);

        //TODO , now removed  check on login
        CommonBill checkBill = commonBillService.findTop1ByIdAndSuccess(payDTO.getBillId(), true).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));

        log.debug("Found checkBill : {}", checkBill);

        //Get account and amount names from fields of recipient, @Cached
//        AccountAndAmountNames accountAndAmountNames = getAccountAndAmount(payDTO);

        Recipient recipient = recipientService.getRecipientOrThrow(checkBill.getRecipientId());

        boolean hasPayAmount = payDTO.getAmount() != null && payDTO.getAmount() > 0L;
        boolean hasCheckAmount = checkBill.getAmount() != null && checkBill.getAmount() > 0L;

        if (!(hasPayAmount || hasCheckAmount)) {
            throw new CodedException(ProblemEnum.INVALID_PARAMS);
        }

        Long amount;

        if (payDTO.getGatewayAmount() != null) {
            amount = hasPayAmount ? convertDoubleToLong(payDTO.getGatewayAmount()) : checkBill.getAmount();
        } else {
            amount = hasPayAmount ? payDTO.getAmount() : checkBill.getAmount();
        }

        CommonBill bill = new Bill()
            .requestJson(checkBill.getRequestJson())
            .type(RequestType.PAY)
            .login(login)
//            .sessionId(checkBill.getSessionId())
            .currency(checkBill.getCurrency())
            .amountRatio(checkBill.getAmountRatio())
//            .profit(checkBill.getProfit())
            .recipientId(checkBill.getRecipientId())
            .recipientShortName(checkBill.getRecipientShortName())
            .account(checkBill.getAccount())
            .amount(amount)
//            .checkId(String.valueOf(checkBill.getId()))
            .requestTime(Instant.now())
            .responseTime(Instant.now())
            .requestJson(payDTO.getParams() != null ? payDTO.getParams() : Collections.emptyMap())
//            .fraudId(0L)
                ;

        bill = commonBillService.save(bill);

        //If no pay service return success

        if (!recipient.isPay()) {
            bill
                .success(true)
                .requestJson(bill.getRequestJson())
                .responseJson(bill.getResponseJson())
                .responseTime(Instant.now())
                .responseText("Pay disabled")
                .responseCode("disabled")
            ;

            SimpleBillResponse response = commonBillService.saveToSimple(bill);
            response.setStatus(TransactionStatus.POSTPONED);
            return response;
        }

        //Validate amount and account params
        if (checkBill.getAccount() == null || checkBill.getAccount().isEmpty() || bill.getAmount() == null || bill.getAmount() <= 0) {
            log.debug("Required fields amount or account not found in params : {}", payDTO);
            throw new CodedException(ProblemEnum.INVALID_PARAMS);
        }

        GatewayParam gatewayParam = gatewayParamRepository.findTop1ByRecipientIdAndTypeAndActiveIsTrue(checkBill.getRecipientId(), RequestType.PAY).orElseThrow(() -> new CodedException(ProblemEnum.SERVICE_NOT_ACTIVE));

        log.debug("Using GatewayPram  : {}", gatewayParam);

        String url = gatewayParam.getUrl();

        log.debug("Url built : {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        CommonPayRequest payDtoToSend = new PayRequest()
            .billId(Long.valueOf(checkBill.getExtId()))
            .amount(payDTO.getGatewayAmount().longValue());

        String body = "";

        try {
            body = objectMapper.writeValueAsString(payDtoToSend);
        } catch (JsonProcessingException e) {
            log.warn("Error with parsing to json string : {}", e.getMessage());
        }

        ResponseEntity<String> result = simpleBmSenderService.send(RequestType.PAY, url, body, headers, gatewayParam.getTimeout().longValue());

        if (!result.getStatusCode().is2xxSuccessful()) {
            log.warn("Http Result is null or not success");

            bill.success(false)
                .responseText(DEFAULT_ERROR_MESSAGE)
                .responseJson(Collections.singletonMap("Ошибка", "RESPONSE_FROM_RECIPIENT_IS_NULL"))
                .responseTime(Instant.now())
            ;
            return commonBillService.saveToSimple(bill);
        }

        // --------- Got response ---------- //

        log.debug("Got reply from billing : {}", result.getBody());

        SimpleBillResponse simpleBillDTO = new SimpleBillResponse();
        try {
            simpleBillDTO = objectMapper.readValue(result.getBody(), SimpleBillResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        bill.success(simpleBillDTO.isSuccess())
            .responseCode(simpleBillDTO.getResponseText())
            .responseText(simpleBillDTO.getResponseText())
            .responseTime(simpleBillDTO.getResponseTime())
            .extId(checkBill.getExtId())
        ;

        return commonBillService.saveToSimple(bill);

    }

    @Override
    public SimpleBillResponse cancel(CommonCancelRequest cancelDTO) {
        if (cancelDTO == null || cancelDTO.getBillId() == null) {
            throw new CodedException(ProblemEnum.INVALID_REQUEST);
        }

        log.info("SimpleBm cancel : {}", cancelDTO);

        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

        log.info("User : {}", login);

        CommonBill checkBill = commonBillService.findTop1ByIdAndSuccess(cancelDTO.getBillId(), true).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));

        log.debug("Found payBill : {}", checkBill);

        Recipient recipient = recipientService.getRecipientOrThrow(checkBill.getRecipientId());

        Bill bill = new Bill()
            .type(RequestType.CANCEL)
            .login(login)
            .requestJson(checkBill.getRequestJson())
//            .sessionId(checkBill.getSessionId())
            .currency(checkBill.getCurrency())
            .amountRatio(checkBill.getAmountRatio())
//            .profit(checkBill.getProfit())
            .recipientId(checkBill.getRecipientId())
            .recipientShortName(checkBill.getRecipientShortName())
            .account(checkBill.getAccount())
            .amount(checkBill.getAmount())
//            .payId(String.valueOf(checkBill.getId()))
            .requestTime(Instant.now())
            .responseTime(Instant.now())
            .requestJson(Collections.emptyMap())
//            .fraudId(0L)
                ;

        if (!(recipient.isCancel() == Boolean.TRUE)) { // NullPointerException tashlagani uchun o'zgartirildi

            bill.success(false)
                .responseText(SKIP_CANCEL_IS_ON)
                .responseTime(Instant.now())
                .responseJson(Collections.singletonMap("skipCancel", "true"))
                .extId("0")
            ;

            log.debug("Cancel id = NULL, saving : {}", bill);

            return commonBillService.saveToSimple(bill);
        }

        GatewayParam gatewayParam = gatewayParamRepository.findTop1ByRecipientIdAndTypeAndActiveIsTrue(checkBill.getRecipientId(), RequestType.CANCEL).orElseThrow(() -> new CodedException(ProblemEnum.SERVICE_NOT_ACTIVE));

        log.debug("Using GatewayPram  : {}", gatewayParam);

        String url = gatewayParam.getUrl();

        log.debug("Url built : {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        CancelDTO cancelDToToSend = new CancelDTO();
        cancelDToToSend.setTranId(Long.valueOf(checkBill.getExtId()));


        String body = "";

        try {
            body = objectMapper.writeValueAsString(cancelDToToSend);
        } catch (JsonProcessingException e) {
            log.warn("Error with parsing to json string : {}", e.getMessage());
        }

        ResponseEntity<String> result = simpleBmSenderService.send(RequestType.CANCEL, url, body, headers, gatewayParam.getTimeout().longValue());

        if (!result.getStatusCode().is2xxSuccessful()) {
            log.warn("Http Result is null or not success");

            bill.success(false)
                .responseText(DEFAULT_ERROR_MESSAGE)
                .responseJson(Collections.singletonMap("Ошибка", "RESPONSE_FROM_RECIPIENT_IS_NULL"))
                .responseTime(Instant.now())
            ;
            return commonBillService.saveToSimple(bill);
        }

        // --------- Got response ---------- //

        log.debug("Got reply from billing : {}", result.getBody());

        SimpleBillResponse simpleBillDTO = new SimpleBillResponse();
        try {
            simpleBillDTO = objectMapper.readValue(result.getBody(), SimpleBillResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        bill.success(simpleBillDTO.isSuccess())
            .responseCode(simpleBillDTO.getResponseText())
            .responseText(simpleBillDTO.getResponseText())
            .responseTime(simpleBillDTO.getResponseTime())
            .extId(checkBill.getExtId())
        ;
        return commonBillService.saveToSimple(bill);
    }


    @Override
    public DepositBalanceResponse deposit() {
        return null;
    }

    @Override
    public SimpleBillResponse status(CommonStatusRequest statusDTO) {

        if (statusDTO == null || statusDTO.getBillId() == null) {
            throw new CodedException(ProblemEnum.INVALID_REQUEST);
        }

        log.info("SimpleBm status : {}", statusDTO);

        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

        log.info("User : {}", login);

        CommonBill checkBill = commonBillService.findTop1ByIdAndSuccess(statusDTO.getBillId(), true).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));

        log.debug("Found payBill : {}", checkBill);

        Recipient recipient = recipientService.getRecipientOrThrow(checkBill.getRecipientId());

        Bill bill = new Bill()
            .type(RequestType.STATUS)
            .login(login)
            .requestJson(checkBill.getRequestJson())
//            .sessionId(checkBill.getSessionId())
            .currency(checkBill.getCurrency())
            .amountRatio(checkBill.getAmountRatio())
//            .profit(checkBill.getProfit())
            .recipientId(checkBill.getRecipientId())
            .recipientShortName(checkBill.getRecipientShortName())
            .account(checkBill.getAccount())
            .amount(checkBill.getAmount())
//            .payId(String.valueOf(checkBill.getId()))
            .requestTime(Instant.now())
            .responseTime(Instant.now())
            .requestJson(Collections.emptyMap())
//            .fraudId(0L)
                ;

        if (!(recipient.isCancel() == Boolean.TRUE)) {

            bill.success(false)
                .responseText(SKIP_CANCEL_IS_ON)
                .responseTime(Instant.now())
                .responseJson(Collections.singletonMap("skipCancel", "true"))
                .extId("0")
            ;

            log.debug("Cancel id = NULL, saving : {}", bill);

            return commonBillService.saveToSimple(bill);
        }

        GatewayParam gatewayParam = gatewayParamRepository.findTop1ByRecipientIdAndTypeAndActiveIsTrue(checkBill.getRecipientId(), RequestType.STATUS).orElseThrow(() -> new CodedException(ProblemEnum.SERVICE_NOT_ACTIVE));

        log.debug("Using GatewayPram  : {}", gatewayParam);

        String url = gatewayParam.getUrl();

        log.debug("Url built : {}", url);

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);


        String body = "";

        StatusDTO statusDTOtoSend = new StatusDTO();
        statusDTOtoSend.setBillId(Long.valueOf(checkBill.getExtId()));

        try {
            body = objectMapper.writeValueAsString(statusDTOtoSend);
        } catch (JsonProcessingException e) {
            log.warn("Error with parsing to json string : {}", e.getMessage());
        }

        ResponseEntity<String> result = simpleBmSenderService.send(RequestType.STATUS, url, body, headers, gatewayParam.getTimeout().longValue());

        if (!result.getStatusCode().is2xxSuccessful()) {
            log.warn("Http Result is null or not success");

            bill.success(false)
                .responseText(DEFAULT_ERROR_MESSAGE)
                .responseJson(Collections.singletonMap("Ошибка", "RESPONSE_FROM_RECIPIENT_IS_NULL"))
                .responseTime(Instant.now())
            ;
            return commonBillService.saveToSimple(bill);
        }

        // --------- Got response ---------- //

        log.debug("Got reply from billing : {}", result.getBody());


        SimpleBillResponse simpleBillDTO = new SimpleBillResponse();
        try {
            simpleBillDTO = objectMapper.readValue(result.getBody(), SimpleBillResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        bill.success(simpleBillDTO.isSuccess())
            .responseCode(simpleBillDTO.getResponseText())
            .responseText(simpleBillDTO.getResponseText())
            .responseTime(simpleBillDTO.getResponseTime())
            .extId(checkBill.getExtId())
        ;
        return commonBillService.saveToSimple(bill);
    }

    private final RecipientService recipientService;
    private final GatewayParamRepository gatewayParamRepository;
    private final SimpleBmSenderService simpleBmSenderService;
    private final ObjectMapper objectMapper;

    public SimpleBmGatewayService(RecipientService recipientService, GatewayParamRepository gatewayParamRepository, SimpleBmSenderService simpleBmSenderService, ObjectMapper objectMapper) {
        this.recipientService = recipientService;
        this.gatewayParamRepository = gatewayParamRepository;
        this.simpleBmSenderService = simpleBmSenderService;
        this.objectMapper = objectMapper;
    }
}
