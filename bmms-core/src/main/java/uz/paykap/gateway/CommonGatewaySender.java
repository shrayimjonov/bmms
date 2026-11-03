package uz.paykap.gateway;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import uz.javlon.commons.result.CommonResultData;
import uz.javlon.commons.result.HasData;
import uz.javlon.commons.result.HasResult;
import uz.paykap.domain.Bill;
import uz.paykap.repository.BillRepository;
import uz.paykap.service.MapService;
import uz.paykap.service.dto.p2p.BmResultStatus;

import java.net.SocketTimeoutException;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@Service
@AllArgsConstructor
public class CommonGatewaySender {

    private final BillRepository billRepository;
    private final MapService mapService;

    public <REQUEST, RESPONSE> Optional<RESPONSE> sendAndReceiveBill(Bill bill, REQUEST request, Function_WithExceptions<REQUEST, HasData<RESPONSE>, Exception> method, ResultChecker<RESPONSE> resultChecker) {

        bill.requestJson(mapService.convert(request));
        bill = billRepository.save(bill);

        log.info("Bill created before request: {}", bill);

        final Optional<RESPONSE> response = sendAndReceive(bill, request, method, resultChecker);

        bill = billRepository.save(bill);

        log.info("Bill updated after response : {}", bill);

        return response;
    }

    public <REQUEST, RESPONSE> Optional<RESPONSE> sendAndReceive(Bill bill, REQUEST request, Function_WithExceptions<REQUEST, HasData<RESPONSE>, Exception> method, ResultChecker<RESPONSE> resultChecker) {

        HasData<RESPONSE> resultData;

        try {
            log.info("Sending request : {} : {}", bill.getRecipientShortName(), request);
            resultData = method.apply(request);
            log.info("Received response : {} : {}", bill.getRecipientShortName(), resultData);
        } catch (Exception e) {
            log.warn("Exception occurred : {} : {}", bill.getRecipientShortName(), e.getLocalizedMessage());

            bill
                    .responseCode("FAILED")
                    .responseText(e.getLocalizedMessage())
                    .responseTime(Instant.now())
                    .success(false);

            return Optional.empty();
        }

        if (resultData == null) {
            bill
                    .responseCode("FAILED")
                    .responseText("Response is null")
                    .responseTime(Instant.now())
                    .success(false);

            return Optional.empty();
        }

        if (!resultData.isSuccess() || resultData.getData() == null || !resultChecker.success(resultData.getData())) {
            log.warn("Response of {} unsuccessful : {}", bill.getRecipientShortName(), resultData.getMessage());
            bill
                    .responseCode("FAILED")
                    .responseText(resultData.getMessage())
                    .responseTime(Instant.now())
                    .success(false)
            ;

            if (resultData.getData() != null) {
                resultChecker.beforeSaveBillResult(bill, resultData.getData());
            }

            return Optional.empty();
        }

        bill
                .responseCode("OK")
                .responseText("Success")
                .responseTime(Instant.now())
                .responseJson(mapService.convert(resultData.getData()))
                .success(true);

        if (resultData.getData() != null) {
            resultChecker.beforeSaveBillResult(bill, resultData.getData());
        }

        return Optional.of(resultData.getData());
    }

    public <REQUEST, RESPONSE> HasData<RESPONSE> sendAndReceive(REQUEST request, Bill bill, Function_WithExceptions<REQUEST, HasData<RESPONSE>, Exception> method, ResultChecker<RESPONSE> resultChecker) {
        return sendAndReceive(request, bill, method, resultChecker, null);
    }

