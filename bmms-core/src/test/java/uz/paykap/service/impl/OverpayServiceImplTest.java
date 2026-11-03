package uz.paykap.service.impl;

import io.github.jhipster.service.filter.InstantFilter;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import uz.paykap.IntegrationTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bm.dummy.adapter.DummyDataAdapterV3;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.RequestType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.paykap.IntegrationTest;
import uz.paykap.PkbillmsApp;
import uz.paykap.adapter.DummyAdapterDelegate;
import uz.paykap.adapter.verification.dto.AmountRatio;
import uz.paykap.config.Constants;
import uz.paykap.domain.Agent;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Recipient;
import uz.paykap.domain.Transaction;
import uz.paykap.repository.AgentRepository;
import uz.paykap.repository.BillRepository;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.service.OverpayService;
import uz.paykap.service.dto.PaymentEditDTO;
import uz.paykap.service.dto.TransactionCriteria;
import uz.paykap.service.dto.TransactionDTO;
import uz.paykap.web.rest.BillResourceIntTest;
import uz.paykap.web.rest.util.DummyUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
public class OverpayServiceImplTest {

    public static final String HG_TEMP_RECIPIENT_SHORT_NAME = "hg_steam";

    @Autowired
    private RecipientRepository recipientRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private TransactionRepository transactionRepository;

//    @Autowired
//    private HgXlsxDataAdapter hgAdapter;

    @Autowired
    private DummyAdapterDelegate dummyAdapterDelegate;

    @Autowired
    private OverpayService overpayService;

    @Autowired
    private OverpayServiceV2 overpayServiceV2;

    @Autowired
    private AgentRepository agentRepository;

    private Transaction initTest() {
//        hgAdapter.sync();
        Optional<Recipient> optionalRecipient = recipientRepository.findTop1ByShortName(HG_TEMP_RECIPIENT_SHORT_NAME);
        if (!optionalRecipient.isPresent())
            fail();

        Recipient recipient = optionalRecipient.get();

        return createTransaction(recipient, Constants.SYSTEM_AGENT_NAME);
    }

    //    @Test
    @Transactional
    @WithMockUser(value = Constants.SYSTEM_AGENT_NAME)
    public void cancelTransaction() {
        DummyUtils.prepareDummyData(dummyAdapterDelegate);

        Optional<Agent> optionalAgent = agentRepository.findTop1ByShortName(DummyDataAdapterV3.DUMMY_AGENT_USD);

        assertThat(optionalAgent.isPresent()).isTrue();

        Optional<Recipient> optionalRecipient = recipientRepository.findTop1ByShortName(DummyDataAdapterV3.DUMMY_RECIPIENT_UZS);

        assertThat(optionalRecipient.isPresent()).isTrue();

        Transaction transaction = createTransaction(optionalRecipient.get(), DummyDataAdapterV3.DUMMY_AGENT_USD);

        Bill bill = createBill(optionalRecipient.get(), Constants.SYSTEM_AGENT_NAME, transaction.getId());
        overpayService.cancelGateway(transaction.getId());

    }

    //@Test
    @Transactional
    @WithMockUser(value = Constants.SYSTEM_AGENT_NAME)
    public void testEditTransactionByAdmin() {

        DummyUtils.prepareDummyData(dummyAdapterDelegate);

        Optional<Agent> optionalAgent = agentRepository.findTop1ByShortName(DummyDataAdapterV3.DUMMY_AGENT_USD);

        assertThat(optionalAgent.isPresent()).isTrue();

        Optional<Recipient> optionalRecipient = recipientRepository.findTop1ByShortName(DummyDataAdapterV3.DUMMY_RECIPIENT_UZS);

        assertThat(optionalRecipient.isPresent()).isTrue();

        Transaction transaction = createTransaction(optionalRecipient.get(), DummyDataAdapterV3.DUMMY_AGENT_USD);

        boolean beforeEdit = transaction.isMain();
        assertThat(transaction).isNotNull();

        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        String beforeEditAccount = transaction.getRecipientAccount();
        String editedAccount = transaction.getRecipientAccount() + "edited";

        PaymentEditDTO paymentEditDTO = new PaymentEditDTO();
        paymentEditDTO.setAccount(editedAccount);
        paymentEditDTO.setAmount(transaction.getRequestAmount());
        paymentEditDTO.setRecipientId(optionalRecipient.get().getId());
        paymentEditDTO.setTransactionId(transaction.getId());

        TransactionDTO edited = overpayServiceV2.edit(paymentEditDTO);

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction oldMainTransaction = transactionList.get(transactionList.size() - 1);

        Optional<Transaction> mainTransactionWithAccount2 = transactionRepository.findById(transaction.getId());

//        assertThat(mainTransactionWithAccount2.isPresent()).isTrue();
        assertThat(edited.getRecipientAccount()).isNotBlank();
        assertThat(edited.getRecipientAccount()).isEqualTo(editedAccount);

        assertThat(beforeEdit != transaction.isMain());
        assertThat(!transaction.isMain());
        assertThat(edited.isMain());
    }

