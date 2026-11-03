//package uz.paykap.service;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.oauth2.client.OAuth2RestTemplate;
//import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.transaction.annotation.Transactional;
//import uz.devops.bm.upay.adapter.BmUpayDataAdapterV3;
//import uz.devops.bm.upay.gateway.BmUpayGatewayServiceV3;
//import uz.devops.bmcore.dto.request.CheckRequest;
//import uz.devops.bmcore.dto.request.CommonCheckRequest;
//import uz.devops.bmcore.dto.request.CommonPayRequest;
//import uz.devops.bmcore.dto.request.PayRequest;
//import uz.devops.bmcore.dto.response.SimpleBillResponse;
//import uz.devops.bmcore.enumeration.Lang;
//import uz.javlon.commons.exception.CodedException;
//import uz.javlon.commons.exception.ProblemEnum;
//import uz.paykap.IntegrationTest;
//import uz.paykap.adapter.DataAdapterServiceV3;
//import uz.paykap.config.Constants;
//import uz.paykap.domain.I18n;
//import uz.paykap.domain.Recipient;
//import uz.paykap.repository.RecipientRepository;
//
//import java.util.*;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.Assert.assertEquals;
//
//@RunWith(SpringRunner.class)
//@IntegrationTest
//public class UpayGatewayServiceTest {
//
//    public static final String TEST_ACCOUNT = "1775";
//    public static final String TEST_PHONE = "901299592";
//    public static final String TEST_PASSPORT = "1234567";
//    public static final String TEST_AMOUNT = "10000";
//    public static final String TEST = "test";
//    public static final String UPAY_UZ_ONLINE = "Upay-UzOnline";
//    public static final String ACCOUNT = "612345";
//
////    @Autowired
////    private BmUpayDataAdapterV3 upayDataAdapter;
//
//    @Autowired
//    private BmUpayGatewayServiceV3 upayGatewayService;
//    @Autowired
//    private DataAdapterServiceV3 dataAdapterService;
//
//    @Autowired
//    private RecipientRepository recipientRepository;
//
//
//    @Test
//    @WithMockUser("admin")
//    @Transactional
//    public void check() {
//        dataAdapterService.sync(BmUpayDataAdapterV3.NAME);
////        upayDataAdapter.returnData();
////        upayDataAdapter.importData();
//
//        Recipient recipient = recipientRepository.findTop1ByShortName(UPAY_UZ_ONLINE).orElseThrow(() -> new CodedException(ProblemEnum.OBJECT_NOT_FOUND));
//
//        Map<String, Object> params = new HashMap<>();
//        params.put("region", "10");
//
//
//        CommonCheckRequest simpleCheckDTO = new CheckRequest();
//        simpleCheckDTO.setParams(params);
//        simpleCheckDTO.setRecipientId(recipient.getId());
//        simpleCheckDTO.setAccount(ACCOUNT);
//
//        SimpleBillResponse simpleBillDTO = upayGatewayService.check(simpleCheckDTO);
//        assertThat(Objects.equals(simpleBillDTO.getRecipientShortName(), UPAY_UZ_ONLINE));
//        assertThat(Objects.equals(simpleBillDTO.getAccount(), ACCOUNT));
//        CommonPayRequest payDTO = new PayRequest();
//        payDTO.setBillId(simpleBillDTO.getId());
//        payDTO.setAmount(100L);
//        payDTO.setGatewayAmount(1000.0);
//        payDTO.setParams(params);
//        upayGatewayService.pay(payDTO);
//
//        String a = "";
//
//    }
//
////    @Test
////    @Transactional
////    public void pay() throws Exception {
////
////        CommonCheckRequest simpleCheckDTO = new CheckRequest();
////
////        Map<String, String> params = new HashMap<>();
////        params.put(BankCreditDataAdapter.ACCOUNT, TEST_ACCOUNT);
////        params.put(BankCreditDataAdapter.PHONE, TEST_PHONE);
////        params.put(BankCreditDataAdapter.PASSPORT, TEST_PASSPORT);
////        params.put(BankCreditDataAdapter.AMOUNT_FIELD_NAME, TEST_AMOUNT);
////
////        simpleCheckDTO.setParams(params);
////
////        Recipient recipient = saveRecipient();
////
////        simpleCheckDTO.setRecipientId(recipient.getId());
////
////        SimpleBillResponse simpleBillDTO = bankCreditGatewayService.check(simpleCheckDTO);
////
////        CommonPayRequest payDTO = new PayRequest();
////        payDTO.setBillId(simpleBillDTO.getId());
////
////        Map<String, String> stringStringMap = new HashMap<>();
////        stringStringMap.put(BankCreditDataAdapter.AMOUNT_FIELD_NAME, "1");
////
////        payDTO.setParams(stringStringMap);
////        SimpleBillResponse simpleBillDTO1 = bankCreditGatewayService.pay(payDTO);
////        assertThat(simpleBillDTO1 != null);
////        if (simpleBillDTO1 != null) {
////            assertThat(Objects.equals(simpleBillDTO1.getId(), simpleBillDTO.getId()));
////        }
////
////    }
//
//    @Test
//    public void status() {
//    }
//
//    @Test
//    public void cancel() {
//    }
//
//    @Test
//    public void deposit() {
//    }
//
//    //    @Test
//    @Transactional
//    public void testConnectDirectlyToResourceServer() throws Exception {
//        ResourceOwnerPasswordResourceDetails resource = new ResourceOwnerPasswordResourceDetails();
//        resource.setAccessTokenUri("http://127.0.0.1:8080/pkuaa/oauth/token");
//        resource.setId("kapital");
//        resource.setClientId("internal");
//        resource.setClientSecret("internal");
//        resource.setTokenName("accessToken");
//        resource.setGrantType("password");
//
//        resource.setScope(Arrays.asList("openid", "read", "write"));
//
//        resource.setUsername("kapital-agent");
//        resource.setPassword("123456");
//
////        LoadBalancedResourceDetails loadBalancedResourceDetails = new LoadBalancedResourceDetails();
//////        JHipsterProperties.Security.ClientAuthorization clientAuthorization = this.jHipsterProperties.getSecurity().getClientAuthorization();
////        loadBalancedResourceDetails.setAccessTokenUri("http://127.0.0.1:8080/pkuaa/oauth/token");
////        loadBalancedResourceDetails.setTokenServiceId("pkuaa");
////        loadBalancedResourceDetails.setClientId("kapital-agent");
////        loadBalancedResourceDetails.setClientSecret("123456");
//
//
//        OAuth2RestTemplate template = new OAuth2RestTemplate(resource);
////        logger.info(" CALLING: " + baseUrl+"/api");
//
//        String result = template.getForObject("http://127.0.0.1:8080/pkbillms/" + "api", String.class);
//
//        System.err.println(result);
//
//        assertEquals("Hello, Trusted User marissa", result);
//    }
//
//    private Recipient saveRecipient() {
//
//        Recipient recipient = new Recipient()
//                .serviceId("01032")
//                .active(true)
//                .thumbnail("1.png")
//                .logo("1.png")
//                .amountRatio(12)
//                .limitRatio(12)
//                .shortName(TEST)
//                .ccy(Constants.UZS_CURRENCY)
//                .commission(1D)
//                .maxAmount(100000L)
//                .minAmount(100L)
//                .ord(1)
//                .titles(getI18ns("EN", "RU", "uz")
//                );
//
//        return recipientRepository.save(recipient);
//    }
//
//    private Set<I18n> getI18ns(String en, String ru, String uz) {
//        HashSet<I18n> i18ns = new HashSet<>(3);
//        i18ns.add(new I18n().lang(Lang.EN).text(en));
//        i18ns.add(new I18n().lang(Lang.RU).text(ru));
//        i18ns.add(new I18n().lang(Lang.UZ).text(uz));
//        return i18ns;
//    }
//}
