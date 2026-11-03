package uz.paykap.service.munisWSDL.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.dto.request.CheckRequest;
import uz.devops.bmcore.dto.request.CommonCheckRequest;
import uz.devops.bmcore.dto.request.CommonPayRequest;
import uz.devops.bmcore.dto.request.PayRequest;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.devops.munisagetdto.*;
import uz.paykap.agent.tran.CommonTranServiceV3;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.domain.Transaction;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.service.munisWSDL.services.certificate.MunisAgentCertificateService;
import uz.paykap.service.munisWSDL.utils.AccountAndRecipientIdExtractor;
import uz.paykap.service.munisWSDL.utils.MunisAgentException;
import uz.paykap.service.munisWSDL.utils.MunisAgentPaymentType;
import uz.paykap.service.munisWSDL.utils.MunisAgentResponseCodes;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class MunisAgentReceiveTransferService {

    private static final String PAYEE_ACCOUNT = "PAYEE_ACCOUNT";
    private static final String PAYEE_NAME = "PAYEE_NAME";
    private static final String PAYEE_BRANCH = "PAYEE_BRANCH";
    private static final String DOCUMENT_NUMBER = "DOCUMENT_NUMBER";
    public static final String DATE = "DATE";
    private static final String TIME = "TIME";
    private static final String PURPOSE_CODE = "PURPOSE_CODE";
    public static final String PAYER_ACCOUNT = "PAYER_ACCOUNT";
    public static final String MUNIS_CURRENCY = "000";
    public static final String BID = "BID";
    private final CommonTranServiceV3 commonTranServiceV3;
    private final AccountAndRecipientIdExtractor extractor;
    private final ApplicationProperties applicationProperties;
    private final MunisAgentCertificateService certificateService;
    private final TransactionRepository transactionRepository;

    public ReceiveTransferResponse receiveTransfer(ReceiveTransfer request) {
        log.info("verify credentials...");
        boolean verifyCredentialsSuccess = verifyCredentials(request);
        if (!verifyCredentialsSuccess) {
            log.error("verify is not success");
            return errorResult(MunisAgentResponseCodes.INVALID_SIGN.getCodeString(), MunisAgentResponseCodes.INVALID_SIGN.getDescription());
        }

        log.debug("verify is success");
        boolean paramIsNullOrEmpty = verifyParamIsNullOrEmpty(request);
        if (paramIsNullOrEmpty) {
            return errorResult(MunisAgentResponseCodes.INVALID_PARAMS.getCodeString(), MunisAgentResponseCodes.INVALID_PARAMS.getDescription());
        }
        log.debug("params is verified!!!");

        log.debug("verify transaction by agentTranId");
        Optional<Transaction> optionalTransaction = transactionRepository.findTop1ByRequestLoginAndRequestTrIdOrderByIdDesc(applicationProperties.getMunisAgent().getBmMunisAgentUsername(), request.getPayment().getSysinfo().getBid());
        if (optionalTransaction.isPresent()) {
            log.warn("transaction by agentRequestId: {} , already exists", request.getPayment().getSysinfo().getBid());
            return errorResult(MunisAgentResponseCodes.TRANSACTION_ALREADY_EXISTS.getCodeString(), MunisAgentResponseCodes.TRANSACTION_ALREADY_EXISTS.getDescription());
        }

        log.debug("verify currency");
        if (!MUNIS_CURRENCY.equals(request.getPayment().getAmount().getCurrency())) {
            log.error("invalid currency");
            return errorResult(MunisAgentResponseCodes.INVALID_CURRENCY.getCodeString(), MunisAgentResponseCodes.INVALID_CURRENCY.getDescription());
        }
        log.debug("currency verified");

        MunisAgentPaymentType paymentType = MunisAgentPaymentType.getByPaymentCode(request.getPayment().getSettlement().getCode());
        ReceiveTransferResponse errorResponse = verifySupplierAndPaymentType(request.getPayment().getSettlement(), paymentType);
        if (errorResponse != null) {
            return errorResponse;
        }
        log.debug("verify supplier and payment type is verified!!!");

        Map<String, String> requestMap = new HashMap<>();
        extractor.fillAccount(request.getPayment().getSubject(), paymentType, requestMap);
        try {
            extractor.fillRecipientId(paymentType, request.getPayment().getAmount().getValue(), requestMap);
        } catch (MunisAgentException e) {
            return errorResult(e.getCodeString(), e.getDescription());
        }
        log.debug("requestMap: {}", requestMap);

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
        if (StringUtils.isEmpty(requestMap.get(AccountAndRecipientIdExtractor.RECIPIENT_SERVICE_ID))) {
            log.error("recipient service id is empty");
            return errorResult(MunisAgentResponseCodes.INVALID_PARAMS.getCodeString(), MunisAgentResponseCodes.INVALID_PARAMS.getDescription());
        }

        CommonCheckRequest checkRequest = createCheckRequest(request, paymentType, requestMap.get(AccountAndRecipientIdExtractor.ACCOUNT), AccountAndRecipientIdExtractor.parseRecipientId(requestMap.get(AccountAndRecipientIdExtractor.RECIPIENT_ID)));
        SimpleBillResponse checkResponse = null;
        try {
            log.info("send check: {}", checkRequest);
            checkResponse = commonTranServiceV3.check(checkRequest);
            if (!checkResponse.isSuccess() || !checkResponse.getStatus().equals(TransactionStatus.CHECK_OK)) {
                String description = !StringUtils.isEmpty(checkResponse.getResponseText()) ? (Objects.equals(MunisAgentResponseCodes.getByDescription(checkResponse.getResponseText()), MunisAgentResponseCodes.UNKNOWN) ? MunisAgentResponseCodes.BILLING_RETURN_ERROR.getDescription() : checkResponse.getResponseText()) : MunisAgentResponseCodes.BILLING_RETURN_ERROR.getDescription();
                return errorResult(MunisAgentResponseCodes.BILLING_RETURN_ERROR.getCodeString(), description);
            }
        } catch (Exception e) {
            String exceptionMessage = !StringUtils.isEmpty(e.getLocalizedMessage()) ? e.getLocalizedMessage() : (!StringUtils.isEmpty(e.getMessage()) ? e.getMessage() : MunisAgentResponseCodes.BILLING_THROW_EXCEPTION.getDescription() + " exception type: " + e.getClass().getSimpleName());
            log.error("check request throw exception: {}", exceptionMessage);

            return errorResult(MunisAgentResponseCodes.BILLING_THROW_EXCEPTION.getCodeString(), MunisAgentResponseCodes.BILLING_THROW_EXCEPTION.getDescription());
        }
        log.info("check result: {}", checkResponse);
        CommonPayRequest payDTO = createPayRequest(request, checkResponse, requestMap.get(AccountAndRecipientIdExtractor.RECIPIENT_SERVICE_ID));
        SimpleBillResponse payResponse = null;
        try {
            log.info("send pay: {}", payDTO);
            payResponse = commonTranServiceV3.pay(payDTO);
            if (!payResponse.isSuccess() || !payResponse.getStatus().equals(TransactionStatus.SUCCESS)) {
                String description = !StringUtils.isEmpty(checkResponse.getResponseText()) ? (Objects.equals(MunisAgentResponseCodes.getByDescription(checkResponse.getResponseText()), MunisAgentResponseCodes.UNKNOWN) ? MunisAgentResponseCodes.BILLING_RETURN_ERROR.getDescription() : checkResponse.getResponseText()) : MunisAgentResponseCodes.BILLING_RETURN_ERROR.getDescription();
                return errorResult(MunisAgentResponseCodes.BILLING_RETURN_ERROR.getCodeString(), description);
            }
        } catch (Exception e) {
            String exceptionMessage = !StringUtils.isEmpty(e.getLocalizedMessage()) ? e.getLocalizedMessage() : (!StringUtils.isEmpty(e.getMessage()) ? e.getMessage() : MunisAgentResponseCodes.BILLING_THROW_EXCEPTION.getDescription() + " exception type: " + e.getClass().getSimpleName());
            log.error("check request throw exception: {}", exceptionMessage);
            return errorResult(MunisAgentResponseCodes.BILLING_THROW_EXCEPTION.getCodeString(), MunisAgentResponseCodes.BILLING_THROW_EXCEPTION.getDescription());
        }

        return parseAndReturnSuccess(payResponse);
    }

    private boolean verifyCredentials(ReceiveTransfer request) {
        if (StringUtils.isEmpty(applicationProperties.getMunisAgent().getBmMunisAgentUsername()) || StringUtils.isEmpty(applicationProperties.getMunisAgent().getBmMunisAgentPassword())) {
            log.error("bmMunisAgentUsername and bmMunisAgentPassword is empty or null");
            return false;
        }
        log.debug("username and password is verified");

        Boolean verified = true;
        if (!applicationProperties.getMunisAgent().getVerifyCertificateDisabled()) {
            verified = certificateService.verify(request);
        }

        if (verified) {
            UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(applicationProperties.getMunisAgent().getBmMunisAgentUsername(), applicationProperties.getMunisAgent().getBmMunisAgentPassword());
            SecurityContextHolder.getContext().setAuthentication(authReq);
            return true;
        }
        return false;
    }

    private ReceiveTransferResponse parseAndReturnSuccess(SimpleBillResponse payResponse) {
        Confirm confirm = new Confirm();
        confirm.setCode(MunisAgentResponseCodes.SUCCESS.getCodeString());
        confirm.setMessage(MunisAgentResponseCodes.SUCCESS.getDescription());

        Affirm affirm = new Affirm();
        affirm.setConfirm(confirm);
        Statement statement = new Statement(payResponse.getExtId());
        affirm.setStatement(statement);

        ReceiveTransferResponse response = new ReceiveTransferResponse();
        response.setAffirm(affirm);

        log.info("return success result: {}", response);
        return response;
    }

    private CommonPayRequest createPayRequest(ReceiveTransfer request, SimpleBillResponse checkResponse, String recipientServiceId) {
        PayRequest payRequest = new PayRequest();
        payRequest.setAmount(checkResponse.getAmount());
        payRequest.setBillId(checkResponse.getId());
        payRequest.setParams(createParams(request, recipientServiceId));
        return payRequest;
    }

    private Map<String, Object> createParams(ReceiveTransfer request, String recipientServiceId) {
        Map<String, Object> params = new HashMap<>();

        params.put(BID, request.getPayment().getSysinfo().getBid());
        params.put(PAYER_ACCOUNT, recipientServiceId);
        params.put(PAYEE_ACCOUNT, request.getPayment().getPayee().getAccount());
        params.put(PAYEE_NAME, request.getPayment().getPayee().getName());
        params.put(PAYEE_BRANCH, request.getPayment().getPayee().getBranch());
        params.put(DOCUMENT_NUMBER, request.getPayment().getDocument().getDocN());
        params.put(DATE, request.getPayment().getSysinfo().getData());
        params.put(TIME, request.getPayment().getSysinfo().getTime());
        params.put(PURPOSE_CODE, request.getPayment().getPurpose().getCode());

        String purposeText = request.getPayment().getPurpose().getText();
        purposeText = purposeText + " Payer : " + "branch- " + request.getPayment().getPayer().getBranch();
        purposeText = purposeText + " account- " + request.getPayment().getPayer().getAccount();
        purposeText = purposeText + " inn- " + request.getPayment().getPayer().getInn();
        purposeText = purposeText + " name- " + request.getPayment().getPayer().getName();
        purposeText = StringUtils.substring(purposeText, 0, 256);
        params.put("PURPOSE_TEXT", purposeText);

        return params;
    }

    private CommonCheckRequest createCheckRequest(ReceiveTransfer request, MunisAgentPaymentType paymentType, String account, Long recipientId) {
        CheckRequest checkRequest = new CheckRequest();
        checkRequest.setAccount(account);
        checkRequest.setRecipientId(recipientId);
        checkRequest.setAgentTranId(request.getPayment().getSysinfo().getBid());
        checkRequest.setCurrency(860);
        checkRequest.setAmount(Long.valueOf(request.getPayment().getAmount().getValue()));

        return checkRequest;
    }

    private ReceiveTransferResponse verifySupplierAndPaymentType(Settlement request, MunisAgentPaymentType paymentType) {
        if (!Objects.equals(applicationProperties.getMunisAgent().getMunisAnorProviderId(), request.getSupplier())) {
            log.error("settlement supplier is stranger: {}", request.getSupplier());
            return errorResult(MunisAgentResponseCodes.INVALID_SETTLEMENT_SUPPLIER.getCodeString(), MunisAgentResponseCodes.INVALID_SETTLEMENT_SUPPLIER.getDescription());
        }

        if (MunisAgentPaymentType.UNDEFINED.equals(paymentType)) {
            log.error("settlement code is stranger: {}", request.getCode());
            return errorResult(MunisAgentResponseCodes.INVALID_SETTLEMENT_CODE.getCodeString(), MunisAgentResponseCodes.INVALID_SETTLEMENT_CODE.getDescription());
        }

        return null;
    }


    private boolean verifyParamIsNullOrEmpty(ReceiveTransfer request) {
        return request == null ||
            request.getPayment() == null ||
            request.getPayment().getAmount() == null ||
            StringUtils.isEmpty(request.getPayment().getAmount().getCurrency()) ||
            StringUtils.isEmpty(request.getPayment().getAmount().getValue()) ||
            request.getPayment().getConfirm() == null ||
            StringUtils.isEmpty(request.getPayment().getConfirm().getCode()) ||
            StringUtils.isEmpty(request.getPayment().getConfirm().getMessage()) ||
            request.getPayment().getDocument() == null ||
            StringUtils.isEmpty(request.getPayment().getDocument().getDocN()) ||
            StringUtils.isEmpty(request.getPayment().getDocument().getDocD()) ||
            request.getPayment().getPayee() == null ||
            StringUtils.isEmpty(request.getPayment().getPayee().getAccount()) ||
            StringUtils.isEmpty(request.getPayment().getPayee().getBranch()) ||
            StringUtils.isEmpty(request.getPayment().getPayee().getName()) ||
            StringUtils.isEmpty(request.getPayment().getPayee().getInn()) ||
            request.getPayment().getSysinfo() == null ||
            StringUtils.isEmpty(request.getPayment().getSysinfo().getBid()) ||
            StringUtils.isEmpty(request.getPayment().getSysinfo().getHash()) ||
            StringUtils.isEmpty(request.getPayment().getSysinfo().getData()) ||
            StringUtils.isEmpty(request.getPayment().getSysinfo().getTime()) ||
            request.getPayment().getPurpose() == null ||
            StringUtils.isEmpty(request.getPayment().getPurpose().getCode()) ||
            StringUtils.isEmpty(request.getPayment().getPurpose().getText());
    }

    private ReceiveTransferResponse errorResult(String codeString, String description) {
        Confirm confirm = new Confirm();
        confirm.setCode(codeString);
        confirm.setMessage(description);

        Statement statement = new Statement("");
        Affirm errorResult = new Affirm();
        errorResult.setConfirm(confirm);
        errorResult.setStatement(statement);

        ReceiveTransferResponse errorResponse = new ReceiveTransferResponse();
        errorResponse.setAffirm(errorResult);

        log.info("return error result: {}", errorResponse);
        return errorResponse;
    }
}
