package uz.paykap.recipient.p2p.reconciliation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bmcore.enumeration.PayType;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.javlon.commons.base.BasicResult;
import uz.paykap.PkbillmsApp;
import uz.paykap.config.Constants;
import uz.paykap.config.TestSecurityConfiguration;
import uz.paykap.domain.ReconcileSummary;
import uz.paykap.domain.Transaction;
import uz.paykap.domain.enumeration.P2pProcessorType;
import uz.paykap.domain.enumeration.ReconcileSummaryStatus;
import uz.paykap.repository.ReconcileSummaryRepository;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.service.reconciliation.p2p.P2pRecoHelperServiceImpl;
import uz.paykap.web.rest.TransactionResourceIntTest;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PkbillmsApp.class, TestSecurityConfiguration.class}, properties = {"humo.simulate=true", "humo.username=test", "humo.password=test", "humo.getPaymentUrl=http://127.0.0.1:11210"})
public class UzCardP2pReconciliationServiceTest {


    public static final String TEST_USERNAME = "";
    public static final long TEST_AMOUNT = 100000L;
    public static final long TEST_AMOUNT2 = 100000000L;
    public static final int FROM_TIME = 1640545200;
    public static final int TO_TIME = 1640545440;
    public static final int EMPTY_FROM_TIME = 1640545200;
    public static final int EMPTY_TO_TIME = 1640545201;
    @Autowired
    private UzCardP2pReconciliationService uzCardReconcileService;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private ReconcileSummaryRepository reconcileSummaryRepository;

