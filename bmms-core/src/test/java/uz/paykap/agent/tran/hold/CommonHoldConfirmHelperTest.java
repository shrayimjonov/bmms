package uz.paykap.agent.tran.hold;

import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bm.dummy.adapter.DummyDataAdapterV3;
import uz.devops.bmcore.dto.request.CheckRequest;
import uz.devops.bmcore.dto.request.PayRequest;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.devops.humo.service.HumoGatewayService;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.result.CommonResultData;
import uz.javlon.commons.result.HasResult;
import uz.javlon.svgate.dto.rpc.request.ConfirmHoldRequestDTO;
import uz.javlon.svgate.service.SvGateHoldService;
import uz.paykap.IntegrationTest;
import uz.paykap.adapter.DummyAdapterDelegate;
import uz.paykap.agent.tran.CommonTranServiceV3;
import uz.paykap.domain.Recipient;
import uz.paykap.repository.RecipientRepository;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.service.dto.p2p.BmResultStatus;
import uz.paykap.web.rest.util.DummyUtils;

import java.util.HashMap;
import java.util.Optional;

import static org.junit.Assert.assertThrows;
import static uz.devops.bm.dummy.adapter.DummyDataAdapterV3.DUMMY_AGENT;

@RunWith(SpringRunner.class)
@IntegrationTest
public class CommonHoldConfirmHelperTest {

    @Autowired
    private DummyAdapterDelegate dummyAdapterDelegate;
    @Autowired
    private RecipientRepository recipientRepository;
    @Autowired
    private CommonHoldConfirmHelper commonHoldConfirmHelper;
    @Autowired
    private CommonTranServiceV3 commonTranServiceV3;
    @Autowired
    private TransactionRepository transactionRepository;
    @MockBean
    public SvGateHoldService svGateHoldService;
    @MockBean
    public HumoGatewayService humoGatewayService;

    @Test
    @WithMockUser(DUMMY_AGENT)
    @Transactional
    public void holdConfirmUzCardErrorResponseFromSvGate() {

        DummyUtils.prepareDummyData(dummyAdapterDelegate);

        Optional<Recipient> optionalRecipient = recipientRepository.findTop1ByShortName(DummyDataAdapterV3.DUMMY_RECIPIENT_USD);

        Assume.assumeTrue(optionalRecipient.isPresent());

        CheckRequest simpleCheckDTO = new CheckRequest();
        simpleCheckDTO.setAmount(1000000L);
        simpleCheckDTO.setAccount("933999959");
        simpleCheckDTO.setRecipientId(optionalRecipient.get().getId());

        // Check payment

        SimpleBillResponse check = commonTranServiceV3.check(simpleCheckDTO);

//         Test CHECK_OK
        Assert.assertNotNull(check);
        Assert.assertTrue(check.isSuccess());
        Assert.assertEquals(check.getStatus(), TransactionStatus.CHECK_OK);

//         Do payment

        PayRequest payDTO = new PayRequest();
        payDTO.setBillId(check.getId());
        var transaction = transactionRepository.findAll().get(0);
        transaction.setStatus(TransactionStatus.HOLD_SUCCESS);
        transaction.setPayType(PayType.UZCARD);
        transaction.setSenderTrId(String.valueOf(System.currentTimeMillis()));
        transactionRepository.save(transaction);
        SimpleBillResponse pay = commonHoldConfirmHelper.holdConfirm(payDTO);

        // Test SUCCESS

        Assert.assertNotNull(pay);
        Assert.assertFalse(pay.isSuccess());
        Assert.assertEquals(pay.getStatus(), TransactionStatus.HOLD_CONFIRM_ERROR);
        Assert.assertEquals(pay.getResponseText(), "SENDER_PC_ERROR : sv response is null, OK");
        Assert.assertNotNull(pay.getReceiver());
        Assert.assertNotNull(pay.getSender());
        Assert.assertNotNull(pay.getData());
    }

