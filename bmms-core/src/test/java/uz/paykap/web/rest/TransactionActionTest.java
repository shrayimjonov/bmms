package uz.paykap.web.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import uz.devops.bmcore.enumeration.TransactionStatus;
import uz.javlon.commons.base.ResultData;
import uz.paykap.IntegrationTest;
import uz.paykap.security.AuthoritiesConstants;
import uz.paykap.service.OverpayService;
import uz.paykap.service.action.transaction.CancelTransaction;
import uz.paykap.service.action.transaction.TransactionActionService;
import uz.paykap.service.dto.OverpayResult;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
@ExtendWith(MockitoExtension.class)
public class TransactionActionTest {

    private static final String ACTION_URL = "/api/transactions/actions";

    @MockBean
    private OverpayService overpayService;

    @Autowired
    private MockMvc restTransactionMockMvc;

    @Autowired
    private TransactionActionService transactionActionService;

    @Test
    @WithMockUser(value = "admin", authorities = {AuthoritiesConstants.ADMIN})
    @Transactional
    void testCancelTran() throws Exception {
        when(overpayService.cancelGateway(anyLong()))
                // TODO: 12/10/2021 impl
                .thenReturn(new ResultData<>(new OverpayResult().success(true)));
        var objectMapper = new ObjectMapper();
        var result = restTransactionMockMvc
                .perform(get(ACTION_URL + '/' + TransactionStatus.SUCCESS).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
//                .andExpect(jsonPath("$.[*].name").value(List.class.getSimpleName()))
//                .andReturn().getResponse();
//        var node = objectMapper.readTree(result.getContentAsString(StandardCharsets.UTF_8));
//        assertThat(node.isArray()).isTrue();
//        var uri = "";
//        for (JsonNode jsonNode : node) {
//            if (jsonNode.get("name").textValue().equals(CancelTransaction.class.getSimpleName())) {
//                uri = jsonNode.get("uri").textValue();
//                break;
//            }
//        }
//        assertThat(StringUtils.isNotBlank(uri)).isEqualTo(true);
//        restTransactionMockMvc.perform(get(uri + "/" + 1))
//                .andExpect(status().isOk());
    }
}

