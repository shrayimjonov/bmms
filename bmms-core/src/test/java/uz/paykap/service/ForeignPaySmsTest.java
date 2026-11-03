package uz.paykap.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uz.javlon.commons.base.Result;
import uz.javlon.commons.otp.SendStrategy;
import uz.javlon.commons.result.HasResult;
import uz.paykap.PkbillmsApp;
import uz.paykap.config.SecurityBeanOverrideConfiguration;
import uz.paykap.service.impl.ForeignPayServiceImpl;

import java.util.regex.Pattern;


@RunWith(SpringRunner.class)
//@Disabled("Mock error : Actually, there were zero interactions with this mock.")
@SpringBootTest(classes = {PkbillmsApp.class, SecurityBeanOverrideConfiguration.class}, properties = {
        "application.otp.otpTemplate=ANORBANK.uz Kod: ${otp} dlya spisaniye s karti ${pan} na ${amount} sum.",
        "application.otp.simulation=true",
        "application.sms.simulation=true",
})
public class ForeignPaySmsTest {

    @Autowired
    private ForeignPayServiceImpl foreignPayService;

    @MockBean
    private SendStrategy smsService;

    @Test
    public void smsTest() {

        Mockito.when(smsService.send(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(HasResult.SUCCESS);

        final HasResult result = foreignPayService.sendVerification("986060NTRFBH4585", 123L, "99890123123", 199912L);

        Mockito.verify(smsService, Mockito.times(1)).send(
                ArgumentMatchers.eq("99890123123"),
                ArgumentMatchers.matches(Pattern.compile("ANORBANK\\.uz Kod: 123321 dlya spisaniye s karti 986060\\*\\*\\*\\*\\*\\*4585 na 1 999,12 sum\\."))
        );

        System.out.println(result);
    }
}