    @Test
    @Transactional
    @WithMockUser(TEST_USERNAME)
    public void testUzCardReconcileToSuccess() {
        initTransaction(100000000, TEST_AMOUNT, P2pProcessorType.UZCARD.name(), P2pProcessorType.HUMO.name());
        initTransaction(14500000, TEST_AMOUNT2, P2pProcessorType.UZCARD.name(), P2pProcessorType.HUMO.name());
        initTransaction(500000, TEST_AMOUNT2, P2pProcessorType.UZCARD.name(), P2pProcessorType.HUMO.name());
        initTransaction(4500000, TEST_AMOUNT2, P2pProcessorType.UZCARD.name(), P2pProcessorType.HUMO.name());
        initTransaction(8200000, TEST_AMOUNT2, P2pProcessorType.UZCARD.name(), P2pProcessorType.HUMO.name());
        initTransaction(105000000, TEST_AMOUNT2, P2pProcessorType.UZCARD.name(), P2pProcessorType.HUMO.name());
        initTransaction(TEST_AMOUNT, 100000000, P2pProcessorType.HUMO.name(), P2pProcessorType.UZCARD.name());
        initTransaction(TEST_AMOUNT, 5000000, P2pProcessorType.HUMO.name(), P2pProcessorType.UZCARD.name());
        initTransaction(TEST_AMOUNT, 1077000, P2pProcessorType.HUMO.name(), P2pProcessorType.UZCARD.name());
        initTransaction(TEST_AMOUNT, 8200000, P2pProcessorType.HUMO.name(), P2pProcessorType.UZCARD.name());

        BasicResult reconcile = uzCardReconcileService.reconcile(Instant.ofEpochSecond(FROM_TIME), Instant.ofEpochSecond(TO_TIME));

        assertThat(reconcile).isNotNull();
        assertThat(reconcile.isSuccess()).isTrue();

        List<ReconcileSummary> all = reconcileSummaryRepository.findAll();
        assertThat(all).isNotNull();
        assertThat(all.size()).isGreaterThan(0);
        assertThat(all.size()).isEqualTo(2);

        Optional<ReconcileSummary> creditReconcileOptional = all.stream().filter(reco -> Objects.equals(reco.getGatewayName(), P2pProcessorType.UZCARD.name() + P2pRecoHelperServiceImpl.P_2_P_CREDIT_CONFIRM)).findAny();
        assertThat(creditReconcileOptional.isPresent()).isTrue();
        ReconcileSummary creditReconcile = creditReconcileOptional.get();
        assertThat(creditReconcile.getStatus()).isEqualTo(ReconcileSummaryStatus.COMPARED_OK);
        assertThat(creditReconcile.getSuccess()).isTrue();
        assertThat(creditReconcile.getGatewayName()).isNotEmpty();
        assertThat(creditReconcile.getSystemAmount()).isNotNull();
        assertThat(creditReconcile.getSystemCount()).isNotNull();
        assertThat(creditReconcile.getSystemCount()).isEqualTo(creditReconcile.getGatewayCount());
        assertThat(creditReconcile.getSystemAmount()).isEqualTo(creditReconcile.getGatewayAmount());

        Optional<ReconcileSummary> debitReconcileOptional = all.stream().filter(reco -> Objects.equals(reco.getGatewayName(), P2pProcessorType.UZCARD.name() + P2pRecoHelperServiceImpl.P_2_P_DEBIT_CONFIRM)).findAny();
        assertThat(debitReconcileOptional.isPresent()).isTrue();
        ReconcileSummary debitReconcile = debitReconcileOptional.get();
        assertThat(debitReconcile.getStatus()).isEqualTo(ReconcileSummaryStatus.COMPARED_OK);
        assertThat(debitReconcile.getSuccess()).isTrue();
        assertThat(debitReconcile.getGatewayName()).isNotEmpty();
        assertThat(debitReconcile.getSystemAmount()).isNotNull();
        assertThat(debitReconcile.getSystemCount()).isNotNull();
        assertThat(debitReconcile.getSystemCount()).isEqualTo(debitReconcile.getGatewayCount());
        assertThat(debitReconcile.getSystemAmount()).isEqualTo(debitReconcile.getGatewayAmount());

    }
    @Test
    @Transactional
    @WithMockUser(TEST_USERNAME)
    public void testUzCardDebitConfirmReconcileForDifferentAmount() {
        initTransaction(100000000, TEST_AMOUNT, P2pProcessorType.UZCARD.name(), P2pProcessorType.HUMO.name());
        initTransaction(14500000 - 100000, TEST_AMOUNT2, P2pProcessorType.UZCARD.name(), P2pProcessorType.HUMO.name());
        initTransaction(500000, TEST_AMOUNT2, P2pProcessorType.UZCARD.name(), P2pProcessorType.HUMO.name());
        initTransaction(4500000, TEST_AMOUNT2, P2pProcessorType.UZCARD.name(), P2pProcessorType.HUMO.name());
        initTransaction(8200000, TEST_AMOUNT2, P2pProcessorType.UZCARD.name(), P2pProcessorType.HUMO.name());
        initTransaction(105000000, TEST_AMOUNT2, P2pProcessorType.UZCARD.name(), P2pProcessorType.HUMO.name());
        initTransaction(TEST_AMOUNT, 100000000, P2pProcessorType.HUMO.name(), P2pProcessorType.UZCARD.name());
        initTransaction(TEST_AMOUNT, 5000000, P2pProcessorType.HUMO.name(), P2pProcessorType.UZCARD.name());
        initTransaction(TEST_AMOUNT, 1077000, P2pProcessorType.HUMO.name(), P2pProcessorType.UZCARD.name());
        initTransaction(TEST_AMOUNT, 8200000, P2pProcessorType.HUMO.name(), P2pProcessorType.UZCARD.name());

        BasicResult reconcile = uzCardReconcileService.reconcile(Instant.ofEpochSecond(FROM_TIME), Instant.ofEpochSecond(TO_TIME));

        assertThat(reconcile).isNotNull();
        assertThat(reconcile.isSuccess()).isFalse();

        List<ReconcileSummary> all = reconcileSummaryRepository.findAll();
        assertThat(all).isNotNull();
        assertThat(all.size()).isGreaterThan(0);
        assertThat(all.size()).isEqualTo(2);

        Optional<ReconcileSummary> debitReconcileOptional = all.stream().filter(reco -> Objects.equals(reco.getGatewayName(), P2pProcessorType.UZCARD.name() + P2pRecoHelperServiceImpl.P_2_P_DEBIT_CONFIRM)).findAny();
        assertThat(debitReconcileOptional.isPresent()).isTrue();

        ReconcileSummary debitReconcile = debitReconcileOptional.get();
        assertThat(debitReconcile.getStatus()).isEqualTo(ReconcileSummaryStatus.COMPARED_FALSE);
        assertThat(debitReconcile.getSuccess()).isFalse();
        assertThat(debitReconcile.getGatewayName()).isNotEmpty();
        assertThat(debitReconcile.getGatewayName()).isEqualTo(P2pProcessorType.UZCARD.name() + P2pRecoHelperServiceImpl.P_2_P_DEBIT_CONFIRM);
        assertThat(debitReconcile.getSystemAmount()).isNotNull();
        assertThat(debitReconcile.getSystemAmount()).isNotEqualTo(debitReconcile.getGatewayAmount());
        assertThat(debitReconcile.getSystemCount()).isEqualTo(debitReconcile.getGatewayCount());

        Optional<ReconcileSummary> creditReconcileOptional = all.stream().filter(reco -> Objects.equals(reco.getGatewayName(), P2pProcessorType.UZCARD.name() + P2pRecoHelperServiceImpl.P_2_P_CREDIT_CONFIRM)).findAny();
        assertThat(creditReconcileOptional.isPresent()).isTrue();
        ReconcileSummary creditReconcile = creditReconcileOptional.get();
        assertThat(creditReconcile.getStatus()).isEqualTo(ReconcileSummaryStatus.COMPARED_OK);
        assertThat(creditReconcile.getSuccess()).isTrue();
        assertThat(creditReconcile.getGatewayName()).isNotEmpty();
        assertThat(creditReconcile.getSystemAmount()).isNotNull();
        assertThat(creditReconcile.getSystemCount()).isNotNull();
        assertThat(creditReconcile.getSystemCount()).isEqualTo(creditReconcile.getGatewayCount());
        assertThat(creditReconcile.getSystemAmount()).isEqualTo(creditReconcile.getGatewayAmount());
    }