    @Test
    @WithMockUser(DUMMY_AGENT)
    @Transactional
    public void holdConfirmUzCardSuccessResponseFromSvGate() {
        setupMocksUzcardSuccess();
        DummyUtils.prepareDummyData(dummyAdapterDelegate);

        Optional<Recipient> optionalRecipient = recipientRepository.findTop1ByShortName(DummyDataAdapterV3.DUMMY_RECIPIENT_USD);

        Assume.assumeTrue(optionalRecipient.isPresent());

        CheckRequest simpleCheckDTO = new CheckRequest();
        simpleCheckDTO.setAmount(1000000L);
        simpleCheckDTO.setAccount("933999959");
        simpleCheckDTO.setRecipientId(optionalRecipient.get().getId());

        // Check payment

        SimpleBillResponse check = commonTranServiceV3.check(simpleCheckDTO);

//         Test CHECK_OK
        Assert.assertNotNull(check);
        Assert.assertTrue(check.isSuccess());
        Assert.assertEquals(check.getStatus(), TransactionStatus.CHECK_OK);

//         Do payment

        PayRequest payDTO = new PayRequest();
        payDTO.setBillId(check.getId());
        var transaction = transactionRepository.findAll().get(0);
        transaction.setStatus(TransactionStatus.HOLD_SUCCESS);
        transaction.setPayType(PayType.UZCARD);
        transaction.setSenderTrId(String.valueOf(System.currentTimeMillis()));
        transactionRepository.save(transaction);
        SimpleBillResponse pay = commonHoldConfirmHelper.holdConfirm(payDTO);

        // Test SUCCESS

        Assert.assertNotNull(pay);
        Assert.assertTrue(pay.isSuccess());
        Assert.assertEquals(pay.getStatus(), TransactionStatus.SUCCESS);
        Assert.assertEquals(pay.getResponseText(), "SUCCESS, OK");
        Assert.assertNotNull(pay.getReceiver());
        Assert.assertNotNull(pay.getSender());
        Assert.assertNotNull(pay.getData());
    }

    @Test
    @WithMockUser(DUMMY_AGENT)
    @Transactional
    public void holdConfirmHumoSuccess() {

        DummyUtils.prepareDummyData(dummyAdapterDelegate);

        Optional<Recipient> optionalRecipient = recipientRepository.findTop1ByShortName(DummyDataAdapterV3.DUMMY_RECIPIENT_USD);

        Assume.assumeTrue(optionalRecipient.isPresent());

        CheckRequest simpleCheckDTO = new CheckRequest();
        simpleCheckDTO.setAmount(1000000L);
        simpleCheckDTO.setAccount("933999959");
        simpleCheckDTO.setRecipientId(optionalRecipient.get().getId());
        // Check payment
        SimpleBillResponse check = commonTranServiceV3.check(simpleCheckDTO);
//         Test CHECK_OK
        Assert.assertNotNull(check);
        Assert.assertTrue(check.isSuccess());
        Assert.assertEquals(check.getStatus(), TransactionStatus.CHECK_OK);
//         Do payment
        PayRequest payDTO = new PayRequest();
        payDTO.setBillId(check.getId());
        payDTO.setParams(new HashMap<>());
        var transaction = transactionRepository.findAll().get(0);
        transaction.setStatus(TransactionStatus.HOLD_SUCCESS);
        transaction.setPayType(PayType.HUMO);
        transaction.setSenderTrId(String.valueOf(System.currentTimeMillis()));
        transactionRepository.save(transaction);
        SimpleBillResponse pay = commonHoldConfirmHelper.holdConfirm(payDTO);
        // Test SUCCESS
        Assert.assertNotNull(pay);
        Assert.assertTrue(pay.isSuccess());
        Assert.assertEquals(pay.getStatus(), TransactionStatus.SUCCESS);
    }

