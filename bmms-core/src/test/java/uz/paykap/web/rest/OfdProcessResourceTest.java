//package uz.paykap.web.rest;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import java.sql.Date;
//import java.time.Instant;
//import java.util.List;
//import lombok.SneakyThrows;
//import org.junit.jupiter.api.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.context.junit4.SpringRunner;
//import uz.paykap.IntegrationTest;
//import uz.paykap.event.dto.ofd.*;
//
//@RunWith(SpringRunner.class)
//@IntegrationTest
//class OfdProcessResourceTest {
//
//    @Autowired
//    private OfdProcessResource ofdProcessResource;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    private static final String PINFL = "12312312312312";
//    private static final String INN = "123456789";
//    private static final String BARCODE = "123456789";
//    private static final String AGENT_REQUEST_ID = "12345";
//
//    @Test
//    @SneakyThrows
//    @WithMockUser
//    void registerQr() {
//        var ofdResponseResponse = ofdProcessResource.registerQr(requestWrapper());
//
//        System.out.println("REQUEST: \n\n" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestWrapper()));
//        System.out.println("OFD RESPONSE: \n\n" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(ofdResponseResponse));
//
//        assertThat(ofdResponseResponse).isNotNull();
//    }
//
//    OfdRequestWrapperDTO requestWrapper() {
//        return new OfdRequestWrapperDTO(ofdRequest(), "paynet");
//    }
//
//    OfdRequest ofdRequest() {
//        Location location = new Location();
//        location.setLatitude(12D);
//        location.setLongitude(13D);
//
//        ExtraInfo extraInfo = new ExtraInfo();
//        extraInfo.setOther("extra info");
//        extraInfo.setPhoneNumber("998901234567");
//
//        TaxiInfo taxiInfo = new TaxiInfo();
//        taxiInfo.setPinfl(PINFL);
//        taxiInfo.setTin(INN);
//        taxiInfo.setCarNumber("01X701RA");
//
//        CommissionInfo commissionInfo = new CommissionInfo();
//        commissionInfo.setPinfl(PINFL);
//        commissionInfo.setTin(INN);
//
//        ItemRequest item = new ItemRequest();
//        item.setAmount(10L);
//        item.setName("item-name");
//        item.setPrice(1500L);
//        item.setGoodPrice(1000L);
//        item.setSpic("spic");
//        item.setUnits(0);
//        item.setVat(20L);
//        item.setVatPercent(5);
//        item.setBarcode(BARCODE);
//        item.setDiscount(2L);
//        item.setLabel("item-label");
//        item.setOther(1L);
//        item.setCommissionInfo(commissionInfo);
//
//        OfdRequest ofdRequest = new OfdRequest();
//        ofdRequest.setIsRefund(RefundType.REFUND);
//        ofdRequest.setReceiptType(ReceiptType.Продажа);
//        ofdRequest.setTime(Date.from(Instant.now()));
//        ofdRequest.setReceivedCardCents(1L);
//        ofdRequest.setReceivedCashCents(2L);
//        ofdRequest.setTotalVat(10L);
//        ofdRequest.setLocation(location);
//        ofdRequest.setExtraInfo(extraInfo);
//        ofdRequest.setTaxiInfo(taxiInfo);
//        ofdRequest.setRefundInfo(refundInfo());
//        ofdRequest.setItems(List.of(item));
//        ofdRequest.setAgentRequestId(AGENT_REQUEST_ID);
//
//        return ofdRequest;
//    }
//
//    RefundInfo refundInfo() {
//        RefundInfo refundInfo = new RefundInfo();
//        refundInfo.setDateTime(java.util.Date.from(Instant.now()));
//        refundInfo.setFiscalSign("fiscal-sign");
//        refundInfo.setRecipientId(10L);
//        refundInfo.setTerminalId("122112");
//        return refundInfo;
//    }
//}