    @Test
    @Transactional
    @WithMockUser(TEST_USERNAME)
    public void testUzCardDebitConfirmReconcileForDifferentCount() {
        initTransaction(100000000, TEST_AMOUNT, P2pProcessorType.UZCARD.name(), P2pProcessorType.HUMO.name());
        initTransaction(14500000, TEST_AMOUNT2, P2pProcessorType.UZCARD.name(), P2pProcessorType.HUMO.name());
//        initTransaction(500000, TEST_AMOUNT2, P2pProcessorType.HUMO.name(), P2pProcessorType.UZCARD.name());
        initTransaction(4500000 + 500000, TEST_AMOUNT2, P2pProcessorType.UZCARD.name(), P2pProcessorType.HUMO.name());
        initTransaction(8200000, TEST_AMOUNT2, P2pProcessorType.UZCARD.name(), P2pProcessorType.HUMO.name());
        initTransaction(105000000, TEST_AMOUNT2, P2pProcessorType.UZCARD.name(), P2pProcessorType.HUMO.name());

        initTransaction(TEST_AMOUNT, 100000000, P2pProcessorType.HUMO.name(), P2pProcessorType.UZCARD.name());
        initTransaction(TEST_AMOUNT, 5000000, P2pProcessorType.HUMO.name(), P2pProcessorType.UZCARD.name());
        initTransaction(TEST_AMOUNT, 1077000, P2pProcessorType.HUMO.name(), P2pProcessorType.UZCARD.name());
        initTransaction(TEST_AMOUNT, 8200000, P2pProcessorType.HUMO.name(), P2pProcessorType.UZCARD.name());

        BasicResult reconcile = uzCardReconcileService.reconcile(Instant.ofEpochSecond(FROM_TIME), Instant.ofEpochSecond(TO_TIME));

        assertThat(reconcile).isNotNull();
        assertThat(reconcile.isSuccess()).isFalse();

        List<ReconcileSummary> all = reconcileSummaryRepository.findAll();
        assertThat(all).isNotNull();
        assertThat(all.size()).isGreaterThan(0);
        assertThat(all.size()).isEqualTo(2);

        Optional<ReconcileSummary> debitReconcileOptional = all.stream().filter(reco -> Objects.equals(reco.getGatewayName(), P2pProcessorType.UZCARD.name() + P2pRecoHelperServiceImpl.P_2_P_DEBIT_CONFIRM)).findAny();
        assertThat(debitReconcileOptional.isPresent()).isTrue();

        ReconcileSummary debitReconcile = debitReconcileOptional.get();
        assertThat(debitReconcile.getStatus()).isEqualTo(ReconcileSummaryStatus.COMPARED_FALSE);
        assertThat(debitReconcile.getSuccess()).isFalse();
        assertThat(debitReconcile.getGatewayName()).isNotEmpty();
        assertThat(debitReconcile.getGatewayName()).isEqualTo(P2pProcessorType.UZCARD.name() + P2pRecoHelperServiceImpl.P_2_P_DEBIT_CONFIRM);
        assertThat(debitReconcile.getSystemAmount()).isNotNull();
        assertThat(debitReconcile.getSystemAmount()).isEqualTo(debitReconcile.getGatewayAmount());
        assertThat(debitReconcile.getSystemCount()).isNotNull();
        assertThat(debitReconcile.getSystemCount()).isNotEqualTo(debitReconcile.getGatewayCount());

        Optional<ReconcileSummary> creditReconcileOptional = all.stream().filter(reco -> Objects.equals(reco.getGatewayName(), P2pProcessorType.UZCARD.name() + P2pRecoHelperServiceImpl.P_2_P_CREDIT_CONFIRM)).findAny();
        assertThat(creditReconcileOptional.isPresent()).isTrue();
        ReconcileSummary creditReconcile = creditReconcileOptional.get();
        assertThat(creditReconcile.getStatus()).isEqualTo(ReconcileSummaryStatus.COMPARED_OK);
        assertThat(creditReconcile.getSuccess()).isTrue();
        assertThat(creditReconcile.getGatewayName()).isNotEmpty();
        assertThat(creditReconcile.getSystemAmount()).isNotNull();
        assertThat(creditReconcile.getSystemCount()).isNotNull();
        assertThat(creditReconcile.getSystemCount()).isEqualTo(creditReconcile.getGatewayCount());
        assertThat(creditReconcile.getSystemAmount()).isEqualTo(creditReconcile.getGatewayAmount());
    }

