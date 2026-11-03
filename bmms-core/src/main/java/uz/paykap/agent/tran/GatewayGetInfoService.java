package uz.paykap.agent.tran;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.devops.bmcore.dto.GatewayQueryResult;
import uz.devops.bmcore.dto.request.CommonCheckRequest;
import uz.devops.bmcore.dto.request.SharedCheckDTO;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.gateway.GatewayServiceV3;
import uz.paykap.security.SecurityUtils;
import uz.paykap.service.cached.GatewaySelectorService;

@Slf4j
@Component
@AllArgsConstructor
public class GatewayGetInfoService {

    private final GatewaySelectorService gatewaySelectorService;
    private final GatewayServiceV3 gatewayServiceV3;

    public SimpleBillResponse getInfo(CommonCheckRequest infoRequest) {

        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND));

        log.info(">>>>>>>>>> Get Info Request >>>>>>>>>>\nLogin : {}\nRequest:\n{}\n", login, infoRequest);

        validateParams(infoRequest);

        SharedCheckDTO sharedCheckDTO = getSharedCheckDto(infoRequest, login);

        SimpleBillResponse info = gatewayServiceV3.getInfo(sharedCheckDTO);

        log.info("<<<<<<<<<< Get Info Response <<<<<<<<<<:\n{}\n", info);

        return info;

    }

    private void validateParams(CommonCheckRequest infoRequest) {
        if (infoRequest.getRecipientId() == null) {
            log.warn("BAD - recipient Id not given (null) : {}", infoRequest);
            throw new CodedException(ProblemEnum.INVALID_PARAMS);
        }

        log.debug("OK - RecipientId is not null : {}", infoRequest.getRecipientId());
    }

    private SharedCheckDTO getSharedCheckDto(CommonCheckRequest infoRequest, String login) {
        GatewayQueryResult validGatewayQueryResultV2 = gatewaySelectorService.getValidGatewayQueryResultV5(infoRequest.getRecipientId(), login, login);
        return new SharedCheckDTO()
                .login(login)
                .checkDTO(infoRequest)
                .recipientParams(validGatewayQueryResultV2);
    }

}
