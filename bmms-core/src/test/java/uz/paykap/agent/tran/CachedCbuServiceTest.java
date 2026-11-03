package uz.paykap.agent.tran;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import uz.paykap.IntegrationTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uz.paykap.PkbillmsApp;
import uz.paykap.agent.tran.dto.RateService;
import uz.paykap.config.Constants;

import java.math.BigDecimal;

@RunWith(SpringRunner.class)
@IntegrationTest
public class CachedCbuServiceTest {

    @Autowired
    private RateService rateService;

    @Test
    @Transactional
    public void convert() {

        BigDecimal convert;

        convert = rateService.convert(Constants.EUR_CURRENCY, 1000L, Constants.USD_CURRENCY);
        System.out.println(convert);
        Assertions.assertThat(convert).isGreaterThanOrEqualTo(new BigDecimal("1207.75274475"));
        Assertions.assertThat(convert).isLessThan(new BigDecimal(1208));

        convert = rateService.convert(Constants.USD_CURRENCY, 1000L, Constants.UZS_CURRENCY);
        System.out.println(convert);
        Assertions.assertThat(convert).isEqualTo(new BigDecimal("7783050.00000000"));

        convert = rateService.convert(Constants.EUR_CURRENCY, 1000L, Constants.UZS_CURRENCY);
        System.out.println(convert);
        Assertions.assertThat(convert).isEqualTo(new BigDecimal("9400000.00000000"));

        convert = rateService.convert(Constants.UZS_CURRENCY, 100000L, Constants.RUR_CURRENCY);
        System.out.println(convert);
        Assertions.assertThat(convert).isGreaterThanOrEqualTo(new BigDecimal("713.67399372"));

        convert = rateService.convert(Constants.UZS_CURRENCY, 100000L, Constants.UZS_CURRENCY);
        System.out.println(convert);
        Assertions.assertThat(convert).isEqualTo(new BigDecimal("100000.00000000"));
    }
}

