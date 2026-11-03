package uz.paykap.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bm.dummy.adapter.DummyDataAdapterV3;
import uz.javlon.commons.result.CommonResultData;
import uz.javlon.commons.result.HasResult;
import uz.javlon.svgate.service.SvGateUniversalServiceV2;
import uz.javlon.svgate.service.simulation.PredefinedCardRepo2;
import uz.paykap.PkbillmsApp;
import uz.paykap.config.TestSecurityConfiguration;
import uz.paykap.domain.Agent;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Deposit;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.DepositState;
import uz.paykap.repository.*;
import uz.paykap.service.abs.AbstractAbsService;
import uz.paykap.service.abs.response.AbsAccountResult;
import uz.paykap.service.abs.response.AbsTransactionResult;
import uz.paykap.service.abs.response.MultiTransactionResultDTO;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.service.dto.p2p.P2pAccount;
import uz.paykap.service.dto.p2p.P2pAccountType;
import uz.paykap.service.dto.p2p.P2pPayRequest;
import uz.paykap.service.dto.result.BmResultData;
import uz.paykap.service.p2p.dto.response.P2pResponse;
import uz.paykap.web.rest.AgentResourceIntTest;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ActiveProfiles(profiles = {"davr", "default"})
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {PkbillmsApp.class, TestSecurityConfiguration.class},
    properties = {"rateService.impl=davrRateService", "nci-is-api.simulate=true"}
)
public class NciAccountToUzcardMultiTranCancelTest {
    @Autowired
    private P2pCheckRequestDecoratorService p2pCheckRequestDecoratorService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private BillRepository billRepository;
    @Autowired
    private P2pParamRepository p2pParamRepository;
    @Autowired
    private P2pCommissionRepository p2pCommissionRepository;
    @Autowired
    private P2pConditionRepository p2pConditionRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @MockBean
    private AbstractAbsService abstractAbsService;
    @Autowired
    private AgentRepository agentRepository;
    @Autowired
    private DepositRepository depositRepository;
    @MockBean
    private SvGateUniversalServiceV2 universalServiceV2;

    public void setupMock() {
        MultiTransactionResultDTO multiTransactionResultDTO = new MultiTransactionResultDTO();
        Mockito.when(abstractAbsService.doMultiTransaction(ArgumentMatchers.any())).thenReturn(new BmResultData<>(BmResultStatus.SUCCESS, Collections.singletonList(multiTransactionResultDTO)));
        Mockito.when(abstractAbsService.sendCancelRequest(ArgumentMatchers.any())).thenReturn(new BmResultData<>(BmResultStatus.SUCCESS, new AbsTransactionResult()));
        AbsAccountResult accountResult = new AbsAccountResult();
        accountResult.setErrorCode("0");
        accountResult.setErrorNote("SUCCESS");
        Mockito.when(abstractAbsService.sendGetAccountRequest(ArgumentMatchers.any())).thenReturn(new BmResultData<>(BmResultStatus.SUCCESS, accountResult));

        Mockito.when(abstractAbsService.sendCancelRequest(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(),ArgumentMatchers.any())).thenReturn(new BmResultData<>(BmResultStatus.SUCCESS, new AbsTransactionResult()));
        Mockito.when(universalServiceV2.p2pUniversalCredit(ArgumentMatchers.any())).thenReturn(new CommonResultData<>(HasResult.UNKNOWN_ERROR, null, null));
    }


    @SneakyThrows
    @Test
    @Transactional
    @WithMockUser(DummyDataAdapterV3.DUMMY_AGENT)
    public void payNciAccount2UzcardToken() {
        createDeposit("JAVLON ERALIYEV");
        setupMock();
        int databaseSizeBeforeCreate = transactionRepository.findAll().size();
        int billSizeBeforeCreate = billRepository.findAll().size();
        Instant instant = Instant.now();
        long amount = 10000L;
        String senderNciAccount = "20208000000548837006";

        var paramsString1 = "{ \"account\": \"" + senderNciAccount + "\", \"mfo\": \"00491\", \"name\": \"JAVLON ERALIYEV\", \"comment\": \"Test payment\" }";

        var senderAccount = new P2pAccount()
            .withType(P2pAccountType.ACCOUNT)
            .withCurrency(860)
            .withParams(objectMapper.readTree(paramsString1));

        var paramsString2 = "{ \"token\": \"" + PredefinedCardRepo2.DEFAULT_TOKEN + "\", \"bin\": \"860049\" }";

        var recipientAccount = new P2pAccount()
            .withType(P2pAccountType.TOKEN)
            .withCurrency(860)
            .withParams(objectMapper.readTree(paramsString2));

        P2pPayRequest p2PPayRequest = new P2pPayRequest()
            .withFrom(senderAccount)
            .withTo(recipientAccount)
            .withAgentTranId("123")
            .withAmount(amount);

        P2pResponse simpleBillResponse = p2pCheckRequestDecoratorService.p2pPay(p2PPayRequest);

        log.info(String.valueOf(simpleBillResponse));

        Assertions.assertThat(simpleBillResponse).isNotNull();
        Assertions.assertThat(simpleBillResponse.isSuccess()).isFalse();

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        List<Bill> billList = billRepository.findAll();
        assertThat(billList).hasSize(billSizeBeforeCreate + 4);

        assertThat(testTransaction.getRecipientStatusText()).isEqualTo("RECIPIENT_PC_ERROR : Uzcard p2p response is empty : null, sender cancelled OK");

    }

    private Agent createAgent(String agentName) {
        Agent agent = AgentResourceIntTest.createEntity(null);
        agent.setShortName(agentName);
        agent.orgName("sd");
        agent.setActive(true);
        return agentRepository.save(agent);
    }

    private Deposit createDeposit(String agentName) {
        Deposit deposit = new Deposit();
        deposit.setBalance(100000000000L);
        deposit.setAgent(createAgent(agentName));
        deposit.setOverdraftLimit(100000000000L);
        deposit.setBalanceCcy(860);
        deposit.setBookedBalance(100000000000L);
        deposit.setLimitInform(10000000000L);
        deposit.setLimitDisable(0L);
        deposit.setBlocked(false);
        deposit.setUnlimit(true);
        deposit.setState(DepositState.ACTIVE);
        return depositRepository.save(deposit);
    }
}
