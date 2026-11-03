package uz.paykap.adapter.verification;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uz.paykap.IntegrationTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bm.paynet.config.PaynetConstants;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.paykap.PkbillmsApp;
import uz.paykap.adapter.DataAdapterServiceV3;
import uz.paykap.adapter.common.CommonDataAdapterV3;
import uz.paykap.config.Constants;
import uz.paykap.config.SecurityBeanOverrideConfiguration;
import uz.paykap.domain.ReconcileDiscrepancy;
import uz.paykap.domain.ReconcileSummary;
import uz.paykap.domain.Transaction;
import uz.paykap.repository.GatewayRepository;
import uz.paykap.repository.ReconcileDiscrepancyRepository;
import uz.paykap.repository.ReconcileSummaryRepository;
import uz.paykap.repository.TransactionRepository;
import uz.paykap.utils.DateUtil;
import uz.paykap.web.rest.TransactionResourceIntTest;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PkbillmsApp.class, SecurityBeanOverrideConfiguration.class}, properties = {"application.reconciliation.enableAuto=true"})
public class AutomaticReconcileServiceTest {

    public static final String MEHRLI_GATEWAY = "mehrli-gateway";
    @Autowired
    private AutomaticReconcileService automaticReconcileService;
    @Autowired
    private ReconcileSummaryRepository reconcileSummaryRepository;
    //    @Autowired
//    private PaynetDataAdapter paynetDataAdapter;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private DataAdapterServiceV3 adapterServiceV3;
    @Autowired
    private GatewayRepository gatewayRepository;

    @Autowired
    private ReconcileDiscrepancyRepository reconcileDiscrepancyRepository;

    @Test
    public void dummy() {

    }

    //TODO - Zafar fix
//    @Test
    @Transactional
    @WithMockUser(username = Constants.WMAGENT_LOGIN)
    public void startReconciliationWithData() {

        int databaseSizeBeforeCreate = reconcileSummaryRepository.findAll().size();
//        int databaseSizeBeforeCreateGateway = gatewayRepository.findAllByActive(true).size();

//        paynetDataAdapter.sync();
        adapterServiceV3.sync(CommonDataAdapterV3.NAME);

        int afterSyncSize = gatewayRepository.findAllByActive(true).size() - databaseSizeBeforeCreate; //TODO - fix
//        int afterSyncSize = 8;

        Transaction transaction = TransactionResourceIntTest.createEntity(null);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.requestGroup(Constants.WMAGENT_LOGIN);
        transaction.requestLogin(Constants.WMAGENT_LOGIN);
        transaction.setRecipientGateway(PaynetConstants.PAYNET_RPC_GATEWAY_NAME);
        transaction.setGatewayAmount(100d);
        transaction.setRecipientAmount(10000L);
        transaction.setRequestAmount(10000L);
        transaction.setDate(DateUtil.todayStart().minus(1, ChronoUnit.DAYS).plusSeconds(600));
        transaction.setRecipientDate(transaction.getDate());

        transactionRepository.save(transaction);

        Transaction transactionMehrli = TransactionResourceIntTest.createEntity(null);
        transactionMehrli.setId(144756419L);
        transactionMehrli.setStatus(TransactionStatus.SUCCESS);
        transactionMehrli.requestGroup(Constants.WMAGENT_LOGIN);
        transactionMehrli.requestLogin(Constants.WMAGENT_LOGIN);
        transactionMehrli.setRecipientGateway(MEHRLI_GATEWAY);
        transactionMehrli.setGatewayAmount(60000d);
        transactionMehrli.setRecipientAmount(600000L);
        transactionMehrli.setRequestAmount(600000L);
        transactionMehrli.setDate(DateUtil.todayStart().minus(1, ChronoUnit.DAYS).plusSeconds(600));
        transactionMehrli.setRecipientDate(transaction.getDate());
        transactionMehrli.setRecipientName("mehrli");

        Transaction transaction1 = transactionRepository.save(transactionMehrli);

        automaticReconcileService.startReconciliation();

        List<ReconcileSummary> reconcileSummaryList = reconcileSummaryRepository.findAll();
        assertThat(reconcileSummaryList).hasSize(databaseSizeBeforeCreate + afterSyncSize);


        //TODO - fix
//        ReconcileSummary testReconcileSummary = reconcileSummaryList.get(reconcileSummaryList.size() - afterSyncSize);
        ReconcileSummary testReconcileSummary = reconcileSummaryRepository.findTop1ByGatewayName(MEHRLI_GATEWAY).orElse(null);

        String a = "";
        assertThat(testReconcileSummary.getGatewayId()).isNotNull();
        assertThat(testReconcileSummary.getSystemAmount()).isEqualTo(transaction.getRecipientAmount());
//        assertThat(testReconcileSummary.getGatewayAmount()).isEqualTo(325212600L); //325212600 - from Paynet simulation file
//        assertThat(testReconcileSummary.getSystemCount()).isEqualTo(1);
//        assertThat(testReconcileSummary.getGatewayCount()).isEqualTo(1703); //1703 - from Paynet simulation file
//        assertThat(testReconcileSummary.getDate()).isEqualTo(DateUtil.toLocalDate(transaction.getDate()));
//        assertThat(testReconcileSummary.isSuccess()).isEqualTo(false);
//        assertThat(testReconcileSummary.getPeriod()).isEqualTo(ReconcilePeriod.DAY);
//        assertThat(testReconcileSummary.getGatewayName()).isEqualTo(PaynetDataAdapter.PAYNET_GATEWAY_NAME);

        ReconcileDiscrepancy reconcileDiscrepancy = reconcileDiscrepancyRepository.findTop1BySystemTransactionId(transaction1.getId()).orElse(null);
        assertThat(Objects.equals(reconcileDiscrepancy.getSystemAmount(), transactionMehrli.getRecipientAmount()));
        assertThat(Objects.equals(reconcileDiscrepancy.getReconcileSummary().getId(), testReconcileSummary.getId()));

    }
}
