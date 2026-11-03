package uz.paykap.gateway;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.CommonBillService;
import uz.devops.bmcore.domain.CommonBill;
import uz.devops.bmcore.dto.GatewayResult;
import uz.devops.bmcore.dto.request.SharedCancelDTO;
import uz.devops.bmcore.dto.request.SharedCheckDTO;
import uz.devops.bmcore.dto.request.SharedPayDTO;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.devops.bmcore.gateway.CancelHandler;
import uz.devops.bmcore.gateway.GatewayBillingService;
import uz.devops.bmcore.gateway.GatewayResultHandler;
import uz.devops.bmcore.gateway.PrepayHandler;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.agent.tran.context.CheckRequestScopedContext;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.config.Constants;
import uz.paykap.domain.Bill;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/*
 * Naming of implementation : prefix + 'GatewayService'
 * Example : uzcardGatewayService
 * */
@Slf4j
@Service
@AllArgsConstructor
public class GatewayServiceV3 {

    public static final int UZS = 860;
    private final CommonBillService commonBillService;
    private final ApplicationContext applicationContext;
    private final CheckRequestScopedContext checkRequestScopedContext;
    private final ApplicationProperties properties;

    public final SimpleBillResponse getInfo(SharedCheckDTO sharedCheckDTO) {

        //TODO - move to service
        GatewayBillingService gatewayService = getServiceBean(sharedCheckDTO.getRecipientParams().getGatewayBean());

        CommonBill bill = new Bill()
                .requestJson(sharedCheckDTO.getCheckDTO().getParams())
                .recipientId(sharedCheckDTO.getCheckDTO().getRecipientId())
                .recipientShortName(sharedCheckDTO.getRecipientParams().getRecipientShortName())
                .account(sharedCheckDTO.getCheckDTO().getAccount())
                .amountRatio(sharedCheckDTO.getRecipientParams().getRecipientAmountRatio())
                .currency(sharedCheckDTO.getCheckDTO().getCurrency())
                // .profit(0L)
                .type(RequestType.CHECK)
                .login(sharedCheckDTO.getLogin())
                .requestTime(Instant.now())
                .responseTime(Instant.now())
                .success(false)
                .responseJson(Collections.emptyMap())
                .responseCode("pre-request code")
                .responseText("pre-request text")
                //
                ;

        commonBillService.save(bill);

        GatewayResult check = gatewayService.check(sharedCheckDTO, new GatewayResultHandler() {
            @Override
            public void preSaveRequestParams(Map<String, Object> requestJson, Instant requestTime) {
                bill
                        .requestJson(requestJson)
                        .requestTime(requestTime);
                commonBillService.save(bill);
            }

            @Override
            public Long getBillId() {
                return bill.getId();
            }
        });

        String extId = check.getExtId();

        if (extId == null || extId.isEmpty()) {
            extId = "-1";
        }

        bill
                .success(Objects.equals(check.isSuccess(), Boolean.TRUE))
                .responseCode(check.getResponseCode())
                .responseText(check.getResponseText())
                .responseTime(check.getResponseTime())
                .responseJson(check.getResponseJson())
                .extId(extId);

        return commonBillService.saveToSimple(bill);
    }

