package uz.paykap.service.munisWSDL.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.dto.request.CancelRequest;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.devops.munisagetdto.*;
import uz.paykap.agent.tran.CommonTranServiceV3;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.domain.Transaction;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.security.SecurityUtils;
import uz.paykap.service.munisWSDL.services.certificate.MunisAgentCertificateService;
import uz.paykap.service.munisWSDL.utils.MunisAgentResponseCodes;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class MunisAgentCancelTransferService {

    private final MunisAgentCertificateService certificateService;
    private final CommonTranServiceV3 commonTranServiceV3;
    private final TransactionRepository transactionRepository;
    private final ApplicationProperties applicationProperties;
    public CancelTransferResponse cancelTransfer(CancelTransfer request){
        boolean verifyCredentialsSuccess = verifyCredentials(request);
        if (!verifyCredentialsSuccess){
            return errorResult(MunisAgentResponseCodes.INVALID_SIGN.getCodeString(), MunisAgentResponseCodes.INVALID_SIGN.getDescription(), null);
        }
        log.debug("verify is success");

        log.debug("verify params");
        if (!verifyParamsIsSuccess(request)) {
            return errorResult(MunisAgentResponseCodes.INVALID_PARAMS.getCodeString(), MunisAgentResponseCodes.INVALID_PARAMS.getDescription(), null);
        }

        Optional<String> optionalUsername = SecurityUtils.getCurrentUserLogin();
        if (!optionalUsername.isPresent()){
            log.error("user not found");
            return errorResult(MunisAgentResponseCodes.USER_NOT_FOUND.getCodeString(), MunisAgentResponseCodes.USER_NOT_FOUND.getDescription(), null);
        }
        String username = optionalUsername.get();
        log.debug("username is: {}", username);

        Optional<Transaction> optionalTransaction = transactionRepository.findTop1ByRequestLoginAndRequestTrIdOrderByIdDesc(username, request.getSysinfo().getBid());
        if (!optionalTransaction.isPresent()){
            log.error("transaction for cancelling is not found....");
            return errorResult(MunisAgentResponseCodes.CANCEL_TRANSACTION_NOT_FOUND.getCodeString(), MunisAgentResponseCodes.CANCEL_TRANSACTION_NOT_FOUND.getDescription(), null);
        }

        Transaction transaction = optionalTransaction.get();
        log.debug("transactionRepository.findTop1ByRequestLoginAndRequestTrIdOrderByIdDesc() result: {}", transaction);
        if (!TransactionStatus.SUCCESS.equals(transaction.getStatus())){
            log.error("transaction for cancelling is not success...");
            return errorResult(MunisAgentResponseCodes.CANCEL_TRANSACTION_IS_NOT_SUCCESS.getCodeString(), MunisAgentResponseCodes.CANCEL_TRANSACTION_IS_NOT_SUCCESS.getDescription(), null);
        }

        CancelRequest cancelRequest = new CancelRequest();
        cancelRequest.setBillId(transaction.getId());

        SimpleBillResponse response = null;
        try {
            log.info("send cancel transaction request: {}", cancelRequest);
            response = commonTranServiceV3.cancel(cancelRequest);
            if (!response.isSuccess() || !TransactionStatus.CANCELLED.equals(response.getStatus())){
                String description = !StringUtils.isEmpty(response.getResponseText()) ? (Objects.equals(MunisAgentResponseCodes.getByDescription(response.getResponseText()), MunisAgentResponseCodes.UNKNOWN) ? MunisAgentResponseCodes.BILLING_RETURN_ERROR.getDescription() : response.getResponseText()) : MunisAgentResponseCodes.BILLING_RETURN_ERROR.getDescription();
                return errorResult(MunisAgentResponseCodes.BILLING_RETURN_ERROR.getCodeString(), description, String.valueOf(response.getId()));
            }
        }catch (Exception e){
            String exceptionMessage = !StringUtils.isEmpty(e.getLocalizedMessage() ) ? e.getLocalizedMessage() : (!StringUtils.isEmpty(e.getMessage()) ? e.getMessage() : MunisAgentResponseCodes.BILLING_THROW_EXCEPTION.getDescription() + " exception type: " + e.getClass().getSimpleName());
            log.error("cancel request throw exception: {}", exceptionMessage);
            return errorResult(MunisAgentResponseCodes.BILLING_THROW_EXCEPTION.getCodeString(), MunisAgentResponseCodes.BILLING_THROW_EXCEPTION.getDescription(), null);
        }

        return parseAndReturnSuccessResult(response);
    }

    private boolean verifyParamsIsSuccess(CancelTransfer request) {
        if (request == null) return false;
        if (request.getConfirm() == null) return false;
        if (request.getSysinfo() == null) return false;
        if (request.getStatepayment() == null) return false;
        if (StringUtils.isEmpty(request.getSysinfo().getBid())) return false;
        if (StringUtils.isEmpty(request.getSysinfo().getSid())) return false;
        if (StringUtils.isEmpty(request.getSysinfo().getTid())) return false;
        if (StringUtils.isEmpty(request.getSysinfo().getData())) return false;
        if (StringUtils.isEmpty(request.getSysinfo().getTime())) return false;
        if (StringUtils.isEmpty(request.getSysinfo().getHash())) return false;
        if (StringUtils.isEmpty(request.getConfirm().getCode())) return false;
        if (StringUtils.isEmpty(request.getConfirm().getMessage())) return false;
        if (StringUtils.isEmpty(request.getStatepayment().getName())) return false;
        if (StringUtils.isEmpty(request.getStatepayment().getState())) return false;
        return true;
    }

    private boolean verifyCredentials(CancelTransfer request) {
        if (StringUtils.isEmpty(applicationProperties.getMunisAgent().getBmMunisAgentUsername()) && StringUtils.isEmpty(applicationProperties.getMunisAgent().getBmMunisAgentPassword())){
            log.error("bmMunisAgentUsername and bmMunisAgentPassword is empty or null");
            return false;
        }

        Boolean verified = true;
        if (!applicationProperties.getMunisAgent().getVerifyCertificateDisabled()) {
            verified = certificateService.verify(request);
        }

        if (verified){
            UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(applicationProperties.getMunisAgent().getBmMunisAgentUsername(), applicationProperties.getMunisAgent().getBmMunisAgentPassword());
            SecurityContextHolder.getContext().setAuthentication(authReq);

            return true;
        }
        return false;
    }

    private CancelTransferResponse parseAndReturnSuccessResult(SimpleBillResponse response) {
        CancelTransferResponse cancelTransferResponse = createResponse(MunisAgentResponseCodes.SUCCESS.getCodeString(), MunisAgentResponseCodes.SUCCESS.getDescription(), String.valueOf(response.getId()));
        log.info("return success response: {}", cancelTransferResponse);

        return cancelTransferResponse;
    }

    private CancelTransferResponse errorResult(String code, String message, String statement) {
        CancelTransferResponse cancelTransferResponse = createResponse(code, message, statement);
        log.error("return error response: {}", cancelTransferResponse);

        return cancelTransferResponse;
    }

    private CancelTransferResponse createResponse(String code, String message, String statement) {
        Confirm confirm = new Confirm();
        confirm.setCode(code);
        confirm.setMessage(message);

        Affirm affirm = new Affirm();
        affirm.setConfirm(confirm);

        Statement statementObj = new Statement("");
        if (statement != null) {
            statementObj.setId(statement);
        }
        affirm.setStatement(statementObj);

        CancelTransferResponse response = new CancelTransferResponse();
        response.setAffirm(affirm);

        return response;
    }
}
