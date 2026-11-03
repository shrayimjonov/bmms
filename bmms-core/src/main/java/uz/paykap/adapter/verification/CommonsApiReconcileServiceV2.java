package uz.paykap.adapter.verification;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import uz.javlon.commons.base.Result;
import uz.javlon.commons.base.ResultData;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.adapter.verification.common.CommonReconcileAdapter;
import uz.paykap.adapter.verification.common.ReconcileDelegateService;
import uz.paykap.adapter.verification.dto.ReconciliationRequestDTO;
import uz.paykap.domain.Recipient;
import uz.paykap.event.dto.SendToMailEvent;
import uz.paykap.gateway.common.CommonGatewayServiceV3;
import uz.paykap.gateway.dto.CompareSchema;
import uz.paykap.gateway.dto.ReconcileGatewayDTO;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.service.dto.GatewayDTO;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class CommonsApiReconcileServiceV2 {

    private final ReconcileDelegateService reconcileDelegateService;
    private final TransactionRepository transactionRepository;
    private final CommonReconcileAdapter commonReconcileAdapter;
    private final RecipientRepository recipientRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    private ResultData<ReconcileGatewayDTO> getGatewayTrans(Instant fromTime, Instant toTime, String gatewayName, String recipientShortName) {
        ReconciliationRequestDTO paramsDTO = new ReconciliationRequestDTO()
            .recipientShortName(recipientShortName)
            .toTime(toTime)
            .fromTime(fromTime)
            .gatewayName(gatewayName)
            //
            ;
        return commonReconcileAdapter.getReconcileList(paramsDTO);
    }

    public Result reconcile(Instant fromTime, Instant toTime) {

        List<Recipient> recipients = recipientRepository.findAllWithGatewayBySpringBeanName(CommonGatewayServiceV3.NAME);

        Result result = Result.SUCCESS;

        for (Recipient recipient : recipients) {
            if (recipient.isReconcile() == Boolean.FALSE) {
                log.debug(recipient.getShortName() + " reconcile offline");
                continue;
            }
            GatewayDTO gatewayDTO = reconcileDelegateService.getGatewayService().findOne(recipient.getGateway().getId()).orElseThrow(() -> new CodedException(ProblemEnum.OBJECT_NOT_FOUND, "gateway for recipient " + recipient.getShortName()));
            try {
//                CompareSchema compareSchema = CompareSchema.ONLY_ID;
                //todo in Recipient entity add column ? ?
                // ?
                Result reconcile = new ApiReconcileServiceV2(gatewayDTO.getName(),
                    (fromTime12, toTime12) -> transactionRepository.getSetByRecipientBySchemaOrder(recipient.getShortName(), fromTime12, toTime12, CompareSchema.ONLY_ID.ordinal()), (fromTime1, toTime1) -> {
                    ResultData<ReconcileGatewayDTO> gatewayTrans = getGatewayTrans(fromTime1, toTime1, gatewayDTO.getName(), recipient.getShortName());
                    if (!gatewayTrans.isSuccess()) {
//                        log.warn("BAD - unsuccessful gatewayTrans");
                        return new ReconcileGatewayDTO();
                    }
                    return gatewayTrans.getData();
                }, reconcileDelegateService).reconcile(fromTime, toTime);


                if (!reconcile.isSuccess()) {
                    result = Result.FAILED;
                }
//                mailService.sendEmail("user@example.com", "Reconcile data", reconcileData(reconcile.isSuccess(), recipient.getShortName()));
                applicationEventPublisher.publishEvent(new SendToMailEvent(this, "user@example.com", "Reconcile data", reconcileData(reconcile.isSuccess(), recipient.getShortName()), false, false));

            } catch (Exception e) {
                log.warn("Error in reconcile common : {}", e.getLocalizedMessage());
            }
        }

        return result;
    }

    private String reconcileData(boolean success, String recipientName) {
        if (success) {
            return "УСПЕШНО! Сверка " + recipientName + " прошла успешно !";
        } else {
            return "РАСХОЖДЕНИЕ! Сверка " + recipientName + " , выявлены расхождения !";
        }
    }
}