    public final SimpleBillResponse check(SharedCheckDTO sharedCheckDTO) {

        //TODO - move to service
        GatewayBillingService gatewayService = getServiceBean(sharedCheckDTO.getRecipientParams().getGatewayBean());

        Long gatewayAmount;
        if (sharedCheckDTO.getGatewayAmount() != null) {
            gatewayAmount = sharedCheckDTO.getGatewayAmount().longValue();
        } else {
            gatewayAmount = gatewayService.getDefaultCheckAmountIfNull();
        }

//        if (gatewayAmount == null) {
//            gatewayAmount = gatewayService.getDefaultCheckAmountIfNull();
//        }

        CommonBill bill = new Bill()
                .transactionId(sharedCheckDTO.getTranId())
                .recipientId(sharedCheckDTO.getCheckDTO().getRecipientId())
                .recipientShortName(sharedCheckDTO.getRecipientParams().getRecipientShortName())
                .account(sharedCheckDTO.getCheckDTO().getAccount())
                .amount(gatewayAmount)
                .amountRatio(sharedCheckDTO.getRecipientParams().getRecipientAmountRatio())
                .currency(sharedCheckDTO.getCheckDTO().getCurrency())
                .type(RequestType.CHECK)
                .login(sharedCheckDTO.getLogin())
                .requestTime(Instant.now())
                .requestJson(sharedCheckDTO.getCheckDTO().getParams())
                .responseTime(Instant.now())
                .extId(sharedCheckDTO.getCheckDTO().getAgentTranId())
                .success(false)
                .responseJson(Collections.emptyMap())
                .responseCode("pre-request code")
                .responseText("pre-request text")
                //
                ;

        commonBillService.save(bill);

        checkRequestScopedContext.setCheckBill(bill);

        if (!Objects.equals(properties.getIgnoreSkipCheckGateways().contains(sharedCheckDTO.getRecipientParams().getGatewayName()), Boolean.TRUE)
                && sharedCheckDTO.getRecipientParams().getHasCheck() == Boolean.FALSE) {

            bill.success(true)
                    .responseText(Constants.SKIP_CHECK_IS_ON)
                    .responseTime(Instant.now())
                    .responseJson(Collections.singletonMap("skipCheck", "true"))
                    .extId("0")
            ;

            log.info("No check method, saving : {}", bill);
            return commonBillService.saveToSimple(bill);
        }

        GatewayResult gatewayResult = gatewayService.check(sharedCheckDTO, new GatewayResultHandler() {
            @Override
            public void preSaveRequestParams(Map<String, Object> requestJson, Instant requestTime) {
                if (requestJson == null) {
                    return;
                }
                if (bill.getRequestJson() != null) {
                    bill.getRequestJson().putAll(requestJson);
                } else {
                    bill.requestJson(requestJson);
                }
                bill.setRequestTime(requestTime);
                commonBillService.save(bill);
            }

            @Override
            public Long getBillId() {
                return bill.getId();
            }
        });

        checkRequestScopedContext.setGatewayResult(gatewayResult);

        String extId = gatewayResult.getExtId();

        if (extId == null || extId.isEmpty()) {
            extId = "-1";
        }

        bill
                .success(Objects.equals(gatewayResult.isSuccess(), Boolean.TRUE))
                .responseCode(gatewayResult.getResponseCode())
                .responseText(gatewayResult.getResponseText())
                .responseTime(gatewayResult.getResponseTime())
                .responseJson(gatewayResult.getResponseJson())
                .extId(extId);

        return commonBillService.saveToSimple(bill);
    }

    private GatewayBillingService getServiceBean(String gatewayBean) {
        try {
            return applicationContext.getBean(gatewayBean, GatewayBillingService.class);
        } catch (Exception e) {
            log.warn(e.getLocalizedMessage());
            throw new CodedException(ProblemEnum.SERVICE_NOT_FOUND, "Bean not found");
        }
    }

    public SimpleBillResponse pay(SharedPayDTO sharedPayDTO) {
        return pay(sharedPayDTO, getServiceBean(sharedPayDTO.getRecipientParams().getGatewayBean()));
    }

