package uz.paykap.service.cached;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bmcore.dto.GatewayQueryResult;
import uz.devops.bmcore.projection.GatewayResultParamProjection;
import uz.paykap.PkbillmsApp;
import uz.paykap.config.Constants;
import uz.paykap.config.TestSecurityConfiguration;
import uz.paykap.domain.AgentRecipient;
import uz.paykap.domain.Gateway;
import uz.paykap.domain.Recipient;
import uz.paykap.gateway.common.CommonGatewayServiceV3;
import uz.paykap.repository.GatewayRepository;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.service.AgentRecipientService;
import uz.paykap.service.AgentService;
import uz.paykap.service.dto.AgentDTO;
import uz.paykap.web.rest.GatewayResourceIntTest;
import uz.paykap.web.rest.RecipientResourceIntTest;

import javax.persistence.EntityManager;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PkbillmsApp.class, TestSecurityConfiguration.class})
public class GatewaySelectorServiceTest {

    @Autowired
    private GatewaySelectorService gatewaySelectorService;
    @Autowired
    private AgentService agentService;
    @Autowired
    private AgentRecipientService agentRecipientService;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private RecipientRepository recipientRepository;
    @Autowired
    private GatewayRepository gatewayRepository;

    @Test
    @WithMockUser(username = Constants.SYSTEM_AGENT_NAME)
    @Transactional
    public void getValidGatewayQueryResultV5() {

        final Gateway gateway = GatewayResourceIntTest.createEntity(entityManager);
        gateway.setSpringBeanName(CommonGatewayServiceV3.NAME);
        gateway.active(Boolean.TRUE);
        gatewayRepository.save(gateway);

        final Recipient recipient = RecipientResourceIntTest.createEntity(entityManager);
        recipient.active(Boolean.TRUE);
        recipient.pay(Boolean.TRUE);
        recipient.maxAmount(1000000L);
        recipient.minAmount(100L);
        recipient.gateway(gateway);
        recipientRepository.save(recipient);

        AgentDTO agent = agentService.getAgentOrCreate(Constants.SYSTEM_AGENT_NAME, 10000000);
        agent.setActive(Boolean.TRUE);
        agentService.save(agent);

        final AgentRecipient agentRecipient = agentRecipientService.createOrGet(recipient.getId(), agent);

        final GatewayQueryResult resultV4 = gatewaySelectorService.getValidGatewayQueryResultV5(recipient.getId(), Constants.SYSTEM_AGENT_NAME, Constants.SYSTEM_AGENT_NAME);

        System.out.println(resultV4);
    }
}