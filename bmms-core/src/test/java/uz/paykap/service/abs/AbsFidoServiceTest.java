package uz.paykap.service.abs;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uz.paykap.PkbillmsApp;
import uz.paykap.config.TestSecurityConfiguration;
import uz.paykap.domain.enumeration.Currency;
import uz.paykap.service.abs.request.AbsCoursesRequest;
import uz.paykap.service.abs.request.AbsForexTransactionRequest;
import uz.paykap.service.abs.request.AbsTransactionRequest;

import static org.assertj.core.api.Assertions.assertThat;


@Disabled
@Ignore
@Transactional
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PkbillmsApp.class, TestSecurityConfiguration.class})
public class AbsFidoServiceTest {

    @Autowired
    private AbstractAbsService abstractAbsService;

    @Test
    @Transactional
    public void doTransaction() {
        var tran = new AbsTransactionRequest();
        tran.setAccCl("17403000300000491643");
        tran.setNameCl("Tranzit 22616");
        tran.setAccCo("22616000660702149001");
        tran.setNameCo("Kasimov F");
        tran.setPurpose("пополнение тест");
        tran.setDocNum("99898125");
        tran.setPurposeId("00634");
        tran.setSumma(2000L);

        var result = abstractAbsService.doTransaction(tran);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getTransId()).isNotBlank();

    }

    @Test
    @Transactional
    public void doForexTransaction() {
        var tran = new AbsForexTransactionRequest();
        tran.setSenderPan("8600300000000000");
        tran.setRecipientPan("5179860000000000");
        tran.setClientId("6557883");
        tran.setPurpose("Test");

        var rateResponse = abstractAbsService.getCourses(new AbsCoursesRequest("ALL"));
        assertThat(rateResponse.isSuccess()).isTrue();
        assertThat(rateResponse.getData()).isNotNull();
        var rateList = rateResponse.getData();

        assertThat(rateList.size() > 0).isTrue();
        var usdRate = rateList.stream().filter(it -> it.getCurCode().equals(Currency.USD.getCode())).findFirst().orElse(rateList.get(0));

        assertThat(usdRate).isNotNull();
        assertThat(usdRate.getRateBuy()).isNotBlank();
        assertThat(usdRate.getRateSell()).isNotBlank();

        tran.setExchangeRate(Double.valueOf(usdRate.getRateBuy()));
        tran.setAmountCl(2000L);
        var result = abstractAbsService.doForexTransaction(tran);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getTransId()).isNotBlank();
    }

    @Test
    @Transactional
    public void getCourses() {
        var result = abstractAbsService.getCourses(new AbsCoursesRequest("ALL"));
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isNotNull();
        var rateList = result.getData();

        assertThat(rateList.size() > 0).isTrue();
        var usdRate = rateList.stream().filter(it -> it.getCurCode().equals(Currency.USD.getCode())).findFirst().orElse(rateList.get(0));

        assertThat(usdRate).isNotNull();
        assertThat(usdRate.getRateBuy()).isNotBlank();
        assertThat(usdRate.getRateSell()).isNotBlank();
    }
}
