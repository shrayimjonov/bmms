package uz.paykap.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import uz.devops.bmcore.projection.GatewayResultParamProjection;
import uz.paykap.IntegrationTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bmcore.dto.GatewayQueryResult;
import uz.paykap.PkbillmsApp;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Gateway;
import uz.paykap.domain.Recipient;
import uz.paykap.web.rest.BillResourceIntTest;
import uz.paykap.web.rest.GatewayResourceIntTest;
import uz.paykap.web.rest.RecipientResourceIntTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@IntegrationTest
public class GatewayRepositoryTest {

    @Autowired
    private BillRepository billRepository;
    @Autowired
    private GatewayRepository gatewayRepository;
    @Autowired
    private RecipientRepository recipientRepository;
//    @Autowired
//    private EntityManager em;

    @Test
    @Transactional
    @WithMockUser("java")
    public void getGatewayBeanName() {
        Gateway gateway = gatewayRepository.save(GatewayResourceIntTest.createEntity(null));

        Recipient recipient = RecipientResourceIntTest.createEntity(null);
        recipient.setGateway(gateway);
        recipient = recipientRepository.save(recipient);

        Bill bill = BillResourceIntTest.createEntity(null, "java");
        bill.setRecipientId(recipient.getId());
        bill = billRepository.save(bill);

        Optional<GatewayResultParamProjection> gatewayBeanName = gatewayRepository.getGatewayBeanName(bill.getId(), "java", bill.getType(), bill.isSuccess());

        assertThat(gatewayBeanName.isPresent()).isTrue();
        assertThat(gatewayBeanName.get().getGatewayActive()).isEqualTo(gateway.isActive());
        assertThat(gatewayBeanName.get().getGatewayBean()).isEqualTo(gateway.getName());
        assertThat(gatewayBeanName.get().getRecipientActive()).isEqualTo(recipient.isActive());

//        System.out.println("OK");
    }
}