    @Test
    @Transactional
    @WithMockUser(TEST_USERNAME)
    public void testUzCardCreditConfirmReconcileForDifferentAmount() {
        initTransaction(100000000, TEST_AMOUNT, P2pProcessorType.UZCARD.name(), P2pProcessorType.HUMO.name());
        initTransaction(14500000, TEST_AMOUNT2, P2pProcessorType.UZCARD.name(), P2pProcessorType.HUMO.name());
        initTransaction(500000, TEST_AMOUNT2, P2pProcessorType.UZCARD.name(), P2pProcessorType.HUMO.name());
        initTransaction(4500000, TEST_AMOUNT2, P2pProcessorType.UZCARD.name(), P2pProcessorType.HUMO.name());
        initTransaction(8200000, TEST_AMOUNT2, P2pProcessorType.UZCARD.name(), P2pProcessorType.HUMO.name());
        initTransaction(105000000, TEST_AMOUNT2, P2pProcessorType.UZCARD.name(), P2pProcessorType.HUMO.name());

        initTransaction(TEST_AMOUNT, 100000000, P2pProcessorType.HUMO.name(), P2pProcessorType.UZCARD.name());
        initTransaction(TEST_AMOUNT, 5000000 - 10000, P2pProcessorType.HUMO.name(), P2pProcessorType.UZCARD.name());
        initTransaction(TEST_AMOUNT, 1077000, P2pProcessorType.HUMO.name(), P2pProcessorType.UZCARD.name());
        initTransaction(TEST_AMOUNT, 8200000, P2pProcessorType.HUMO.name(), P2pProcessorType.UZCARD.name());

        BasicResult reconcile = uzCardReconcileService.reconcile(Instant.ofEpochSecond(FROM_TIME), Instant.ofEpochSecond(TO_TIME));

        assertThat(reconcile).isNotNull();
        assertThat(reconcile.isSuccess()).isFalse();

        List<ReconcileSummary> all = reconcileSummaryRepository.findAll();
        assertThat(all).isNotNull();
        assertThat(all.size()).isGreaterThan(0);
        assertThat(all.size()).isEqualTo(2);

        Optional<ReconcileSummary> creditReconcileOptional = all.stream().filter(reco -> Objects.equals(reco.getGatewayName(), P2pProcessorType.UZCARD.name() + P2pRecoHelperServiceImpl.P_2_P_CREDIT_CONFIRM)).findAny();
        assertThat(creditReconcileOptional.isPresent()).isTrue();

        ReconcileSummary creditReconcile = creditReconcileOptional.get();
        assertThat(creditReconcile.getStatus()).isEqualTo(ReconcileSummaryStatus.COMPARED_FALSE);
        assertThat(creditReconcile.getSuccess()).isFalse();
        assertThat(creditReconcile.getGatewayName()).isNotEmpty();
        assertThat(creditReconcile.getGatewayName()).isEqualTo(P2pProcessorType.UZCARD.name() + P2pRecoHelperServiceImpl.P_2_P_CREDIT_CONFIRM);
        assertThat(creditReconcile.getSystemAmount()).isNotNull();
        assertThat(creditReconcile.getSystemAmount()).isNotEqualTo(creditReconcile.getGatewayAmount());
        assertThat(creditReconcile.getSystemCount()).isEqualTo(creditReconcile.getGatewayCount());

        Optional<ReconcileSummary> debitReconcileOptional = all.stream().filter(reco -> Objects.equals(reco.getGatewayName(), P2pProcessorType.UZCARD.name() + P2pRecoHelperServiceImpl.P_2_P_DEBIT_CONFIRM)).findAny();
        assertThat(debitReconcileOptional.isPresent()).isTrue();
        ReconcileSummary debitReconcile = debitReconcileOptional.get();
        assertThat(debitReconcile.getStatus()).isEqualTo(ReconcileSummaryStatus.COMPARED_OK);
        assertThat(debitReconcile.getSuccess()).isTrue();
        assertThat(debitReconcile.getGatewayName()).isNotEmpty();
        assertThat(debitReconcile.getSystemAmount()).isNotNull();
        assertThat(debitReconcile.getSystemCount()).isNotNull();
        assertThat(debitReconcile.getSystemCount()).isEqualTo(debitReconcile.getGatewayCount());
        assertThat(debitReconcile.getSystemAmount()).isEqualTo(debitReconcile.getGatewayAmount());
    }

