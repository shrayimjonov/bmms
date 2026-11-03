package uz.paykap.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class DepositTransactionDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(DepositTransactionDTO.class);
        DepositTransactionDTO depositTransactionDTO1 = new DepositTransactionDTO();
        depositTransactionDTO1.setId(1L);
        DepositTransactionDTO depositTransactionDTO2 = new DepositTransactionDTO();
        assertThat(depositTransactionDTO1).isNotEqualTo(depositTransactionDTO2);
        depositTransactionDTO2.setId(depositTransactionDTO1.getId());
        assertThat(depositTransactionDTO1).isEqualTo(depositTransactionDTO2);
        depositTransactionDTO2.setId(2L);
        assertThat(depositTransactionDTO1).isNotEqualTo(depositTransactionDTO2);
        depositTransactionDTO1.setId(null);
        assertThat(depositTransactionDTO1).isNotEqualTo(depositTransactionDTO2);
    }
}
