package uz.paykap.utils;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class CompanyUtilTest {

    @Test
    public void getShortName() {

        String companyName1 = "\"TEST-AVTO\" MAS'ULIYATI CHEKLANGAN JAMIYATI";
        String companyName2 = "DEMO ACCOUNT";
        String companyName3 = "\"AMMOFOS-MAXAM\" AKSIYADORLIK JAMIYATI";
        String companyName4 = "АКЦИОНЕРНОЕ ОБЩЕСТВО \"QIZILQUMSEMENT\"";
        String companyName5 = "ИНОС ПРЕ ОБЩЕСТВО С ОГРАНИЧЕННОЙ ОТВЕТСТВЕННОСТЬЮ \"ASKLEPIY\"";
        String companyName6 = "ОБЩЕСТВО С ОГРАНИЧЕННОЙ ОТВЕТСТВЕННОСТЬЮ \"NIKA FARM SERVIS\"";
        String companyName7 = "ОБЩЕСТВО С ОГРАНИЧЕННОЙ ОТВЕТСТВЕННОСТЬЮ \"INTOUCH SOLUTIONS\"";
        String companyName8 = "ОБЩЕСТВО С ОГРАНИЧЕННОЙ ОТВЕТСТВЕННОСТЬЮ \"GOFRAMASTER\"";
        String companyName9 = "ОБЩЕСТВО С ОГРАНИЧЕННОЙ ОТВЕТСТВЕННОСТЬЮ \"FLORENCE HEALTHCARE\"";
        String companyName10 = "ОБЩЕСТВО С ОГРАНИЧЕННОЙ ОТВЕТСТВЕННОСТЬЮ \"GREENUP TRADING\"";

        String shortName1 = CompanyUtil.getShortName(companyName1);
        Assertions.assertThat(shortName1).isEqualTo("TEST-AVTO");

        Assertions.assertThat(CompanyUtil.getShortName(companyName2)).isEqualTo("DEMO ACCOUNT");
        Assertions.assertThat(CompanyUtil.getShortName(companyName3)).isEqualTo("AMMOFOS-MAXAM");
        Assertions.assertThat(CompanyUtil.getShortName(companyName4)).isEqualTo("QIZILQUMSEMENT");
        Assertions.assertThat(CompanyUtil.getShortName(companyName5)).isEqualTo("ASKLEPIY");
        Assertions.assertThat(CompanyUtil.getShortName(companyName6)).isEqualTo("NIKA FARM SERVIS");
        Assertions.assertThat(CompanyUtil.getShortName(companyName7)).isEqualTo("INTOUCH SOLUTIONS");
        Assertions.assertThat(CompanyUtil.getShortName(companyName8)).isEqualTo("GOFRAMASTER");
        Assertions.assertThat(CompanyUtil.getShortName(companyName9)).isEqualTo("FLORENCE HEALTHCARE");
        Assertions.assertThat(CompanyUtil.getShortName(companyName10)).isEqualTo("GREENUP TRADING");

    }
}