    @Test
    @Transactional
    @WithMockUser(TEST_USERNAME)
    public void testUzCardCreditConfirmReconcileForDifferentCount() {
        initTransaction(100000000, TEST_AMOUNT, P2pProcessorType.UZCARD.name(), P2pProcessorType.HUMO.name());
        initTransaction(14500000, TEST_AMOUNT2, P2pProcessorType.UZCARD.name(), P2pProcessorType.HUMO.name());
        initTransaction(500000, TEST_AMOUNT2, P2pProcessorType.UZCARD.name(), P2pProcessorType.HUMO.name());
        initTransaction(4500000, TEST_AMOUNT2, P2pProcessorType.UZCARD.name(), P2pProcessorType.HUMO.name());
        initTransaction(8200000, TEST_AMOUNT2, P2pProcessorType.UZCARD.name(), P2pProcessorType.HUMO.name());
        initTransaction(105000000, TEST_AMOUNT2, P2pProcessorType.UZCARD.name(), P2pProcessorType.HUMO.name());

        initTransaction(TEST_AMOUNT, 100000000, P2pProcessorType.HUMO.name(), P2pProcessorType.UZCARD.name());
//        initTransaction(TEST_AMOUNT, 5000000, P2pProcessorType.HUMO.name(), P2pProcessorType.UZCARD.name());
        initTransaction(TEST_AMOUNT, 1077000 + 5000000, P2pProcessorType.HUMO.name(), P2pProcessorType.UZCARD.name());
        initTransaction(TEST_AMOUNT, 8200000, P2pProcessorType.HUMO.name(), P2pProcessorType.UZCARD.name());

        BasicResult reconcile = uzCardReconcileService.reconcile(Instant.ofEpochSecond(FROM_TIME), Instant.ofEpochSecond(TO_TIME));

        assertThat(reconcile).isNotNull();
        assertThat(reconcile.isSuccess()).isFalse();

        List<ReconcileSummary> all = reconcileSummaryRepository.findAll();
        assertThat(all).isNotNull();
        assertThat(all.size()).isGreaterThan(0);
        assertThat(all.size()).isEqualTo(2);

        Optional<ReconcileSummary> creditReconcileOptional = all.stream().filter(reco -> Objects.equals(reco.getGatewayName(), P2pProcessorType.UZCARD.name() + P2pRecoHelperServiceImpl.P_2_P_CREDIT_CONFIRM)).findAny();
        assertThat(creditReconcileOptional.isPresent()).isTrue();

        ReconcileSummary creditReconcile = creditReconcileOptional.get();
        assertThat(creditReconcile.getStatus()).isEqualTo(ReconcileSummaryStatus.COMPARED_FALSE);
        assertThat(creditReconcile.getSuccess()).isFalse();
        assertThat(creditReconcile.getGatewayName()).isNotEmpty();
        assertThat(creditReconcile.getGatewayName()).isEqualTo(P2pProcessorType.UZCARD.name() + P2pRecoHelperServiceImpl.P_2_P_CREDIT_CONFIRM);
        assertThat(creditReconcile.getSystemAmount()).isNotNull();
        assertThat(creditReconcile.getSystemAmount()).isEqualTo(creditReconcile.getGatewayAmount());
        assertThat(creditReconcile.getSystemCount()).isNotNull();
        assertThat(creditReconcile.getSystemCount()).isNotEqualTo(creditReconcile.getGatewayCount());

        Optional<ReconcileSummary> debitReconcileOptional = all.stream().filter(reco -> Objects.equals(reco.getGatewayName(), P2pProcessorType.UZCARD.name() + P2pRecoHelperServiceImpl.P_2_P_DEBIT_CONFIRM)).findAny();
        assertThat(debitReconcileOptional.isPresent()).isTrue();
        ReconcileSummary debitReconcile = debitReconcileOptional.get();
        assertThat(debitReconcile.getStatus()).isEqualTo(ReconcileSummaryStatus.COMPARED_OK);
        assertThat(debitReconcile.getSuccess()).isTrue();
        assertThat(debitReconcile.getGatewayName()).isNotEmpty();
        assertThat(debitReconcile.getSystemAmount()).isNotNull();
        assertThat(debitReconcile.getSystemCount()).isNotNull();
        assertThat(debitReconcile.getSystemCount()).isEqualTo(debitReconcile.getGatewayCount());
        assertThat(debitReconcile.getSystemAmount()).isEqualTo(debitReconcile.getGatewayAmount());
    }

