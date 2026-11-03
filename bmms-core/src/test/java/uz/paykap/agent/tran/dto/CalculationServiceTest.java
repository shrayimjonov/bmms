package uz.paykap.agent.tran.dto;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import uz.javlon.commons.result.HasResult;
import uz.paykap.IntegrationTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bmcore.dto.GatewayQueryResult;
import uz.javlon.commons.base.ResultData;
import uz.javlon.commons.exception.CodedException;
import uz.paykap.PkbillmsApp;
import uz.paykap.utils.ExtIdGeneratorInterface;
import uz.paykap.utils.UzcardExtIdGenerator;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(SpringRunner.class)
@IntegrationTest
public class CalculationServiceTest {

    @Autowired
    private CalculationService calculationService;
//    @Autowired
//    private CachedCbuService cbuService;

//    @Test
//    @Transactional
//    public void calcCcyDiff() {
//        double diff = calculationService.ccyDiff(10000L, 860, 860);
//        assertThat(diff).isEqualTo(1d);
//
//        Integer usdCurrencyRate = cbuService.getCurrencyRate(840);
//
//        diff = calculationService.ccyDiff(10000L, 860, 840);
//        assertThat(diff).isEqualTo(100d / usdCurrencyRate);
//
//        diff = calculationService.ccyDiff(10000L, 840, 860);
//        assertThat(diff).isEqualTo(1d * usdCurrencyRate / 100d);
//    }


    @Test
    @Transactional
    public void testValidateAmountOrThrowOk() {

        // Is 1 US Dollar enough for recipient with limit from 1000 Sums to 1000'000 Sums?

        GatewayQueryResult gatewayQueryResult = new GatewayQueryResult().withGatewayActive(true).withRecipientActive(true).withGatewayBean("test");

        gatewayQueryResult.setRecipientCcy(860);
        gatewayQueryResult.setRecipientMinAmount(100000L);
        gatewayQueryResult.setRecipientMaxAmount(100000000L);
        gatewayQueryResult.setRecipientLimitRatio(100);

        HasResult resultData = calculationService.validateAmount(100L, 840, gatewayQueryResult);

        assertThat(resultData).isNotNull();
        assertThat(resultData.getMessage()).isNotNull();
        assertThat(resultData.isSuccess()).isTrue();
//        assertThat(resultData.getException()).isNull();
    }

    @Test
    @Transactional
    public void testValidateAmountShouldThrowMinLimitException() {

        // Is 1 US Dollar enough for recipient with limit from 10'000 Sums to 1000'000 Sums?

        GatewayQueryResult gatewayQueryResult = new GatewayQueryResult().withGatewayActive(true).withRecipientActive(true).withGatewayBean("test");

        gatewayQueryResult.setRecipientCcy(860);
        gatewayQueryResult.setRecipientMinAmount(1000000L);
        gatewayQueryResult.setRecipientMaxAmount(100000000L);
        gatewayQueryResult.setRecipientLimitRatio(100);

        HasResult resultData = calculationService.validateAmount(100L, 840, gatewayQueryResult);

        assertThat(resultData).isNotNull();
        assertThat(resultData.getMessage()).isNotNull();
        assertThat(resultData.isSuccess()).isFalse();
//        assertThat(resultData.getException()).isNotNull();
//        assertThat(resultData.getException()).isInstanceOf(CodedException.class);
        assertThat(resultData.getMessage()).contains("MIN_LIMIT");
    }

    @Test
    @Transactional
    public void testValidateAmountShouldThrowMaxLimitException() {

        // Are 100 US Dollars enough for recipient with limit from 1000 Sums to 100'000 Sums?

        GatewayQueryResult gatewayQueryResult = new GatewayQueryResult().withGatewayActive(true).withRecipientActive(true).withGatewayBean("test");

        gatewayQueryResult.setRecipientCcy(860);
        gatewayQueryResult.setRecipientMinAmount(100000L);
        gatewayQueryResult.setRecipientMaxAmount(10000000L);
        gatewayQueryResult.setRecipientLimitRatio(100);

        HasResult resultData = calculationService.validateAmount(10000L, 840, gatewayQueryResult);

        assertThat(resultData).isNotNull();
        assertThat(resultData.getMessage()).isNotNull();
        assertThat(resultData.isSuccess()).isFalse();
//        assertThat(resultData.getException()).isNotNull();
//        assertThat(resultData.getException()).isInstanceOf(CodedException.class);
        assertThat(resultData.getMessage()).contains("MAX_LIMIT");
    }

//    @Test
//    @Transactional
//    public void testRequestAmountInUZSTiyin() {
//
//        Integer usdCurrencyRate = cbuService.getCurrencyRate(840);
//
//        double requestAmountInUZSTiyin = calculationService.getRequestAmountInUZSTiyin(1L, 840);
//        assertThat(requestAmountInUZSTiyin).isEqualTo(1d * usdCurrencyRate);
//    }

    @Test
    public void testExtIdGeneratorToSuccess() {
        String[] extIdArray = {"12345", "12345678910111", "1", "00233"};
        String prefix = "5";
        for (String extId : extIdArray) {
            ExtIdGeneratorInterface generator = new UzcardExtIdGenerator();
            String result = generator.generate(prefix, extId);

            assertThat(result).isNotNull();
            assertThat(result).isNotEmpty();
            System.out.println(result);
            assertThat(result.length()).isEqualTo(15);
            assertThat(result.substring(0, 1)).isEqualTo(prefix);
            assertThat(result.substring(result.length() - extId.length())).isEqualTo(extId);
        }
    }

    @Test(expected = CodedException.class)
    public void testExtIdGeneratorToException() {
        String extId = "1234567891011112345";
        String prefix = "5";
        ExtIdGeneratorInterface generator = new UzcardExtIdGenerator();
        String result = generator.generate(prefix, extId);
    }

}
