package uz.paykap.service.munisWSDL;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bm.ncitrans.adapter.NciTransAdapter;
import uz.devops.bm.ncitrans.config.NciTransProperties;
import uz.devops.munisagetdto.*;
import uz.paykap.PkbillmsApp;
import uz.paykap.adapter.DataAdapterServiceV3;
import uz.paykap.config.TestSecurityConfiguration;
import uz.paykap.domain.Agent;
import uz.paykap.domain.AgentRecipient;
import uz.paykap.domain.Deposit;
import uz.paykap.domain.Recipient;
import uz.paykap.repository.AgentRecipientRepository;
import uz.paykap.repository.AgentRepository;
import uz.paykap.repository.DepositRepository;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.web.rest.MunisAgentResource;
import uz.paykap.web.rest.TestUtil;
import uz.paykap.web.rest.errors.ExceptionTranslator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static uz.paykap.web.rest.TestUtil.createFormattingConversionService;

//@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
@SpringBootTest(classes = {PkbillmsApp.class, TestSecurityConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MunisAgentServiceTest {

    @LocalServerPort
    int randomServerPort;

    @Autowired
    private DataAdapterServiceV3 dataAdapterServiceV3;

    @Autowired
    private NciTransProperties nciTransProperties;

    @Autowired
    private RecipientRepository recipientRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private AgentRecipientRepository agentRecipientRepository;

    @Autowired
    private DepositRepository depositRepository;

    @Autowired
    private MunisAgentCommonService munisAgentCommonService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    private MockMvc mockMvc;


    @Transactional
    public void syncNciTransAdapter() {
        initMock();

//        ArrayList<DataAdapterResponse> allAdapters2 = dataAdapterServiceV3.getAllAdapters();
//
//        assertThat(allAdapters2).isNotEmpty();
//
//        for (DataAdapterResponse dataAdapterResponse : allAdapters2) {
//            if (NciTransAdapter.NAME.equals(dataAdapterResponse.getName())) {
//                Result sync = dataAdapterServiceV3.sync(dataAdapterResponse.getName());
//                assertThat(sync).isNotNull();
//                assertThat(sync.isSuccess()).isTrue();
//            }
//        }

        dataAdapterServiceV3.sync(NciTransAdapter.NAME);

        recipientRepository.findAll();
    }

    private void initMock() {
        MockitoAnnotations.initMocks(this);
        final MunisAgentResource adapterResource = new MunisAgentResource(munisAgentCommonService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(adapterResource)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setControllerAdvice(exceptionTranslator)
                .setConversionService(createFormattingConversionService())
                .setMessageConverters(jacksonMessageConverter).build();
    }

    @Transactional
    public void addAgentRecipient() throws Exception {

        Recipient recipient = recipientRepository.findTop1ByShortName(NciTransAdapter.WALLET_RECIPIENT_SHORT_NAME).orElseThrow(() -> new Exception("recipient not found"));

        Agent agent = new Agent();
        agent.setActive(true);
        agent.setShortName("user");
        agent.setCurrencies(getCurrencies());
        agent.setAmountRatio(100);
        agent.setApiKey("bomBom123");
        agent = agentRepository.save(agent);

        Deposit deposit = new Deposit();
        deposit.setBalance(10000000L);
        deposit.setAgent(agent);
        deposit.setBalanceCcy(860);
        deposit.setBlocked(false);
        deposit.setLimitInform(0L);
        deposit.setLimitDisable(0L);
        deposit.setUnlimit(false);
        deposit.setOverdraftLimit(0L);
        deposit.setBookedBalance(0L);
        deposit = depositRepository.save(deposit);

        agent.setDeposit(deposit);
        agentRepository.save(agent);

        AgentRecipient agentRecipient = new AgentRecipient();
        agentRecipient.setAgent(agent);
        agentRecipient.setRecipient(recipient);
        agentRecipient.setActive(true);
        agentRecipientRepository.save(agentRecipient);
    }

    private Set<Integer> getCurrencies() {
        Set<Integer> integers = new HashSet<>();
        integers.add(860);
        return integers;
    }

    @Test
    @Transactional
    public void testGetPayeeForCashBackToSuccess() throws Exception {
        syncNciTransAdapter();
        addAgentRecipient();

        Settlement settlement = new Settlement();
        settlement.setCode("06");
        settlement.setSupplier("9999");

        Subjectinfo subject = new Subjectinfo();
        subject.setElement("PHONE");
        subject.setValue("999333907");
        List<Subjectinfo> subjectinfoList = new ArrayList<>();
        subjectinfoList.add(subject);
        GetPayee getPayeeRequest = new GetPayee();
        getPayeeRequest.setSettlement(settlement);
        getPayeeRequest.setSubject(subjectinfoList);

        MvcResult mvcResult = mockMvc.perform(
                post("/api/munis/getPayee")
                        .content(TestUtil.convertObjectToJsonBytes(getPayeeRequest))
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andReturn();

        assertThat(mvcResult).isNotNull();
    }

    @Test
    @Transactional
    public void testReceiveTransferForCashBackToSuccess() throws Exception {
        syncNciTransAdapter();
        addAgentRecipient();

        Document document = new Document();
        document.setDocD("0189758045");
        document.setDocN("20.01.2013");

        Amount amount = new Amount();
        amount.setCurrency("000");
        amount.setValue("10000000");

        Confirm confirm = new Confirm();
        confirm.setCode("0");
        confirm.setMessage("Успешно");

        Payee payee = new Payee();
        payee.setAccount("22616000790000001001");
        payee.setBranch("00491");
        payee.setInn("571245212");
        payee.setName("Наименование клиента (получателя)");

        Payer payer = new Payer();
        payer.setAccount("22618000309936001001");
        payer.setBranch("00684");
        payer.setInn("265360010");
        payer.setName("Наименование клиента");

        Purpose purpose = new Purpose();
        purpose.setCode("00667");
        purpose.setText("перевод средств на эл. кошелек");

        Settlement settlement = new Settlement();
        settlement.setSupplier("9999");
        settlement.setCode("06");

        Subjectinfo subject = new Subjectinfo();
        subject.setValue("998901234567");
        subject.setElement("PHONE");
        List<Subjectinfo> subjectinfos = new ArrayList<>();
        subjectinfos.add(subject);
        Sysinfo sysinfo = new Sysinfo();
        sysinfo.setData("20.01.2013");
        sysinfo.setTime("11:23:00");
        sysinfo.setBid("45851gr6258746");
        sysinfo.setTid("123456789");
        sysinfo.setSid("555");
        sysinfo.setHash("9FF844B9BBFB93799F5A1196C8511196");

        Payment payment = new Payment();
        payment.setDocument(document);
        payment.setAmount(amount);
        payment.setConfirm(confirm);
        payment.setPayee(payee);
        payment.setPayer(payer);
        payment.setPurpose(purpose);
        payment.setSettlement(settlement);
        payment.setSubject(subjectinfos);
        payment.setSysinfo(sysinfo);
        payment.setDsign(String.valueOf(System.currentTimeMillis()));

        ReceiveTransfer transferRequest = new ReceiveTransfer();
        transferRequest.setPayment(payment);

        MvcResult mvcResult = mockMvc.perform(post("/api/munis/transfer")
                .content(TestUtil.convertObjectToJsonBytes(transferRequest))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();

        assertThat(mvcResult).isNotNull();


    }

    @Test
    @Transactional
    public void testCancelTransactionToSuccess() throws Exception {
        testReceiveTransferForCashBackToSuccess();

        Confirm confirm = new Confirm();
        confirm.setCode("0");
        confirm.setMessage("Успешно");

        Sysinfo sysinfo = new Sysinfo();
        sysinfo.setData("20.01.2013");
        sysinfo.setTime("11:23:00");
        sysinfo.setBid("45851gr6258746");
        sysinfo.setTid("123456789");
        sysinfo.setSid("555");
        sysinfo.setHash("9FF844B9BBFB93799F5A1196C8511196");

        Statepayment statepayment = new Statepayment();
        statepayment.setName("Несоответствие параметрам взаимодействия с МУНИС");
        statepayment.setState("2");

        CancelTransfer cancelTransferRequest = new CancelTransfer();
        cancelTransferRequest.setConfirm(confirm);
        cancelTransferRequest.setSysinfo(sysinfo);
        cancelTransferRequest.setStatepayment(statepayment);
        cancelTransferRequest.setDsign(String.valueOf(System.currentTimeMillis()));

        MvcResult mvcResult = mockMvc.perform(post("/api/munis/cancel")
                .content(TestUtil.convertObjectToJsonBytes(cancelTransferRequest))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();

        assertThat(mvcResult).isNotNull();
    }

}