    public <REQUEST, RESPONSE> HasData<RESPONSE> sendAndReceive(REQUEST request, Bill bill, Function_WithExceptions<REQUEST, HasData<RESPONSE>, Exception> method, ResultChecker<RESPONSE> resultChecker, Function<Exception, String> exceptionDetailsExtractor) {

        HasData<RESPONSE> resultData;

        try {
            log.info("Sending request : {} : {}", bill.getRecipientShortName(), request);
            resultData = method.apply(request);
            log.info("Received response : {} : {}", bill.getRecipientShortName(), resultData);
        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                log.error("Timeout error occurred : {}", e.getLocalizedMessage());
                fillErrorResult(bill, HasResult.TIMEOUT, e.getLocalizedMessage());
                return new CommonResultData<>(HasResult.TIMEOUT, null, e.getLocalizedMessage());
            }
            log.error("Resource access error occurred : {}", e.getLocalizedMessage());
            fillErrorResult(bill, HasResult.UNKNOWN_ERROR, e.getLocalizedMessage());
            return new CommonResultData<>(HasResult.UNKNOWN_ERROR, null, NestedExceptionUtils.getMostSpecificCause(e).getLocalizedMessage());
        } catch (Exception e) {

            String exceptionDetails = e.getLocalizedMessage();

            if (exceptionDetailsExtractor != null) {
                exceptionDetails = exceptionDetailsExtractor.apply(e);
            }

            log.warn("Exception occurred : {} : {}", bill.getRecipientShortName(), exceptionDetails);

            fillErrorResult(bill, HasResult.UNKNOWN_ERROR, exceptionDetails);

            return new CommonResultData<>(HasResult.UNKNOWN_ERROR, null, exceptionDetails);
        }

        if (resultData == null) {
            fillErrorResult(bill, BmResultStatus.INVALID_RESPONSE, "Response is null");

            return new CommonResultData<>(BmResultStatus.INVALID_RESPONSE, null, "Response is null");
        }

        if (!resultData.isSuccess() || resultData.getData() == null || !resultChecker.success(resultData.getData())) {
            log.warn("Response of {} unsuccessful : {}", bill.getRecipientShortName(), resultData.getMessage());

            fillErrorResult(bill, BmResultStatus.FAILED, resultData.getMessage());

            if (resultData.getData() != null) {
                resultChecker.beforeSaveBillResult(bill, resultData.getData());
            }

            return new CommonResultData<>(BmResultStatus.FAILED, null, resultData.getMessage());
        }

        bill
                .responseCode(BmResultStatus.SUCCESS.name())
                .responseText("Success")
                .responseTime(Instant.now())
                .responseJson(mapService.convert(resultData.getData()))
                .success(true);

        if (resultData.getData() != null) {
            resultChecker.beforeSaveBillResult(bill, resultData.getData());
        }

        billRepository.save(bill);

        return new CommonResultData<>(BmResultStatus.SUCCESS, resultData.getData(), resultData.getMessage());
    }

    private Bill fillErrorResult(Bill bill, HasResult hasResult, String responseText) {
        return bill
                .responseCode(String.valueOf(hasResult.getCode()))
                .responseText(responseText)
                .responseTime(Instant.now())
                .success(false);
    }

    public <REQUEST, RESPONSE> HasData<RESPONSE> sendAndReceive(REQUEST request, Function_WithExceptions<REQUEST, HasData<RESPONSE>, Exception> method, ResultChecker<RESPONSE> resultChecker) {

        HasData<RESPONSE> resultData;

        try {
            log.info("Sending request : {}", request);
            resultData = method.apply(request);
            log.info("Received response : {}", resultData);

        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof  SocketTimeoutException) {
                log.error("Timeout error occurred : {}", e.getLocalizedMessage());
                return new CommonResultData<>(HasResult.TIMEOUT, null, e.getLocalizedMessage());
            }
            log.error("Resource access error occurred : {}", e.getLocalizedMessage());
            return new CommonResultData<>(HasResult.UNKNOWN_ERROR, null, NestedExceptionUtils.getMostSpecificCause(e).getLocalizedMessage());
        } catch (Exception e) {
            log.warn("Exception occurred : {}", e.getLocalizedMessage());
            return new CommonResultData<>(HasResult.UNKNOWN_ERROR, null, e.getLocalizedMessage());
        }

        if (resultData == null) {
            return new CommonResultData<>(BmResultStatus.PC_ERROR, null, "Response is null");
        }

        if (!resultData.isSuccess()) {
            log.warn("Response unsuccessful : {}", resultData.getMessage());
            return resultData;

        }
        if (resultData.getData() == null) {
            log.warn("Response data is null : {}", resultData.getMessage());
            return new CommonResultData<>(BmResultStatus.FAILED, null, "Response data is null : " + resultData.getMessage());

        }
        if (!resultChecker.success(resultData.getData())) {
            log.warn("Response final checker failed");

            return new CommonResultData<>(BmResultStatus.FAILED, resultData.getData(), "Response final checker failed");
        }

        return resultData;
    }

}
