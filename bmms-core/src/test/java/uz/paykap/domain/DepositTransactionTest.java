package uz.paykap.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uz.paykap.web.rest.TestUtil;

class DepositTransactionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(DepositTransaction.class);
        DepositTransaction depositTransaction1 = new DepositTransaction();
        depositTransaction1.setId(1L);
        DepositTransaction depositTransaction2 = new DepositTransaction();
        depositTransaction2.setId(depositTransaction1.getId());
        assertThat(depositTransaction1).isEqualTo(depositTransaction2);
        depositTransaction2.setId(2L);
        assertThat(depositTransaction1).isNotEqualTo(depositTransaction2);
        depositTransaction1.setId(null);
        assertThat(depositTransaction1).isNotEqualTo(depositTransaction2);
    }
}
