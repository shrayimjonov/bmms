package uz.paykap.gateway.common;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.dto.GatewayResult;
import uz.devops.bmcore.dto.request.SharedCancelDTO;
import uz.devops.bmcore.dto.request.SharedCheckDTO;
import uz.devops.bmcore.dto.request.SharedPayDTO;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.gateway.CancelHandler;
import uz.devops.bmcore.gateway.GatewayBillingService;
import uz.devops.bmcore.gateway.GatewayResultHandler;
import uz.devops.bmcore.gateway.PrepayHandler;
import uz.paykap.config.Constants;
import uz.paykap.domain.GatewayParam;
import uz.paykap.repository.GatewayParamRepository;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
@Service(CommonGatewayServiceV3.NAME)
public class CommonGatewayServiceV3 implements GatewayBillingService {

    public static final String NAME = "commonGatewayServiceV3";

    private final GatewayParamRepository gatewayParamRepository;
    private final CommonGatewayHelperService commonGatewayHelperService;

    @Override
    public GatewayResult check(@NotNull SharedCheckDTO requestDTO, @NotNull GatewayResultHandler gatewayResultHandler) {

        log.debug("Common check request : {}", requestDTO);

        if (requestDTO == null) {
            return commonGatewayHelperService.getErrorGatewayResult("checkDTO is null");
        }

        if (requestDTO.getRecipientParams() == null) {
            return commonGatewayHelperService.getErrorGatewayResult("recipientParams is null");
        }

        // If no check service return success
        if (!Objects.equals(requestDTO.getRecipientParams().getHasCheck(), Boolean.TRUE)) {

            return new GatewayResult()
                    .responseJson(Collections.singletonMap(Constants.PARAM_SKIP_CHECK, "true"))
                    .responseTime(Instant.now())
                    .responseText(Constants.NO_CHECK_ID_MESSAGE)
                    .responseCode("true")
//                .extId(EXT_CHECK_ID)
                    .success(true);
        }

        //Validate amount and account params
        if (requestDTO.getCheckDTO().getAccount() == null || requestDTO.getCheckDTO().getAccount().isEmpty()) {
            log.debug("Required fields amount or account not found in params : {}", requestDTO.getCheckDTO().getParams());
            return commonGatewayHelperService.getErrorGatewayResult("Required fields amount or account not found");
        }

        Optional<GatewayParam> gatewayParam = gatewayParamRepository.findTop1ByRecipientIdAndTypeAndActiveIsTrue(requestDTO.getCheckDTO().getRecipientId(), RequestType.CHECK);

        if (gatewayParam.isEmpty()) {
            return commonGatewayHelperService.getErrorGatewayResult("Gateway param not found for " + requestDTO.getRecipientParams().getRecipientShortName() + " (" + requestDTO.getRecipientParams().getRecipientId() + ")");
        }

        return commonGatewayHelperService.sendAndReceive(
                RequestType.CHECK,
                gatewayParam.get(),
                requestDTO.getCheckDTO().getParams(),
                gatewayResultHandler.getBillId(),
                requestDTO.getTranId(),
                requestDTO.getGatewayAmount() != null ? requestDTO.getGatewayAmount().longValue() : getDefaultCheckAmountIfNull(),
                requestDTO.getCheckDTO().getAccount(),
                null,
                Instant.now(),
                Instant.now(),
                requestDTO.getRecipientParams().getRecipientShortName(),
                gatewayParam.get().isSuccessOn2xx() == Boolean.TRUE).getGatewayResult();
    }

