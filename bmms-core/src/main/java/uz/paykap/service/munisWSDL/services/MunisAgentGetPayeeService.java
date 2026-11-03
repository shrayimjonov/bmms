package uz.paykap.service.munisWSDL.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uz.devops.bm.ncitrans.config.NciTransProperties;
import uz.devops.bmcore.dto.GatewayQueryResult;
import uz.devops.bmcore.dto.request.CheckRequest;
import uz.devops.bmcore.dto.request.CommonCheckRequest;
import uz.devops.bmcore.dto.request.SharedCheckDTO;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.devops.munisagetdto.*;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.config.Constants;
import uz.paykap.gateway.GatewayServiceV3;
import uz.paykap.security.SecurityUtils;
import uz.paykap.service.cached.GatewaySelectorService;
import uz.paykap.service.munisWSDL.utils.AccountAndRecipientIdExtractor;
import uz.paykap.service.munisWSDL.utils.MunisAgentException;
import uz.paykap.service.munisWSDL.utils.MunisAgentPaymentType;
import uz.paykap.service.munisWSDL.utils.MunisAgentResponseCodes;

import java.util.*;

@Slf4j
@Service
public class MunisAgentGetPayeeService {

    private static final String WALLET_PAN = "WALLET_PAN";
    public static final String INN = "INN";
    private static final String WALLET_TYPE = "WALLET_TYPE";
    private static final String WALLET_HOLDER = "WALLET_HOLDER";
    private static final String SUBJECT_NAME = "SUBJECT_NAME";
    private static final String LIMIT_MIN = "LIMIT_MIN";
    private static final String LIMIT_MAX = "LIMIT_MAX";
    private final ApplicationProperties applicationProperties;
    private final GatewayServiceV3 gatewayServiceV3;
    private final GatewaySelectorService gatewaySelectorService;
    private final AccountAndRecipientIdExtractor extractor;
    private final RestTemplate restTemplate;
    private final NciTransProperties nciTransProperties;

    public MunisAgentGetPayeeService(ApplicationProperties applicationProperties, GatewayServiceV3 gatewayServiceV3, GatewaySelectorService gatewaySelectorService, AccountAndRecipientIdExtractor extractor, @Qualifier("nciTransRestTemplate") RestTemplate restTemplate, NciTransProperties nciTransProperties) {
        this.applicationProperties = applicationProperties;
        this.gatewayServiceV3 = gatewayServiceV3;
        this.gatewaySelectorService = gatewaySelectorService;
        this.extractor = extractor;
        this.restTemplate = restTemplate;
        this.nciTransProperties = nciTransProperties;
    }

