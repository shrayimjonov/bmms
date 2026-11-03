package uz.paykap.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bm.upay.adapter.BmUpayDataAdapterV3;
import uz.devops.bmcore.dto.request.CheckRequest;
import uz.devops.bmcore.dto.request.CommonPayRequest;
import uz.devops.bmcore.dto.request.PayRequest;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.PayType;
import uz.paykap.PkbillmsApp;
import uz.paykap.config.SecurityBeanOverrideConfiguration;
import uz.paykap.domain.Recipient;
import uz.paykap.domain.Transaction;
import uz.paykap.repository.RecipientRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PkbillmsApp.class, SecurityBeanOverrideConfiguration.class}
)
public class UpayPaymentTest extends BasePaymentTest {

    public static final String DUMMY_ACCOUNT = "DUMMY_ACCOUNT";
    public static final String TRAN_ID = "TRAN_ID";
    public static final Long DUMMY_AMOUNT = 500100L;
    public static final String USER_LOGIN = "user";
    public static final String RECIPIENT_NAME_1 = "upay_humans";
    @Autowired
    private RecipientRepository recipientRepository;
    private Long billId;

    @Before
    public void setup() {
        initSharedAgentResource();
    }

    @Test
    @Transactional
    @WithMockUser(USER_LOGIN)
    public void testCheckToSuccess() throws Exception {
        syncAdapter(BmUpayDataAdapterV3.NAME);

        addAgentRecipient(USER_LOGIN, RECIPIENT_NAME_1);


        Optional<Recipient> optionalRecipient = recipientRepository.findTop1ByShortName(RECIPIENT_NAME_1);
        assertThat(optionalRecipient.isPresent()).isTrue();
        Long recipientId = optionalRecipient.get().getId();


        CheckRequest simpleCheckDTO = getSimpleCheckDTO();
        simpleCheckDTO.setAmount(null);
        simpleCheckDTO.setRecipientId(recipientId);

        MvcResult mvcResult = restAgentMockMvc.perform(
                        post("/api/agent/check/v3")
                                .content(TestUtil.convertObjectToJsonBytes(simpleCheckDTO))
                                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                )
                .andExpect(status().isOk())
                .andReturn();


        assertThat(mvcResult.getResponse().getContentAsString()).isNotEmpty();

        SimpleBillResponse simpleBillDTO = jacksonMessageConverter.getObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), SimpleBillResponse.class);

        assertThat(simpleBillDTO).isNotNull();
        assertThat(simpleBillDTO.isSuccess()).isTrue();
        assertThat(simpleBillDTO.getAmount()).isEqualTo(simpleCheckDTO.getAmount());
        assertThat(simpleBillDTO.getAccount()).isEqualTo(simpleCheckDTO.getAccount());

        billId = simpleBillDTO.getId();

        List<Transaction> transactionList = transactionRepository.findAll();
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);

        assertThat(simpleBillDTO.getId()).isEqualTo(testTransaction.getId());
    }

//    @Test
//    @Transactional
//    @WithMockUser(USER_LOGIN)
//    public void testCheckGaiShtrafWithFieldInvoiceType() throws Exception {
//
//        syncAdapter(BmUpayDataAdapterV3.NAME);
//        addAgentRecipient(USER_LOGIN, GAI_SHTRAF);
//
//        Optional<Recipient> optionalRecipient = recipientRepository.findTop1ByShortName(GAI_SHTRAF);
//        assertThat(optionalRecipient.isPresent()).isTrue();
//        Long recipientId = optionalRecipient.get().getId();
//
//
//        CheckRequest simpleCheckDTO = getSimpleCheckDTO();
//        simpleCheckDTO.setAmount(null);
//        simpleCheckDTO.setRecipientId(recipientId);
//
//        MvcResult mvcResult = restAgentMockMvc.perform(
//                        post("/api/agent/check/v3")
//                                .content(TestUtil.convertObjectToJsonBytes(simpleCheckDTO))
//                                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
//                )
//                .andExpect(status().isOk())
//                .andReturn();
//
//
//        assertThat(mvcResult.getResponse().getContentAsString()).isNotEmpty();
//
//        SimpleBillResponse simpleBillDTO = jacksonMessageConverter.getObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), SimpleBillResponse.class);
//
//        assertThat(simpleBillDTO).isNotNull();
//        assertThat(simpleBillDTO.isSuccess()).isTrue();
//        assertThat(simpleBillDTO.getAmount()).isEqualTo(simpleCheckDTO.getAmount());
//        assertThat(simpleBillDTO.getAccount()).isEqualTo(simpleCheckDTO.getAccount());
//
//        billId = simpleBillDTO.getId();
//
//        List<Transaction> transactionList = transactionRepository.findAll();
//        Transaction testTransaction = transactionList.get(transactionList.size() - 1);
//
//        assertThat(simpleBillDTO.getId()).isEqualTo(testTransaction.getId());
//    }

    @Test
    @Transactional
    @WithMockUser(USER_LOGIN)
    public void testPayToSuccess() throws Exception {
        testCheckToSuccess();

        CommonPayRequest payDTO = new PayRequest();
        payDTO.setBillId(billId);
        payDTO.setType(PayType.CASH);
        payDTO.setAmount(DUMMY_AMOUNT);

        if (payDTO.getParams() == null)
            payDTO.setParams(new HashMap<>());

        payDTO.getParams().put("pan", "986060HQLNJM5367");

        MvcResult mvcResult = restAgentMockMvc.perform(
                        post("/api/agent/pay/v3")
                                .content(TestUtil.convertObjectToJsonBytes(payDTO))
                                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                )
                .andExpect(status().isOk())
                .andReturn();

        SimpleBillResponse simpleBillDTO = jacksonMessageConverter.getObjectMapper().readValue(mvcResult.getResponse().getContentAsByteArray(), SimpleBillResponse.class);

        assertThat(simpleBillDTO).isNotNull();
        assertThat(simpleBillDTO.isSuccess()).isTrue();
        assertThat(simpleBillDTO.getAmount()).isEqualTo(DUMMY_AMOUNT);
        assertThat(simpleBillDTO.getAccount()).isEqualTo(DUMMY_ACCOUNT);
    }



}