    @Override
    public GatewayResult pay(@NotNull SharedPayDTO requestDTO, @NotNull PrepayHandler prepayHandler) {

        if (requestDTO == null || requestDTO.getPayDTO() == null || requestDTO.getPayDTO().getBillId() == null) {
            return commonGatewayHelperService.getErrorGatewayResult("payDto or billId is null");
        }

        log.info("Common pay request : {}", requestDTO);

//        Long amount = requestDTO.getGatewayAmount().longValue();

        //If no pay service return success
        if (!requestDTO.getRecipientParams().getHasPay()) {
            return new GatewayResult()
                    .success(true)
//                .requestJson(bill.getRequestJson())
                    .responseJson(Collections.singletonMap("skipPay", "true"))
                    .responseTime(Instant.now())
                    .responseText("Pay not active, simulated!")
                    .responseCode("WAITING")
                    ;
        }

        // Validate amount and account params
        if (prepayHandler.getAccount() == null || prepayHandler.getAccount().isEmpty()) {
            log.debug("Required field account not found in params : {}", prepayHandler.getCheckResponseJson());
            return commonGatewayHelperService.getErrorGatewayResult("Required field account not found in params");
        }

        Optional<GatewayParam> gatewayParam = gatewayParamRepository.findTop1ByRecipientIdAndTypeAndActiveIsTrue(requestDTO.getRecipientParams().getRecipientId(), RequestType.PAY);

        if (gatewayParam.isEmpty()) {
            return commonGatewayHelperService.getErrorGatewayResult("Gateway param not found for " + requestDTO.getRecipientParams().getRecipientShortName() + " (" + requestDTO.getRecipientParams().getRecipientId() + ")");
        }

        return commonGatewayHelperService.sendAndReceive(
                RequestType.PAY,
                gatewayParam.get(),
                commonGatewayHelperService.mergeAll(requestDTO.getPayDTO().getParams(), prepayHandler.getCheckRequestJson(), prepayHandler.getCheckResponseJson(), requestDTO.getPayDTO().getAgentParams()),
                null,
                requestDTO.getTranId(),
                requestDTO.getGatewayAmount().longValue(),
                prepayHandler.getAccount(),
                prepayHandler.getExtId(),
                Instant.now(),
                Instant.now(),
                requestDTO.getRecipientParams().getRecipientShortName(),
                gatewayParam.get().isSuccessOn2xx() == Boolean.TRUE).getGatewayResult();
    }

    @Override
    public GatewayResult cancel(@NotNull SharedCancelDTO requestDTO, @NotNull CancelHandler cancelHandler) {

        if (requestDTO == null || requestDTO.getBillId() == null) {
            return commonGatewayHelperService.getErrorGatewayResult("SharedCancelDTO or billId is null");
        }

        log.info("Common cancel : {}", requestDTO);

        if (!Objects.equals(requestDTO.getRecipientParams().getHasCancel(), Boolean.TRUE)) {

            return new GatewayResult()
                    .responseJson(Collections.singletonMap(Constants.PARAM_SKIP_CANCEL, "true"))
                    .responseTime(Instant.now())
                    .responseText(Constants.SKIP_CANCEL_IS_ON)
                    .responseCode("false")
//                .extId(EXT_CHECK_ID)
                    .success(false);
        }

        Optional<GatewayParam> gatewayParam = gatewayParamRepository.findTop1ByRecipientIdAndTypeAndActiveIsTrue(requestDTO.getRecipientParams().getRecipientId(), RequestType.CANCEL);

//        String timeWithZone = "";
//        if (checkBill.getRequestJson() != null) {
//            timeWithZone = checkBill.getRequestJson().get(TIME_WITH_ZONE);
//        }

        if (gatewayParam.isEmpty()) {
            return commonGatewayHelperService.getErrorGatewayResult("Gateway param not found for " + requestDTO.getRecipientParams().getRecipientShortName() + " (" + requestDTO.getRecipientParams().getRecipientId() + ")");
        }

        return commonGatewayHelperService.sendAndReceive(
                RequestType.CANCEL,
                gatewayParam.get(),
                commonGatewayHelperService.mergeAll(cancelHandler.getPayRequestJson(), cancelHandler.getPayResponseJson()),
                requestDTO.getBillId(),
                requestDTO.getTranId(),
                requestDTO.getGatewayAmount().longValue(),
                cancelHandler.getAccount(),
                cancelHandler.getExtId(),
                Instant.now(),
                Instant.now(),
                requestDTO.getRecipientParams().getRecipientShortName(),
                gatewayParam.get().isSuccessOn2xx() == Boolean.TRUE).getGatewayResult();
    }

    @Override
    public Long getDefaultCheckAmountIfNull() {
        return 0L;
    }


}