    @Test
    @WithMockUser(DUMMY_AGENT)
    @Transactional
    public void holdConfirm2HumoError() {
        setupMocksHumoSuccess();
        DummyUtils.prepareDummyData(dummyAdapterDelegate);

        Optional<Recipient> optionalRecipient = recipientRepository.findTop1ByShortName(DummyDataAdapterV3.DUMMY_RECIPIENT_USD);

        Assume.assumeTrue(optionalRecipient.isPresent());

        CheckRequest simpleCheckDTO = new CheckRequest();
        simpleCheckDTO.setAmount(1000000L);
        simpleCheckDTO.setAccount("933999959");
        simpleCheckDTO.setRecipientId(optionalRecipient.get().getId());
        // Check payment
        SimpleBillResponse check = commonTranServiceV3.check(simpleCheckDTO);
//         Test CHECK_OK
        Assert.assertNotNull(check);
        Assert.assertTrue(check.isSuccess());
        Assert.assertEquals(check.getStatus(), TransactionStatus.CHECK_OK);
//         Do payment
        PayRequest payDTO = new PayRequest();
        payDTO.setBillId(check.getId());
        payDTO.setParams(new HashMap<>());
        var transaction = transactionRepository.findAll().get(0);
        transaction.setStatus(TransactionStatus.HOLD_SUCCESS);
        transaction.setPayType(PayType.HUMO);
        transaction.setSenderTrId(String.valueOf(System.currentTimeMillis()));
        transactionRepository.save(transaction);
        SimpleBillResponse pay = commonHoldConfirmHelper.holdConfirm(payDTO);
        // Test SUCCESS
        Assert.assertNotNull(pay);
        Assert.assertFalse(pay.isSuccess());
        Assert.assertEquals(pay.getStatus(), TransactionStatus.HOLD_CONFIRM_ERROR);
    }

    @Test
    @WithMockUser(DUMMY_AGENT)
    @Transactional
    public void holdConfirmTransactionNotFound() {
        DummyUtils.prepareDummyData(dummyAdapterDelegate);
        Optional<Recipient> optionalRecipient = recipientRepository.findTop1ByShortName(DummyDataAdapterV3.DUMMY_RECIPIENT_USD);

        Assume.assumeTrue(optionalRecipient.isPresent());

        CheckRequest simpleCheckDTO = new CheckRequest();
        simpleCheckDTO.setAmount(1000000L);
        simpleCheckDTO.setAccount("933999959");
        simpleCheckDTO.setRecipientId(optionalRecipient.get().getId());

        SimpleBillResponse check = commonTranServiceV3.check(simpleCheckDTO);

        Assert.assertNotNull(check);
        Assert.assertTrue(check.isSuccess());
        Assert.assertEquals(check.getStatus(), TransactionStatus.CHECK_OK);

        PayRequest payDTO = new PayRequest();
        payDTO.setBillId(check.getId());
        assertThrows("400, Чек не найден, bill_not_found, langs={uz=Chek topilmadi, en=Cheque not found, ru=Чек не найден", CodedException.class, () -> commonHoldConfirmHelper.holdConfirm(payDTO));
    }

    @Test
    @WithMockUser(DUMMY_AGENT)
    @Transactional
    public void holdConfirmTranTypeNotFound() {

        DummyUtils.prepareDummyData(dummyAdapterDelegate);

        Optional<Recipient> optionalRecipient = recipientRepository.findTop1ByShortName(DummyDataAdapterV3.DUMMY_RECIPIENT_USD);

        Assume.assumeTrue(optionalRecipient.isPresent());

        CheckRequest simpleCheckDTO = new CheckRequest();
        simpleCheckDTO.setAmount(1000000L);
        simpleCheckDTO.setAccount("933999959");
        simpleCheckDTO.setRecipientId(optionalRecipient.get().getId());
        // Check payment
        SimpleBillResponse check = commonTranServiceV3.check(simpleCheckDTO);
//         Test CHECK_OK
        Assert.assertNotNull(check);
        Assert.assertTrue(check.isSuccess());
        Assert.assertEquals(check.getStatus(), TransactionStatus.CHECK_OK);
//         Do payment
        PayRequest payDTO = new PayRequest();
        payDTO.setBillId(check.getId());
        payDTO.setParams(new HashMap<>());
        var transaction = transactionRepository.findAll().get(0);
        transaction.setStatus(TransactionStatus.HOLD_SUCCESS);
        transactionRepository.save(transaction);
        SimpleBillResponse pay = commonHoldConfirmHelper.holdConfirm(payDTO);
        Assert.assertFalse(pay.isSuccess());
    }

    private void setupMocksUzcardError() {
        Mockito.when(svGateHoldService.confirmHold(ArgumentMatchers.any())).thenReturn(new CommonResultData<>(BmResultStatus.FAILED, null));
        ConfirmHoldRequestDTO requestDTO = new ConfirmHoldRequestDTO();
//        requestDTO.setStan("");
        Mockito.when(svGateHoldService.confirmHold(requestDTO)).thenReturn(new CommonResultData<>(BmResultStatus.SUCCESS, null));
    }