    public SimpleBillResponse pay(SharedPayDTO sharedPayDTO, GatewayBillingService gatewayService) {

        if (sharedPayDTO.getPayDTO().getBillId() == null || sharedPayDTO.getGatewayAmount() == null) {
            throw new CodedException(ProblemEnum.INVALID_PARAMS, "billId or gatewayAmount is null");
        }

        CommonBill checkBill = commonBillService.findTop1ByIdAndSuccessAndLoginAndType(sharedPayDTO.getPayDTO().getBillId(), true, sharedPayDTO.getLogin(), RequestType.CHECK).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));

        Long gatewayAmount = 0L; // TODO - in pay method amount must be given
        if (sharedPayDTO.getGatewayAmount() != null) {
            gatewayAmount = sharedPayDTO.getGatewayAmount().longValue();
        }

        if (gatewayAmount == null || gatewayAmount == 0L) {
            throw new CodedException(ProblemEnum.INVALID_PARAMS, "gatewayAmount is null or 0");
        }

        CommonBill payBill = new Bill()
                .transactionId(sharedPayDTO.getTranId())
                .requestJson(checkBill.getRequestJson())
                .recipientId(checkBill.getRecipientId())
                .recipientShortName(sharedPayDTO.getRecipientParams().getRecipientShortName())
                .account(checkBill.getAccount())
                .amount(gatewayAmount)
                .amountRatio(sharedPayDTO.getRecipientParams().getRecipientAmountRatio())
                .currency(checkBill.getCurrency())
                .type(RequestType.PAY)
                .login(sharedPayDTO.getLogin())
                .requestTime(Instant.now())
                .responseTime(Instant.now())
                .extId(checkBill.getExtId())
                .success(false)
                .responseJson(Collections.emptyMap())
                .responseCode("pre-request code")
                .responseText("pre-request text")
                //
                ;

        Map<String, Object> requestJson;
        if (checkBill.getRequestJson() == null || checkBill.getRequestJson().isEmpty()) {
            requestJson = new HashMap<>();
        } else {
            requestJson = new HashMap<>(checkBill.getRequestJson());
        }
        if (sharedPayDTO.getPayDTO().getParams() != null) {
            requestJson.putAll(sharedPayDTO.getPayDTO().getParams());
            payBill.setRequestJson(requestJson);
        }

        commonBillService.save(payBill);

        GatewayResult payResult = gatewayService.pay(sharedPayDTO, new PrepayHandler() {
            @Override
            public void saveRequestParams(Map<String, Object> requestJson, Instant requestTime) {
                if (payBill.getRequestJson() != null) {
                    payBill.getRequestJson().putAll(requestJson);
                } else {
                    payBill.requestJson(requestJson);
                }
                payBill.requestTime(requestTime);
                commonBillService.save(payBill);
            }

            @Override
            public void saveRequestParams(@NotNull Map<String, Object> map, @NotNull String outExtId, @NotNull Instant requestTime) {
                if (payBill.getRequestJson() != null) {
                    payBill.getRequestJson().putAll(requestJson);
                } else {
                    payBill.requestJson(requestJson);
                }
                if (outExtId != null && !outExtId.isEmpty()) {
                    payBill.setOutExtId(outExtId);
                }
                payBill.requestTime(requestTime);
                commonBillService.save(payBill);
            }

            @Override
            public Map<String, Object> getCheckRequestJson() {
                return checkBill.getRequestJson();
            }

            @Override
            public Map<String, Object> getCheckResponseJson() {
                return checkBill.getResponseJson();
            }

            @Override
            public String getAccount() {
                return checkBill.getAccount();
            }

            @Override
            public Integer getCurrency() {
                return checkBill.getCurrency();
            }

            @Override
            public String getExtId() {
                return checkBill.getExtId();
            }

            //TODO - think about, HG will retry and after should change Transaction status
        });

        String extId = payResult.getExtId();

        if (extId == null || extId.isEmpty()) {
            extId = "-1";
        }

        payBill
                .success(Objects.equals(payResult.isSuccess(), Boolean.TRUE) || Objects.equals(payResult.getStatus(), TransactionStatus.WAITING))
                .responseCode(payResult.getResponseCode())
                .responseText(payResult.getResponseText())
                .responseTime(payResult.getResponseTime())
                .responseJson(payResult.getResponseJson())
                .extId(extId);

        SimpleBillResponse response = commonBillService.saveToSimple(payBill);
        if (payResult.getStatus() != null) {
            response.status(payResult.getStatus());
        }
        return response;
    }

    public SimpleBillResponse cancel(SharedCancelDTO sharedCancelDTO) {

        if (sharedCancelDTO.getBillId() == null || sharedCancelDTO.getRecipientTrId() == null) {
            throw new CodedException(ProblemEnum.INVALID_PARAMS);
        }

        CommonBill payBill = commonBillService.findTop1ByIdAndSuccessAndLoginAndType(sharedCancelDTO.getBillId(), true, sharedCancelDTO.getLogin(), RequestType.PAY).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND, "Bill=" + sharedCancelDTO.getBillId()));

        CommonBill cancelBill = new Bill()
                .transactionId(sharedCancelDTO.getTranId())
                .requestJson(payBill.getRequestJson())
                .recipientId(payBill.getRecipientId())
                .recipientShortName(sharedCancelDTO.getRecipientParams().getRecipientShortName())
                .account(payBill.getAccount())
                .amount(payBill.getAmount())
                .amountRatio(sharedCancelDTO.getRecipientParams().getRecipientAmountRatio())
                .currency(payBill.getCurrency())
                .type(RequestType.CANCEL)
                .login(sharedCancelDTO.getLogin())
                .requestTime(Instant.now())
                .responseTime(Instant.now())
                .extId(payBill.getExtId())
                .success(false)
                .responseJson(Collections.emptyMap())
                .responseCode("pre-request code")
                .responseText("pre-request text")
                //
                ;

        commonBillService.save(cancelBill);

        GatewayBillingService gatewayService = getServiceBean(sharedCancelDTO.getRecipientParams().getGatewayBean());

        GatewayResult cancelResult = gatewayService.cancel(sharedCancelDTO, new CancelHandler() {
            @Override
            public void saveRequestParams(Map<String, Object> requestJson, Instant requestTime) {
                cancelBill
                        .requestJson(requestJson)
                        .requestTime(requestTime);
                commonBillService.save(payBill);
            }

            @Override
            public Map<String, Object> getPayRequestJson() {
                return payBill.getRequestJson();
            }

            @Override
            public Map<String, Object> getPayResponseJson() {
                return payBill.getResponseJson();
            }

            @Override
            public String getAccount() {
                return payBill.getAccount();
            }

            @Override
            public Integer getCurrency() {
                return payBill.getCurrency();
            }

            @Override
            public String getExtId() {
                return payBill.getExtId();
            }
        });

        cancelBill
                .success(cancelResult.isSuccess())
                .responseCode(cancelResult.getResponseCode())
                .responseText(cancelResult.getResponseText())
                .responseTime(cancelResult.getResponseTime())
                .responseJson(cancelResult.getResponseJson())
                .extId(cancelResult.getExtId());

        return commonBillService.saveToSimple(cancelBill);
    }
}
