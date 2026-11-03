package uz.paykap.gateway;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.dto.request.CommonCancelRequest;
import uz.devops.bmcore.dto.request.CommonCheckRequest;
import uz.devops.bmcore.dto.request.CommonPayRequest;
import uz.devops.bmcore.dto.request.GetInfoRequest;
import uz.devops.bmcore.dto.response.DepositBalanceResponse;
import uz.devops.bmcore.dto.response.GetInfoResponse;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.gateway.GatewayService;
import uz.devops.bmcore.projection.GatewayResultParamProjection;
import uz.javlon.commons.base.Result;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.adapter.verification.dto.ReconciliationRequestDTO;
import uz.paykap.domain.Gateway;
import uz.paykap.repository.GatewayRepository;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.security.SecurityUtils;

import java.time.Instant;

@Deprecated // TODO: 20.06.2022 Javlon aka agar iwlatilmasa o`chirib yuborish kere
@Slf4j
@Service
@AllArgsConstructor
public class GatewayServiceResolver extends GatewayService {

    public GetInfoResponse getInfo(GetInfoRequest infoRequest) {

        log.debug("Request to get recipient by ID : {}", infoRequest.getRecipientId());

        GatewayResultParamProjection gatewayQueryResult = resolveGatewayServiceById(infoRequest.getRecipientId());

        GatewayService gatewayService = applicationContext.getBean(gatewayQueryResult.getGatewayBean(), GatewayService.class);

        return gatewayService.getInfo(infoRequest);
    }

    @Override
    public SimpleBillResponse check(CommonCheckRequest checkDTO) {

        log.debug("Request to check by recipientId : {}", checkDTO.getRecipientId());

        GatewayResultParamProjection gatewayQueryResult = resolveGatewayServiceById(checkDTO.getRecipientId());

        GatewayService gatewayService = applicationContext.getBean(gatewayQueryResult.getGatewayBean(), GatewayService.class);

        return gatewayService.check(checkDTO);
    }

    public Result reconcile(Instant from, Instant to, String springBeanName) {

        Gateway gateway = gatewayRepository.findTop1BySpringBeanNameAndActiveTrue(springBeanName).orElseThrow(() -> new CodedException(ProblemEnum.OBJECT_NOT_FOUND, "gateway : " + springBeanName));

        return reconcile(from, to, gateway);
    }

    public Result reconcile(ReconciliationRequestDTO reconciliationDTO) {

        Gateway gateway = gatewayRepository.findTop1ByNameAndActiveTrue(reconciliationDTO.getGatewayName()).orElseThrow(() -> new CodedException(ProblemEnum.OBJECT_NOT_FOUND, "gateway : " + reconciliationDTO.getGatewayName()));

        return reconcile(reconciliationDTO.getFromTime(), reconciliationDTO.getToTime(), gateway);
    }

    public Result reconcile(Instant from, Instant to, Gateway gateway) {
        log.info("\n###### Reconciliation started : {} : {} : {} ######", gateway.getName(), from, to);

        GatewayService gatewayService = applicationContext.getBean(gateway.getSpringBeanName(), GatewayService.class);

        Result reconcile = gatewayService.reconcile(from, to);

        log.info("\n###### Reconciliation ended : {} ######", reconcile);
        return reconcile;
    }

    @Override
    public SimpleBillResponse pay(CommonPayRequest payDTO) {

        log.debug("Request to get recipientID by PayRequest : {}", payDTO);

        String gatewayBeanName = gatewayRepository
                .getOnlyGatewayBeanName(payDTO.getBillId(), SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND)), RequestType.CHECK, true)
                .orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));

        log.debug("Request for pay to gateway : {}", payDTO);

        GatewayService gatewayService = applicationContext.getBean(gatewayBeanName, GatewayService.class);

        SimpleBillResponse response = gatewayService.pay(payDTO);

        Gateway gateway = gatewayRepository.findTop1ByNameAndActiveTrue(gatewayBeanName).orElse(new Gateway());

        response.setGatewayName(gateway.getOrg());

        return response;
    }

    @Override
    public SimpleBillResponse cancel(CommonCancelRequest cancelDTO) {

        log.debug("Request to get recipientID by CancelDTO : {}", cancelDTO);

        String gatewayBeanName = gatewayRepository
                .getOnlyGatewayBeanName(cancelDTO.getBillId(), RequestType.PAY, true)
                .orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND));

        log.debug("Request for cancel to gateway : {}", cancelDTO);

        GatewayService gatewayService = applicationContext.getBean(gatewayBeanName, GatewayService.class);

        return gatewayService.cancel(cancelDTO);
    }

    @Override
    public DepositBalanceResponse deposit() {
        throw new CodedException(ProblemEnum.NOT_IMPLEMENTED);
    }

    public DepositBalanceResponse deposit(String gatewayName) {
        log.debug("Request to get deposit by gateway: {}", gatewayName);

        Gateway gateway = gatewayRepository
                .findTop1ByName(gatewayName)
                .orElseThrow(() -> new CodedException(ProblemEnum.SERVICE_NOT_FOUND));

        log.debug("Request for deposit to gateway : {}", gateway.getName());

        log.info("gatewayServiceBeanName, {}", gateway.getSpringBeanName());
        GatewayService gatewayService = applicationContext.getBean(gateway.getSpringBeanName(), GatewayService.class);
        log.info("find gatewayService!!!");

        return gatewayService.deposit();
    }

    private GatewayResultParamProjection resolveGatewayServiceById(Long recipientId) {

        GatewayResultParamProjection gatewayQueryResult = recipientRepository
                .getGatewayBeanName(recipientId)
                .orElseThrow(() -> new CodedException(ProblemEnum.SERVICE_NOT_FOUND));

        log.debug("Request for check to gatewayQueryResult : {}", gatewayQueryResult);

        if (!gatewayQueryResult.getGatewayActive() || !gatewayQueryResult.getRecipientActive()) {
            throw new CodedException(ProblemEnum.SERVICE_NOT_ACTIVE);
        }
        return gatewayQueryResult;
    }

    private final RecipientRepository recipientRepository;
    private final ApplicationContext applicationContext;
    private final GatewayRepository gatewayRepository;

}
