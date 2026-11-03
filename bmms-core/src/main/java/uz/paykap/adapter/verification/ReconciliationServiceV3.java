package uz.paykap.adapter.verification;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.reconciliation.AbstractGatewayRecoService;
import uz.javlon.commons.base.BasicResult;
import uz.javlon.commons.base.Result;
import uz.paykap.service.reconciliation.p2p.AbstractP2pRecoService;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ReconciliationServiceV3 {

    private final List<AbstractGatewayRecoService> gatewayRecoServices;
//    private final List<AbstractRecipientRecoService> recipientRecoServices;
    private final List<AbstractP2pRecoService> p2pRecoServices;

    public Result startReconcileByGateway(@NotNull final Instant from, @NotNull final Instant to) {

        int iSuccess = 0;

        for (AbstractGatewayRecoService recoService : gatewayRecoServices) {

            BasicResult result = recoService.reconcile(from, to);

            if (!result.isSuccess()) {
                log.warn("{} reconciliation failed : {}", recoService.getShortName(), result.getResponseText());
            } else {
                iSuccess++;
                log.info("{} reconciliation successfully done : {}", recoService.getShortName(), result.getResponseText());
            }

        }

        log.info("Reconciliation results : success = {}, failed = {}", iSuccess, gatewayRecoServices.size() - iSuccess);

        return Result.SUCCESS;
    }

    public Result startReconcileByP2p(@NotNull final Instant from, @NotNull final Instant to) {

        int iSuccess = 0;

        for (AbstractP2pRecoService recoService : p2pRecoServices) {

            BasicResult result = recoService.reconcile(from, to);

            if (!result.isSuccess()) {
                log.warn("{} reconciliation failed : {}", recoService.getProcessorType(), result.getResponseText());
            } else {
                iSuccess++;
                log.info("{} reconciliation successfully done : {}", recoService.getProcessorType(), result.getResponseText());
            }

        }

        log.info("Reconciliation results : success = {}, failed = {}", iSuccess, gatewayRecoServices.size() - iSuccess);

        return Result.SUCCESS;
    }

}
