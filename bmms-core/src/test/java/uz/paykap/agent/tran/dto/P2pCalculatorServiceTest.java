//package uz.paykap.agent.tran.dto;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//import uz.paykap.PkbillmsApp;
//import uz.paykap.config.TestSecurityConfiguration;
//import uz.paykap.service.dto.p2p.P2pAccount;
//import uz.paykap.service.dto.p2p.P2pAccountType;
//import uz.paykap.service.dto.p2p.P2pPayRequest;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//
//@Slf4j
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = { PkbillmsApp.class, TestSecurityConfiguration.class })
//public class P2pCalculatorServiceTest {
//
//    @Autowired
//    private P2pCalculatorService p2pCalculatorService;
////    @Autowired
////    private P2pCommissionService5mlnBonusImpl service5mlnBonus;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Test
//    public void testGetRequestAmountWithoutBonusToSuccess() throws JsonProcessingException {
//        assertCommissionBonusAmount(-100, -100, 0);
//        assertCommissionBonusAmount(-100, 0, 0);
//        assertCommissionBonusAmount(-100, 100, 0);
//        assertCommissionBonusAmount(-100, 10000, 0);
//        assertCommissionBonusAmount(0, -100, 0);
//        assertCommissionBonusAmount(0, 0, 0);
//        assertCommissionBonusAmount(0, 100, 0);
//        assertCommissionBonusAmount(0, 10000, 0);
//        assertCommissionBonusAmount(100, -100, 100);
//        assertCommissionBonusAmount(100, 0, 100);
//        assertCommissionBonusAmount(100, 100, 0);
//        assertCommissionBonusAmount(100, 10000, 0);
//        assertCommissionBonusAmount(10000, -100, 10000);
//        assertCommissionBonusAmount(10000, 0, 10000);
//        assertCommissionBonusAmount(10000, 100, 10000 - 100);
//    }
//
//    private void assertCommissionBonusAmount(long requestAmount, long commissionBonusAmount, long requestAmountWithoutCommission) throws JsonProcessingException {
//        assertThat(callMethod(requestAmount, commissionBonusAmount)).isEqualTo(requestAmountWithoutCommission);
//    }
//
////    private Long callMethod(long requestAmount, long commissionBonusAmount) throws JsonProcessingException {
////        P2pPayRequest request = getP2pPayRequest(requestAmount, commissionBonusAmount);
////        return service5mlnBonus.getRequestAmountWithoutBonus(request);
////    }
//
//    private P2pPayRequest getP2pPayRequest(long requestAmount, long commissionBonusAmount) throws JsonProcessingException {
//        var paramsString = "{ \"pan\": \"9860090000000000\", \"bin\": \"986009\", \"additionalParam\": {\"citizen\":  \"Tashkent\", \"birthDate\":  \"2022-01-05\", \"docType\":  \"Биометрическая паспорт гражданина Респ.Узбекистан\", \"docNumber\": \"AA0000000\", \"docDate\": \"2000-01-01\", \"pinFl\":  \"30000000000000\", \"firstName\":  \"Test\", \"lastName\":  \"Testov\", \"middleName\":  \"Testovich\"} }";
//        var senderAccount = new P2pAccount().withType(P2pAccountType.PAN).withCurrency(860).withParams(objectMapper.readTree(paramsString));
//        senderAccount.setCommissionFreeAmount(commissionBonusAmount);
//
//        var paramsString2 = "{ \"pan\": \"8600490000000000\", \"exp\": \"1025\" }";
//        var recipientAccount = new P2pAccount().withType(P2pAccountType.PAN).withCurrency(860).withParams(objectMapper.readTree(paramsString2));
//
//        return new P2pPayRequest()
//                .withFrom(senderAccount)
//                .withTo(recipientAccount)
//                .withAgentTranId("123")
//                .withAmount(requestAmount);
//    }
//}