    @Test
    @Transactional
    @WithMockUser(TEST_USERNAME)
    public void testUzCardReconcileForEmptyResult() {
        initTransaction(100000000, TEST_AMOUNT, P2pProcessorType.UZCARD.name(), P2pProcessorType.HUMO.name(), Instant.ofEpochSecond(EMPTY_FROM_TIME).plusMillis(11), Instant.ofEpochSecond(FROM_TIME).plusMillis(12));
        initTransaction(14500000, TEST_AMOUNT2, P2pProcessorType.UZCARD.name(), P2pProcessorType.HUMO.name(), Instant.ofEpochSecond(EMPTY_FROM_TIME).plusMillis(11), Instant.ofEpochSecond(FROM_TIME).plusMillis(12));
        initTransaction(500000, TEST_AMOUNT2, P2pProcessorType.UZCARD.name(), P2pProcessorType.HUMO.name(), Instant.ofEpochSecond(EMPTY_FROM_TIME).plusMillis(11), Instant.ofEpochSecond(FROM_TIME).plusMillis(12));
        initTransaction(4500000, TEST_AMOUNT2, P2pProcessorType.UZCARD.name(), P2pProcessorType.HUMO.name(), Instant.ofEpochSecond(EMPTY_FROM_TIME).plusMillis(11), Instant.ofEpochSecond(FROM_TIME).plusMillis(12));
        initTransaction(8200000, TEST_AMOUNT2, P2pProcessorType.UZCARD.name(), P2pProcessorType.HUMO.name(), Instant.ofEpochSecond(EMPTY_FROM_TIME).plusMillis(11), Instant.ofEpochSecond(FROM_TIME).plusMillis(12));
        initTransaction(105000000, TEST_AMOUNT2, P2pProcessorType.UZCARD.name(), P2pProcessorType.HUMO.name(), Instant.ofEpochSecond(EMPTY_FROM_TIME).plusMillis(11), Instant.ofEpochSecond(FROM_TIME).plusMillis(12));

        initTransaction(TEST_AMOUNT, 100000000, P2pProcessorType.HUMO.name(), P2pProcessorType.UZCARD.name(), Instant.ofEpochSecond(EMPTY_FROM_TIME).plusMillis(11), Instant.ofEpochSecond(FROM_TIME).plusMillis(12));
        initTransaction(TEST_AMOUNT, 5000000, P2pProcessorType.HUMO.name(), P2pProcessorType.UZCARD.name(), Instant.ofEpochSecond(EMPTY_FROM_TIME).plusMillis(11), Instant.ofEpochSecond(FROM_TIME).plusMillis(12));
        initTransaction(TEST_AMOUNT, 1077000, P2pProcessorType.HUMO.name(), P2pProcessorType.UZCARD.name(), Instant.ofEpochSecond(EMPTY_FROM_TIME).plusMillis(11), Instant.ofEpochSecond(FROM_TIME).plusMillis(12));
        initTransaction(TEST_AMOUNT, 8200000, P2pProcessorType.HUMO.name(), P2pProcessorType.UZCARD.name(), Instant.ofEpochSecond(EMPTY_FROM_TIME).plusMillis(11), Instant.ofEpochSecond(FROM_TIME).plusMillis(12));

        BasicResult reconcile = uzCardReconcileService.reconcile(Instant.ofEpochSecond(EMPTY_FROM_TIME), Instant.ofEpochSecond(EMPTY_TO_TIME));

        assertThat(reconcile).isNotNull();
        assertThat(reconcile.isSuccess()).isFalse();

        List<ReconcileSummary> all = reconcileSummaryRepository.findAll();
        assertThat(all).isNotNull();
        assertThat(all.size()).isGreaterThan(0);
        assertThat(all.size()).isEqualTo(2);

        Optional<ReconcileSummary> creditReconcileOptional = all.stream().filter(reco -> Objects.equals(reco.getGatewayName(), P2pProcessorType.UZCARD.name() + P2pRecoHelperServiceImpl.P_2_P_CREDIT_CONFIRM)).findAny();
        assertThat(creditReconcileOptional.isPresent()).isTrue();

        ReconcileSummary creditReconcile = creditReconcileOptional.get();
        assertThat(creditReconcile.getStatus()).isEqualTo(ReconcileSummaryStatus.COMPARED_FALSE);
        assertThat(creditReconcile.getSuccess()).isFalse();
        assertThat(creditReconcile.getGatewayName()).isNotEmpty();
        assertThat(creditReconcile.getGatewayName()).isEqualTo(P2pProcessorType.UZCARD.name() + P2pRecoHelperServiceImpl.P_2_P_CREDIT_CONFIRM);
        assertThat(creditReconcile.getSystemAmount()).isNotNull();
        assertThat(creditReconcile.getSystemAmount()).isGreaterThan(creditReconcile.getGatewayAmount());
        assertThat(creditReconcile.getSystemCount()).isNotNull();
        assertThat(creditReconcile.getSystemCount()).isGreaterThan(creditReconcile.getGatewayCount());
        assertThat(creditReconcile.getGatewayAmount()).isEqualTo(0);
        assertThat(creditReconcile.getGatewayCount()).isEqualTo(0);

        Optional<ReconcileSummary> debitReconcileOptional = all.stream().filter(reco -> Objects.equals(reco.getGatewayName(), P2pProcessorType.UZCARD.name() + P2pRecoHelperServiceImpl.P_2_P_DEBIT_CONFIRM)).findAny();
        assertThat(debitReconcileOptional.isPresent()).isTrue();
        ReconcileSummary debitReconcile = debitReconcileOptional.get();
        assertThat(debitReconcile.getStatus()).isEqualTo(ReconcileSummaryStatus.COMPARED_FALSE);
        assertThat(debitReconcile.getSuccess()).isFalse();
        assertThat(debitReconcile.getGatewayName()).isNotEmpty();
        assertThat(debitReconcile.getGatewayName()).isEqualTo(P2pProcessorType.UZCARD.name() + P2pRecoHelperServiceImpl.P_2_P_DEBIT_CONFIRM);
        assertThat(debitReconcile.getSystemAmount()).isNotNull();
        assertThat(debitReconcile.getSystemAmount()).isGreaterThan(debitReconcile.getGatewayAmount());
        assertThat(debitReconcile.getSystemCount()).isNotNull();
        assertThat(debitReconcile.getSystemCount()).isGreaterThan(debitReconcile.getGatewayCount());
        assertThat(debitReconcile.getGatewayAmount()).isEqualTo(0);
        assertThat(debitReconcile.getGatewayCount()).isEqualTo(0);
    }

