//package uz.paykap.util;
//
//import org.assertj.core.api.Assertions;
//import org.json.JSONObject;
//import org.junit.runner.RunWith;
//import uz.paykap.IntegrationTest;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.client.SimpleClientHttpRequestFactory;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.client.HttpServerErrorException;
//import org.springframework.web.client.ResourceAccessException;
//import org.springframework.web.client.RestClientException;
//import org.springframework.web.client.RestTemplate;
//import uz.paykap.PkbillmsApp;
//
//@RunWith(SpringRunner.class)
//@IntegrationTest
//public class RestTimeoutTest {
//
//    /**
//     * This test example for "how to
//     * set timeout for RestTemplate" and
//     * "handle timeout exception" and
//     * "parse JSON string".
//     *
//     * In this test used billMS token
//     * authentication method.
//     */
////    @Test
//    @Transactional
//    public void testConnectionFailed() {
//        String token = getToken();
//
//        SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
//        //Connect timeout
//        clientHttpRequestFactory.setConnectTimeout(3000);//hostni qidirib topib- bog'languncha ketadigan vaqt
//        //Read timeout
//        clientHttpRequestFactory.setReadTimeout(3000);//hostga bog'lanib bo'lgach, serverdan javob kelishini kutadigan vaqt
//
//        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
////        RestTemplate restTemplate = new RestTemplate();
//
//        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
//        map.set("Authorization", "Bearer " + token);
//        HttpEntity httpEntity = new HttpEntity(null, map);
//        try {
//            String responseEntity = restTemplate.postForObject("https://127.0.0.1/pkbillms/api/agent/echo", httpEntity, String.class);
//            System.out.println(responseEntity);
//            Assertions.assertThat(responseEntity).isEqualTo("true");
//        }catch (RestClientException e){
//
//            if (e instanceof HttpServerErrorException){//serverni softi o'chib qolsa
//                System.out.println("serverni softi o'chib qoldi");
//            }
//            if (e instanceof ResourceAccessException){//clientni seti o'chib qolsa
//                System.out.println("clientni seti o'chib qoldi");
//            }
//            e.printStackTrace();
//        }
//    }
//
//    private String getToken() {
//        RestTemplate restTemplate = new RestTemplate();
//        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
//        String body = "{\"username\": \"username\",\"password\": \"password\"}";
//        HttpEntity httpEntity = new HttpEntity(body, httpHeaders);
//        String response = restTemplate.postForObject("https://internal.example.com/auth/login", httpEntity, String.class);
//        System.out.println(response);
//        JSONObject jsonObject = new JSONObject(response);
//
//        return jsonObject.getString("access_token");
//    }
//}
