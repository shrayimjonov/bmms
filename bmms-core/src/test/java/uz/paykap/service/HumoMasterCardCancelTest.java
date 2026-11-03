package uz.paykap.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import uz.paykap.IntegrationTest;
import org.springframework.test.context.junit4.SpringRunner;
import uz.paykap.service.humo.HumoP2pHelperService;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@IntegrationTest
public class HumoMasterCardCancelTest {

    @Autowired
    private HumoP2pHelperService humoHelperService;

    @Test
    public void centreIdXalqMasterCardTest(){
        String pan = "55553608****1111";
        String centreId =humoHelperService.getCentreId(pan);
        assertThat(centreId).isEqualTo("Xalqbank");
    }

    @Test
    public void centreIdAnorBankVirtualMasterCardTest(){
        String pan = "5555360000000000";
        String centreId =humoHelperService.getCentreId(pan);
        assertThat(centreId).isEqualTo("AnorBank");
    }

    @Test
    public void centreIdXalqHumoCardTest(){
        String pan = "986008******1111";
        String centreId =humoHelperService.getCentreId(pan);
        assertThat(centreId).isEqualTo("Xalqbank");
    }

    @Test
    public void centreIdAsiaAlliancMasterCardTest(){
        String pan = "55553609****1111";
        String centreId =humoHelperService.getCentreId(pan);
        assertThat(centreId).isEqualTo("AsiaAllianc");
    }

    @Test
    public void centreIdAsiaAlliancHumoCardTest(){
        String pan = "986009****1111";
        String centreId =humoHelperService.getCentreId(pan);
        assertThat(centreId).isEqualTo("AsiaAllianc");
    }
}
