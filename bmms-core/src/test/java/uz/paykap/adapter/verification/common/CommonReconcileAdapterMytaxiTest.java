package uz.paykap.adapter.verification.common;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import uz.paykap.IntegrationTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bmcore.enumeration.RequestType;
import uz.javlon.commons.base.ResultData;
import uz.paykap.PkbillmsApp;
import uz.paykap.adapter.verification.dto.ReconciliationRequestDTO;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.Gateway;
import uz.paykap.domain.GatewayParam;
import uz.paykap.domain.Recipient;
import uz.paykap.domain.enumeration.MethodType;
import uz.paykap.gateway.common.CommonGatewayServiceV3;
import uz.paykap.gateway.dto.ReconcileGatewayDTO;
import uz.paykap.repository.GatewayParamRepository;
import uz.paykap.repository.GatewayRepository;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.utils.DateUtil;
import uz.paykap.web.rest.GatewayParamResourceIntTest;
import uz.paykap.web.rest.GatewayResourceIntTest;
import uz.paykap.web.rest.RecipientResourceIntTest;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@IntegrationTest
public class CommonReconcileAdapterMytaxiTest {

    @Autowired
    private GatewayParamRepository gatewayParamRepository;

    @Autowired
    private GatewayRepository gatewayRepository;

    @Autowired
    private RecipientRepository recipientRepository;

    @Autowired
    private CommonReconcileAdapter commonReconcileAdapter;

    @Test
    @Transactional
    public void getReconcileList() {
        Recipient recipient = getRecipient();

        saveGatewayParamMyTaxiReconciliation(recipient);

        ReconciliationRequestDTO reconciliationRequestDTO = new ReconciliationRequestDTO();

        reconciliationRequestDTO.setRecipientShortName(recipient.getShortName());
        reconciliationRequestDTO.setFromTime(DateUtil.todayStart());
        reconciliationRequestDTO.setToTime(DateUtil.todayEnd());

        ResultData<ReconcileGatewayDTO> gatewayTranDTOS = commonReconcileAdapter.getReconcileList(reconciliationRequestDTO);

        assertThat(gatewayTranDTOS != null);
    }

    private Recipient getRecipient() {
        Recipient recipient = RecipientResourceIntTest.createEntity(null);
        recipient.setShortName("my-taxi");
        recipient.setServiceId("1");
        recipient.setCheck(true);
        recipient.setPay(true);
        recipient.setActive(true);
        recipient.setAmountRatio(1);
        recipient.setGateway(getGateway());
        return recipientRepository.save(recipient);
    }

    private Gateway getGateway() {
        Gateway gateway = GatewayResourceIntTest.createEntity(null);
        gateway.setActive(true);


        gateway.setSpringBeanName(CommonGatewayServiceV3.NAME);
        return gatewayRepository.save(gateway);
    }

    private void saveGatewayParamMyTaxiReconciliation(Recipient recipient) {
        GatewayParam gatewayParam = GatewayParamResourceIntTest.createEntity(null);
        gatewayParam.setActive(true);
        gatewayParam.setRecipient(recipient);
        gatewayParam.setType(RequestType.RECONCILE);
        gatewayParam.setHttpMethodType(MethodType.POST);
        gatewayParam.setTimeout(5000);
        gatewayParam.setUrl("https://paynet.mytaxi.uz/api/server.php");
        gatewayParam.setRequestMediaType("application/xml");
//        gatewayParam.setHeaders("{\"SOAPAction\":\"\"}");
        gatewayParam.setPassword("mYt@Xi20_15clicK");
        gatewayParam.setResponseMediaType("application/xml");
        gatewayParam.setUsername("MytaxiAPIClick2015");

        gatewayParam.setBody(
            "<x:Envelope xmlns:x=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://service.payment.uzbilling\">" +
                "    <x:Header/>" +
                "    <x:Body>" +
                "        <uws:GetStatementArguments xmlns:uws=\"http://uws.provider.com/\">" +
                "<ser:username>${username}</ser:username>" +
                "<password>${password}</password>" +
                "<dateFrom>${dateFrom}</dateFrom>" +
                "<dateTo>${dateTo}</dateTo>" +
                "<serviceId>1</serviceId>" +
                "<parameters>" +
                "<paramKey>reason</paramKey>" +
                "<paramValue></paramValue>" +
                "</parameters>" +
                "</uws:GetStatementArguments>" +
                "    </x:Body>" +
                "</x:Envelope>");
        gatewayParam.setStatusQuery("//*[local-name()='status']");
        gatewayParam.setMessageQuery("//*[local-name()='errorMsg']");
        gatewayParam.successCode("0");
        gatewayParam.setDataQueries("{\"queryRootName\": \"statements\",\"queryAmount\": \"amount\",\"queryExtId\": \"providerTrnId\",\"queryTranId\": \"transactionId\",\"queryExtTime\": \"transactionTime\",\"requestDateFormat\": \"yyyy-MM-dd'T'HH:mm:ss\"}");

        gatewayParamRepository.save(gatewayParam);
    }
}
