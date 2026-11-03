package uz.paykap.utils;

import uz.devops.bmcore.dto.request.CheckRequest;
import uz.devops.bmcore.dto.request.CommonPayRequest;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.javlon.commons.exception.CodedException;
import uz.paykap.config.Constants;

import java.time.Instant;

import static uz.paykap.config.Constants.*;

public class SimpleBillUtil {

    public static SimpleBillResponse checkDisabledResult(CheckRequest checkDTO) {

        return new SimpleBillResponse()
            .recipientShortName(Constants.SYSTEM_ACCOUNT)
            .sessionId(checkDTO.getAgentTranId())
            .amountRatio(checkDTO.getAmountRatio())
            .amount(checkDTO.getAmount())
            .account(checkDTO.getAccount())
            .currency(checkDTO.getCurrency())
            .requestTime(Instant.now())
            .responseTime(Instant.now())
            .responseText(THE_SYSTEM_IS_TEMPORARILY_UNAVAILABLE) // Система временна недоступна
            .success(false)
            .status(TransactionStatus.CHECK_FAIL);

    }

    public static SimpleBillResponse payDisabledResult(CommonPayRequest payDTO) {

        return new SimpleBillResponse()
            .recipientShortName(Constants.SYSTEM_ACCOUNT)
            .sessionId(DEFAULT_SESSION_ID)//todo
            .amount(DEFAULT_AMOUNT)
            .account(DEFAULT_ACCOUNT) //todo
            .requestTime(Instant.now())
            .responseTime(Instant.now())
            .responseText(THE_SYSTEM_IS_TEMPORARILY_UNAVAILABLE) // Система временна недоступна
            .success(false)
            .status(TransactionStatus.FAILED);

    }

    public static SimpleBillResponse serviceUnavailableErrorResponse() {

        return new SimpleBillResponse()
            .recipientShortName(Constants.SYSTEM_ACCOUNT)
            .sessionId(DEFAULT_SESSION_ID)
            .amount(DEFAULT_AMOUNT)
            .account(DEFAULT_ACCOUNT)
            .requestTime(Instant.now())
            .responseTime(Instant.now())
            .responseText(THE_SYSTEM_IS_TEMPORARILY_UNAVAILABLE) // Система временна недоступна
            .success(false)
            .status(TransactionStatus.FAILED);

    }

    public static SimpleBillResponse codedExceptionResponse(CodedException ex) {

        return new SimpleBillResponse()
            .recipientShortName(Constants.SYSTEM_ACCOUNT)
            .sessionId(DEFAULT_SESSION_ID)
            .amount(DEFAULT_AMOUNT)
            .account(DEFAULT_ACCOUNT)
            .requestTime(Instant.now())
            .responseTime(Instant.now())
            .responseText(ex.getMessage())
            .success(false)
            .status(TransactionStatus.FAILED);

    }

}
