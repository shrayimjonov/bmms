package uz.paykap.gateway.common.impl;

import org.apache.commons.codec.binary.Base64;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import uz.paykap.IntegrationTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import uz.devops.bmcore.enumeration.RequestType;
import uz.paykap.PkbillmsApp;
import uz.paykap.IntegrationTest;
import uz.paykap.domain.enumeration.MethodType;
import uz.paykap.gateway.common.CommonSender;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@RunWith(SpringRunner.class)
@IntegrationTest
public class CommonSenderImplTest {

    @Autowired
    private CommonSender commonSender;

    @Test
    @Ignore("Just for dev")
    public void sendAndReceive() {

        String body1 = "{\n" +
            "\t\"id_client\": \"99111740\",\n" +
            "\t\"filial\": \"00000\",\n" +
            "\t\"dep\":  \"ON1\",\n" +
            "\t\"sum\": 50000,\n" +
            "\t\"acc_to\": \"22618000399111740500\",\n" +
            "\t\"id_mobile\": -5106\n" +
            "}";

        String body2 = "{\"id_client\":\"99111740\",\"filial\":\"00000\",\"dep\":\"ON1\",\"sum\":50000,\"acc_to\":\"22618000399111740500\",\"id_mobile\":-5106}";

        HttpHeaders headers = createHeaders("123", "123");
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setHost(InetSocketAddress.createUnresolved("ptsv2.com", 80));
        ResponseEntity<String> test = commonSender.sendAndReceive(RequestType.CHECK, "test", MethodType.POST, "http://ptsv2.com/t/oax7h-1624264357/post",
            body1.replaceAll("\\s+", ""), headers, Collections.emptyMap());

        System.out.println(test);
    }

    // TODO
//    @Test
    public void testJsonTrim() {

        String regexp = "/(\"(?:\\\\\"|[^\"])*\")|\\s/g";

        String input1 = "{\n" +
            "\t\"id_client\": \"99111740\",\n" +
            "\t\"filial\": \"00000\",\n" +
            "\t\"dep\":  \"ON1\",\n" +
            "\t\"sum\": 50000,\n" +
            "\t\"acc_to\": \"22618000399111740500\",\n" +
            "\t\"id_mobile\": -5106\n" +
            "}";

        String output1 = input1.replace(regexp, "$1");

        Assertions.assertThat(output1).isEqualTo("{\"id_client\":\"99111740\",\"filial\":\"00000\",\"dep\":\"ON1\",\"sum\":50000,\"acc_to\":\"22618000399111740500\",\"id_mobile\":-5106}");

    }

    private HttpHeaders createHeaders(String username, String password) {
        return new HttpHeaders() {{
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.encodeBase64(
                auth.getBytes(StandardCharsets.US_ASCII));
            String authHeader = "Basic " + new String(encodedAuth);
            set("Authorization", authHeader);
        }};
    }
}