    //    @Test
    @Transactional
    @WithMockUser(value = Constants.SYSTEM_AGENT_NAME)
    public void getTransactionList() {
        initTest();
        InstantFilter instantFilter = new InstantFilter();
        instantFilter.setGreaterThan(LocalDateTime.now().toInstant(ZoneOffset.ofHours(0)).minusSeconds(3600));
        instantFilter.setLessThanOrEqual(LocalDateTime.now().toInstant(ZoneOffset.ofHours(0)));

        TransactionCriteria transactionCriteria = new TransactionCriteria();
        transactionCriteria.setDate(instantFilter);

        Instant fromDate = transactionCriteria.getDate().getGreaterThan();
        Instant toDate = transactionCriteria.getDate().getLessThanOrEqual();

        List<Transaction> transactionList = overpayService.getTransactionListByDate(fromDate, toDate);

        assertNotNull(transactionList);
        /*for (TransactionDTO transactionDTO : transactionList) {

        }*/

    }

    private Bill createBill(Recipient recipient, String login, Long id) {
        Bill bill = BillResourceIntTest.createEntity(null);
        bill.setId(id);
        bill.setRecipientId(recipient.getId());
        bill.setLogin(login);
        bill.setSuccess(true);
        bill.setType(RequestType.PAY);

        return billRepository.save(bill);
    }

    private Transaction createTransaction(Recipient recipient, String requestLogin) {
        Transaction transaction = new Transaction();
        transaction.setPayType(PayType.AGENT);
        transaction.setRecipientName(recipient.getShortName());
        transaction.setRecipientStatus("Успещно оплачен");
        transaction.setRecipientSuccess(true);
        transaction.setSenderSuccess(true);
        transaction.setRequestAmount(1000L);
        transaction.setRequestCcy(840);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setGatewayAmount(1.1);
        transaction.setAmountRatio(1);
        transaction.setRequestGroup("2");
        transaction.setSenderGateway("paynet");
        transaction.setRequestTrId(String.valueOf(System.currentTimeMillis()));
        transaction.setSenderName("paynet_agent");
        transaction.setSenderAmount(100000L);
        transaction.setSenderDate(Instant.now());
        transaction.setRecipientDate(Instant.now());
        transaction.setSystemFeeAmount(null);
        transaction.setComment("comment");
        transaction.systemCommissionDownAmount(5L);
        transaction.setSystemFeeAmount(1000L);
        transaction.setRecipientServiceId(recipient.getId());
        transaction.setRecipientSuccess(false);
        transaction.setRequestLogin(requestLogin);
        transaction.setRecipientAccount("wizlocker");
        transaction.setRecipientCcy(860);
        transaction.setDate(LocalDateTime.now().toInstant(ZoneOffset.ofHours(0)));
        transactionRepository.save(transaction);
        transaction.setRecipientBillId(createBill(transaction));
        return transaction;
    }

    private Long createBill(Transaction transaction) {
        Bill bill = new Bill();
        bill.setSuccess(false);
        bill.setAccount(transaction.getRecipientAccount());
        bill.setAmountRatio(AmountRatio.TIYIN.getRatio());
        bill.setAmount((long) (transaction.getGatewayAmount() * 100));
        bill.setCurrency(transaction.getSystemCurrency());
        bill.setType(RequestType.PAY);
        bill.setResponseText("Ошибка оператора");
        bill.setResponseCode("-300");
        bill.setRecipientShortName(transaction.getRecipientName());
//        bill.setSessionId(String.valueOf(System.currentTimeMillis()));
//        bill.setProfit(0L);
        bill.setRequestTime(LocalDateTime.now().toInstant(ZoneOffset.ofHours(0)));
        bill.setResponseTime(LocalDateTime.now().toInstant(ZoneOffset.ofHours(0)));
        bill.setTransactionId(transaction.getId());
        return billRepository.save(bill).getId();
    }

    //    @Test
    @Transactional
    @WithMockUser(value = Constants.SYSTEM_AGENT_NAME)
    public void doNewPayBill() {

    }

    @Test
    public void testNumberFormatException() {
        String number = "12345";
        if (NumberUtils.isCreatable(number)) {
            try {
                int num = Integer.parseInt(number);
                assertEquals(12345, num);
            } catch (NumberFormatException e) {
                fail();
            }
        }
    }
}
