package uz.paykap.gateway.humo.helperService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.Constants;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.humo.service.HumoGatewayService;
import uz.devops.humo.service.dto.ReconciliationConfirmRequest;
import uz.devops.humo.service.dto.ReconciliationConfirmResult;
import uz.devops.humo.service.dto.ReconciliationCreateRequest;
import uz.devops.humo.service.dto.ReconciliationCreateResult;
import uz.javlon.commons.result.CommonResult;
import uz.javlon.commons.result.HasResult;
import uz.paykap.domain.Bill;
import uz.paykap.gateway.CommonGatewaySender;
import uz.paykap.gateway.ResultChecker;
import uz.paykap.service.GenerateNumberService;

import java.time.Instant;
import java.util.Optional;


@Slf4j
@Service
@AllArgsConstructor
public class HumoDebitReconcileServiceV2 {

    private final HumoGatewayService humoGatewayService;
    private final GenerateNumberService generateNumberService;
    private final HumoDebitReconcileVerifyService verifyService;
    private final CommonGatewaySender commonGatewaySender;

    public CommonResult reconcile(String terminalId) {

        log.info("Start reconcile by terminalId: {}", terminalId);

        final Bill bill = new Bill()
                .recipientShortName("HumoRecoCreate")
                .type(RequestType.RECONCILE)
                .amountRatio(Constants.DEFAULT_AMOUNT_RATIO)
                .amount(0L)
                .account(terminalId)
                .outExtId(generateNumberService.humoPaymentRef4Reco(terminalId))
                .requestTime(Instant.now())
                .responseTime(Instant.now())
                .login("system");

        ReconciliationCreateRequest request = new ReconciliationCreateRequest();
        request.setPaymentRef(bill.getOutExtId());
        request.setSessionId(generateNumberService.humoSessionId("humoDebitReconcileService", "reconcile_" + bill.getId()));
        request.setTerminalId(terminalId);

        final Optional<ReconciliationCreateResult> createResult = commonGatewaySender.sendAndReceiveBill(bill, request, humoGatewayService::reconciliationCreate, new ResultChecker<>() {
            @Override
            public boolean success(ReconciliationCreateResult result) {
                return result.getAction() == 4;
            }

            @Override
            public void beforeSaveBillResult(Bill bill, ReconciliationCreateResult result) {
                bill.responseCode(String.valueOf(result.getAction()));
            }
        });

        if (createResult.isEmpty()) {
            return new CommonResult(HasResult.UNKNOWN_ERROR, "Humo createReco failed");
        }

        ReconciliationConfirmRequest confirmRequest = new ReconciliationConfirmRequest();
        confirmRequest.setConfirmed(true);
        confirmRequest.setFinished(true);
        confirmRequest.setPaymentId(createResult.get().getPaymentId());
        confirmRequest.setPaymentRef(createResult.get().getPaymentRef());

        final Bill recoConfirmBill = new Bill()
                .recipientShortName("HumoRecoConfirm")
                .type(RequestType.RECONCILE)
                .amountRatio(Constants.DEFAULT_AMOUNT_RATIO)
                .amount(0L)
                .account(terminalId)
                .outExtId(generateNumberService.humoPaymentRef4Reco(terminalId))
                .requestTime(Instant.now())
                .responseTime(Instant.now())
                .login("system");

        final Optional<ReconciliationConfirmResult> confirmResult = commonGatewaySender.sendAndReceiveBill(recoConfirmBill, confirmRequest, humoGatewayService::reconciliationConfirm, new ResultChecker<>() {
            @Override
            public boolean success(ReconciliationConfirmResult result) {
                return result.getAction() == 10;
            }

            @Override
            public void beforeSaveBillResult(Bill bill, ReconciliationConfirmResult result) {
                bill.responseCode(String.valueOf(result.getAction()));
                bill.amount(result.getDebitsAmount() - result.getCreditsReversalAmount());
            }
        });

        if (confirmResult.isEmpty()) {
            return new CommonResult(HasResult.UNKNOWN_ERROR, "Humo confirmReco failed");
        }

        Long count = confirmResult.get().getDebitsNumber() - confirmResult.get().getCreditsReversalNumber();

        verifyService.verify(bill.getAmount(), count);

        log.info("### reconcile SUCCESS!!! ###");
        return new CommonResult(HasResult.SUCCESS, "Amount : " + bill.getAmount() + ", count : " + count);
    }


}