    private void initTransaction(long senderAmount, long recipientAmount, String senderGatewayName, String recipientGatewayName) {
        initTransaction(senderAmount, recipientAmount, senderGatewayName, recipientGatewayName, Instant.ofEpochSecond(FROM_TIME).plusSeconds(11), Instant.ofEpochSecond(FROM_TIME).plusSeconds(12));
    }

    private void initTransaction(long senderAmount, long recipientAmount, String senderGatewayName, String recipientGatewayName, Instant fromDate, Instant toDate) {
        Transaction transaction = TransactionResourceIntTest.createEntity(null);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.requestGroup(Constants.WMAGENT_LOGIN);
        transaction.requestLogin(Constants.WMAGENT_LOGIN);
        transaction.setDate(Instant.ofEpochSecond(FROM_TIME).plusSeconds(10));
        transaction.setGatewayAmount((double) recipientAmount);
        transaction.setPayType(PayType.P2P);
        transaction.setRequestAmount(senderAmount);

        transaction.setSenderGateway(senderGatewayName);
        transaction.setSenderSuccess(true);
        transaction.setSenderAmount(senderAmount);
        transaction.setSenderDate(fromDate);

        transaction.setRecipientGateway(recipientGatewayName);
        transaction.setRecipientSuccess(true);
        transaction.setRecipientAmount(recipientAmount);
        transaction.setRecipientDate(toDate);

        transactionRepository.save(transaction);
    }

}
