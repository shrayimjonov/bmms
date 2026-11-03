package uz.paykap.service.abs;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uz.paykap.PkbillmsApp;
import uz.paykap.config.TestSecurityConfiguration;
import uz.paykap.service.abs.request.AbsForexTransactionRequest;
import uz.paykap.service.abs.request.AbsTransactionRequest;
import uz.paykap.service.abs.response.AbsTransactionResult;
import uz.paykap.service.dto.result.BmResultData;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PkbillmsApp.class, TestSecurityConfiguration.class}, properties = {"application.abs.serviceBeanName=absIsApiService"})
public class AbsIsApiServiceTest {

    public static final String MICROSERVICE_NAME = "PK_BILL_MS";

    @Autowired
    private AbstractAbsService absService;

    @Test
    public void testDoTransaction() {

        AbsTransactionRequest request = new AbsTransactionRequest()
                .withAccCl("12345123451234512345")
                .withBankCl("12345")
                .withNameCl("name")
                .withAccCo("12345123451234512345")
                .withBankCo("00491")
                .withNameCo("name")
                .withTransactionId("id")
                .withPurpose("purpose")
                .withDocNum("doc num")
                .withPurposeId("pid")
                .withSumma(123L)
                .withTransactionPurposeId("tpId")
                //
                ;

        BmResultData<AbsTransactionResult> resultData = absService.doTransaction(request);

        Assertions.assertThat(resultData).isNotNull();
        Assertions.assertThat(resultData.getData()).isNotNull();
        Assertions.assertThat(resultData.getData().getErrorCode()).isEqualTo("1");

        resultData = absService.doTransaction(request.withTransactionId("wrapper-null"));

        Assertions.assertThat(resultData).isNotNull();
        Assertions.assertThat(resultData.getData()).isNotNull();
        Assertions.assertThat(resultData.getData().getErrorCode()).isNotEqualTo("1");


        resultData = absService.doTransaction(request.withTransactionId("inner-null"));

        Assertions.assertThat(resultData).isNotNull();
        Assertions.assertThat(resultData.getData()).isNotNull();
        Assertions.assertThat(resultData.getData().getErrorCode()).isNotEqualTo("1");

        resultData = absService.doTransaction(request.withTransactionId("all-new"));

        Assertions.assertThat(resultData).isNotNull();
        Assertions.assertThat(resultData.getData()).isNotNull();
        Assertions.assertThat(resultData.getData().getErrorCode()).isNotEqualTo("1");

        resultData = absService.doTransaction(request.withTransactionId("error"));

        Assertions.assertThat(resultData).isNotNull();
        Assertions.assertThat(resultData.getData()).isNotNull();
        Assertions.assertThat(resultData.getData().getErrorCode()).isNotEqualTo("1");


    }

    @Test
    public void TestDoForex() {

        AbsForexTransactionRequest request = new AbsForexTransactionRequest();

        request.setRequestId(MICROSERVICE_NAME + System.currentTimeMillis());
        request.setMfo("00981");
        request.setAccCl("22618840099095484901");
        request.setAccCo("22618000099095484500");
        request.setAmountCo(2131L);
        request.setDeal(8);

        BmResultData<AbsTransactionResult> resultData = absService.doForexTransaction(request);

        Assertions.assertThat(resultData).isNotNull();
        Assertions.assertThat(resultData.getData()).isNotNull();
        Assertions.assertThat(resultData.getData().getErrorCode()).isEqualTo("1");
    }
}