    private void setupMocksUzcardSuccess() {
        Mockito.when(svGateHoldService.confirmHold(ArgumentMatchers.any())).thenReturn(new CommonResultData<>(BmResultStatus.SUCCESS, null));
    }

    @SneakyThrows
    private void setupMocksHumoSuccess() {
        Mockito.when(humoGatewayService.confirmDualMessageDebit(ArgumentMatchers.any())).thenReturn(new CommonResultData<>(HasResult.UNKNOWN_ERROR, null));
    }

    @SneakyThrows
    private void setupMocksHumo() {
        Mockito.when(humoGatewayService.confirmDualMessageDebit(ArgumentMatchers.any())).thenReturn(new CommonResultData<>(BmResultStatus.FAILED, null));
    }

    @Test
    @WithMockUser(DUMMY_AGENT)
    @Transactional
    public void holdConfirmUzCardError() {
        setupMocksUzcardError();
        DummyUtils.prepareDummyData(dummyAdapterDelegate);

        Optional<Recipient> optionalRecipient = recipientRepository.findTop1ByShortName(DummyDataAdapterV3.DUMMY_RECIPIENT_USD);

        Assume.assumeTrue(optionalRecipient.isPresent());

        CheckRequest simpleCheckDTO = new CheckRequest();
        simpleCheckDTO.setAmount(1000000L);
        simpleCheckDTO.setAccount("933999959");
        simpleCheckDTO.setRecipientId(optionalRecipient.get().getId());

        // Check payment

        SimpleBillResponse check = commonTranServiceV3.check(simpleCheckDTO);

//         Test CHECK_OK

        Assert.assertNotNull(check);
        Assert.assertTrue(check.isSuccess());
        Assert.assertEquals(check.getStatus(), TransactionStatus.CHECK_OK);

//         Do payment

        PayRequest payDTO = new PayRequest();
        payDTO.setBillId(check.getId());
        var transaction = transactionRepository.findAll().get(0);
        transaction.setStatus(TransactionStatus.HOLD_SUCCESS);
        transaction.setPayType(PayType.UZCARD);
        transaction.setSenderTrId(String.valueOf(System.currentTimeMillis()));
        transactionRepository.save(transaction);
        SimpleBillResponse pay = commonHoldConfirmHelper.holdConfirm(payDTO);

        // Test SUCCESS

        Assert.assertNotNull(pay);
        Assert.assertFalse(pay.isSuccess());
        Assert.assertEquals(pay.getStatus(), TransactionStatus.HOLD_CONFIRM_ERROR);
    }

    @Test
    @WithMockUser(DUMMY_AGENT)
    @Transactional
    public void holdConfirmHumoError() {
        setupMocksHumo();
        DummyUtils.prepareDummyData(dummyAdapterDelegate);

        Optional<Recipient> optionalRecipient = recipientRepository.findTop1ByShortName(DummyDataAdapterV3.DUMMY_RECIPIENT_USD);

        Assume.assumeTrue(optionalRecipient.isPresent());

        CheckRequest simpleCheckDTO = new CheckRequest();
        simpleCheckDTO.setAmount(1000000L);
        simpleCheckDTO.setAccount("933999959");
        simpleCheckDTO.setRecipientId(optionalRecipient.get().getId());
        // Check payment
        SimpleBillResponse check = commonTranServiceV3.check(simpleCheckDTO);
//         Test CHECK_OK
        Assert.assertNotNull(check);
        Assert.assertTrue(check.isSuccess());
        Assert.assertEquals(check.getStatus(), TransactionStatus.CHECK_OK);
//         Do payment
        PayRequest payDTO = new PayRequest();
        payDTO.setBillId(check.getId());
        payDTO.setParams(new HashMap<>());
        var transaction = transactionRepository.findAll().get(0);
        transaction.setStatus(TransactionStatus.HOLD_SUCCESS);
        transaction.setPayType(PayType.HUMO);
        transaction.setSenderTrId(String.valueOf(System.currentTimeMillis()));
        transactionRepository.save(transaction);
        SimpleBillResponse pay = commonHoldConfirmHelper.holdConfirm(payDTO);
        // Test SUCCESS
        Assert.assertNotNull(pay);
        Assert.assertFalse(pay.isSuccess());
        Assert.assertEquals(pay.getStatus(), TransactionStatus.HOLD_CONFIRM_ERROR);
    }
}