    public GetPayeeResponse getPayee(GetPayee request) {
        boolean verifyCredentialsSuccess = verifyCredentials();
        if (!verifyCredentialsSuccess) {
            return errorResult(MunisAgentResponseCodes.USER_NOT_FOUND.getCodeString(), MunisAgentResponseCodes.USER_NOT_FOUND.getDescription());
        }

        boolean paramsIsNullOrEmpty = verifyParamsIsNullOrEmpty(request);
        if (paramsIsNullOrEmpty) {
            return errorResult(MunisAgentResponseCodes.INVALID_PARAMS.getCodeString(), MunisAgentResponseCodes.INVALID_PARAMS.getDescription());
        }
        log.info("params is verified!!!");

        MunisAgentPaymentType paymentType = MunisAgentPaymentType.getByPaymentCode(request.getSettlement().getCode());
        GetPayeeResponse errorResponse = verifySupplierAndPaymentType(request, paymentType);
        if (errorResponse != null) {
            return errorResponse;
        }
        log.info("verify supplier and payment type is verified!!!");

        if ((MunisAgentPaymentType.WALLET_PAY.equals(paymentType) && request.getSubject().size() > 3) ||
                (MunisAgentPaymentType.LEGAL_ACCOUNT_PAY.equals(paymentType) && request.getSubject().size() > 2)) {
            log.error("request.getSubject().size() > 1 : {}", request.getSubject());
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    sendTelegramNotify(request.getSubject().toString());
                }
            });
            thread.start();
            return errorResult(MunisAgentResponseCodes.INVALID_PARAMS.getCodeString(), MunisAgentResponseCodes.INVALID_PARAMS.getDescription());
        }

        Map<String, String> requestMap = new HashMap<>();
        extractor.fillAccount(request.getSubject(), paymentType, requestMap);
        try {
            extractor.fillRecipientId(paymentType, null, requestMap);
        } catch (MunisAgentException e) {
            return errorResult(e.getCodeString(), e.getDescription());
        }

        if (requestMap.get(AccountAndRecipientIdExtractor.RECIPIENT_BANK) == null) {
            return errorResult(MunisAgentResponseCodes.INVALID_PARAMS.getCodeString(), MunisAgentResponseCodes.INVALID_PARAMS.getDescription());
        }
        if (!Objects.equals(requestMap.get(AccountAndRecipientIdExtractor.RECIPIENT_BANK), applicationProperties.getMunisAgent().getAnorRecipientIdInMunis())) {
            log.error("chujoy bank");
            return errorResult(MunisAgentResponseCodes.INVALID_PARAMS.getCodeString(), MunisAgentResponseCodes.INVALID_PARAMS.getDescription());
        }
        if (StringUtils.isEmpty(requestMap.get(AccountAndRecipientIdExtractor.ACCOUNT))) {
            log.error("account is empty");
            return errorResult(MunisAgentResponseCodes.INVALID_PARAMS.getCodeString(), MunisAgentResponseCodes.INVALID_PARAMS.getDescription());
        }
        if (StringUtils.isEmpty(requestMap.get(AccountAndRecipientIdExtractor.RECIPIENT_ID))) {
            log.error("recipient id is empty");
            return errorResult(MunisAgentResponseCodes.INVALID_PARAMS.getCodeString(), MunisAgentResponseCodes.INVALID_PARAMS.getDescription());
        }
        /*if (StringUtils.isEmpty(requestMap.get(AccountAndRecipientIdExtractor.RECIPIENT_SERVICE_ID))){
            log.error("recipient service id is empty");
            return errorResult(MunisAgentResponseCodes.INVALID_PARAMS.getCodeString(), MunisAgentResponseCodes.INVALID_PARAMS.getDescription());
        }*/

        Optional<String> optionalUsername = SecurityUtils.getCurrentUserLogin();
        if (!optionalUsername.isPresent()) {
            log.error("username is not present");
            return errorResult(MunisAgentResponseCodes.USER_NOT_FOUND.getCodeString(), MunisAgentResponseCodes.USER_NOT_FOUND.getDescription());
        }
        String username = optionalUsername.get();
        log.info("username is: {}", username);

        CommonCheckRequest commonCheckRequest = getCheckDto(requestMap.get(AccountAndRecipientIdExtractor.ACCOUNT), AccountAndRecipientIdExtractor.parseRecipientId(requestMap.get(AccountAndRecipientIdExtractor.RECIPIENT_ID)));
        SharedCheckDTO sharedCheckDTO = null;
        try {
            sharedCheckDTO = createSharedCheckDto(username, commonCheckRequest);
        } catch (Exception e) {
            String message = !StringUtils.isEmpty(e.getLocalizedMessage()) ? e.getLocalizedMessage() : (!StringUtils.isEmpty(e.getMessage()) ? e.getMessage() : "exception type: " + e.getClass().getSimpleName());
            log.error("gatewaySelectorService throw exception: {}", message);
            return errorResult(MunisAgentResponseCodes.ACCESS_DENIED.getCodeString(), MunisAgentResponseCodes.ACCESS_DENIED.getDescription());
        }

        SimpleBillResponse simpleBillResponse = null;
        try {
            log.info("do gatewayServiceV3.getInfo() with params, {}", sharedCheckDTO);
            simpleBillResponse = gatewayServiceV3.getInfo(sharedCheckDTO);
            log.info("gatewayServiceV3.getInfo() return result: {}", simpleBillResponse);
        } catch (Exception e) {
            String message = !StringUtils.isEmpty(e.getLocalizedMessage()) ? e.getLocalizedMessage() : (!StringUtils.isEmpty(e.getMessage()) ? e.getMessage() : "exception type: " + e.getClass().getSimpleName());
            log.error("billing throw exception: {}", message);
            return errorResult(MunisAgentResponseCodes.BILLING_THROW_EXCEPTION.getCodeString(), MunisAgentResponseCodes.BILLING_THROW_EXCEPTION.getDescription());
        }

        return parseAndReturnResult(simpleBillResponse, request);
    }

    private boolean verifyCredentials() {
        if (StringUtils.isEmpty(applicationProperties.getMunisAgent().getBmMunisAgentUsername()) && StringUtils.isEmpty(applicationProperties.getMunisAgent().getBmMunisAgentPassword())) {
            log.error("bmMunisAgentUsername and bmMunisAgentPassword is empty or null");
            return false;
        }

        UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(applicationProperties.getMunisAgent().getBmMunisAgentUsername(), applicationProperties.getMunisAgent().getBmMunisAgentPassword());
        SecurityContextHolder.getContext().setAuthentication(authReq);
        return true;
    }

    private SharedCheckDTO createSharedCheckDto(String username, CommonCheckRequest commonCheckRequest) {
        GatewayQueryResult validGatewayQueryResultV2 = gatewaySelectorService.getValidGatewayQueryResultV5(commonCheckRequest.getRecipientId(), username, username);

        return new SharedCheckDTO()
                .login(username)
                .checkDTO(commonCheckRequest)
                .recipientParams(validGatewayQueryResultV2);
    }

    private CommonCheckRequest getCheckDto(String account, Long recipientId) {
        return new CheckRequest()
                .account(account)
                .recipientId(recipientId)
                .currency(Constants.UZS_CURRENCY);
    }

    private GetPayeeResponse parseAndReturnResult(SimpleBillResponse simpleBillDTO, GetPayee getPayee) {
        if (simpleBillDTO == null) {
            log.error("commonTransService return null...");
            return errorResult(MunisAgentResponseCodes.BILLING_RETURN_NULL.getCodeString(), MunisAgentResponseCodes.BILLING_RETURN_NULL.getDescription());
        }

        if (simpleBillDTO.isSuccess() == null || !simpleBillDTO.isSuccess()) {
            log.error("simpleBillDto.isSuccess() null or false");
            String message = Objects.equals(MunisAgentResponseCodes.getByDescription(simpleBillDTO.getResponseText()), MunisAgentResponseCodes.UNKNOWN) ? MunisAgentResponseCodes.BILLING_RETURN_ERROR.getDescription() : simpleBillDTO.getResponseText();
            return errorResult(MunisAgentResponseCodes.BILLING_RETURN_ERROR.getCodeString(), message);
        }

        if (!TransactionStatus.NEW.equals(simpleBillDTO.getStatus())) {
            log.error("simpleBillDto.isSuccess() true, but transactionStatus is not CHECK_OK");
            String message = Objects.equals(MunisAgentResponseCodes.getByDescription(simpleBillDTO.getResponseText()), MunisAgentResponseCodes.UNKNOWN) ? MunisAgentResponseCodes.BILLING_RETURN_ERROR.getDescription() : simpleBillDTO.getResponseText();
            return errorResult(MunisAgentResponseCodes.BILLING_RETURN_ERROR.getCodeString(), message);
        }

        Confirm confirm = new Confirm();
        confirm.setCode(MunisAgentResponseCodes.SUCCESS.getCodeString());
        confirm.setMessage(MunisAgentResponseCodes.SUCCESS.getDescription());

        Payee payee = parseAndGetPayee(simpleBillDTO);
        List<Subjectinfo> infoServiceList = parseAndGetInfoServices(simpleBillDTO, getPayee);

        GetPayeeResult getPayeeResult = new GetPayeeResult();
        getPayeeResult.setConfirm(confirm);
        getPayeeResult.setPayee(payee);
        getPayeeResult.getInfoservice().addAll(infoServiceList);

        GetPayeeResponse response = new GetPayeeResponse();
        response.setGetPayeeResult(getPayeeResult);

        log.info("return success result: {}", response);
        return response;
    }

    private List<Subjectinfo> parseAndGetInfoServices(SimpleBillResponse simpleBillDTO, GetPayee getPayee) {
        List<Subjectinfo> infoServiceList = new ArrayList<>();

        Map<String, Object> responseJson = simpleBillDTO.getData();
        String walletHolder = "";
        String walletPan = "";
        String walletType = "";
        String limitMin = "";
        String limitMax = "";

        if (!StringUtils.isEmpty(String.valueOf(responseJson.get(WALLET_HOLDER)))) {
            walletHolder = String.valueOf(responseJson.get(WALLET_HOLDER));
        }
        if (!StringUtils.isEmpty(String.valueOf(responseJson.get(WALLET_PAN)))) {
            walletPan = String.valueOf(responseJson.get(WALLET_PAN));
        }
        if (!StringUtils.isEmpty(String.valueOf(responseJson.get(WALLET_TYPE)))) {
            walletType = String.valueOf(responseJson.get(WALLET_TYPE));
        }
        if (!StringUtils.isEmpty(String.valueOf(responseJson.get(LIMIT_MIN)))) {
            limitMin = String.valueOf(responseJson.get(LIMIT_MIN));
        }
        if (!StringUtils.isEmpty(String.valueOf(responseJson.get(LIMIT_MAX)))) {
            limitMax = String.valueOf(responseJson.get(LIMIT_MAX));
        }
        if (Objects.equals(getPayee.getSettlement().getCode(), MunisAgentPaymentType.WALLET_PAY.getPaymentCode())) {
            infoServiceList.add(createInfoService(WALLET_PAN, walletPan));
            infoServiceList.add(createInfoService(WALLET_TYPE, walletType));
            infoServiceList.add(createInfoService(WALLET_HOLDER, walletHolder));
        }
        if (Objects.equals(getPayee.getSettlement().getCode(), MunisAgentPaymentType.LEGAL_ACCOUNT_PAY.getPaymentCode())) {
            infoServiceList.add(createInfoService(SUBJECT_NAME, walletHolder));
        }

        infoServiceList.add(createInfoService(LIMIT_MIN, limitMin));
        infoServiceList.add(createInfoService(LIMIT_MAX, limitMax));

        return infoServiceList;
    }

    private Subjectinfo createInfoService(String element, String value) {
        Subjectinfo infoservice = new Subjectinfo();
        infoservice.setElement(element);
        infoservice.setValue(value);

        log.info("created new infoService: {}", infoservice);
        return infoservice;
    }

    private Payee parseAndGetPayee(SimpleBillResponse simpleBillDTO) {
        Map<String, Object> responseJson = simpleBillDTO.getData();
        Payee payee = new Payee();
        payee.setBranch(applicationProperties.getMunisAgent().getAnorBankMFo());
        String walletPan = "";
        String inn = "";
        String walletType = "";
        if (!StringUtils.isEmpty(String.valueOf(responseJson.get(WALLET_PAN)))) {
            walletPan = String.valueOf(responseJson.get(WALLET_PAN));
        }

        if (!StringUtils.isEmpty(String.valueOf(responseJson.get(INN)))) {
            inn = String.valueOf(responseJson.get(INN));
        }

        if (!StringUtils.isEmpty(String.valueOf(responseJson.get(WALLET_TYPE)))) {
            walletType = String.valueOf(responseJson.get(WALLET_TYPE));
        }

        payee.setAccount(walletPan);
        payee.setInn(inn);
        payee.setName(walletType);

        log.info("create new Payee: {}", payee);
        return payee;
    }

    private GetPayeeResponse verifySupplierAndPaymentType(GetPayee request, MunisAgentPaymentType paymentType) {
        if (!Objects.equals(applicationProperties.getMunisAgent().getMunisAnorProviderId(), request.getSettlement().getSupplier())) {
            log.error("settlement supplier is stranger: {}", request.getSettlement().getSupplier());
            return errorResult(MunisAgentResponseCodes.INVALID_SETTLEMENT_SUPPLIER.getCodeString(), MunisAgentResponseCodes.INVALID_SETTLEMENT_SUPPLIER.getDescription());
        }

        if (MunisAgentPaymentType.UNDEFINED.equals(paymentType)) {
            log.error("settlement code is stranger: {}", request.getSettlement().getCode());
            return errorResult(MunisAgentResponseCodes.INVALID_SETTLEMENT_CODE.getCodeString(), MunisAgentResponseCodes.INVALID_SETTLEMENT_CODE.getDescription());
        }

        return null;
    }

    private boolean verifyParamsIsNullOrEmpty(GetPayee request) {
        log.info("begin verify params");
        return request == null ||
                request.getSettlement() == null ||
                StringUtils.isEmpty(request.getSettlement().getCode()) ||
                StringUtils.isEmpty(request.getSettlement().getSupplier()) ||
                request.getSubject() == null ||
                request.getSubject().isEmpty();
    }

    private void sendTelegramNotify(String message) {
        String request = "https://api.telegram.org/bot" + nciTransProperties.getTelegramNotifyBotApiKey() + "/sendMessage?chat_id=" + nciTransProperties.getTelegramChatId() + "&text=invalid get payee structure: " + message;
        String result = restTemplate.getForObject(request, String.class);
        log.info("telegram bot return result: {}", result);
    }

    private GetPayeeResponse errorResult(String code, String message) {
        Confirm confirm = new Confirm();
        confirm.setCode(code);
        confirm.setMessage(message);

        GetPayeeResult errorResult = new GetPayeeResult();
        errorResult.setConfirm(confirm);

        GetPayeeResponse errorResponse = new GetPayeeResponse();
        errorResponse.setGetPayeeResult(errorResult);

        log.info("return error result: {}", errorResponse);
        return errorResponse;
    }
}
